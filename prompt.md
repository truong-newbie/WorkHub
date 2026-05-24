Bạn là Senior Java Backend Engineer kiêm Recommendation System Engineer.

Tôi đang xây dựng dự án WorkHub - hệ thống tuyển dụng giống ITviec bằng:

- Spring Boot 3
- Java 17
- Maven
- MySQL
- Spring Security + JWT
- JPA/Hibernate
- DTO + Mapper architecture
- MapStruct
- i18n message
- GlobalExceptionHandler
- Base response wrapper
- Role-based authorization

Package root:

org.example.workhub

API base path:

/api/v1

Dự án hiện đã có:
- Auth
- User
- Company
- Skill
- Job
- Resume
- Job Application
- Favorite Job
- Subscriber
- ATS Resume Screening
- Assessment
- Personalized Job Feed MVP

Hiện tại đã có MVP recommendation flow:
- Candidate tạo job preference
- Candidate gọi recommended jobs
- Score dựa trên skill, title, location/work mode, experience, salary
- Exclude jobs đã apply
- Sort theo matchScore DESC, createdDate DESC

Tôi muốn nâng cấp thành Hybrid Recommendation Engine v2.

==================================================
MỤC TIÊU FEATURE
==================================================

Xây dựng hệ thống gợi ý việc làm cá nhân hóa gồm 3 lớp scoring:

1. Content-based Recommendation
   Dựa trên hồ sơ/preference của candidate:
   - desired title
   - skills
   - location
   - work mode
   - employment type
   - level
   - experience years
   - expected salary

2. Behavioral Recommendation
   Dựa trên hành vi thực tế của candidate:
   - viewed jobs
   - clicked jobs
   - saved/favorite jobs
   - applied jobs
   - search keywords

3. Collaborative Filtering
   Dựa trên những user giống candidate:
   - users có skill/preference/behavior tương tự
   - những job mà users giống họ đã apply/save/view nhiều
   - recommend job candidate chưa apply

Mục tiêu cuối:
GET /api/v1/candidate/jobs/recommended
phải trả job được sắp xếp theo hybridScore.

Formula MVP v2:

hybridScore =
contentScore * 0.50
+ behaviorScore * 0.30
+ collaborativeScore * 0.20

Có thể cấu hình weight trong application.yml.

==================================================
YÊU CẦU BẮT BUỘC TRƯỚC KHI CODE
==================================================

Hãy đọc kỹ AI_Context.md trước khi implement.

Tuân thủ tuyệt đối architecture hiện tại:
- Không đổi package root
- Không phá API cũ
- Không refactor lan man
- Không hardcode message lỗi
- Không bỏ i18n
- Không đổi response wrapper
- Không thay đổi security rule hiện có nếu không cần
- Không xóa MVP recommendation cũ, chỉ nâng cấp theo hướng v2
- MySQL vẫn là source of truth

Response format:
return VsResponseUtil.success(data);

Pagination:
PaginationResponseDto<T>

Exception:
ErrorMessage + i18n properties

Security:
Candidate endpoints yêu cầu authenticated user.
Service-layer phải check current user ownership.

==================================================
DATABASE DESIGN
==================================================

Tạo các entity tracking hành vi:

1. JobViewHistory

Table: tbl_job_view_histories

Fields:
- id: Long
- user: User ManyToOne nullable false
- job: Job ManyToOne nullable false
- viewedAt: LocalDateTime
- source: String nullable
- sessionId: String nullable

Index:
- user_id
- job_id
- viewed_at
- user_id + job_id

2. JobClickHistory

Table: tbl_job_click_histories

Fields:
- id: Long
- user: User ManyToOne nullable false
- job: Job ManyToOne nullable false
- clickedAt: LocalDateTime
- source: String nullable
- position: Integer nullable

Index:
- user_id
- job_id
- clicked_at

3. JobSearchHistory

Table: tbl_job_search_histories

Fields:
- id: Long
- user: User ManyToOne nullable false
- keyword: String nullable false
- filtersJson: TEXT nullable
- searchedAt: LocalDateTime

Index:
- user_id
- searched_at
- keyword

4. Optional: JobRecommendationLog

Table: tbl_job_recommendation_logs

Fields:
- id: Long
- user: User ManyToOne
- job: Job ManyToOne
- contentScore: Double
- behaviorScore: Double
- collaborativeScore: Double
- hybridScore: Double
- reasonCodes: TEXT
- generatedAt: LocalDateTime

MVP có thể không cần lưu log nếu muốn đơn giản. Nhưng nếu có log thì rất tốt để debug/demo.

==================================================
ENUMS
==================================================

Tạo enum nếu cần:

RecommendationEventType:
- VIEW
- CLICK
- SAVE
- APPLY
- SEARCH

RecommendationReasonCode:
- MATCHED_SKILL
- MATCHED_TITLE
- MATCHED_LOCATION
- MATCHED_WORK_MODE
- MATCHED_EMPLOYMENT_TYPE
- MATCHED_EXPERIENCE
- MATCHED_SALARY
- BASED_ON_VIEW_HISTORY
- BASED_ON_CLICK_HISTORY
- BASED_ON_SAVED_JOBS
- BASED_ON_APPLIED_JOBS
- BASED_ON_SEARCH_KEYWORD
- USERS_LIKE_YOU_APPLIED
- USERS_LIKE_YOU_SAVED

==================================================
PACKAGE STRUCTURE ĐỀ XUẤT
==================================================

Tạo/sửa các file theo convention hiện tại.

Entities:
domain/entity/JobViewHistory.java
domain/entity/JobClickHistory.java
domain/entity/JobSearchHistory.java
domain/entity/JobRecommendationLog.java optional

Repositories:
repository/JobViewHistoryRepository.java
repository/JobClickHistoryRepository.java
repository/JobSearchHistoryRepository.java
repository/JobRecommendationLogRepository.java optional

DTO request:
domain/dto/request/JobBehaviorTrackRequest.java
domain/dto/request/JobSearchTrackRequest.java
domain/dto/request/RecommendedJobRequest.java nếu cần

DTO response:
domain/dto/response/RecommendedJobResponse.java
domain/dto/response/RecommendationReasonResponse.java
domain/dto/response/JobBehaviorSummaryResponse.java optional

Service:
service/JobBehaviorService.java
service/RecommendationEngineService.java
service/impl/JobBehaviorServiceImpl.java
service/impl/RecommendationEngineServiceImpl.java

Controller:
controller/JobBehaviorController.java
controller/CandidateRecommendationController.java nếu controller hiện có chưa đủ

Mapper:
domain/mapper/RecommendationMapper.java nếu cần

Config:
config/RecommendationProperties.java

Docs:
docs/hybrid-recommendation-engine-v2.md
docs/hybrid-recommendation-test-checklist.md

==================================================
APPLICATION CONFIG
==================================================

Thêm config:

recommendation:
  weights:
    content: 0.50
    behavior: 0.30
    collaborative: 0.20
  behavior:
    view-weight: 1
    click-weight: 3
    save-weight: 5
    apply-weight: 10
    search-keyword-weight: 2
    history-days: 30
  collaborative:
    similar-user-limit: 20
    min-similarity: 0.25
  result:
    max-candidates: 200
    default-page-size: 10

Tạo RecommendationProperties bind bằng @ConfigurationProperties.

==================================================
API DESIGN
==================================================

1. Track job view

POST /api/v1/candidate/jobs/{jobId}/view

Access:
Authenticated candidate

Body optional:
{
  "source": "HOME_RECOMMENDATION",
  "sessionId": "abc"
}

Logic:
- Check job exists, not deleted
- Save JobViewHistory
- Return success

2. Track job click

POST /api/v1/candidate/jobs/{jobId}/click

Access:
Authenticated candidate

Body:
{
  "source": "RECOMMENDATION",
  "position": 3
}

Logic:
- Check job exists, not deleted
- Save JobClickHistory
- Return success

3. Track search keyword

POST /api/v1/candidate/jobs/search-track

Access:
Authenticated candidate

Body:
{
  "keyword": "java developer",
  "filtersJson": "{\"location\":\"hanoi\"}"
}

Logic:
- Save JobSearchHistory
- Return success

4. Recommended jobs v2

GET /api/v1/candidate/jobs/recommended

Query params:
- pageNum default 1
- pageSize default 10
- location optional
- refresh optional boolean
- explain optional boolean

Logic:
- Load current candidate
- Load candidate preference
- Load active/published jobs
- Exclude already applied jobs
- Calculate contentScore
- Calculate behaviorScore
- Calculate collaborativeScore
- Calculate hybridScore
- Sort hybridScore DESC, createdDate DESC
- Return PaginationResponseDto<RecommendedJobResponse>

5. Optional behavior summary

GET /api/v1/candidate/recommendation/behavior-summary

Return:
- totalViewed
- totalClicked
- totalSaved
- totalApplied
- topSkillInterests
- topTitleKeywords
- topLocations

==================================================
RECOMMENDED JOB RESPONSE
==================================================

RecommendedJobResponse fields:

- id
- title
- slug
- companyId
- companyName
- companyLogo
- location
- salaryMin
- salaryMax
- negotiableSalary
- level
- workMode
- employmentType
- experienceYears
- skillNames
- expiredAt
- createdDate

Recommendation scores:
- contentScore
- behaviorScore
- collaborativeScore
- hybridScore

Explainability:
- matchedSkills
- missingSkills
- reasonCodes
- reasonText

Example reasonText:
"Phù hợp vì bạn có 3 kỹ năng trùng với job, đã lưu nhiều job Java Backend và những ứng viên giống bạn cũng ứng tuyển job này."

==================================================
CONTENT-BASED SCORING
==================================================

Score từ 0 đến 100.

Gợi ý formula:

contentScore =
skillScore * 0.40
+ titleScore * 0.20
+ locationWorkModeScore * 0.15
+ experienceScore * 0.15
+ salaryScore * 0.10

skillScore:
- candidateSkills ∩ jobSkills / jobSkills
- nếu job không yêu cầu skill thì 0 hoặc neutral 50

titleScore:
- simple contains/token match giữa desiredTitle và job.title
- ví dụ "java developer" match "senior java developer"
- có thể dùng normalized lowercase tokens

locationWorkModeScore:
- location match: +50
- workMode match: +50
- nếu thiếu field thì tính phần còn lại

experienceScore:
- nếu candidate experience >= job experienceYears → 100
- nếu thấp hơn → candidateExp / requiredExp * 100
- nếu job không yêu cầu experience → 80 hoặc 100

salaryScore:
- nếu expectedSalary overlap với salaryMin/salaryMax → 100
- nếu negotiableSalary = true → 80
- nếu thiếu salary → 50

==================================================
BEHAVIORAL SCORING
==================================================

Score từ 0 đến 100.

Dựa trên hành vi trong N ngày gần nhất, mặc định 30 ngày.

Nguồn hành vi:
- viewed jobs
- clicked jobs
- saved/favorite jobs
- applied jobs
- search keywords

Cách tính đơn giản:

1. Build user interest profile:
- skillInterestMap: skillName -> weight
- titleKeywordInterestMap: keyword -> weight
- locationInterestMap: location -> weight
- companyInterestMap: companyId -> weight

Weight:
- view = 1
- click = 3
- save = 5
- apply = 10
- search keyword = 2

2. Với mỗi candidate job:
- nếu job có skill nằm trong skillInterestMap → cộng điểm
- nếu job title chứa title keyword user hay search/click → cộng điểm
- nếu job location trùng location user hay xem → cộng điểm
- nếu company từng được user quan tâm → cộng điểm

3. Normalize về 0-100.

Ví dụ:
User hay view/click/save Java Spring jobs.
Job mới có Java + Spring Boot.
behaviorScore cao.

==================================================
COLLABORATIVE FILTERING SCORING
==================================================

MVP collaborative không cần ML phức tạp.

Mục tiêu:
"Users giống bạn đã apply/save job này."

Step 1:
Find similar users.

User similarity dựa trên:
- shared skills/preference skills
- shared saved jobs
- shared applied jobs
- shared viewed/clicked jobs

Simple formula:

similarity =
skillSimilarity * 0.40
+ appliedJobSimilarity * 0.30
+ savedJobSimilarity * 0.20
+ behaviorSimilarity * 0.10

Skill similarity:
Jaccard similarity:
intersection / union

Applied/saved similarity:
Jaccard trên tập jobId.

Step 2:
Lấy top similar users:
- similarity >= minSimilarity
- limit 20

Step 3:
Candidate jobs được recommend nếu:
- similar users applied
- similar users saved
- similar users clicked/viewed nhiều
- current user chưa apply

Step 4:
collaborativeScore:
- similarAppliedCount * 10
- similarSavedCount * 5
- similarClickedCount * 2
- weighted by average similarity
- normalize max 100

Reason code:
- USERS_LIKE_YOU_APPLIED
- USERS_LIKE_YOU_SAVED

==================================================
EXCLUSION RULES
==================================================

Không recommend job nếu:
- job.deleted = true
- job.published = false
- expiredAt < now
- company inactive hoặc unverified nếu project có rule
- candidate đã apply job đó
- job thuộc company bị disable
- recruiter/company bị deleted nếu có rule

==================================================
EVENT INTEGRATION
==================================================

Khi user thực hiện các hành động có sẵn, nên tự track behavior:

1. Get job detail:
Nếu candidate gọi GET /api/v1/job/{id}
→ optional: record view

2. Save favorite:
Khi candidate POST /api/v1/job/{jobId}/favorite
→ behavior source SAVE đã có thể lấy từ FavoriteJob table, không nhất thiết tạo riêng history

3. Apply job:
Khi candidate apply
→ behavior source APPLY lấy từ JobApplication table

4. Search:
Khi gọi search job API
→ optional record search keyword

Không làm hỏng flow cũ.

==================================================
PERFORMANCE
==================================================

MVP có thể tính realtime nếu dữ liệu nhỏ.

Nhưng phải thiết kế dễ nâng cấp:
- Query giới hạn active jobs gần đây hoặc max-candidates
- Có config recommendation.result.max-candidates
- Có thể cache user interest profile sau này
- Có thể batch precompute recommendation logs sau này

Nếu dữ liệu lớn:
- Không load toàn bộ jobs nếu quá nhiều
- Ưu tiên lọc trước:
  - published
  - not deleted
  - not expired
  - same skills/location/title keyword
  - latest jobs

==================================================
SECURITY
==================================================

Các API dưới /api/v1/candidate/**:
- authenticated
- service lấy current user từ SecurityContext
- candidate chỉ xem behavior/recommendation của chính mình

Không cho user truyền userId để xem recommendation của người khác.

==================================================
I18N
==================================================

Cập nhật ErrorMessage.java:

Recommendation:
- ERR_PREFERENCE_NOT_FOUND
- ERR_JOB_NOT_FOUND
- ERR_INVALID_WEIGHT_CONFIG

messages_en.properties:
exception.recommendation.preference.not.found=Candidate job preference not found
exception.recommendation.job.not.found=Job not found with id: {0}
exception.recommendation.invalid.weight.config=Invalid recommendation weight configuration

messages_vn.properties / messages_vi.properties:
exception.recommendation.preference.not.found=Không tìm thấy cấu hình gợi ý việc làm của ứng viên
exception.recommendation.job.not.found=Không tìm thấy việc làm với id: {0}
exception.recommendation.invalid.weight.config=Cấu hình trọng số gợi ý không hợp lệ

==================================================
DOCUMENTATION
==================================================

Tạo docs/hybrid-recommendation-engine-v2.md gồm:

1. Mục tiêu
2. Kiến trúc Hybrid Recommendation
3. Content-based scoring
4. Behavioral scoring
5. Collaborative filtering MVP
6. Database tables
7. API list
8. Example response
9. Cách test
10. CV line

Tạo docs/hybrid-recommendation-test-checklist.md gồm checklist:

1. Candidate tạo preference với Java/Spring.
2. Tạo nhiều jobs:
   - Java Developer
   - React Developer
   - DevOps Engineer
3. Candidate view/click/save nhiều React jobs.
4. Candidate gọi recommended jobs.
5. Kiểm tra React jobs được tăng behaviorScore.
6. Tạo user khác có skill giống candidate.
7. User khác apply Java job.
8. Candidate gọi recommended jobs.
9. Kiểm tra Java job có collaborativeScore.
10. Candidate apply một job.
11. Gọi recommended jobs.
12. Job đã apply không còn xuất hiện.
13. Check reasonCodes trả về đúng.
14. Check pagination đúng.
15. Check security: không xem được recommendation của user khác.

==================================================
POSTMAN
==================================================

Nếu project có folder postman, tạo:

postman/WorkHub_Hybrid_Recommendation_V2.postman_collection.json

Bao gồm request:
- Track job view
- Track job click
- Track search keyword
- Get recommended jobs
- Get behavior summary
- Create candidate preference nếu đã có API
- Save favorite job
- Apply job

==================================================
ACCEPTANCE CRITERIA
==================================================

Feature hoàn thành khi:

1. Project build thành công:
   mvn clean package -DskipTests

2. Có tracking behavior:
   - view
   - click
   - search
   - save lấy từ FavoriteJob
   - apply lấy từ JobApplication

3. Recommended jobs sử dụng hybridScore:
   - contentScore
   - behaviorScore
   - collaborativeScore

4. Có reasonCodes/reasonText để giải thích vì sao job được recommend.

5. Candidate không thấy job đã apply.

6. Chỉ recommend job:
   - published
   - not deleted
   - not expired
   - company active/verified nếu project có rule

7. Behavioral score thay đổi khi user view/click/save/apply nhiều job cùng loại.

8. Collaborative score hoạt động khi có users tương tự.

9. API vẫn dùng response wrapper hiện có.

10. Không hardcode message lỗi.

11. Không phá endpoint cũ.

12. Có docs test checklist.

==================================================
OUTPUT YÊU CẦU
==================================================

Trước khi code, hãy trả lời:

1. Danh sách file sẽ tạo.
2. Danh sách file sẽ sửa.
3. Giải thích ngắn cách tính:
   - contentScore
   - behaviorScore
   - collaborativeScore
   - hybridScore
4. API mới sẽ thêm.
5. Database table mới sẽ thêm.

Sau đó mới implement.

Sau khi implement, hãy trả về summary:

- Files created
- Files modified
- APIs added
- Tables added
- Scoring formula
- How to test
- Maven build result
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