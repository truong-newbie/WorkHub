Bạn là Senior Java Backend Engineer.

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

Package root hiện tại:

org.example.workhub

API base path:

/api/v1

Dự án hiện đã có các module chính:
- Auth
- User
- Company
- Skill
- Job
- Resume
- Subscriber
- ATS Resume Screening
- Assessment
- Personalized Job Feed / Recommendation

Hiện tại hệ thống đang dùng JPA Specification để search/filter job, skill, resume, subscriber. Tôi muốn nâng cấp riêng phần tìm kiếm Job sang Elasticsearch hoặc OpenSearch để hỗ trợ:

- full-text search
- fuzzy search
- typo tolerance
- ranking
- autocomplete
- scalable querying
- indexing job data

Ví dụ user search:
"jav develper"

Hệ thống vẫn phải tìm được:
"Java Developer"

==================================================
YÊU CẦU BẮT BUỘC TRƯỚC KHI CODE
==================================================

Hãy đọc kỹ AI_Context.md trước khi implement.

Tuân thủ tuyệt đối architecture hiện tại:
- Không đổi package root
- Không đổi response format
- Không hardcode message lỗi
- Không phá API cũ
- Không refactor lan man
- Không thay thế toàn bộ JPA Specification cũ nếu chưa cần
- Chỉ bổ sung Elasticsearch/OpenSearch như search engine cho Job module
- MySQL vẫn là source of truth
- Elasticsearch/OpenSearch chỉ dùng làm read/search index

Response format bắt buộc:
return VsResponseUtil.success(data);

Pagination vẫn dùng:
PaginationResponseDto<T>

Exception phải dùng:
ErrorMessage + i18n properties

==================================================
MỤC TIÊU FEATURE
==================================================

Implement Elasticsearch/OpenSearch-powered Job Search.

Mục tiêu:
1. Khi tạo/sửa/xóa/publish/unpublish Job trong MySQL, hệ thống đồng bộ dữ liệu sang Elasticsearch/OpenSearch.
2. Người dùng có thể search job bằng keyword gần đúng.
3. Hỗ trợ typo tolerance:
   - "jav" → Java
   - "develper" → Developer
   - "sprng boot" → Spring Boot
4. Hỗ trợ autocomplete/suggestion.
5. Hỗ trợ ranking kết quả theo độ liên quan.
6. Vẫn hỗ trợ filter theo:
   - skill
   - location
   - level
   - company
   - salary range
   - work mode nếu entity hiện có
   - employment type nếu entity hiện có
   - published
   - expired
7. Chỉ trả job:
   - published = true
   - deleted = false
   - expiredAt > now
   - company active/verified nếu project đang có rule này

==================================================
TECH STACK
==================================================

Ưu tiên dùng một trong hai hướng:

Option A:
- Elasticsearch 8.x
- Spring Data Elasticsearch

Option B:
- OpenSearch
- opensearch-java client

Nếu project dễ tích hợp Spring Data Elasticsearch hơn thì dùng Option A.

Cấu hình bằng application.yml:

spring:
  elasticsearch:
    uris: http://localhost:9200

hoặc custom config nếu cần.

Docker Compose thêm service:

elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.x.x
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
  ports:
    - "9200:9200"

Nếu dùng OpenSearch thì thêm service opensearch tương ứng.

==================================================
INDEX DESIGN
==================================================

Tạo index:

workhub_jobs

Document class:

JobSearchDocument

Fields đề xuất:

- id: Long
- title: String
- slug: String
- description: String
- requirement: String
- benefit: String
- location: String
- companyId: Long
- companyName: String
- companyLogo: String
- recruiterId: String
- salaryMin: BigDecimal
- salaryMax: BigDecimal
- negotiableSalary: Boolean
- experienceYears: Integer
- level: String
- workMode: String nếu có
- employmentType: String nếu có
- skillIds: List<Long>
- skillNames: List<String>
- published: Boolean
- deleted: Boolean
- expiredAt: LocalDateTime
- createdDate: LocalDateTime
- lastModifiedDate: LocalDateTime

Mapping yêu cầu:

title:
- text analyzer cho full-text
- keyword subfield
- autocomplete subfield bằng edge_ngram

description, requirement, benefit:
- text analyzer

skillNames:
- text + keyword

location:
- text + keyword

companyName:
- text + keyword

Suggest/autocomplete:
- dùng completion suggester hoặc edge_ngram analyzer

Ranking:
- title có boost cao nhất
- skillNames boost cao
- companyName boost trung bình
- description/requirement boost thấp hơn

Ví dụ scoring:
- title boost 5
- skillNames boost 4
- requirement boost 2
- description boost 1
- companyName boost 1.5

==================================================
PACKAGE STRUCTURE ĐỀ XUẤT
==================================================

Tạo các file:

search/document/JobSearchDocument.java
search/repository/JobSearchRepository.java
search/service/JobSearchService.java
search/service/impl/JobSearchServiceImpl.java
search/mapper/JobSearchMapper.java
search/dto/request/JobSearchRequest.java
search/dto/response/JobSearchResponse.java
search/dto/response/JobSuggestionResponse.java
search/controller/JobSearchController.java
search/config/ElasticsearchConfig.java nếu cần
search/listener/JobIndexEventListener.java nếu dùng event-driven sync

Nếu project muốn giữ convention domain/dto thì có thể đặt DTO vào:

domain/dto/request
domain/dto/response

Nhưng phần document/repository/service search nên tách rõ package search để không lẫn với JPA repository.

==================================================
API DESIGN
==================================================

Base path:

/api/v1/jobs/search

1. GET /api/v1/jobs/search

Query params:
- keyword
- location
- skillIds
- skillNames
- level
- companyId
- salaryMin
- salaryMax
- workMode
- employmentType
- pageNum
- pageSize
- sortBy
- isAscending

Mục tiêu:
Search full-text + filter.

Ví dụ:
GET /api/v1/jobs/search?keyword=jav develper&location=hanoi&pageNum=1&pageSize=10

Response:
PaginationResponseDto<JobSearchResponse>

2. GET /api/v1/jobs/search/autocomplete

Query params:
- keyword
- limit

Ví dụ:
GET /api/v1/jobs/search/autocomplete?keyword=jav&limit=10

Response:
List<JobSuggestionResponse>

3. POST /api/v1/jobs/search/reindex

Access:
ADMIN only

Mục tiêu:
Rebuild toàn bộ index từ MySQL sang Elasticsearch/OpenSearch.

Dùng khi:
- deploy lần đầu
- index bị lỗi
- mapping thay đổi

Response:
{
  "indexedCount": 100
}

==================================================
SEARCH LOGIC
==================================================

JobSearchService cần có:

1. search(JobSearchRequest request)

Logic:
- Build bool query
- must/should cho keyword
- filter cho published/deleted/expired/company/skills/location/salary/level
- apply pagination
- apply sorting
- return PaginationResponseDto<JobSearchResponse>

Keyword search nên dùng:
- multi_match query
- fuzziness AUTO
- prefix query/autocomplete cho short keyword
- minimum_should_match phù hợp

Pseudo logic:

if keyword exists:
  should:
    - multi_match keyword against title, skillNames, companyName, requirement, description
      fuzziness AUTO
      boosts:
        title^5
        skillNames^4
        companyName^1.5
        requirement^2
        description^1
    - match_phrase_prefix on title^4
    - match_phrase_prefix on skillNames^3

filter:
  - published = true
  - deleted = false
  - expiredAt > now
  - company active/verified nếu document có
  - skillIds contains any selected skillIds
  - level equals
  - salary overlap
  - location fuzzy hoặc keyword filter tùy request

Sorting:
- default: _score DESC, createdDate DESC
- nếu sortBy truyền vào thì sort theo field tương ứng
- vẫn ưu tiên relevance khi có keyword

2. autocomplete(String keyword, int limit)

Logic:
- search theo title autocomplete field
- search theo skillNames autocomplete field
- search theo companyName autocomplete field
- remove duplicate
- return suggestions

3. indexJob(Long jobId)

Logic:
- load Job từ MySQL
- map sang JobSearchDocument
- save vào Elasticsearch/OpenSearch

4. deleteJob(Long jobId)

Logic:
- xóa document khỏi index hoặc set deleted = true

5. reindexAll()

Logic:
- delete old index hoặc clear documents
- lấy toàn bộ job từ MySQL
- map sang documents
- bulk index
- return count

==================================================
SYNC STRATEGY
==================================================

MySQL là source of truth.

Elasticsearch/OpenSearch phải sync sau các nghiệp vụ:

- create job
- update job
- delete job
- publish job
- unpublish job
- update company active/verified/name/logo
- update skill name nếu skill được dùng trong job

Khuyến nghị:
Dùng event-driven sync.

Tạo event:
- JobCreatedEvent
- JobUpdatedEvent
- JobDeletedEvent
- JobPublishedEvent
- JobUnpublishedEvent
- CompanyChangedEvent
- SkillChangedEvent

Listener:
JobIndexEventListener

Dùng:
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)

Để đảm bảo:
- chỉ index sau khi transaction MySQL commit thành công
- tránh index dữ liệu rollback

Nếu project hiện tại chưa có event system, có thể gọi JobSearchService.indexJob(jobId) trực tiếp sau save, nhưng ưu tiên event-driven.

==================================================
FALLBACK STRATEGY
==================================================

Nếu Elasticsearch/OpenSearch down:
- Không được làm hỏng create/update/delete job.
- Ghi log lỗi sync index.
- Search API có thể:
  Option A: throw InternalServerException với i18n key search.engine.unavailable
  Option B: fallback sang JPA Specification search cũ nếu đã có sẵn

Ưu tiên Option B nếu dễ tích hợp.

Cần thêm i18n key:
exception.search.engine.unavailable=Search engine is currently unavailable
exception.search.reindex.failed=Failed to rebuild search index

Vietnamese:
exception.search.engine.unavailable=Hệ thống tìm kiếm hiện không khả dụng
exception.search.reindex.failed=Không thể xây dựng lại chỉ mục tìm kiếm

==================================================
SECURITY
==================================================

GET /api/v1/jobs/search:
- public hoặc authenticated tùy rule hiện tại của project
- Nếu hiện tại GET job yêu cầu authenticated thì giữ authenticated

GET /api/v1/jobs/search/autocomplete:
- public hoặc authenticated tương tự search

POST /api/v1/jobs/search/reindex:
- ADMIN only
- @PreAuthorize("hasRole('ADMIN')")

Không làm yếu security hiện tại.

==================================================
DTO DESIGN
==================================================

JobSearchRequest:

- keyword: String
- location: String
- skillIds: List<Long>
- skillNames: List<String>
- level: String
- companyId: Long
- salaryMin: BigDecimal
- salaryMax: BigDecimal
- workMode: String
- employmentType: String
- pageNum: Integer default 1
- pageSize: Integer default 10
- sortBy: String default createdDate
- isAscending: Boolean default false

JobSearchResponse:

- id
- title
- slug
- location
- companyId
- companyName
- companyLogo
- salaryMin
- salaryMax
- negotiableSalary
- experienceYears
- level
- workMode
- employmentType
- skillNames
- expiredAt
- createdDate
- matchScore nếu lấy được _score
- highlightTitle nếu implement highlight
- highlightDescription nếu implement highlight

JobSuggestionResponse:

- text
- type: JOB_TITLE / SKILL / COMPANY
- jobId nullable
- skillId nullable
- companyId nullable

==================================================
HIGHLIGHTING
==================================================

Nếu dùng Elasticsearch NativeQuery, hãy thêm highlight cho:

- title
- description
- requirement
- skillNames

Response có thể chứa:
- highlights: Map<String, List<String>>

MVP có thể chỉ highlight title/description.

==================================================
INDEX SETTINGS
==================================================

Nếu có thể, tạo custom analyzer:

autocomplete_analyzer:
- tokenizer: standard
- filter:
  - lowercase
  - asciifolding
  - edge_ngram_filter

edge_ngram_filter:
- type: edge_ngram
- min_gram: 2
- max_gram: 20

search_analyzer:
- standard
- lowercase
- asciifolding

Mục tiêu:
- search không phân biệt hoa thường
- support gõ thiếu từ đầu
- support tiếng Việt không dấu nếu có thể

Ví dụ:
"Ha Noi" vẫn match "Hà Nội" nếu asciifolding hoạt động.

==================================================
REINDEX API
==================================================

Implement:

POST /api/v1/jobs/search/reindex

Flow:
1. Check ADMIN.
2. Delete/recreate index nếu cần.
3. Load jobs từ MySQL.
4. Map sang JobSearchDocument.
5. Bulk save.
6. Return count.

Response DTO:

SearchReindexResponse:
- indexedCount
- startedAt
- finishedAt
- durationMs

==================================================
DOCKER COMPOSE
==================================================

Cập nhật docker-compose.yml thêm Elasticsearch hoặc OpenSearch.

Ví dụ Elasticsearch:

elasticsearch:
  image: docker.elastic.co/elasticsearch/elasticsearch:8.13.4
  container_name: workhub-elasticsearch
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false
    - ES_JAVA_OPTS=-Xms512m -Xmx512m
  ports:
    - "9200:9200"
  volumes:
    - elasticsearch-data:/usr/share/elasticsearch/data

volumes:
  elasticsearch-data:

Backend config:
- ELASTICSEARCH_URIS=http://elasticsearch:9200

==================================================
APPLICATION CONFIG
==================================================

Cập nhật application.yml hoặc application.properties:

spring.elasticsearch.uris=${ELASTICSEARCH_URIS:http://localhost:9200}

search:
  job:
    index-name: workhub_jobs
    fuzzy-enabled: true
    autocomplete-limit: 10
    fallback-to-jpa: true

==================================================
TEST CASES
==================================================

Tạo docs/elasticsearch-job-search-checklist.md gồm test cases:

1. Start MySQL + Backend + Elasticsearch.
2. Create job "Java Developer".
3. Publish job.
4. Call reindex API.
5. Search "java developer" trả đúng job.
6. Search "jav develper" vẫn trả "Java Developer".
7. Search "spring boot" match requirement/skills.
8. Search theo skillIds chỉ trả job có skill tương ứng.
9. Search theo location "hanoi" match "Hà Nội" nếu analyzer support.
10. Autocomplete "jav" trả "Java Developer" hoặc "Java".
11. Unpublish job → search không còn thấy job.
12. Soft delete job → search không còn thấy job.
13. Update job title → index update theo.
14. Reindex chỉ ADMIN gọi được.
15. Elasticsearch down thì create/update job không fail.
16. User không có quyền không được gọi reindex.

==================================================
POSTMAN
==================================================

Nếu project có thư mục postman, tạo:

postman/WorkHub_Elasticsearch_Job_Search.postman_collection.json

Bao gồm:
- Search job
- Autocomplete
- Reindex
- Create job
- Publish job
- Update job
- Delete job

==================================================
ACCEPTANCE CRITERIA
==================================================

Feature được coi là hoàn thành khi:

1. Project build thành công:
   mvn clean package -DskipTests

2. Elasticsearch/OpenSearch chạy được qua Docker Compose.

3. Có index workhub_jobs.

4. Job create/update/delete/publish/unpublish sync được sang index.

5. Search "jav develper" trả được job "Java Developer".

6. Autocomplete hoạt động.

7. Ranking ưu tiên title và skill hơn description.

8. Filter theo skill/location/level/salary/company hoạt động.

9. Reindex API hoạt động và chỉ ADMIN gọi được.

10. Không phá Job API cũ.

11. Không thay MySQL bằng Elasticsearch.

12. Có fallback/logging nếu search engine lỗi.

13. Có docs test checklist.

14. Không hardcode error message.

15. Tuân thủ response wrapper, DTO, i18n và security convention hiện có.

==================================================
OUTPUT YÊU CẦU
==================================================

Trước khi code, hãy trả lời:

1. Danh sách file sẽ tạo.
2. Danh sách file sẽ sửa.
3. Design ngắn gọn về cách sync MySQL → Elasticsearch.
4. API mới sẽ thêm.
5. Các dependency Maven cần thêm.

Sau đó mới implement.

Sau khi implement, hãy trả về summary:

- Files created
- Files modified
- APIs added
- Index name
- Search examples
- How to test
- Lệnh chạy Docker Compose
- Lệnh build Maven
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