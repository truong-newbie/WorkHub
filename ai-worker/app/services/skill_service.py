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


def extract_skills(text: str) -> list[str]:
    text_lower = (text or "").lower()
    return [skill for skill in SKILL_DICTIONARY if skill.lower() in text_lower]
