Hiện chatbot hỗ trợ các nhóm câu hỏi sau:
Nhóm
Ví dụ câu hỏi
Tìm việc
Tìm job Java backend tại Hà Nội
Tìm việc theo kỹ năng
Có job nào yêu cầu React và TypeScript không?
Tìm việc theo mức lương
Tìm việc Java lương từ 1000 USD
Việc phù hợp
Gợi ý các công việc phù hợp với hồ sơ của tôi
Chi tiết việc làm
Cho tôi thông tin job ID 12
Việc đã lưu
Tôi đã lưu những việc làm nào?
Trạng thái ứng tuyển
Các đơn ứng tuyển của tôi đang ở trạng thái gì?
CV cá nhân
Tôi đã tải lên những CV nào?
Công ty
Công ty ABC hiện có việc làm nào?
Hướng dẫn sử dụng
Làm thế nào để lưu một việc làm?
Hướng dẫn ứng tuyển
Làm sao để ứng tuyển vào một job?
Gợi ý cá nhân hóa
Làm thế nào để cập nhật sở thích việc làm?
Email việc làm
Làm sao để đăng ký nhận email việc làm phù hợp?
Chatbot chỉ tư vấn và đọc dữ liệu. Nó chưa thực hiện thay đổi trực tiếp như:
•
Ứng tuyển giúp tôi vào job ID 12
•
Xóa CV của tôi
•
Lưu job này giúp tôi
•
Hủy đơn ứng tuyển giúp tôi
# Candidate Chatbot Module

## Purpose

Provides a read-only, authenticated AI assistant for candidates. The assistant
answers only WorkHub candidate questions and remains grounded in sanitized data
retrieved by Spring Boot.

## Architecture

```text
FE widget
  -> Spring Boot /api/v1/chat/**
  -> candidate ownership and rate-limit checks
  -> AI worker intent classifier
  -> allowlisted Spring services and repositories
  -> sanitized context
  -> AI worker grounded answer generator
  -> trusted Spring-generated source cards and actions
```

Gemini never queries MySQL or Elasticsearch directly. FE never calls Gemini or
the AI worker directly. Chat is synchronous and does not use RabbitMQ.

## Main Files

- `controller/ChatController.java`
- `service/ChatService.java`
- `service/impl/ChatServiceImpl.java`
- `service/AiChatClient.java`
- `service/impl/AiChatClientImpl.java`
- `service/ChatRateLimitService.java`
- `service/impl/InMemoryChatRateLimitService.java`
- `domain.entity.ChatConversation.java`
- `domain.entity.ChatMessage.java`
- `domain.mapper.ChatMapper.java`
- `repository/ChatConversationRepository.java`
- `repository/ChatMessageRepository.java`
- `ai-worker/app/models/chat_models.py`
- `ai-worker/app/services/chat_service.py`

## External APIs

All endpoints require `ROLE_CANDIDATE`.

- `POST /api/v1/chat/messages`
- `GET /api/v1/chat/conversations?page=0&size=20`
- `GET /api/v1/chat/conversations/{conversationId}/messages?page=0&size=30`
- `DELETE /api/v1/chat/conversations/{conversationId}`

## Internal AI Worker APIs

These endpoints are backend integrations, not FE contracts.

- `POST /api/v1/ai/chat/intent`
- `POST /api/v1/ai/chat/respond`

## Allowed Intents

- `SEARCH_JOBS`
- `RECOMMEND_JOBS`
- `JOB_DETAIL`
- `SAVED_JOBS`
- `APPLICATION_STATUS`
- `MY_RESUMES`
- `COMPANY_INFO`
- `PLATFORM_HELP`
- `OUT_OF_SCOPE`

Unknown or unsafe intent output is normalized to `OUT_OF_SCOPE`.

## Privacy Rules

- Spring Boot builds source cards and action URLs.
- AI worker receives bounded sanitized context only.
- Resume parsed content, resume file URL, file bytes, contact information,
  tokens, passwords, and secrets are not sent to Gemini.
- Conversation access always includes candidate ownership and soft-delete
  filtering.
- Raw prompts and raw AI responses are not logged at info level.

## Fallback

If intent classification is unavailable, return a safe localized temporary
unavailable answer. If answer generation fails after retrieval, return a
deterministic localized summary with `responseMode=FALLBACK`.

## Rate Limit

The MVP uses an in-memory per-candidate limiter:

```properties
chat.rate-limit.max-requests=20
chat.rate-limit.window-seconds=60
```

Replace it with a shared Redis-backed limiter before running multiple backend
instances.

## Configuration

```properties
chat.enabled=true
chat.max-message-length=1000
chat.max-context-items=5
chat.max-history-messages=10
chat.rate-limit.max-requests=20
chat.rate-limit.window-seconds=60
chat.ai.connect-timeout-seconds=5
chat.ai.read-timeout-seconds=30
```

The AI worker reuses the ATS Gemini environment variables:

```text
LLM_ENABLED
GEMINI_API_KEY
GEMINI_MODEL
GEMINI_BASE_URL
GEMINI_TIMEOUT_SECONDS
GEMINI_MAX_TEXT_LENGTH
```

