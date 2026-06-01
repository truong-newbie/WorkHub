import logging

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from pydantic import BaseModel

from app.services.analysis_service import analyze_resume_text
from app.models.chat_models import (
    ChatGroundedResponse,
    ChatGroundedResponseRequest,
    ChatIntentRequest,
    ChatIntentResponse,
)
from app.services.chat_service import ChatAiUnavailableError, ChatService
from app.services.embedding_service import EmbeddingModelError, SemanticInputError
from app.services.parser_service import ResumeParsingError, parse_resume_file
from app.services.skill_service import parse_required_skills

app = FastAPI(title="WorkHub AI Worker")
LOGGER = logging.getLogger(__name__)
chat_service = ChatService()


class AiResumeAnalysisResponse(BaseModel):
    raw_text: str
    resume_skills: list[str]
    job_skills: list[str]
    matched_skills: list[str]
    missing_skills: list[str]
    extra_skills: list[str]
    skill_score: float
    semantic_score: float
    final_score: float
    semantic_status: str
    semantic_reason: str | None = None
    strengths: list[str]
    weaknesses: list[str]
    recommendation: str
    confidence: float
    summary: str
    explanation_status: str
    explanation_reason: str | None = None
    ai_summary: str | None = None


@app.post("/api/v1/ai/resume/analyze", response_model=AiResumeAnalysisResponse)
async def analyze_resume(
    file: UploadFile = File(...),
    job_description: str = Form(...),
    required_skills: str | None = Form(None),
    job_title: str = Form(""),
    resume_id: int | None = Form(None),
    job_id: int | None = Form(None),
):
    try:
        file_bytes = await file.read()
        file_name = file.filename or ""
        raw_text = parse_resume_file(file_bytes, file_name)
        analysis = analyze_resume_text(
            raw_text,
            job_description,
            parse_required_skills(required_skills),
            job_title=job_title,
            resume_id=resume_id,
            job_id=job_id,
        )
        return AiResumeAnalysisResponse(**analysis)
    except SemanticInputError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc
    except ResumeParsingError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc
    except EmbeddingModelError as exc:
        LOGGER.exception("ATS embedding model is unavailable")
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except HTTPException:
        raise
    except Exception as exc:
        LOGGER.exception("Could not parse or analyze resume")
        raise HTTPException(
            status_code=422, detail="Could not parse or analyze resume file"
        ) from exc


@app.post("/api/v1/ai/chat/intent", response_model=ChatIntentResponse)
def classify_chat_intent(request: ChatIntentRequest):
    try:
        return chat_service.classify_intent(request)
    except ChatAiUnavailableError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc


@app.post("/api/v1/ai/chat/respond", response_model=ChatGroundedResponse)
def generate_chat_response(request: ChatGroundedResponseRequest):
    try:
        return chat_service.generate_response(request)
    except ChatAiUnavailableError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
