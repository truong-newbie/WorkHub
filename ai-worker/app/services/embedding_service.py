import logging
import os
import threading
import unicodedata
from functools import lru_cache

import numpy as np

LOGGER = logging.getLogger(__name__)

DEFAULT_MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"
DEFAULT_MAX_TEXT_LENGTH = 8000


class SemanticInputError(ValueError):
    """Raised when semantic scoring cannot run because required text is empty."""


class EmbeddingModelError(RuntimeError):
    """Raised when the embedding model cannot load or encode input text."""


class EmbeddingService:
    def __init__(
        self,
        model_name: str | None = None,
        max_text_length: int | None = None,
        model=None,
    ):
        self.model_name = model_name or os.getenv(
            "AI_WORKER_EMBEDDING_MODEL", DEFAULT_MODEL_NAME
        )
        self.max_text_length = max_text_length or int(
            os.getenv("AI_WORKER_MAX_TEXT_LENGTH", str(DEFAULT_MAX_TEXT_LENGTH))
        )
        self._model = model
        self._model_lock = threading.Lock()

    def calculate_similarity_score(self, resume_text: str, job_text: str) -> float:
        normalized_resume = self.normalize_text(resume_text)
        normalized_job = self.normalize_text(job_text)
        if not normalized_resume:
            raise SemanticInputError("Resume text is empty after parsing")
        if not normalized_job:
            raise SemanticInputError("Job description and required skills are empty")

        try:
            vectors = np.asarray(
                self._get_model().encode([normalized_resume, normalized_job]),
                dtype=float,
            )
            resume_vector, job_vector = vectors[0], vectors[1]
            denominator = np.linalg.norm(resume_vector) * np.linalg.norm(job_vector)
            if denominator == 0:
                raise EmbeddingModelError("Embedding model returned an empty vector")
            similarity = float(np.dot(resume_vector, job_vector) / denominator)
        except EmbeddingModelError:
            raise
        except Exception as exc:
            raise EmbeddingModelError("Embedding model failed to encode ATS text") from exc

        return round(max(0.0, min(similarity * 100.0, 100.0)), 2)

    def normalize_text(self, text: str | None) -> str:
        normalized = unicodedata.normalize("NFKC", text or "")
        printable = "".join(
            character
            for character in normalized
            if character.isprintable() or character.isspace()
        )
        compact = " ".join(printable.split())
        return compact[: self.max_text_length].strip()

    def _get_model(self):
        if self._model is not None:
            return self._model

        with self._model_lock:
            if self._model is not None:
                return self._model
            try:
                from sentence_transformers import SentenceTransformer

                LOGGER.info("Loading ATS embedding model %s", self.model_name)
                self._model = SentenceTransformer(self.model_name)
                LOGGER.info("ATS embedding model loaded")
            except Exception as exc:
                raise EmbeddingModelError(
                    f"Could not load embedding model {self.model_name}"
                ) from exc
        return self._model


@lru_cache(maxsize=1)
def get_embedding_service() -> EmbeddingService:
    return EmbeddingService()
