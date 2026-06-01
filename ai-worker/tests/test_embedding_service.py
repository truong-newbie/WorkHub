import os
import unittest

from app.services.analysis_service import analyze_resume_text
from app.services.embedding_service import EmbeddingService, SemanticInputError


class FakeEmbeddingModel:
    def encode(self, texts):
        vectors = []
        for text in texts:
            lowered = text.lower()
            if "marketing" in lowered or "sales" in lowered:
                vectors.append([0.0, 1.0])
            elif "authentication" in lowered or "authorization" in lowered:
                vectors.append([0.9, 0.1])
            else:
                vectors.append([1.0, 0.0])
        return vectors


class EmbeddingServiceTest(unittest.TestCase):
    def setUp(self):
        self.service = EmbeddingService(model=FakeEmbeddingModel())

    def test_related_text_has_high_semantic_score(self):
        score = self.service.calculate_similarity_score(
            "Implemented secure backend authentication and token-based authorization",
            "Spring Security JWT REST API authentication",
        )

        self.assertGreater(score, 90.0)

    def test_unrelated_text_has_low_semantic_score(self):
        score = self.service.calculate_similarity_score(
            "Marketing sales campaign planning",
            "Java Spring Boot backend developer",
        )

        self.assertEqual(0.0, score)

    def test_empty_resume_fails_clearly(self):
        with self.assertRaisesRegex(SemanticInputError, "Resume text is empty"):
            self.service.calculate_similarity_score(" \n ", "Java backend")

    def test_normalize_text_compacts_and_truncates_input(self):
        service = EmbeddingService(model=FakeEmbeddingModel(), max_text_length=12)

        self.assertEqual("Java Spring", service.normalize_text(" Java   Spring   Boot "))


class ResumeAnalysisServiceTest(unittest.TestCase):
    def test_strong_keyword_match_combines_skill_and_semantic_scores(self):
        result = analyze_resume_text(
            "Java Spring Boot Docker developer",
            "Backend engineer",
            ["Java", "Spring Boot", "Docker"],
            semantic_calculator=lambda _resume, _job: 90.0,
        )

        self.assertEqual(100.0, result["skill_score"])
        self.assertEqual(90.0, result["semantic_score"])
        self.assertEqual(96.0, result["final_score"])

    def test_semantic_match_can_score_when_keywords_do_not_match(self):
        result = analyze_resume_text(
            "Implemented secure backend authentication and token-based authorization for web services",
            "Build secure backend services",
            ["Spring Security", "JWT", "REST API"],
            semantic_calculator=lambda _resume, _job: 82.0,
        )

        self.assertEqual(0.0, result["skill_score"])
        self.assertGreater(result["semantic_score"], result["skill_score"])
        self.assertEqual(32.8, result["final_score"])

    def test_unrelated_resume_has_low_final_score(self):
        result = analyze_resume_text(
            "Marketing sales campaign planning",
            "Java backend engineer",
            ["Java"],
            semantic_calculator=lambda _resume, _job: 10.0,
        )

        self.assertEqual(0.0, result["skill_score"])
        self.assertEqual(4.0, result["final_score"])

    def test_required_skills_are_fallback_when_job_description_is_empty(self):
        captured_job_text = []

        result = analyze_resume_text(
            "Java developer",
            "",
            ["Java"],
            semantic_calculator=lambda _resume, job: captured_job_text.append(job) or 75.0,
        )

        self.assertIn("Required skills: Java", captured_job_text[0])
        self.assertEqual(100.0, result["skill_score"])


@unittest.skipUnless(
    os.getenv("RUN_EMBEDDING_INTEGRATION_TESTS") == "true",
    "Set RUN_EMBEDDING_INTEGRATION_TESTS=true to download and run the local model",
)
class RealEmbeddingIntegrationTest(unittest.TestCase):
    def test_real_model_scores_related_text_above_unrelated_text(self):
        service = EmbeddingService()
        related_score = service.calculate_similarity_score(
            "Implemented secure backend authentication and token-based authorization for web services",
            "Spring Security JWT REST API authentication and authorization",
        )
        unrelated_score = service.calculate_similarity_score(
            "Marketing sales campaign planning and customer outreach",
            "Java Spring Boot backend developer",
        )

        self.assertGreater(related_score, unrelated_score)


if __name__ == "__main__":
    unittest.main()
