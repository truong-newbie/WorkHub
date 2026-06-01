import json
import logging
import threading
from collections import OrderedDict
from collections.abc import Callable
from dataclasses import dataclass
from functools import lru_cache
from typing import Literal
from urllib.parse import quote
from urllib.request import Request, urlopen

from pydantic import BaseModel, ConfigDict, Field

from app.core.config import GeminiSettings, get_gemini_settings

LOGGER = logging.getLogger(__name__)
MAX_GEMINI_ATTEMPTS = 2
MAX_CACHE_ENTRIES = 256


class GeminiExplanationPayload(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    recommendation: Literal["PASS", "CONSIDER", "REJECT"]
    confidence: float = Field(ge=0.0, le=100.0)
    strengths: list[str] = Field(default_factory=list)
    weaknesses: list[str] = Field(default_factory=list)
    missing_skills: list[str] = Field(default_factory=list, alias="missingSkills")
    summary: str = Field(min_length=1)


class AtsExplanation(BaseModel):
    recommendation: Literal["PASS", "CONSIDER", "REJECT"]
    confidence: float = Field(ge=0.0, le=100.0)
    strengths: list[str] = Field(default_factory=list)
    weaknesses: list[str] = Field(default_factory=list)
    missing_skills: list[str] = Field(default_factory=list)
    summary: str
    explanation_status: str
    explanation_reason: str | None = None


@dataclass(frozen=True)
class AtsExplanationRequest:
    job_title: str
    job_description: str
    required_skills: list[str]
    resume_text: str
    keyword_score: float
    semantic_score: float
    final_score: float
    matched_skills: list[str]
    missing_skills: list[str]
    resume_id: int | None = None
    job_id: int | None = None


GeminiTransport = Callable[[str, dict[str, str], dict, float], dict]


class GeminiService:
    def __init__(
        self,
        settings: GeminiSettings | None = None,
        transport: GeminiTransport | None = None,
    ):
        self.settings = settings or get_gemini_settings()
        self._transport = transport or _post_json
        self._cache: OrderedDict[tuple[int, int], AtsExplanation] = OrderedDict()
        self._cache_lock = threading.Lock()

    def explain(self, request: AtsExplanationRequest) -> AtsExplanation:
        if not self.settings.llm_enabled:
            return build_fallback_explanation(request, "SKIPPED_DISABLED")
        if not self.settings.api_key:
            LOGGER.warning("Gemini explanation skipped because GEMINI_API_KEY is missing")
            return build_fallback_explanation(
                request,
                "SKIPPED_MISSING_API_KEY",
                "GEMINI_API_KEY is not configured",
            )

        cache_key = self._cache_key(request)
        cached = self._get_cached(cache_key)
        if cached is not None:
            return cached

        for attempt in range(1, MAX_GEMINI_ATTEMPTS + 1):
            try:
                response = self._transport(
                    self._build_url(),
                    {
                        "Content-Type": "application/json",
                        "x-goog-api-key": self.settings.api_key,
                    },
                    self._build_request_body(request),
                    self.settings.timeout_seconds,
                )
                payload = GeminiExplanationPayload.model_validate_json(
                    _extract_response_text(response)
                )
                result = AtsExplanation(
                    recommendation=payload.recommendation,
                    confidence=round(payload.confidence, 2),
                    strengths=_clean_list(payload.strengths),
                    weaknesses=_clean_list(payload.weaknesses),
                    missing_skills=_clean_list(payload.missing_skills),
                    summary=payload.summary.strip(),
                    explanation_status="CALCULATED",
                )
                self._put_cached(cache_key, result)
                return result
            except Exception as exc:
                LOGGER.warning(
                    "Gemini ATS explanation attempt %s/%s failed: %s",
                    attempt,
                    MAX_GEMINI_ATTEMPTS,
                    exc,
                )

        return build_fallback_explanation(
            request,
            "FALLBACK_ERROR",
            "Gemini explanation unavailable after retry",
        )

    def _build_url(self) -> str:
        model = quote(self.settings.model, safe="")
        return f"{self.settings.base_url}/v1beta/models/{model}:generateContent"

    def _build_request_body(self, request: AtsExplanationRequest) -> dict:
        return {
            "contents": [
                {
                    "role": "user",
                    "parts": [{"text": self._build_prompt(request)}],
                }
            ],
            "generationConfig": {
                "responseMimeType": "application/json",
                "responseJsonSchema": GeminiExplanationPayload.model_json_schema(
                    by_alias=True
                ),
                "temperature": 0.2,
            },
        }

    def _build_prompt(self, request: AtsExplanationRequest) -> str:
        resume_text = _truncate(request.resume_text, self.settings.max_text_length)
        job_description = _truncate(
            request.job_description, self.settings.max_text_length
        )
        return f"""
You are a Senior Technical Recruiter and ATS Specialist.

Analyze the candidate resume against the job description.
Use the supplied deterministic ATS scores as evidence. Explain the result clearly
for a recruiter. You may choose only PASS, CONSIDER, or REJECT.

Default recommendation guide:
- PASS when final score is at least 80
- CONSIDER when final score is from 60 to 79.99
- REJECT when final score is below 60

You may override the default recommendation only when the supplied evidence
provides a reasonable explanation.

Return ONLY valid JSON matching the requested schema.
Do not return markdown or text outside JSON.

Job Title:
{request.job_title}

Job Description:
{job_description}

Required Skills:
{", ".join(request.required_skills)}

Parsed Resume Text:
{resume_text}

Keyword Score:
{request.keyword_score}

Semantic Score:
{request.semantic_score}

Final ATS Score:
{request.final_score}

Matched Skills:
{", ".join(request.matched_skills)}

Missing Skills:
{", ".join(request.missing_skills)}
""".strip()

    def _cache_key(
        self, request: AtsExplanationRequest
    ) -> tuple[int, int] | None:
        if request.resume_id is None or request.job_id is None:
            return None
        return request.resume_id, request.job_id

    def _get_cached(
        self, cache_key: tuple[int, int] | None
    ) -> AtsExplanation | None:
        if cache_key is None:
            return None
        with self._cache_lock:
            value = self._cache.get(cache_key)
            if value is not None:
                self._cache.move_to_end(cache_key)
                return value.model_copy(deep=True)
        return None

    def _put_cached(
        self, cache_key: tuple[int, int] | None, value: AtsExplanation
    ) -> None:
        if cache_key is None:
            return
        with self._cache_lock:
            self._cache[cache_key] = value.model_copy(deep=True)
            self._cache.move_to_end(cache_key)
            while len(self._cache) > MAX_CACHE_ENTRIES:
                self._cache.popitem(last=False)


def build_fallback_explanation(
    request: AtsExplanationRequest,
    status: str,
    reason: str | None = None,
) -> AtsExplanation:
    matched_text = ", ".join(request.matched_skills) or "none"
    missing_text = ", ".join(request.missing_skills) or "none"
    strengths = (
        [f"Matched required skills: {matched_text}"]
        if request.matched_skills
        else ["No required skill keyword match was detected"]
    )
    weaknesses = (
        [f"Missing required skills: {missing_text}"]
        if request.missing_skills
        else ["No required skill gaps were detected"]
    )
    return AtsExplanation(
        recommendation=_recommendation_for_score(request.final_score),
        confidence=round(max(0.0, min(request.final_score, 100.0)), 2),
        strengths=strengths,
        weaknesses=weaknesses,
        missing_skills=request.missing_skills,
        summary=(
            f"ATS score {request.final_score}. Matched skills: {matched_text}. "
            f"Missing skills: {missing_text}."
        ),
        explanation_status=status,
        explanation_reason=reason,
    )


def _recommendation_for_score(score: float) -> Literal["PASS", "CONSIDER", "REJECT"]:
    if score >= 80.0:
        return "PASS"
    if score >= 60.0:
        return "CONSIDER"
    return "REJECT"


def _extract_response_text(response: dict) -> str:
    try:
        return response["candidates"][0]["content"]["parts"][0]["text"]
    except (KeyError, IndexError, TypeError) as exc:
        raise ValueError("Gemini response does not contain generated JSON text") from exc


def _post_json(
    url: str, headers: dict[str, str], payload: dict, timeout_seconds: float
) -> dict:
    request = Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers=headers,
        method="POST",
    )
    with urlopen(request, timeout=timeout_seconds) as response:
        return json.loads(response.read().decode("utf-8"))


def _clean_list(values: list[str]) -> list[str]:
    return [value.strip() for value in values if value and value.strip()]


def _truncate(value: str, max_length: int) -> str:
    return (value or "")[:max_length].strip()


@lru_cache(maxsize=1)
def get_gemini_service() -> GeminiService:
    return GeminiService()

