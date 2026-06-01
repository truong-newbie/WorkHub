from collections.abc import Callable

from app.services.gemini_service import (
    AtsExplanationRequest,
    GeminiService,
    get_gemini_service,
)
from app.services.scoring_service import calculate_final_score, calculate_skill_score
from app.services.semantic_service import calculate_semantic_score
from app.services.skill_service import extract_skills


def analyze_resume_text(
    raw_text: str,
    job_description: str,
    required_skills: list[str] | None = None,
    semantic_calculator: Callable[[str, str], float] = calculate_semantic_score,
    job_title: str = "",
    resume_id: int | None = None,
    job_id: int | None = None,
    explanation_service: GeminiService | None = None,
) -> dict:
    required_skills = required_skills or []
    job_text = build_job_text(job_description, required_skills)
    resume_skills = extract_skills(raw_text, required_skills)
    job_skills = _merge_skills(required_skills, extract_skills(job_text))
    resume_skill_keys = {skill.lower() for skill in resume_skills}
    job_skill_keys = {skill.lower() for skill in job_skills}
    matched_skills = [
        skill for skill in job_skills if skill.lower() in resume_skill_keys
    ]
    missing_skills = [
        skill for skill in job_skills if skill.lower() not in resume_skill_keys
    ]
    extra_skills = [
        skill for skill in resume_skills if skill.lower() not in job_skill_keys
    ]
    skill_score = calculate_skill_score(matched_skills, job_skills)
    semantic_score = semantic_calculator(raw_text, job_text)
    final_score = calculate_final_score(skill_score, semantic_score)
    explanation = (explanation_service or get_gemini_service()).explain(
        AtsExplanationRequest(
            job_title=job_title,
            job_description=job_description,
            required_skills=required_skills,
            resume_text=raw_text,
            keyword_score=skill_score,
            semantic_score=semantic_score,
            final_score=final_score,
            matched_skills=matched_skills,
            missing_skills=missing_skills,
            resume_id=resume_id,
            job_id=job_id,
        )
    )

    return {
        "raw_text": raw_text,
        "resume_skills": resume_skills,
        "job_skills": job_skills,
        "matched_skills": matched_skills,
        "missing_skills": missing_skills,
        "extra_skills": extra_skills,
        "skill_score": skill_score,
        "semantic_score": semantic_score,
        "final_score": final_score,
        "semantic_status": "CALCULATED",
        "semantic_reason": None,
        "strengths": explanation.strengths,
        "weaknesses": explanation.weaknesses,
        "recommendation": explanation.recommendation,
        "confidence": explanation.confidence,
        "summary": explanation.summary,
        "explanation_status": explanation.explanation_status,
        "explanation_reason": explanation.explanation_reason,
        "ai_summary": explanation.summary,
    }


def build_job_text(job_description: str, required_skills: list[str]) -> str:
    skills_text = ", ".join(required_skills)
    return "\n".join(
        part
        for part in [
            job_description.strip(),
            f"Required skills: {skills_text}" if skills_text else "",
        ]
        if part
    )


def _merge_skills(first: list[str], second: list[str]) -> list[str]:
    result = []
    seen = set()
    for skill in first + second:
        key = skill.lower()
        if key not in seen:
            result.append(skill)
            seen.add(key)
    return result
