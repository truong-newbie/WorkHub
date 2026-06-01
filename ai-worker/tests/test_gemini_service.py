import json
import unittest

from app.core.config import GeminiSettings
from app.services.gemini_service import (
    AtsExplanationRequest,
    GeminiService,
    build_fallback_explanation,
)


def explanation_request(
    final_score: float = 78.0,
    resume_id: int | None = 1,
    job_id: int | None = 2,
) -> AtsExplanationRequest:
    return AtsExplanationRequest(
        job_title="Backend Java Developer",
        job_description="Build Spring Boot services",
        required_skills=["Java", "Spring Boot", "Kafka"],
        resume_text="Java developer with Spring Boot REST API experience",
        keyword_score=66.67,
        semantic_score=95.0,
        final_score=final_score,
        matched_skills=["Java", "Spring Boot"],
        missing_skills=["Kafka"],
        resume_id=resume_id,
        job_id=job_id,
    )


def gemini_response(
    recommendation: str = "CONSIDER",
    confidence: float = 86.0,
) -> dict:
    payload = {
        "recommendation": recommendation,
        "confidence": confidence,
        "strengths": ["Strong Java backend experience"],
        "weaknesses": ["Kafka is not shown in the resume"],
        "missingSkills": ["Kafka"],
        "summary": "The candidate matches the core backend requirements.",
    }
    return {
        "candidates": [
            {
                "content": {
                    "parts": [{"text": json.dumps(payload)}],
                }
            }
        ]
    }


class GeminiServiceTest(unittest.TestCase):
    def test_llm_disabled_uses_deterministic_fallback_without_transport(self):
        transport_calls = []
        service = GeminiService(
            GeminiSettings(llm_enabled=False),
            transport=lambda *_args: transport_calls.append(True),
        )

        result = service.explain(explanation_request())

        self.assertEqual("SKIPPED_DISABLED", result.explanation_status)
        self.assertEqual("CONSIDER", result.recommendation)
        self.assertEqual([], transport_calls)

    def test_valid_gemini_json_is_parsed_and_cached_by_resume_and_job(self):
        transport_calls = []

        def transport(url, headers, payload, timeout):
            transport_calls.append((url, headers, payload, timeout))
            return gemini_response()

        service = GeminiService(
            GeminiSettings(api_key="test-key", llm_enabled=True),
            transport=transport,
        )

        first = service.explain(explanation_request())
        second = service.explain(explanation_request())

        self.assertEqual("CALCULATED", first.explanation_status)
        self.assertEqual("CONSIDER", first.recommendation)
        self.assertEqual(first, second)
        self.assertEqual(1, len(transport_calls))
        self.assertIn("gemini-2.5-flash:generateContent", transport_calls[0][0])
        self.assertEqual("test-key", transport_calls[0][1]["x-goog-api-key"])
        self.assertEqual(
            "application/json",
            transport_calls[0][2]["generationConfig"]["responseMimeType"],
        )

    def test_invalid_json_is_retried_once(self):
        responses = [
            {"candidates": [{"content": {"parts": [{"text": "not-json"}]}}]},
            gemini_response("PASS", 91.0),
        ]

        def transport(*_args):
            return responses.pop(0)

        result = GeminiService(
            GeminiSettings(api_key="test-key", llm_enabled=True),
            transport=transport,
        ).explain(explanation_request(88.0))

        self.assertEqual("CALCULATED", result.explanation_status)
        self.assertEqual("PASS", result.recommendation)
        self.assertEqual([], responses)

    def test_transport_failure_retries_once_then_falls_back(self):
        transport_calls = []

        def transport(*_args):
            transport_calls.append(True)
            raise RuntimeError("temporary error")

        result = GeminiService(
            GeminiSettings(api_key="test-key", llm_enabled=True),
            transport=transport,
        ).explain(explanation_request())

        self.assertEqual(2, len(transport_calls))
        self.assertEqual("FALLBACK_ERROR", result.explanation_status)
        self.assertEqual("CONSIDER", result.recommendation)

    def test_missing_api_key_falls_back_without_transport(self):
        transport_calls = []
        service = GeminiService(
            GeminiSettings(llm_enabled=True),
            transport=lambda *_args: transport_calls.append(True),
        )

        result = service.explain(explanation_request())

        self.assertEqual("SKIPPED_MISSING_API_KEY", result.explanation_status)
        self.assertEqual([], transport_calls)


class RecommendationFallbackTest(unittest.TestCase):
    def test_backend_java_resume_is_pass(self):
        result = build_fallback_explanation(explanation_request(85.0), "TEST")
        self.assertEqual("PASS", result.recommendation)

    def test_frontend_resume_for_backend_java_job_is_reject(self):
        result = build_fallback_explanation(explanation_request(35.0), "TEST")
        self.assertEqual("REJECT", result.recommendation)

    def test_fullstack_resume_is_consider(self):
        result = build_fallback_explanation(explanation_request(70.0), "TEST")
        self.assertEqual("CONSIDER", result.recommendation)

    def test_lower_keyword_but_high_semantic_score_is_consider(self):
        result = build_fallback_explanation(explanation_request(68.0), "TEST")
        self.assertEqual("CONSIDER", result.recommendation)


if __name__ == "__main__":
    unittest.main()

