from fastapi import FastAPI, File, Form, UploadFile
from pydantic import BaseModel

from app.services.parser_service import parse_pdf
from app.services.semantic_service import calculate_semantic_score
from app.services.skill_service import extract_skills

app = FastAPI(title="WorkHub AI Worker")


class AiResumeAnalysisResponse(BaseModel):
    raw_text: str
    resume_skills: list[str]
    job_skills: list[str]
    matched_skills: list[str]
    missing_skills: list[str]
    extra_skills: list[str]
    skill_score: float
    semantic_score: float
    ai_summary: str | None = None


@app.post("/api/v1/ai/resume/analyze", response_model=AiResumeAnalysisResponse)
async def analyze_resume(
    file: UploadFile = File(...),
    job_description: str = Form(...),
):
    file_bytes = await file.read()
    raw_text = parse_pdf(file_bytes) if file.filename.lower().endswith(".pdf") else file_bytes.decode("utf-8", errors="ignore")
    resume_skills = extract_skills(raw_text)
    job_skills = extract_skills(job_description)

    matched = [skill for skill in job_skills if skill.lower() in {item.lower() for item in resume_skills}]
    missing = [skill for skill in job_skills if skill.lower() not in {item.lower() for item in resume_skills}]
    extra = [skill for skill in resume_skills if skill.lower() not in {item.lower() for item in job_skills}]
    skill_score = (len(matched) / len(job_skills) * 100) if job_skills else 0.0

    return AiResumeAnalysisResponse(
        raw_text=raw_text,
        resume_skills=resume_skills,
        job_skills=job_skills,
        matched_skills=matched,
        missing_skills=missing,
        extra_skills=extra,
        skill_score=round(skill_score, 2),
        semantic_score=calculate_semantic_score(raw_text, job_description),
        ai_summary=None,
    )
