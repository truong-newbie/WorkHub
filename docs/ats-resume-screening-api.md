# ATS Resume Screening API Documentation for Frontend

Tai lieu nay mo ta luong ATS resume screening hien tai cua WorkHub de FE co the
trien khai man hinh recruiter.

## 1. Tong quan

Backend da co luong async hoan chinh:

1. Recruiter/admin queue screening cho mot job application.
2. Backend publish message qua RabbitMQ.
3. Consumer goi AI worker de parse CV va cham diem.
4. Backend luu hoac cap nhat `tbl_screening_results`.
5. Backend chuyen application sang `SCREENED`.
6. Backend gui notification realtime va luu notification trong database.
7. FE goi result API de doc bao cao.

Gioi han hien tai:

- AI worker da cham `skillScore`, nhung `semanticScore` dang la placeholder `0`.
- `experienceScore`, `educationScore` va `aiSummary` co the la `null`.
- Chua co khoa idempotency o buoc queue. Recruiter co the bam screen lai; ket
  qua cu cua application se duoc cap nhat thay vi tao them row moi.

## 2. Base URL va authentication

- Base URL: `/api/v1`
- REST header:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

Response thanh cong co wrapper:

```json
{
  "status": "SUCCESS",
  "data": {}
}
```

Actor hop le:

- `RECRUITER`
- `ADMIN`

Recruiter chi duoc thao tac neu la recruiter cua job hoac thuoc company so huu
job. Admin duoc phep thao tac moi job.

## 3. Luong FE

1. Lay danh sach applicants cua job.
2. Hien thi nut `Screen resume` tren tung application.
3. Khi recruiter bam nut, goi API queue va doi trang thai UI sang `PROCESSING`.
4. Subscribe notification WebSocket hoac polling notification REST API.
5. Khi nhan notification `ATS_SCREENING_COMPLETED`, goi result API theo
   `applicationId` dang theo doi hoac refresh danh sach result cua job.
6. Hien thi score, skills matched/missing/extra va summary neu co.

## 4. Queue ATS screening

```http
POST /api/v1/recruiter/applications/{applicationId}/screen
Authorization: Bearer <access_token>
```

Request body: khong co.

Response HTTP `200`:

```json
{
  "status": "SUCCESS",
  "data": {
    "applicationId": 501,
    "resumeId": 30,
    "jobId": 101,
    "screeningStatus": "PROCESSING",
    "message": "ATS screening job has been queued",
    "eventId": "5bf72c07-9db3-47a3-92f0-60f2ddb47ad6"
  }
}
```

FE khong gui `resumeId`. Backend uu tien CV da gan voi application. Duong
fallback CV shareable chi dung cho du lieu cu.

Error cases:

| HTTP status | Truong hop |
| --- | --- |
| `401` | Token khong hop le |
| `403` | Khong phai recruiter/admin hoac khong co quyen tren job |
| `404` | Application hoac CV khong ton tai |
| `500` | RabbitMQ hoac backend gap loi |

## 5. Lay applicants

Co hai URL tuong duong:

```http
GET /api/v1/job/{jobId}/applications?page=0&size=10
GET /api/v1/recruiter/jobs/{jobId}/applications?page=0&size=10
Authorization: Bearer <access_token>
```

FE dung `items[].id` lam `applicationId`.

Application status lien quan:

```text
APPLIED, PENDING, REVIEWING, SCREENED, APPROVED, REJECTED
```

## 6. Lay ket qua cua application

```http
GET /api/v1/recruiter/applications/{applicationId}/screening-result
Authorization: Bearer <access_token>
```

Response HTTP `200`:

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 900,
    "applicationId": 501,
    "candidateId": "candidate-uuid",
    "candidateName": "candidate01",
    "jobId": 101,
    "jobTitle": "Senior Java Developer",
    "totalScore": 75.0,
    "skillScore": 75.0,
    "semanticScore": 0.0,
    "experienceScore": null,
    "educationScore": null,
    "matchedSkills": ["Java", "Spring Boot", "Docker"],
    "missingSkills": ["Kafka"],
    "extraSkills": ["Redis"],
    "aiSummary": null,
    "screenedAt": "2026-05-31T10:35:00"
  }
}
```

Neu consumer chua xu ly xong hoac chua tung screen, backend tra `404`.

## 7. Lay bang xep hang cua job

```http
GET /api/v1/recruiter/jobs/{jobId}/screening-results
Authorization: Bearer <access_token>
```

Response `data` la array `ScreeningResultResponse`, sap xep giam dan theo
`totalScore`.

```json
{
  "status": "SUCCESS",
  "data": [
    {
      "id": 900,
      "applicationId": 501,
      "candidateId": "candidate-uuid",
      "candidateName": "candidate01",
      "jobId": 101,
      "jobTitle": "Senior Java Developer",
      "totalScore": 75.0,
      "skillScore": 75.0,
      "semanticScore": 0.0,
      "matchedSkills": ["Java", "Spring Boot", "Docker"],
      "missingSkills": ["Kafka"],
      "extraSkills": ["Redis"],
      "screenedAt": "2026-05-31T10:35:00"
    }
  ]
}
```

## 8. Notification

Sau khi scoring va luu database thanh cong, backend phat notification:

```json
{
  "type": "ATS_SCREENING_COMPLETED",
  "title": "ATS screening completed",
  "content": "Screening result for candidate01 is ready",
  "targetType": "SCREENING_RESULT",
  "targetId": "900"
}
```

`targetId` la screening result ID, khong phai application ID.

WebSocket topic cua user:

```text
/user/queue/notifications
```

## 9. AI worker va queue

Day la thong tin backend/DevOps. FE khong goi truc tiep.

- AI worker endpoint: `POST http://ai-worker:8000/api/v1/ai/resume/analyze`
- RabbitMQ exchange: `workhub.exchange`
- Queue: `ats.screening.queue`
- Routing key: `ats.screening.request`
- Dead-letter queue: `ats.screening.dlq`
- Retry mac dinh: `3`

## 10. File backend chinh

- `src/main/java/org/example/workhub/controller/JobApplicationController.java`
- `src/main/java/org/example/workhub/controller/ScreeningController.java`
- `src/main/java/org/example/workhub/service/impl/JobApplicationServiceImpl.java`
- `src/main/java/org/example/workhub/service/impl/ScreeningServiceImpl.java`
- `src/main/java/org/example/workhub/queue/consumer/AtsScreeningQueueConsumer.java`
- `src/main/java/org/example/workhub/domain/entity/ScreeningResult.java`
- `ai-worker/app/main.py`
