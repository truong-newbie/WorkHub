from app.services.embedding_service import get_embedding_service


def calculate_semantic_score(resume_text: str, job_text: str) -> float:
    return get_embedding_service().calculate_similarity_score(resume_text, job_text)
