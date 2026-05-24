Bạn là Senior Java Backend Engineer.

Tôi đang xây dựng dự án WorkHub - hệ thống tuyển dụng giống ITviec bằng Spring Boot 3, Java 17, MySQL, JWT, Spring Security, JPA/Hibernate, DTO/Mapper, i18n, GlobalExceptionHandler, Base response wrapper.

Dự án hiện đã có các module: User, Auth, Company, Skill, Job, Resume, Subscriber, ATS Screening, Assessment, Personalized Job Feed. Package root là:

org.example.workhub

API base path:

/api/v1

Hãy đọc kỹ AI_Context.md trước khi code để tuân thủ architecture hiện tại, bao gồm response format VsResponseUtil, exception pattern, ErrorMessage i18n keys, DTO pattern, MapStruct, security rule và package structure hiện có.

==================================================
MỤC TIÊU FEATURE
==================================================

Implement Real-time Notification System cho WorkHub.

Feature này dùng để gửi thông báo realtime cho user khi có các sự kiện quan trọng trong hệ thống tuyển dụng.

Các event cần hỗ trợ MVP:

1. Candidate apply job thành công
   - Gửi notification cho recruiter/owner của job.
   - Nội dung: Candidate A đã ứng tuyển vào Job B.

2. Recruiter/Admin cập nhật trạng thái application
   - Gửi notification cho candidate.
   - Nội dung: Đơn ứng tuyển Job B đã được chuyển sang APPROVED/REJECTED/REVIEWING.

3. Recruiter assign assessment test cho candidate
   - Gửi notification cho candidate.
   - Nội dung: Bạn đã được giao bài test cho Job B.

4. Candidate submit assessment
   - Gửi notification cho recruiter.
   - Nội dung: Candidate A đã hoàn thành bài test cho Job B.

5. Admin approve/reject company
   - Gửi notification cho company owner/recruiter.
   - Nội dung: Công ty của bạn đã được APPROVED/REJECTED.

6. ATS screening completed
   - Gửi notification cho recruiter.
   - Nội dung: Kết quả ATS screening cho Candidate A đã sẵn sàng.

==================================================
YÊU CẦU KỸ THUẬT
==================================================

Sử dụng:

- Spring WebSocket
- STOMP over WebSocket
- SockJS fallback nếu phù hợp
- Spring Security JWT cho WebSocket handshake
- MySQL lưu notification history
- REST API để lấy danh sách notification
- Realtime push đến user đang online

Không dùng hardcode message lỗi.
Không phá API/module hiện có.
Không refactor lan man.
Không đổi package root.
Không đổi response format hiện tại.
Không bỏ qua i18n.
Không tạo architecture mới nếu project đã có pattern sẵn.

==================================================
DATABASE DESIGN
==================================================

Tạo entity Notification:

Table: tbl_notifications

Fields đề xuất:

- id: Long, auto increment
- recipient: User, ManyToOne, nullable false
- sender: User, ManyToOne, nullable true
- type: NotificationType enum
- title: String
- content: String, columnDefinition TEXT
- targetType: NotificationTargetType enum
- targetId: String
- read: Boolean, default false
- createdAt: LocalDateTime
- readAt: LocalDateTime nullable

Enum NotificationType:

- JOB_APPLICATION_CREATED
- JOB_APPLICATION_STATUS_UPDATED
- ASSESSMENT_ASSIGNED
- ASSESSMENT_SUBMITTED
- COMPANY_APPROVED
- COMPANY_REJECTED
- ATS_SCREENING_COMPLETED
- SYSTEM

Enum NotificationTargetType:

- JOB
- APPLICATION
- ASSESSMENT
- COMPANY
- SCREENING_RESULT
- USER
- NONE

Yêu cầu:
- Notification phải soft/history friendly, không xóa cứng.
- Có index cho recipient_id, read, created_at.
- Mỗi notification thuộc về một recipient cụ thể.

==================================================
PACKAGE STRUCTURE ĐỀ XUẤT
==================================================

Tạo các file theo structure hiện tại:

domain/entity/Notification.java
constant/NotificationType.java
constant/NotificationTargetType.java

domain/dto/request/NotificationSearchRequest.java
domain/dto/response/NotificationResponse.java
domain/dto/response/NotificationCountResponse.java

domain/mapper/NotificationMapper.java
domain/specification/NotificationSpecification.java

repository/NotificationRepository.java

service/NotificationService.java
service/impl/NotificationServiceImpl.java

controller/NotificationController.java

config/WebSocketConfig.java
security/websocket/JwtHandshakeInterceptor.java hoặc class tương ứng nếu cần
security/websocket/WebSocketAuthChannelInterceptor.java nếu cần

==================================================
WEBSOCKET DESIGN
==================================================

Endpoint WebSocket:

/ws

STOMP destination:

Client subscribe:
- /user/queue/notifications

Server send:
- convertAndSendToUser(userId hoặc username/email, "/queue/notifications", notificationResponse)

Client send không bắt buộc trong MVP.

Yêu cầu:
- Chỉ authenticated user mới connect được.
- Lấy JWT từ:
  1. Authorization header: Bearer token
  2. hoặc query param: ?token=xxx nếu browser/SockJS khó gửi header
- Validate JWT bằng JwtTokenProvider hiện có.
- Set Principal đúng user id hoặc username để convertAndSendToUser hoạt động.

==================================================
REST API DESIGN
==================================================

Base path:

/api/v1/notifications

Endpoints:

1. GET /api/v1/notifications
   - Lấy notification của current user
   - Query params:
     - keyword
     - type
     - read
     - fromDate
     - toDate
     - pageNum
     - pageSize
     - sortBy
     - isAscending
   - Access: authenticated

2. GET /api/v1/notifications/unread-count
   - Trả số lượng notification chưa đọc của current user
   - Access: authenticated

3. PUT /api/v1/notifications/{id}/read
   - Mark một notification là read
   - Chỉ owner notification mới được mark
   - Access: authenticated

4. PUT /api/v1/notifications/read-all
   - Mark toàn bộ notification của current user là read
   - Access: authenticated

5. DELETE /api/v1/notifications/{id}
   - Optional: soft delete/hide nếu muốn
   - Nếu chưa có deleted field thì có thể bỏ endpoint này ở MVP

Response format bắt buộc dùng:
- VsResponseUtil.success(...)
- PaginationResponseDto<T> nếu phân trang

==================================================
SERVICE METHODS
==================================================

NotificationService cần có:

- NotificationResponse createAndSend(
    User recipient,
    User sender,
    NotificationType type,
    String title,
    String content,
    NotificationTargetType targetType,
    String targetId
  )

- PaginationResponseDto<NotificationResponse> getMyNotifications(NotificationSearchRequest request)

- NotificationCountResponse getUnreadCount()

- NotificationResponse markAsRead(Long id)

- void markAllAsRead()

Yêu cầu:
- createAndSend phải:
  1. lưu DB
  2. map sang response DTO
  3. push realtime qua SimpMessagingTemplate
  4. return response

==================================================
EVENT-DRIVEN DESIGN
==================================================

Ưu tiên implement theo event listener để không làm bẩn service chính.

Tạo event classes nếu phù hợp:

- JobApplicationCreatedEvent
- JobApplicationStatusUpdatedEvent
- AssessmentAssignedEvent
- AssessmentSubmittedEvent
- CompanyModeratedEvent
- AtsScreeningCompletedEvent

Tạo listener:

listener/NotificationEventListener.java

Khi các service hiện có hoàn thành nghiệp vụ, publish event bằng ApplicationEventPublisher.

Ví dụ:
- Sau khi candidate apply job thành công → publish JobApplicationCreatedEvent
- Sau khi update application status → publish JobApplicationStatusUpdatedEvent
- Sau khi assign test → publish AssessmentAssignedEvent
- Sau khi submit test → publish AssessmentSubmittedEvent
- Sau khi approve/reject company → publish CompanyModeratedEvent
- Sau khi ATS screening completed → publish AtsScreeningCompletedEvent

Yêu cầu:
- Không làm thay đổi business logic cũ.
- Chỉ thêm event publish sau khi transaction thành công.
- Nếu cần, dùng @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)

==================================================
SECURITY RULES
==================================================

Cập nhật WebSecurityConfig:

- /ws/** yêu cầu authenticated hoặc cho phép handshake nhưng validate token trong interceptor.
- /api/v1/notifications/** yêu cầu authenticated.

Controller:
- @PreAuthorize("isAuthenticated()")

Owner check:
- User chỉ được đọc/update notification của chính mình.
- Nếu notification không tồn tại hoặc không thuộc current user thì throw NotFoundException hoặc ForbiddenException theo convention hiện có.

==================================================
DTO DESIGN
==================================================

NotificationResponse gồm:

- id
- type
- title
- content
- targetType
- targetId
- read
- createdAt
- readAt
- senderId
- senderName
- recipientId

NotificationCountResponse:

- unreadCount: long

NotificationSearchRequest:

- keyword
- type
- read
- fromDate
- toDate
- pageNum
- pageSize
- sortBy
- isAscending

Validation message phải dùng i18n key nếu có validation.

==================================================
I18N MESSAGES
==================================================

Cập nhật ErrorMessage.java thêm nhóm Notification:

- ERR_NOT_FOUND_ID
- ERR_FORBIDDEN_ACCESS

Cập nhật messages_en.properties, messages_vn.properties, messages_vi.properties nếu project đang có:

English:
exception.notification.not.found.id=Notification not found with id: {0}
exception.notification.forbidden=You do not have permission to access this notification

Vietnamese:
exception.notification.not.found.id=Không tìm thấy thông báo với id: {0}
exception.notification.forbidden=Bạn không có quyền truy cập thông báo này

==================================================
NOTIFICATION CONTENT
==================================================

MVP có thể generate content ở backend.

Ví dụ:

JOB_APPLICATION_CREATED:
Title: New job application
Content: {candidateName} has applied for {jobTitle}

JOB_APPLICATION_STATUS_UPDATED:
Title: Application status updated
Content: Your application for {jobTitle} has been updated to {status}

ASSESSMENT_ASSIGNED:
Title: Assessment assigned
Content: You have been assigned an assessment for {jobTitle}

ASSESSMENT_SUBMITTED:
Title: Assessment submitted
Content: {candidateName} has submitted the assessment for {jobTitle}

COMPANY_APPROVED:
Title: Company approved
Content: Your company {companyName} has been approved

COMPANY_REJECTED:
Title: Company rejected
Content: Your company {companyName} has been rejected

ATS_SCREENING_COMPLETED:
Title: ATS screening completed
Content: Screening result for {candidateName} is ready

==================================================
FRONTEND INTEGRATION GUIDE
==================================================

Sau khi code backend, tạo thêm file docs/notification-websocket-guide.md hướng dẫn frontend connect.

Nội dung cần có:

1. WebSocket URL:
ws://localhost:8080/ws?token=<access_token>

2. Subscribe:
destination: /user/queue/notifications

3. REST APIs:
- GET /api/v1/notifications
- GET /api/v1/notifications/unread-count
- PUT /api/v1/notifications/{id}/read
- PUT /api/v1/notifications/read-all

4. Example JS code dùng @stomp/stompjs hoặc SockJS.

==================================================
POSTMAN / TESTING
==================================================

Tạo docs/notification-test-checklist.md gồm checklist test:

1. Login candidate/recruiter/admin.
2. Recruiter mở WebSocket subscribe notification.
3. Candidate apply job.
4. Recruiter nhận notification realtime.
5. Recruiter gọi GET /api/v1/notifications thấy notification đã lưu DB.
6. Recruiter mark read.
7. unread-count giảm.
8. Candidate subscribe WebSocket.
9. Recruiter update application status.
10. Candidate nhận notification realtime.
11. User A không thể mark notification của User B.

Nếu project có postman folder, tạo collection:
postman/WorkHub_Notification_APIs.postman_collection.json

==================================================
ACCEPTANCE CRITERIA
==================================================

Feature hoàn thành khi:

1. Project build thành công bằng:
   mvn clean package -DskipTests

2. Có entity Notification và migration/schema tương ứng.

3. REST APIs hoạt động đúng response wrapper.

4. WebSocket connect được bằng JWT.

5. User chỉ nhận notification của chính mình.

6. Notification được lưu DB trước khi push realtime.

7. Mark read và unread-count hoạt động đúng.

8. Các business events chính đã publish notification:
   - apply job
   - update application status
   - assign assessment
   - submit assessment
   - approve/reject company
   - ATS screening completed

9. Không hardcode error messages.

10. Không phá security config cũ.

11. Không đổi URL existing API.

12. Có documentation test checklist.

==================================================
OUTPUT YÊU CẦU
==================================================

Hãy implement đầy đủ theo từng bước:

1. Phân tích code hiện tại liên quan:
   - security
   - user principal
   - job application service
   - assessment service
   - company service
   - ATS screening service

2. Lập danh sách file sẽ tạo/sửa trước khi code.

3. Implement backend.

4. Cập nhật i18n.

5. Cập nhật security config.

6. Thêm docs hướng dẫn test.

7. Chạy build hoặc nêu rõ lệnh cần chạy.

8. Trả về summary:
   - Files created
   - Files modified
   - APIs added
   - WebSocket endpoint
   - How to test
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