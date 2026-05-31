SKILL_WEIGHT = 0.6
SEMANTIC_WEIGHT = 0.4


def calculate_skill_score(matched_skills: list[str], job_skills: list[str]) -> float:
    if not job_skills:
        return 0.0
    return round(len(matched_skills) / len(job_skills) * 100.0, 2)


def calculate_final_score(skill_score: float, semantic_score: float) -> float:
    score = skill_score * SKILL_WEIGHT + semantic_score * SEMANTIC_WEIGHT
    return round(max(0.0, min(score, 100.0)), 2)
