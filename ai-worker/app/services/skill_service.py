SKILL_DICTIONARY = [
    "Java",
    "Spring Boot",
    "Docker",
    "Redis",
    "MySQL",
    "PostgreSQL",
    "React",
    "Angular",
    "AWS",
    "Kafka",
    "Kubernetes",
    "Python",
]


def extract_skills(text: str, additional_skills: list[str] | None = None) -> list[str]:
    text_lower = (text or "").lower()
    known_skills = _unique_skills(SKILL_DICTIONARY + (additional_skills or []))
    return [skill for skill in known_skills if skill.lower() in text_lower]


def parse_required_skills(required_skills: str | None) -> list[str]:
    if not required_skills:
        return []
    return _unique_skills(required_skills.split(","))


def _unique_skills(skills: list[str]) -> list[str]:
    result = []
    seen = set()
    for skill in skills:
        normalized_skill = skill.strip()
        key = normalized_skill.lower()
        if normalized_skill and key not in seen:
            result.append(normalized_skill)
            seen.add(key)
    return result
