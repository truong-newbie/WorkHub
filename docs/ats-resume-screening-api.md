# ATS Resume Screening API Documentation for Frontend

Tai lieu nay mo ta dung source code hien tai cua WorkHub cho chuc nang ATS
resume screening. Muc tieu la de FE hoac AI co the trien khai man hinh recruiter
screening ma khong hieu nham phan nao da hoan thanh va phan nao backend con
thieu.

## 1. Ket luan nhanh

Backend hien tai da co:

- API recruiter/admin gui yeu cau ATS screening cho mot application.
- Kiem tra quyen recruiter doi voi job.
- Tu dong chon CV shareable cua candidate.
- Dua tac vu vao RabbitMQ.
- Consumer nhan tac vu ATS.
- Notification REST va WebSocket cho recruiter.

Backend hien tai chua co:

- AI worker hoac logic cham diem ATS thuc te.
- Bang du lieu `ScreeningResult`.
- API lay ket qua ATS theo application.
- Trang thai screening duoc luu trong database.
- Cap nhat application thanh `SCREENED` sau khi cham diem.
- Idempotency de ngan recruiter bam screening nhieu lan.

Vi vay FE co the lam luong gui request va hien thi notification. FE chua the
hien thi diem ATS hoac bao cao phan tich hoan chinh neu backend chua bo sung
result API.

## 2. Base URL va authentication

- Base URL: `/api/v1`
- Header REST API:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

Response thanh cong:

```json
{
  "status": "SUCCESS",
  "data": {}
}
```

Response loi:

```json
{
  "status": "ERROR",
  "message": "Error message"
}
```

## 3. Actor va business rules

Chuc nang ATS screening danh cho:

- `RECRUITER`
- `ADMIN`

Recruiter chi duoc screen application neu:

- Recruiter la nguoi tao job; hoac
- Recruiter dang thuoc company so huu job.

Admin co the screen moi application.

Candidate phai co it nhat mot CV shareable:

- `isDefault = true`; hoac
- `isPublic = true`.

Neu co nhieu CV shareable, backend tu dong chon CV theo thu tu:

1. CV default truoc.
2. Neu khong co default thi lay CV public upload moi nhat.

FE khong gui `resumeId` khi queue screening.

## 4. Luong FE nen trien khai

1. Recruiter mo danh sach candidate cua job.
2. FE goi API lay applicants.
3. Moi applicant hien thi nut `Screen resume`.
4. Khi recruiter bam nut, FE goi API queue ATS screening.
5. Neu thanh cong, FE doi nut thanh `Processing` va luu `eventId` o state.
6. FE nhan notification qua WebSocket hoac polling REST notifications.
7. Hien tai notification chi xac nhan queue consumer da nhan request. No chua
   phai ket qua cham diem.
8. Khong hien thi score/result page that cho toi khi backend co result API.

## 5. API chinh: Queue ATS screening

```http
POST /api/v1/recruiter/applications/{applicationId}/screen
Authorization: Bearer <access_token>
```

Quyen: `RECRUITER` hoac `ADMIN`.

Path param:

| Param | Type | Required | Mo ta |
| --- | --- | --- | --- |
| `applicationId` | long | Yes | ID cua job application |

Request body: khong co.

Vi du:

```http
POST /api/v1/recruiter/applications/501/screen
Authorization: Bearer eyJ...
```

Response thanh cong HTTP `200`:

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

### Y nghia response

| Field | Type | Mo ta |
| --- | --- | --- |
| `applicationId` | long | Application dang duoc screen |
| `resumeId` | long | CV backend da tu dong chon |
| `jobId` | long | Job dung de screening |
| `screeningStatus` | string | Hien tai backend luon tra `"PROCESSING"` |
| `message` | string | Thong bao da queue thanh cong |
| `eventId` | string | UUID cua queue message, FE co the luu de debug |

### Error cases

| HTTP status | Truong hop |
| --- | --- |
| `401` | Chua dang nhap hoac token khong hop le |
| `403` | Khong phai recruiter/admin hoac khong co quyen doi voi job |
| `404` | Application khong ton tai |
| `404` | Candidate khong co CV default/public de screen |
| `500` | Khong the publish tac vu vao RabbitMQ |

## 6. API lay applicants truoc khi screen

```http
GET /api/v1/job/{jobId}/applications?page=0&size=10
Authorization: Bearer <access_token>
```

Quyen: recruiter cua job hoac `ADMIN`.

Pagination:

- `page`: 0-based, mac dinh `0`.
- `size`: mac dinh `10`.

Response:

```json
{
  "status": "SUCCESS",
  "data": {
    "meta": {
      "totalElements": 1,
      "totalPages": 1,
      "pageNum": 1,
      "pageSize": 10,
      "sortBy": "appliedAt",
      "sortType": "DESC"
    },
    "items": [
      {
        "id": 501,
        "status": "PENDING",
        "coverLetter": "I am interested in this position.",
        "appliedAt": "2026-05-31T10:30:00Z",
        "reviewedAt": null,
        "reviewNote": null,
        "job": {
          "id": 101,
          "title": "Senior Java Developer",
          "location": "Ho Chi Minh City",
          "companyName": "WorkHub"
        },
        "candidate": {
          "id": "candidate-uuid",
          "username": "candidate01",
          "email": "candidate@example.com",
          "phone": "0900000000",
          "avatar": "https://example.com/avatar.png",
          "headline": "Backend Developer"
        },
        "createdDate": "2026-05-31T10:30:00"
      }
    ]
  }
}
```

FE dung `items[].id` lam `applicationId` khi goi API screening.

Application status co the la:

```text
PENDING, REVIEWING, SCREENED, APPROVED, REJECTED
```

Luu y: consumer ATS hien tai chua tu dong cap nhat application status thanh
`SCREENED`.

## 7. API xem CV cua candidate

### 7.1. Xem CV

```http
GET /api/v1/job/{jobId}/candidates/{candidateId}/resume
Authorization: Bearer <access_token>
```

Quyen: recruiter tao job do hoac `ADMIN`. Candidate phai da apply job.

Response:

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 30,
    "title": "Senior Java Backend Resume",
    "fileName": "resume.pdf",
    "fileUrl": "https://example.com/resume.pdf",
    "fileType": "pdf",
    "fileSize": 120000,
    "isDefault": true,
    "isPublic": false,
    "deleted": false,
    "summary": "Java backend engineer",
    "atsScore": null,
    "parsedContent": null,
    "uploadedAt": "2026-05-30T09:00:00",
    "candidate": {
      "id": "candidate-uuid",
      "username": "candidate01",
      "email": "candidate@example.com",
      "phone": "0900000000",
      "headline": "Backend Developer",
      "avatar": "https://example.com/avatar.png"
    },
    "skills": [
      {
        "id": 1,
        "name": "Java",
        "level": "ADVANCED"
      }
    ]
  }
}
```

`atsScore` va `parsedContent` hien co trong Resume model nhung queue consumer
chua tu dong ghi du lieu vao hai field nay.

### 7.2. Lay metadata download CV

```http
GET /api/v1/job/{jobId}/candidates/{candidateId}/resume/download
Authorization: Bearer <access_token>
```

Response:

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 30,
    "title": "Senior Java Backend Resume",
    "fileName": "resume.pdf",
    "fileUrl": "https://example.com/resume.pdf",
    "fileType": "pdf",
    "fileSize": 120000
  }
}
```

## 8. Notification cho FE

Sau khi ATS consumer nhan queue message, backend publish mot notification cho
recruiter cua job. Notification nay duoc luu trong database va gui realtime qua
WebSocket.

Notification hien tai:

```json
{
  "id": 900,
  "type": "ATS_SCREENING_COMPLETED",
  "title": "ATS screening queued",
  "content": "ATS screening request has been consumed and is ready for the AI worker integration.",
  "targetType": "APPLICATION",
  "targetId": "501",
  "read": false,
  "createdAt": "2026-05-31T10:31:00",
  "readAt": null,
  "senderId": null,
  "senderName": null,
  "recipientId": "recruiter-uuid"
}
```

Quan trong: type dang ten `ATS_SCREENING_COMPLETED`, nhung voi source code hien
tai no chi co nghia queue consumer da nhan request. FE khong duoc coi day la ket
qua ATS da cham diem xong.

### 8.1. WebSocket realtime

SockJS endpoint:

```text
/ws?token=<access_token>
```

STOMP subscribe destination:

```text
/user/queue/notifications
```

Backend cung chap nhan token qua `Authorization: Bearer <access_token>` trong
handshake hoac STOMP CONNECT header.

### 8.2. REST notifications fallback

Lay notification ATS:

```http
GET /api/v1/notifications?type=ATS_SCREENING_COMPLETED&pageNum=1&pageSize=10&sortBy=createdAt&isAscending=false
Authorization: Bearer <access_token>
```

Pagination cua notification dung `pageNum` 1-based.

Lay so notification chua doc:

```http
GET /api/v1/notifications/unread-count
Authorization: Bearer <access_token>
```

Response:

```json
{
  "status": "SUCCESS",
  "data": {
    "unreadCount": 3
  }
}
```

Danh dau da doc:

```http
PUT /api/v1/notifications/{notificationId}/read
Authorization: Bearer <access_token>
```

Danh dau tat ca da doc:

```http
PUT /api/v1/notifications/read-all
Authorization: Bearer <access_token>
```

## 9. State FE co the dung ngay

FE nen quan ly trang thai nut screening theo application:

```ts
type AtsScreeningUiState =
  | "IDLE"
  | "REQUESTING"
  | "PROCESSING"
  | "QUEUE_CONSUMED"
  | "ERROR";
```

Mapping:

| Thoi diem | UI state |
| --- | --- |
| Chua bam nut | `IDLE` |
| Dang goi API | `REQUESTING` |
| API tra `screeningStatus: "PROCESSING"` | `PROCESSING` |
| Nhan notification hien tai | `QUEUE_CONSUMED` |
| API loi | `ERROR` |

Khong nen them state `COMPLETED` hoac hien thi score that cho toi khi backend co
result API.

## 10. Man hinh FE co the lam ngay

### Recruiter applicant list

Moi dong candidate nen co:

- Candidate name, email, headline.
- Application status.
- Applied time.
- Nut xem CV.
- Nut download CV.
- Nut `Screen resume`.
- Badge `Processing` sau khi queue thanh cong.

### Notification center

- Subscribe WebSocket `/user/queue/notifications`.
- Fallback bang REST polling.
- Khi nhan `type = ATS_SCREENING_COMPLETED`, doc `targetId` nhu application ID.
- Cap nhat badge cua dong applicant tuong ung thanh `Queue consumed`.

### Result panel

Hien tai chi nen de placeholder:

```text
ATS result is not available yet.
```

## 11. Backend con thieu de FE lam ATS hoan chinh

Day la blocker, khong phai API hien co.

### 11.1. Luu screening result

Backend can entity rieng, vi du:

```text
AtsScreeningResult
- id
- eventId
- applicationId
- resumeId
- jobId
- status: QUEUED | PROCESSING | COMPLETED | FAILED
- overallScore
- matchedSkills
- missingSkills
- summary
- recommendation
- errorMessage
- requestedByUserId
- createdAt
- completedAt
```

### 11.2. API doc ket qua

Backend can bo sung endpoint, vi du:

```http
GET /api/v1/recruiter/applications/{applicationId}/screening-result
```

Response de FE hien thi:

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 700,
    "applicationId": 501,
    "resumeId": 30,
    "jobId": 101,
    "status": "COMPLETED",
    "overallScore": 86,
    "matchedSkills": ["Java", "Spring Boot"],
    "missingSkills": ["Kafka"],
    "summary": "Candidate matches the core backend requirements.",
    "recommendation": "REVIEW"
  }
}
```

### 11.3. AI worker va lifecycle

Backend can:

1. Luu row `QUEUED` truoc khi publish RabbitMQ.
2. Consumer doi row thanh `PROCESSING`.
3. AI worker parse CV va so sanh voi job description.
4. Luu ket qua `COMPLETED` hoac `FAILED`.
5. Cap nhat application thanh `SCREENED` neu thanh cong.
6. Phat notification completed that voi `targetType = SCREENING_RESULT`.
7. Ngan queue trung neu dang co result `QUEUED` hoac `PROCESSING`.

## 12. Luu y quyen truy cap hien tai

Co mot diem khong dong nhat trong backend:

- API queue screening cho phep recruiter cung company voi job.
- API xem/download candidate resume chi cho recruiter tao job do hoac admin.

Vi vay mot recruiter cung company nhung khong tao job co the queue screening
thanh cong nhung lai bi `403` khi xem CV. Backend nen thong nhat rule truoc khi
FE mo tinh nang cho toan bo recruiter cua company.

## 13. Phan FE khong can quan tam

Khong can gui cho FE chi tiet RabbitMQ sau:

- Exchange: `workhub.exchange`
- Queue: `ats.screening.queue`
- Routing key: `ats.screening.request`
- Dead-letter queue: `ats.screening.dlq`
- Retry mac dinh: `3`

Day la tai lieu backend/DevOps. FE chi goi REST API va subscribe notification.

## 14. Source files doi chieu

- `src/main/java/org/example/workhub/controller/JobApplicationController.java`
- `src/main/java/org/example/workhub/service/impl/JobApplicationServiceImpl.java`
- `src/main/java/org/example/workhub/domain/dto/response/AtsScreeningQueuedResponse.java`
- `src/main/java/org/example/workhub/queue/consumer/AtsScreeningQueueConsumer.java`
- `src/main/java/org/example/workhub/queue/producer/BackgroundJobProducer.java`
- `src/main/java/org/example/workhub/controller/ResumeController.java`
- `src/main/java/org/example/workhub/controller/NotificationController.java`
- `src/main/java/org/example/workhub/config/WebSocketConfig.java`
