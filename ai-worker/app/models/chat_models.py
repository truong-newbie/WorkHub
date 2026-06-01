from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field


ChatIntent = Literal[
    "SEARCH_JOBS",
    "RECOMMEND_JOBS",
    "JOB_DETAIL",
    "SAVED_JOBS",
    "APPLICATION_STATUS",
    "MY_RESUMES",
    "COMPANY_INFO",
    "PLATFORM_HELP",
    "OUT_OF_SCOPE",
]


class ChatHistoryItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    sender_type: str = Field(alias="senderType", max_length=30)
    content: str = Field(max_length=5000)


class ChatIntentRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    message: str = Field(min_length=1, max_length=1000)
    recent_messages: list[ChatHistoryItem] = Field(
        default_factory=list, alias="recentMessages", max_length=10
    )


class ChatIntentResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    intent: ChatIntent
    out_of_scope: bool = Field(default=False, alias="outOfScope")
    keyword: str | None = Field(default=None, max_length=120)
    location: str | None = Field(default=None, max_length=120)
    job_id: int | None = Field(default=None, alias="jobId")
    company_id: int | None = Field(default=None, alias="companyId")
    company_name: str | None = Field(default=None, alias="companyName", max_length=120)
    skill_names: list[str] = Field(default_factory=list, alias="skillNames", max_length=10)
    level: str | None = Field(default=None, max_length=50)
    employment_type: str | None = Field(
        default=None, alias="employmentType", max_length=50
    )
    salary_min: float | None = Field(default=None, alias="salaryMin", ge=0)
    salary_max: float | None = Field(default=None, alias="salaryMax", ge=0)


class ChatContextItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="allow")

    type: str = Field(max_length=30)
    id: str | None = Field(default=None, max_length=100)
    title: str | None = Field(default=None, max_length=240)
    data: dict[str, Any] = Field(default_factory=dict)


class ChatGroundedResponseRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    message: str = Field(min_length=1, max_length=1000)
    intent: ChatIntent
    recent_messages: list[ChatHistoryItem] = Field(
        default_factory=list, alias="recentMessages", max_length=10
    )
    context_items: list[dict[str, Any]] = Field(
        default_factory=list, alias="contextItems", max_length=5
    )


class ChatGroundedResponse(BaseModel):
    answer: str = Field(min_length=1, max_length=5000)

