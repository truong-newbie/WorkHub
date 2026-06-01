# WorkHub

WorkHub la backend cho nen tang tuyen dung viec lam IT theo mo hinh ITviec.
Du an ho tro candidate, recruiter va admin trong mot he thong thong nhat:
quan ly cong ty, dang tin tuyen dung, ung tuyen bang CV, ATS resume screening,
goi y viec lam, bai test tuyen dung, realtime notification va AI chatbot.

## Tinh Nang Chinh

| Module | Mo ta |
|---|---|
| Authentication | Dang ky, dang nhap, JWT access/refresh token, logout, forgot password va OAuth2 callback |
| User | Quan ly profile, avatar, khoa/mo khoa tai khoan va role |
| Company | Quan ly cong ty, admin moderation, recruiter join request, logo va cover |
| Skill | CRUD skill, enable/disable, search, suggestion va popular skills |
| Job | CRUD job, publish/unpublish, search, favorite va application |
| Resume | Upload CV, thay file, CV mac dinh, visibility va recruiter access sau khi candidate apply |
| ATS screening | Queue resume screening, semantic similarity, Gemini explanation va ranking candidate |
| Recommendation | Goi y viec lam ket hop preference, behavior va collaborative score |
| Assessment | Recruiter tao bai test, giao bai, candidate lam bai va cham diem |
| Subscriber | Dang ky email viec lam phu hop theo skill |
| Notification | Luu notification va push realtime qua WebSocket/STOMP |
| Candidate chatbot | Chatbot read-only, tra loi dua tren du lieu WorkHub va Gemini |

## Kien Truc

```text
Frontend
   |
   | REST / WebSocket
   v
Spring Boot API :8080
   |-- MySQL :3306              Source of truth
   |-- Elasticsearch :9200      Job search index
   |-- RabbitMQ :5672           Background jobs
   |-- WebSocket /ws            Realtime notifications
   `-- FastAPI AI Worker :8000
          |-- Local embedding model for ATS
          `-- Gemini API for ATS explanation and chatbot
```

MySQL luon la nguon du lieu chinh. Elasticsearch chi phuc vu search.
RabbitMQ chi xu ly background jobs; frontend khong ket noi truc tiep vao queue.
Gemini khong truy cap MySQL hoac Elasticsearch truc tiep.

## Tech Stack

| Thanh phan | Cong nghe |
|---|---|
| Backend API | Java 17, Spring Boot 3.2, Maven |
| Security | Spring Security, JWT, OAuth2 |
| Database | MySQL 8, Spring Data JPA, Hibernate |
| Search | Elasticsearch 8.13 |
| Queue | RabbitMQ |
| Realtime | WebSocket, STOMP, SockJS |
| AI worker | Python 3.11, FastAPI |
| ATS semantic score | Sentence Transformers, scikit-learn, CPU-only PyTorch |
| Explainable AI | Gemini API |
| File storage | Cloudinary |
| Documentation | Springdoc OpenAPI, Swagger UI |

## Cau Truc Thu Muc

```text
WorkHub/
|-- src/main/java/org/example/workhub/
|   |-- controller/             REST controllers
|   |-- service/                Service interfaces
|   |-- service/impl/           Business logic
|   |-- repository/             JPA repositories
|   |-- domain/entity/          JPA entities
|   |-- domain/dto/             Request, response va internal DTOs
|   |-- domain/mapper/          DTO mappers
|   |-- queue/                  RabbitMQ config, message, producer, consumer
|   |-- listener/               Event listeners
|   |-- security/               JWT va WebSocket security
|   `-- config/                 Spring configuration
|-- src/main/resources/
|   |-- application.properties
|   |-- application-dev.properties.example
|   `-- i18n/
|-- ai-worker/
|   |-- app/
|   |-- tests/
|   |-- requirements.txt
|   `-- Dockerfile
|-- docs/ai/                    Tai lieu ky thuat theo module
|-- docker-compose.yml
|-- .env.example
`-- pom.xml
```

## Yeu Cau Moi Truong

Can cai dat:

- Java 17
- Docker Desktop va Docker Compose
- Git

Khong can cai Maven rieng vi du an co Maven Wrapper:

```powershell
.\mvnw.cmd --version
```

## Cau Hinh Local

### 1. Tao `.env`

`.env` duoc Docker Compose dung de khoi dong AI worker va MySQL.

```powershell
Copy-Item .env.example .env
```

De bat Gemini that, dien key vao `.env`:

```properties
GEMINI_API_KEY=<your-gemini-api-key>
LLM_ENABLED=true
```

Neu `LLM_ENABLED=false`:

- ATS van tinh semantic score bang embedding model local.
- ATS explanation se dung deterministic fallback.
- Candidate chatbot se tra fallback vi khong the phan loai intent bang Gemini.

Khong commit `.env` hoac API key that.

### 2. Tao `application-dev.properties`

```powershell
Copy-Item `
  .\src\main\resources\application-dev.properties.example `
  .\src\main\resources\application-dev.properties
```

File nay da nam trong `.gitignore`. Cau hinh mac dinh phu hop voi MySQL Docker:

```text
Database: workhub
Host:     localhost:3306
Username: root
Password: password
```

Upload CV, avatar va gui mail chi hoat dong khi dien Cloudinary va SMTP
credentials hop le. OAuth2 cung can them provider credentials neu su dung.

## Chay Du An

### Cach Khuyen Nghi: Dependency Bang Docker, Backend Chay Local

Khoi dong MySQL, Elasticsearch, RabbitMQ va AI worker:

```powershell
docker compose up -d mysql elasticsearch rabbitmq ai-worker
```

Kiem tra container:

```powershell
docker compose ps
```

Chay Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

Hoac chay class `WorkHubApplication` trong IntelliJ IDEA.

### Chay Toan Bo Bang Docker

Backend Docker image copy file JAR da build san. Build JAR truoc:

```powershell
.\mvnw.cmd clean package -DskipTests
docker compose up -d --build
```

### Dung He Thong

```powershell
docker compose down
```

Lenh tren giu nguyen database va model cache trong Docker volumes.

Chi dung lenh sau khi muon xoa toan bo du lieu local:

```powershell
docker compose down -v
```

## Dia Chi Dich Vu

| Dich vu | URL |
|---|---|
| Backend API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| AI worker Swagger | `http://localhost:8000/docs` |
| Elasticsearch | `http://localhost:9200` |
| RabbitMQ dashboard | `http://localhost:15672` |
| WebSocket SockJS endpoint | `http://localhost:8080/ws` |

RabbitMQ local mac dinh:

```text
Username: guest
Password: guest
```

## Kiem Tra AI Worker

### Kiem Tra Chatbot Intent

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8000/api/v1/ai/chat/intent" `
  -ContentType "application/json" `
  -Body '{"message":"Find Java backend jobs in Hanoi","recentMessages":[]}'
```

Ket qua mong doi:

```json
{
  "intent": "SEARCH_JOBS",
  "outOfScope": false,
  "keyword": "Java backend",
  "location": "Hanoi"
}
```

### Rebuild AI Worker Sau Khi Sua Code Python

```powershell
docker compose up -d --build ai-worker
```

Lan build dau tien co the lau vi Docker phai tai CPU-only PyTorch va
Sentence Transformers.

## API Tong Quan

Base path:

```text
/api/v1
```

Request can dang nhap gui:

```http
Authorization: Bearer <accessToken>
```

Response thanh cong thuong co dang:

```json
{
  "status": "SUCCESS",
  "data": {}
}
```

Nhom API chinh:

| Nhom | Prefix |
|---|---|
| Auth | `/api/v1/auth/**` |
| User | `/api/v1/user/**` |
| Company | `/api/v1/companies/**` |
| Skill | `/api/v1/skills/**` |
| Job | `/api/v1/job/**`, `/api/v1/jobs/**` |
| Resume | `/api/v1/resume/**` |
| Recommendation | `/api/v1/candidate/**` |
| Recruiter assessment | `/api/v1/recruiter/**` |
| Notification | `/api/v1/notifications/**` |
| Candidate chatbot | `/api/v1/chat/**` |

Chi tiet contract xem tai Swagger UI va [docs/ai/api-overview.md](docs/ai/api-overview.md).

## ATS Resume Screening

ATS flow la bat dong bo:

```text
Recruiter queue screening
  -> RabbitMQ
  -> ATS consumer
  -> AI worker
  -> local embedding score
  -> optional Gemini explanation
  -> persist ScreeningResult
  -> realtime notification
```

API recruiter:

```http
POST /api/v1/recruiter/applications/{applicationId}/screen
GET  /api/v1/recruiter/applications/{applicationId}/screening-result
GET  /api/v1/recruiter/jobs/{jobId}/screening-results
```

Chi tiet: [docs/ai/modules/ats.md](docs/ai/modules/ats.md).

## Candidate Chatbot

Chatbot la synchronous va read-only. Spring Boot truy van du lieu duoc phep,
rut gon context, sau do moi gui sang AI worker de Gemini phan loai intent va
dien dat cau tra loi.

API candidate:

```http
POST   /api/v1/chat/messages
GET    /api/v1/chat/conversations
GET    /api/v1/chat/conversations/{conversationId}/messages
DELETE /api/v1/chat/conversations/{conversationId}
```

Chatbot khong tu apply job, khong sua CV va khong truy cap du lieu user khac.

Chi tiet: [docs/ai/modules/chatbot.md](docs/ai/modules/chatbot.md).

## Realtime Notification

Frontend ket noi SockJS/STOMP:

```text
Endpoint:     /ws
Subscribe:    /user/queue/notifications
JWT:          Authorization header hoac ?token=
```

Chi tiet: [docs/ai/modules/notification.md](docs/ai/modules/notification.md).

## Background Queue

RabbitMQ transport cac background job:

```text
email.queue
ats.screening.queue
resume.parsing.queue
notification.queue
```

Moi queue co DLQ va retry. Frontend khong truy cap RabbitMQ truc tiep.

Chi tiet: [docs/ai/modules/queue-system.md](docs/ai/modules/queue-system.md).

## Chay Test

### Spring Boot

```powershell
.\mvnw.cmd test
```

Build artifact:

```powershell
.\mvnw.cmd clean package -DskipTests
```

### AI Worker

Chay test bang Python local:

```powershell
Set-Location .\ai-worker
python -m unittest discover -s tests -p "test_*.py"
Set-Location ..
```

Hoac chay trong container:

```powershell
docker compose run --rm ai-worker `
  python -m unittest discover -s tests -p "test_*.py"
```

## Troubleshooting

### Port `8080` Da Duoc Su Dung

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

Stop process Spring Boot cu trong IntelliJ hoac tat container backend cu.

### Chatbot Luon Tra `FALLBACK`

Kiem tra AI worker:

```powershell
docker compose ps ai-worker
docker compose logs -f ai-worker
```

Neu endpoint chatbot tra `404`, image AI worker dang cu:

```powershell
docker compose up -d --build ai-worker
```

Kiem tra `.env`:

```properties
LLM_ENABLED=true
GEMINI_API_KEY=<your-gemini-api-key>
```

### Elasticsearch Khong Hoat Dong

```powershell
docker compose up -d elasticsearch
```

Job search co JPA fallback khi:

```properties
search.job.fallback-to-jpa=true
```

### RabbitMQ Khong Hoat Dong

```powershell
docker compose up -d rabbitmq
docker compose logs -f rabbitmq
```

ATS screening, resume parsing, notification queue va email queue can RabbitMQ.

## Tai Lieu Cho Developer

- [Architecture](docs/ai/architecture.md)
- [API overview](docs/ai/api-overview.md)
- [Security](docs/ai/security.md)
- [Database notes](docs/ai/database.md)
- [Coding conventions](docs/ai/conventions.md)
- [Module docs](docs/ai/modules)

## Bao Mat

- Khong commit `.env`.
- Khong commit `application-dev.properties`.
- Khong dua Gemini API key xuong frontend.
- Khong ghi API key, JWT secret, SMTP password hoac Cloudinary secret vao log.
- Rotate ngay secret neu secret da tung bi chia se cong khai.

