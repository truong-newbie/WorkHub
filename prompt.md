==================================================
Bạn là Senior Java Backend Engineer.

Tôi đang xây dựng hệ thống tuyển dụng giống ITviec bằng:

* Spring Boot 3
* Java 17
* MySQL
* JWT Authentication
* JPA + Specification
* DTO + Mapper architecture

TRƯỚC KHI CODE:

* Hãy đọc file AI_Context.md để hiểu toàn bộ kiến trúc dự án, coding convention, business flow, naming convention và các module hiện tại.
* Không được bỏ qua file AI_Context.md
* Nếu thiếu thông tin trong AI_Context.md thì hỏi lại tôi trước khi implement.

==================================================
PROJECT HIỆN TẠI
================

Project đã có sẵn:

* Base response util
* Exception util
* JWT authentication
* OAuth2 login
* Constant package
* Dynamic search/filter Specification
* Pagination
* DTO + Mapper architecture
* Swagger/OpenAPI
* i18n
* Global exception handling

Architecture hiện tại:

* controller
* service
* service.impl
* repository
* entity
* dto.request
* dto.response
* mapper
* specification
* constant
* exception
* util
* base

Tech stack:

* Spring Boot 3
* Java 17
* Spring Security
* JWT
* MySQL
* JPA
* Lombok
* MapStruct hoặc manual mapper
* JpaSpecificationExecutor

==================================================
YÊU CẦU QUAN TRỌNG
==================

* KHÔNG viết code kiểu demo/tutorial
* KHÔNG tạo architecture mới
* KHÔNG đổi naming convention
* KHÔNG generate pseudo code
* KHÔNG dùng TODO
* KHÔNG bỏ trống method
* Mọi code phải compile được
* Reuse toàn bộ util/base/exceptions/specification hiện tại
* Mọi API phải RESTful
* Mọi message phải dùng i18n
* Không hardcode message
* Ưu tiên production-ready code
* Tuân thủ SOLID
* Tối ưu query tránh N+1
* Transaction đúng chỗ
* Validation đầy đủ
* Security đầy đủ

==================================================
ENTITY JOB
==========

Hãy thiết kế module JOB đầy đủ cho hệ thống tuyển dụng giống ITviec.

Nếu cần bổ sung entity thì hãy giải thích trước khi code.

JOB nên hỗ trợ:

* recruiter tạo job
* recruiter update job
* recruiter delete job
* recruiter publish/unpublish job
* candidate xem danh sách job
* candidate xem detail job
* candidate search/filter job
* candidate apply job
* candidate save/favorite job
* recruiter xem applicants
* recruiter approve/reject application
* admin moderation job

==================================================
JOB ENTITY GỢI Ý
================

Job entity nên có các field production-ready như:

```java
private String id;

private String title;

private String slug;

private String description;

private String requirement;

private String benefit;

private String location;

private String level;

private String employmentType;

private String salaryMin;

private String salaryMax;

private Boolean negotiableSalary;

private Integer quantity;

private Integer experienceYears;

private LocalDate expiredAt;

private Boolean published;

private Boolean deleted;

private Company company;

private User recruiter;

private List<Skill> skills;

private List<JobApplication> applications;
```

==================================================
YÊU CẦU MODULE JOB
==================

1. JOB CRUD

* create job
* update job
* delete job (soft delete)
* get detail job
* get all jobs

2. SEARCH + FILTER + PAGINATION
   Filter theo:

* title
* company
* salary
* location
* level
* employment type
* skills
* experience years
* published
* created date

Support:

* pagination
* sorting
* dynamic specification hiện có

Ví dụ:
title:*java*
salaryMin>1000
location:*hanoi*
(level:JUNIOR|level:MIDDLE)

3. JOB APPLICATION

* apply job
* withdraw application
* recruiter view applicants
* recruiter approve/reject application

4. FAVORITE JOB

* save favorite job
* remove favorite job
* get favorite jobs

5. SECURITY

* recruiter chỉ quản lý job của chính mình
* candidate chỉ apply bằng account của mình
* admin có full quyền moderation
* JWT authorization

6. VALIDATION

* validate expired date
* validate salary
* validate required fields
* validate recruiter role
* validate company ownership

7. DTO
   Tách:

* request dto
* response dto

8. RESPONSE FORMAT
   Bắt buộc dùng:

* VsResponseUtil.success
* VsResponseUtil.error

9. EXCEPTION HANDLING
   Reuse exception util hiện tại.

10. MAPPER
    Generate mapper đầy đủ.

11. REPOSITORY
    Sử dụng:

* JpaRepository
* JpaSpecificationExecutor

12. SWAGGER
    Thêm OpenAPI annotations đầy đủ.

==================================================
I18N
====

BẮT BUỘC:

* Mọi message lỗi phải đưa vào:

  * messages_en.properties
  * messages_vi.properties

Ví dụ:

messages_en.properties

```properties
job.not.found=Job not found
job.create.success=Create job successfully
job.update.success=Update job successfully
job.delete.success=Delete job successfully
job.expired.invalid=Expired date must be greater than current date
job.salary.invalid=Invalid salary range
job.permission.denied=You do not have permission
application.already.applied=You already applied this job
```

messages_vi.properties

```properties
job.not.found=Không tìm thấy công việc
job.create.success=Tạo công việc thành công
job.update.success=Cập nhật công việc thành công
job.delete.success=Xóa công việc thành công
job.expired.invalid=Ngày hết hạn phải lớn hơn ngày hiện tại
job.salary.invalid=Mức lương không hợp lệ
job.permission.denied=Bạn không có quyền truy cập
application.already.applied=Bạn đã ứng tuyển công việc này
```

==================================================
OUTPUT FORMAT
=============

Trả lời theo đúng thứ tự:

1. Feature plan
2. Database design
3. Entity relationship
4. API list
5. URL design
6. Request DTO
7. Response DTO
8. Repository
9. Mapper
10. Service
11. ServiceImpl
12. Controller
13. Security config adjustment
14. Validation strategy
15. Specification search integration
16. Exception handling
17. i18n messages
18. Sample request body
19. Sample response body
20. Postman examples
21. Folder structure
22. Performance optimization
23. Future scalable suggestions

==================================================
IMPORTANT
=========

* Không generate code thiếu import
* Không dùng field không tồn tại
* Không tự giả định architecture mới
* Reuse class hiện tại
* Mọi endpoint phải đúng RESTful convention
* Mọi business logic phải hợp lý như production system
* Nếu cần thêm entity/table mới thì explain rõ trước khi code
* Nếu thiếu thông tin thì hỏi tôi trước khi implement

AI_CONTEXT.md USAGE RULE
========================

TRƯỚC KHI CODE:

* Hãy đọc file AI_Context.md để hiểu:

  * kiến trúc dự án
  * coding convention
  * business flow
  * package structure
  * naming convention
  * các module đã tồn tại
  * các util/base/specification hiện có

Mục đích của AI_Context.md:

* chỉ dùng làm CONTEXT tham khảo trước khi implement
* giúp hiểu dự án tốt hơn
* tránh generate sai architecture
* tránh tạo code trùng lặp
* tránh đổi naming convention

==================================================
QUAN TRỌNG
==========

* NHIỆM VỤ CHÍNH là HOÀN THIỆN FEATURE/DỰ ÁN theo yêu cầu
* KHÔNG tập trung vào việc chỉnh sửa AI_Context.md trong lúc code
* KHÔNG dừng feature để đi rewrite documentation
* AI_Context.md KHÔNG phải output chính
* Output chính là:

  * code hoàn chỉnh
  * API hoàn chỉnh
  * business logic hoàn chỉnh
  * security hoàn chỉnh
  * validation hoàn chỉnh
  * production-ready implementation

==================================================
SAU KHI HOÀN THIỆN FEATURE
==========================

CHỈ SAU KHI:

* code xong
* API xong
* service xong
* controller xong
* security xong
* validation xong
* i18n xong
* request/response xong
* module hoàn chỉnh

THÌ MỚI:

* cập nhật lại AI_Context.md

==================================================
MỤC TIÊU CỦA VIỆC UPDATE AI_CONTEXT.md
======================================

Để:

* lưu lại những gì đã implement
* giúp AI ở conversation sau hiểu dự án
* giúp developer mới đọc hiểu hệ thống
* giúp maintain feature lâu dài

AI_Context.md đóng vai trò:

* technical memory
* project knowledge base
* architecture summary

==================================================
KHI UPDATE AI_CONTEXT.md
========================

Chỉ update:

* feature mới đã làm
* entity mới
* API mới
* business flow mới
* database change
* security change
* endpoint mới
* request/response mới
* validation mới

KHÔNG rewrite toàn bộ file nếu không cần.

==================================================
PRIORITY ORDER
==============

Thứ tự ưu tiên:

1. Hoàn thiện feature/module
2. Hoàn thiện API/business logic
3. Hoàn thiện validation/security
4. Hoàn thiện request/response
5. Test flow
6. Sau cùng mới update AI_Context.md


==================================================
IMPORTANT
=========

* Không được dành phần lớn output cho AI_Context.md
* Không được xem AI_Context.md là nhiệm vụ chính
* AI_Context.md chỉ là bước cuối cùng để lưu project context
* Ưu tiên tuyệt đối cho implementation thực tế
