import os
from dataclasses import dataclass
from functools import lru_cache


def _read_bool(name: str, default: bool = False) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


@dataclass(frozen=True)
class GeminiSettings:
    api_key: str | None = None
    llm_enabled: bool = False
    model: str = "gemini-2.5-flash"
    base_url: str = "https://generativelanguage.googleapis.com"
    timeout_seconds: float = 30.0
    max_text_length: int = 12000

    @classmethod
    def from_env(cls) -> "GeminiSettings":
        return cls(
            api_key=os.getenv("GEMINI_API_KEY"),
            llm_enabled=_read_bool("LLM_ENABLED"),
            model=os.getenv("GEMINI_MODEL", "gemini-2.5-flash"),
            base_url=os.getenv(
                "GEMINI_BASE_URL", "https://generativelanguage.googleapis.com"
            ).rstrip("/"),
            timeout_seconds=float(os.getenv("GEMINI_TIMEOUT_SECONDS", "30")),
            max_text_length=int(os.getenv("GEMINI_MAX_TEXT_LENGTH", "12000")),
        )


@lru_cache(maxsize=1)
def get_gemini_settings() -> GeminiSettings:
    return GeminiSettings.from_env()

