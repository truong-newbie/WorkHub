Bạn là Senior Backend Engineer kiêm AI Integration Engineer.

Tôi đang xây dựng hệ thống tuyển dụng giống ITviec bằng:

- Java 17
- Spring Boot 3
- Maven
- MySQL/PostgreSQL
- Spring Security + JWT
- JPA + Specification
- DTO + Mapper architecture
- Global Exception Handling
- i18n
- Swagger/OpenAPI
- Docker

Dự án hiện tại đã có core chuẩn và các chức năng cơ bản sau:

- Register
- Login
- Logout
- Forgot password
- User module
- Company module
- Skill module
- Job module
- Resume module
- Subscriber module

Nhiệm vụ của bạn là implement chức năng:

AI-powered ATS Resume Screening

Mục tiêu nghiệp vụ:

Recruiter tạo Job.
Candidate upload Resume/CV.
Candidate apply vào Job.
Recruiter có thể chạy ATS Screening để đánh giá CV của từng ứng viên so với Job Description.
Hệ thống trả về:
- total score
- skill score
- semantic score
- matched skills
- missing skills
- AI summary
- ranking danh sách ứng viên theo điểm giảm dần

QUAN TRỌNG:

Trước khi code, hãy đọc kỹ file AI_Context.md nếu có.
Không được tự tạo architecture mới nếu project đã có convention.
Phải follow đúng package structure hiện tại:
- controller
- service
- service.impl
- repository
- entity
- dto.request
- dto.response
- mapper
- specification
- constant
- exception
- util
- base
- config

Không viết lại các chức năng đã có:
- auth
- user
- company
- skill
- job
- resume
- subscriber

Chỉ bổ sung phần còn thiếu cho ATS Screening.

==================================================
KIẾN TRÚC MONG MUỐN
==================================================

Spring Boot giữ vai trò Core Backend:

- quản lý Job
- quản lý Resume
- quản lý Application
- quản lý ScreeningResult
- tính Weighted Score
- lưu kết quả vào database
- trả API cho Frontend
- bảo vệ API bằng JWT/RBAC

Python FastAPI giữ vai trò AI Worker:

- parse resume file PDF
- extract skills
- calculate semantic score
- generate AI summary sau này
- trả JSON về Spring Boot

Giai đoạn đầu, nếu chưa có Python service thật thì hãy tạo interface/client bên Spring Boot trước, có thể mock response để test flow.

==================================================
CÁC MODULE CẦN BỔ SUNG
==================================================

1. Application module

Candidate apply job bằng resume đã upload.

Entity cần có nếu chưa tồn tại:

Application:
- id
- job
- candidate/user
- resume
- status
- appliedAt
- createdAt
- updatedAt

Status enum:
- APPLIED
- SCREENED
- SHORTLISTED
- REJECTED
- INTERVIEWING
- HIRED

API cần có:

POST /api/v1/candidate/jobs/{jobId}/apply
Body:
{
  "resumeId": 1
}

GET /api/v1/candidate/applications

GET /api/v1/recruiter/jobs/{jobId}/applications

Yêu cầu:
- Candidate chỉ được apply bằng resume của chính mình.
- Không cho apply trùng cùng một job bằng cùng một candidate.
- Recruiter chỉ được xem applications thuộc job/company của mình nếu project đã có ownership logic.
- Khi apply thành công, status mặc định là APPLIED.

2. ScreeningResult module

Lưu kết quả ATS Screening.

Entity:

ScreeningResult:
- id
- application
- totalScore
- skillScore
- semanticScore
- experienceScore
- educationScore
- matchedSkills
- missingSkills
- extraSkills
- aiSummary
- createdAt
- updatedAt

Có thể lưu matchedSkills/missingSkills/extraSkills dạng JSON string hoặc tạo bảng phụ nếu project đã có convention tốt hơn.

API cần có:

POST /api/v1/recruiter/applications/{applicationId}/screen

GET /api/v1/recruiter/applications/{applicationId}/screening-result

GET /api/v1/recruiter/jobs/{jobId}/screening-results

Yêu cầu:
- API screen sẽ gọi AI Worker Client.
- Nhận kết quả từ AI Worker.
- Spring Boot tính final totalScore.
- Lưu ScreeningResult.
- Update Application status = SCREENED.
- API ranking trả danh sách ScreeningResult theo totalScore giảm dần.

3. AI Worker Client trong Spring Boot

Tạo client để Spring Boot gọi Python FastAPI.

Tên gợi ý:
AiWorkerClient

Endpoint Python dự kiến:

POST http://localhost:8000/api/v1/ai/resume/analyze

Request dạng multipart/form-data:
- file: resume file
- job_description: String

Response:

{
  "raw_text": "...",
  "resume_skills": ["Java", "Spring Boot", "Docker"],
  "job_skills": ["Java", "Spring Boot", "Redis", "Docker"],
  "matched_skills": ["Java", "Spring Boot", "Docker"],
  "missing_skills": ["Redis"],
  "extra_skills": ["React"],
  "skill_score": 75.0,
  "semantic_score": 82.5,
  "ai_summary": "Ứng viên phù hợp với vị trí Backend Java nhưng còn thiếu Redis."
}

Yêu cầu:
- Dùng WebClient hoặc RestTemplate theo convention của project.
- Config base URL trong application.properties:
  ai.worker.base-url=http://localhost:8000
- Có timeout hợp lý.
- Có xử lý lỗi nếu Python service down.
- Nếu service down thì trả exception rõ ràng, không làm crash toàn bộ app.

DTO cần có:

AiResumeAnalysisResponse:
- rawText
- resumeSkills
- jobSkills
- matchedSkills
- missingSkills
- extraSkills
- skillScore
- semanticScore
- aiSummary

4. Weighted Scoring

Spring Boot chịu trách nhiệm tính final score.

Giai đoạn MVP dùng công thức:

totalScore = skillScore * 0.6 + semanticScore * 0.4

Nếu semanticScore null hoặc bằng 0 thì:
totalScore = skillScore

Tạo service riêng:

ScoreCalculatorService

Method:
calculateTotalScore(AiResumeAnalysisResponse response)

Không hardcode trong controller.

5. DTO Response cho frontend

ScreeningResultResponse cần trả:

- applicationId
- candidateId
- candidateName
- jobId
- jobTitle
- totalScore
- skillScore
- semanticScore
- experienceScore
- educationScore
- matchedSkills
- missingSkills
- extraSkills
- aiSummary
- screenedAt

ApplicationResponse cần trả:

- id
- jobId
- jobTitle
- candidateId
- candidateName
- resumeId
- status
- appliedAt

6. Security/RBAC

Áp dụng phân quyền nếu project đã có:

Candidate:
- apply job
- xem application của chính mình

Recruiter:
- xem applications của job mình tạo
- chạy screening
- xem screening results

Admin:
- có thể xem toàn bộ nếu hệ thống đã hỗ trợ admin

Không phá security config hiện tại.

7. Repository cần có

ApplicationRepository:
- existsByJobIdAndCandidateId
- findByCandidateId
- findByJobId
- findByJobIdAndStatus
- findById

ScreeningResultRepository:
- findByApplicationId
- findByApplicationJobIdOrderByTotalScoreDesc
- existsByApplicationId

Nếu đã có base repository/convention thì follow.

8. Mapper

Nếu project dùng MapStruct, hãy tạo:

ApplicationMapper
ScreeningResultMapper

Không mapping thủ công trong controller.

9. Exception

Sử dụng exception convention hiện tại.

Các case cần xử lý:
- Job not found
- Resume not found
- Application not found
- Candidate already applied
- Resume does not belong to candidate
- Recruiter has no permission for this job
- AI Worker unavailable
- Screening result not found

10. Python AI Worker skeleton

Nếu repo có thể chứa thêm folder ai-worker thì tạo skeleton:

ai-worker/
- app/main.py
- app/services/parser_service.py
- app/services/skill_service.py
- app/services/semantic_service.py
- requirements.txt
- Dockerfile

requirements.txt:
fastapi
uvicorn
python-multipart
pdfplumber
pydantic

Endpoint:

POST /api/v1/ai/resume/analyze

Input:
- file
- job_description

Output JSON giống AiResumeAnalysisResponse.

MVP Python:
- parse PDF bằng pdfplumber
- extract skill bằng dictionary đơn giản
- skill_score = matched / job_skills * 100
- semantic_score = 0
- ai_summary = null

Chưa cần OpenAI/Gemini ở giai đoạn này.

11. Docker Compose

Nếu project đã có docker-compose thì bổ sung service:

ai-worker:
  build: ./ai-worker
  ports:
    - "8000:8000"
  environment:
    - PYTHONUNBUFFERED=1

Spring Boot gọi bằng:
ai.worker.base-url=http://ai-worker:8000
khi chạy docker compose.

Khi chạy local:
ai.worker.base-url=http://localhost:8000

12. Swagger/OpenAPI

Bổ sung mô tả API cho:
- apply job
- get candidate applications
- get recruiter job applications
- screen application
- get screening result
- get ranking

13. Không được làm

- Không viết lại auth.
- Không viết lại job/company/resume nếu đã có.
- Không đổi response format chung nếu project đã có BaseResponse.
- Không bypass security.
- Không hardcode userId.
- Không để controller chứa business logic.
- Không gọi AI Worker trực tiếp từ controller.
- Không lưu file resume lộn xộn nếu project đã có file/media service.
- Không thêm microservice phức tạp ngoài FastAPI worker đơn giản.

==================================================
THỨ TỰ IMPLEMENT
==================================================

Bước 1:
Đọc AI_Context.md và inspect các module có sẵn:
- User
- Job
- Resume
- Company
- Skill
- Security
- BaseResponse
- Exception
- Mapper
- Repository convention

Bước 2:
Tạo Application entity/module nếu chưa có.

Bước 3:
Tạo API Candidate apply job.

Bước 4:
Tạo ScreeningResult entity/module.

Bước 5:
Tạo AiWorkerClient + DTO response.

Bước 6:
Tạo ScoreCalculatorService.

Bước 7:
Tạo ScreeningService:
- lấy application
- lấy job description
- lấy resume file/path/text
- gọi AiWorkerClient
- tính total score
- lưu ScreeningResult
- update status SCREENED
- trả ScreeningResultResponse

Bước 8:
Tạo Recruiter APIs:
- screen application
- get screening result
- get ranking by job

Bước 9:
Tạo Python ai-worker skeleton nếu cần.

Bước 10:
Test end-to-end bằng Postman:
- Recruiter tạo job
- Candidate upload resume
- Candidate apply job
- Recruiter screen application
- Recruiter xem ranking

==================================================
KẾT QUẢ MONG MUỐN
==================================================

Sau khi hoàn thành, tôi muốn có flow chạy được:

1. Candidate upload CV.
2. Candidate apply vào một Job.
3. Recruiter mở danh sách ứng viên của Job.
4. Recruiter bấm Screening.
5. Spring Boot gọi Python AI Worker.
6. Python trả về skills, matched, missing, skillScore, semanticScore.
7. Spring Boot tính totalScore.
8. Lưu kết quả.
9. Recruiter xem ranking ứng viên theo điểm.

Hãy implement theo từng bước, mỗi bước cần giải thích file nào được tạo/sửa và lý do.
Nếu thiếu thông tin từ codebase, hãy hỏi lại trước khi sửa architecture lớn.

==================================================

IMPORTANT

Không generate code thiếu import
Không dùng field không tồn tại
Không tự tạo architecture mới
Không tự implement lại CRUD Skill đã có
Reuse class hiện tại
Mọi endpoint phải đúng RESTful convention
Mọi business logic phải production-ready
Nếu cần thêm entity/table mới thì explain rõ trước khi code
Nếu thiếu thông tin thì hỏi tôi trước khi implement

==================================================

AI_CONTEXT.md USAGE RULE

TRƯỚC KHI CODE:

Hãy đọc file AI_Context.md để hiểu:

kiến trúc dự án
coding convention
business flow
package structure
naming convention
các module đã tồn tại
các util/base/specification hiện có
Skill module hiện tại đã làm gì

Mục đích của AI_Context.md:

chỉ dùng làm CONTEXT tham khảo trước khi implement
giúp hiểu dự án tốt hơn
tránh generate sai architecture
tránh tạo code trùng lặp
tránh đổi naming convention

==================================================

QUAN TRỌNG

NHIỆM VỤ CHÍNH là NÂNG CẤP SKILL MODULE
KHÔNG tập trung vào việc chỉnh sửa AI_Context.md trong lúc code
KHÔNG dừng feature để đi rewrite documentation
AI_Context.md KHÔNG phải output chính

Output chính là:

code hoàn chỉnh
API hoàn chỉnh
business logic hoàn chỉnh
security hoàn chỉnh
validation hoàn chỉnh
production-ready implementation

==================================================

SAU KHI HOÀN THIỆN FEATURE

CHỈ SAU KHI:

code xong
API xong
service xong
controller xong
security xong
validation xong
i18n xong
request/response xong
module hoàn chỉnh

THÌ MỚI:

cập nhật lại AI_Context.md

==================================================

PRIORITY ORDER

Thứ tự ưu tiên:

Đọc AI_Context.md và phân tích Skill module hiện tại
Xác định chức năng Skill đã có để không làm trùng
Kiểm tra URL API hiện tại và đồng bộ nếu cần
Hoàn thiện feature nâng cấp Skill
Hoàn thiện API/business logic
Hoàn thiện validation/security
Hoàn thiện request/response
Test flow
Sau cùng mới update AI_Context.md