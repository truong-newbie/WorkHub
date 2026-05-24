Bạn là Senior Software Engineer và Technical Architect.

Tôi muốn bạn đọc toàn bộ code của một chức năng/module trong dự án backend của tôi và tạo ra một tài liệu học tập chuyên sâu để tôi có thể:

* hiểu bản chất chức năng
* hiểu business flow
* hiểu code flow
* hiểu database design
* hiểu security flow
* hiểu API flow
* hiểu vì sao code được viết như vậy
* debug được
* tự implement lại được từ đầu
* đọc lại sau này là nhớ lại toàn bộ chức năng

==================================================
MỤC TIÊU DOCUMENT
=================

Document KHÔNG được chỉ mô tả code.

Document phải giúp tôi:

1. Hiểu business thật sự
2. Hiểu kiến trúc
3. Hiểu flow dữ liệu
4. Hiểu security
5. Hiểu transaction
6. Hiểu database design
7. Hiểu vì sao chọn giải pháp đó
8. Hiểu các edge case
9. Hiểu các lỗi thường gặp
10. Có thể tự code lại chức năng mà không cần nhìn source

==================================================
YÊU CẦU KHI PHÂN TÍCH CODE
==========================

Hãy đọc và phân tích:

* controller
* service
* service impl
* repository
* entity
* dto
* mapper
* specification
* security
* util
* config
* exception handling
* filter
* interceptor
* validator
* transaction flow

KHÔNG chỉ đọc từng file riêng lẻ.

Hãy phân tích flow end-to-end của chức năng.

==================================================
FORMAT DOCUMENT BẮT BUỘC
========================

# 1. Tổng quan chức năng

* Chức năng dùng để làm gì
* Actor nào sử dụng
* Business problem giải quyết là gì
* Chức năng này xuất hiện ở đâu trong flow hệ thống

Ví dụ:

* Candidate apply job
* Recruiter screen CV
* Assessment
* Resume upload

==================================================

# 2. Business Flow thực tế

Mô tả flow thực tế như trong doanh nghiệp.

Ví dụ:

Candidate upload CV
↓
System parse CV
↓
AI extract skills
↓
Recruiter review
↓
Shortlist candidate

Phải mô tả:

* actor
* trigger
* rule
* expected result

==================================================

# 3. API Design

Phân tích:

* endpoint
* method
* request body
* response
* status code
* authentication
* authorization

Ví dụ:

POST /api/v1/candidate/jobs/{jobId}/apply

Giải thích:

* vì sao dùng POST
* vì sao path như vậy
* vì sao dùng JWT
* vì sao role candidate mới được gọi

==================================================

# 4. Database Design

Phân tích:

* entity
* relationship
* foreign key
* indexing
* normalization
* audit fields
* soft delete
* enum

Bắt buộc giải thích:

* tại sao thiết kế như vậy
* tại sao OneToMany
* tại sao ManyToOne
* nếu scale lớn thì vấn đề gì

Phải có ERD dạng text.

Ví dụ:

User 1-N Resume
Job 1-N JobApplication

==================================================

# 5. Entity Analysis

Với từng entity:

* ý nghĩa business
* từng field dùng để làm gì
* field nào quan trọng
* nullable hay không
* lifecycle của entity

Ví dụ:

JobApplication.status

* PENDING
* REVIEWING
* REJECTED
* HIRED

Giải thích business meaning của từng trạng thái.

==================================================

# 6. Request Flow (Deep Dive)

Phân tích chi tiết 1 request chạy qua hệ thống như thế nào:

Client
↓
Controller
↓
DTO Validation
↓
Security Filter
↓
JWT Parsing
↓
Service
↓
Transaction
↓
Repository
↓
Hibernate
↓
Database

Giải thích:

* từng layer làm gì
* data thay đổi ra sao
* object nào được tạo
* object nào được map

==================================================

# 7. Security Analysis

Phân tích:

* JWT flow
* Authentication
* Authorization
* @PreAuthorize
* hasRole vs hasAuthority
* SecurityContext
* Filter chain

Giải thích:

* vì sao dùng role đó
* lỗi 401 và 403 khác nhau thế nào
* edge case security

==================================================

# 8. Validation & Exception

Phân tích:

* @Valid
* custom validation
* exception flow
* global exception handler

Cho ví dụ:

* invalid request
* duplicate data
* forbidden action
* unauthorized

==================================================

# 9. Transaction & Consistency

Phân tích:

* @Transactional
* rollback
* race condition
* concurrent update
* inventory issue
* duplicate apply

Nếu chức năng chưa xử lý tốt thì chỉ ra luôn.

==================================================

# 10. Hibernate/JPA Deep Dive

Phân tích:

* lazy loading
* eager loading
* n+1 problem
* generated SQL
* cascade
* orphan removal

Phải giải thích:

* Hibernate thực sự làm gì phía dưới

==================================================

# 11. Performance Analysis

Phân tích:

* bottleneck
* query issue
* scalability problem
* indexing
* caching possibility

==================================================

# 12. Edge Cases

Liệt kê tất cả edge cases.

Ví dụ:

* candidate apply duplicate
* recruiter deleted
* resume missing
* token expired
* job unpublished

==================================================

# 13. Common Bugs & Debugging Guide

Liệt kê:

* lỗi thường gặp
* nguyên nhân
* cách debug
* cách fix

Ví dụ:

* 401 Unauthorized
* 403 Forbidden
* LazyInitializationException
* Multipart error
* Enum mapping error

==================================================

# 14. System Design Perspective

Phân tích ở level kiến trúc:

* tại sao dùng microservice
* tại sao tách AI worker
* tại sao dùng queue
* tại sao dùng S3
* tại sao dùng Redis
* tại sao dùng DTO

==================================================

# 15. Re-implementation Guide

Sau khi đọc xong document này,
tôi phải có thể tự code lại chức năng.

Hãy hướng dẫn:

1. cần tạo entity nào
2. cần tạo API nào
3. service flow ra sao
4. security thế nào
5. database thế nào
6. transaction thế nào

==================================================

# 16. Mental Model / Core Insight

Đây là phần QUAN TRỌNG NHẤT.

Tóm tắt:

* bản chất thật sự của chức năng
* tư duy thiết kế
* vấn đề business cốt lõi
* tại sao hệ thống hoạt động được

Phải giúp tôi:

* nhớ rất lâu
* hiểu rất sâu
* tự thiết kế được chức năng tương tự

==================================================
YÊU CẦU QUAN TRỌNG
==================

* Không được mô tả hời hợt
* Không chỉ giải thích syntax
* Không chỉ liệt kê code
* Phải giải thích “WHY”
* Phải giải thích “FLOW”
* Phải giải thích “BUSINESS”
* Phải giải thích “SYSTEM DESIGN”

Document phải giống tài liệu internal engineering của công ty lớn.

Nếu cần, hãy đọc nhiều file liên quan trước khi viết document.
