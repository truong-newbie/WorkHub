from collections.abc import Callable

from app.services.scoring_service import calculate_final_score, calculate_skill_score
from app.services.semantic_service import calculate_semantic_score
from app.services.skill_service import extract_skills


def analyze_resume_text(
    raw_text: str,
    job_description: str,
    required_skills: list[str] | None = None,
    semantic_calculator: Callable[[str, str], float] = calculate_semantic_score,
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
        "ai_summary": build_summary(matched_skills, missing_skills, final_score),
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


def build_summary(
    matched_skills: list[str], missing_skills: list[str], final_score: float
) -> str:
    matched_text = ", ".join(matched_skills) if matched_skills else "none"
    missing_text = ", ".join(missing_skills) if missing_skills else "none"
    return (
        f"ATS score {final_score}. Matched skills: {matched_text}. "
        f"Missing skills: {missing_text}."
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
