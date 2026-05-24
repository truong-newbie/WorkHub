# AI Context - WorkHub Project

> Technical documentation cho dự án WorkHub - Hệ thống tuyển dụng giống ITviec
> **Maintainer**: Cập nhật sau mỗi feature lớn

---

## 📌 PROJECT OVERVIEW

- **Project**: WorkHub - Recruitment System (ITviec-like)
- **Tech Stack**: Spring Boot 3, Java 17, MySQL, JWT Authentication, JPA
- **Package Structure**: `org.example.workhub`
- **Build Tool**: Maven
- **API Base Path**: `/api/v1`

---

## 📁 PROJECT STRUCTURE

### Package Organization

```
src/main/java/org/example/workhub/
├── base/                          # Base classes
│   ├── RestApiV1.java             # @RestController + /api/v1
│   ├── RestData.java              # Response wrapper {status, data}
│   ├── VsResponseUtil.java        # success/error helpers
│   └── RestStatus.java            # SUCCESS/ERROR enum
│
├── constant/                      # Constants
│   ├── RoleConstant.java          # ROLE_ADMIN, ROLE_RECRUITER, ROLE_CANDIDATE
│   ├── ErrorMessage.java          # Error message keys (i18n)
│   ├── UrlConstant.java          # API URL constants
│   ├── GenderEnum.java            # MALE, FEMALE, OTHER
│   ├── LevelEnum.java             # INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR
│   ├── StatusEnum.java            # PENDING, REVIEWING, APPROVED, REJECTED
│   ├── SearchOperation.java      # EQUALITY, GREATER_THAN, CONTAINS, etc.
│   ├── SortByDataConstant.java    # Sorting field mappings
│   └── CommonConstant.java        # Common values
│
├── controller/                    # REST Controllers
│   ├── UserController.java       # User management APIs
│   ├── AuthController.java       # Authentication APIs
│   ├── CompanyController.java    # Company management APIs
│   ├── SkillController.java      # Skill management APIs
│   └── ForgotPasswordController.java
│
├── domain/
│   ├── dto/
│   │   ├── request/              # Request DTOs (Create, Update, Filter)
│   │   ├── response/             # Response DTOs
│   │   └── pagination/           # Pagination DTOs
│   ├── entity/                   # JPA Entities
│   │   ├── User.java
│   │   ├── Company.java
│   │   ├── Job.java
│   │   ├── Resume.java
│   │   ├── Skill.java
│   │   ├── Subscriber.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── ForgotPassword.java
│   │   ├── TokenBlacklist.java
│   │   └── UserSession.java
│   ├── mapper/                   # MapStruct
│   └── specification/            # JPA Specifications
│
├── exception/                     # Exception handling
│   ├── VsException.java          # Base exception
│   ├── NotFoundException.java    # 404
│   ├── BadRequestException.java # 400
│   ├── ConflictException.java    # 409
│   ├── ForbiddenException.java   # 403
│   ├── UnauthorizedException.java # 401
│   ├── InvalidException.java    # 400
│   ├── InternalServerException.java # 500
│   ├── UploadFileException.java  # 502
│   └── GlobalExceptionHandler.java
│
├── repository/                    # JPA Repositories
├── security/                      # Security
│   ├── WebSecurityConfig.java   # Security config
│   ├── UserPrincipal.java       # Principal
│   ├── CustomUserDetailsService.java
│   └── jwt/
│       ├── JwtTokenProvider.java
│       └── JwtAuthenticationFilter.java
│
├── service/                       # Service interfaces
│   └── impl/                      # Service implementations
└── util/                          # Utilities
    └── PaginationUtil.java
```

### Resources

```
src/main/resources/
├── i18n/
│   ├── messages_en.properties     # English messages
│   └── messages_vn.properties    # Vietnamese messages
└── application.yml
```

---

## 📊 DATABASE STRUCTURE

### Entities & Relationships

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│    User     │──────│    Role     │◄─────│ Permission  │
│  (users)    │      │ (tbl_roles) │      │(tbl_perms)  │
└─────────────┘      └─────────────┘      └─────────────┘
       │                    │
       │                    │
       ▼                    │
┌─────────────┐            │
│   Company   │◄───────────┘
│(tbl_companies)│
└─────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│    Job      │──────│    Skill     │◄─────│  Subscriber  │
│  (tbl_jobs) │──────│ (tbl_skills) │      │(tbl_subs)   │
└─────────────┘      └─────────────┘      └─────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐
│   Resume    │
│(tbl_resumes)│
└─────────────┘
```

### Entity Details

#### User (users table)
```java
@Entity @Table(name = "users")
public class User extends DateAuditing {
    @Id @UuidGenerator private String id;
    private String username;           // name field in DB
    private String email;
    private String password;
    private Integer age;
    private GenderEnum gender;
    private LocalDate dob;
    private String address;
    private String refreshToken;       // JWT refresh token
    private Boolean enabled = true;    // Account status
    private Boolean deleted = false;  // Soft delete
    private String avatar;
    private String phone;
    private String headline;
    private String bio;
    private Integer experienceYears;
    private String website;
    private String linkedinUrl;
    private String githubUrl;
    private String location;
    private String provider;          // GOOGLE / FACEBOOK
    private String providerId;

    @ManyToOne Company company;
    @ManyToOne Role role;
    @OneToMany List<Resume> resumes;
    @OneToOne ForgotPassword forgotPassword;
}
```

#### Company (tbl_companies)
```java
@Entity @Table(name = "tbl_companies")
public class Company extends FlagUserDateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; // Long!
    private String name;
    private String description;
    private String address;
    private String logo;
    private Boolean deleted = false;

    @OneToMany(mappedBy = "company") List<User> users;
    @OneToMany(mappedBy = "company") List<Job> jobs;
}
```

#### Job (tbl_jobs)
```java
@Entity @Table(name = "tbl_jobs")
public class Job extends FlagUserDateAuditing {
    @Id @GeneratedValue private Long id;
    private String name;
    private String location;
    private double salary;
    private int quantity;
    private LevelEnum level;
    private String description;       // TEXT
    private Instant startDate;
    private Instant endDate;

    @ManyToOne Company company;
    @ManyToMany List<Skill> skills;
    @OneToMany List<Resume> resumes;
}

// ENHANCED Job Entity (v1.1)
@Entity @Table(name = "tbl_jobs")
public class Job extends FlagUserDateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String title;              // Job title
    private String slug;               // URL-friendly slug (unique)
    @Column(columnDefinition = "TEXT")
    private String description;        // Full description
    @Column(columnDefinition = "TEXT")
    private String requirement;        // Job requirements
    @Column(columnDefinition = "TEXT")
    private String benefit;            // Job benefits
    private BigDecimal salaryMin;      // Min salary
    private BigDecimal salaryMax;      // Max salary
    private Boolean negotiableSalary;  // Salary negotiable flag
    private Integer experienceYears;    // Required experience
    private LocalDateTime expiredAt;   // Expiration date
    private Boolean published;          // Publication status
    private Boolean deleted;           // Soft delete flag

    @ManyToOne User recruiter;         // Job creator
    @ManyToOne Company company;       // Company posting
    @ManyToMany Set<Skill> skills;     // Required skills
    @OneToMany List<JobApplication> applications;
    @OneToMany List<FavoriteJob> favoriteJobs;
}
```

#### JobApplication (tbl_job_applications)
```java
@Entity @Table(name = "tbl_job_applications")
public class JobApplication extends DateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private StatusEnum status;         // PENDING, REVIEWING, APPROVED, REJECTED
    @Column(columnDefinition = "TEXT")
    private String coverLetter;        // Application letter
    private LocalDateTime appliedAt;    // Application date
    private LocalDateTime reviewedAt;  // Review date
    private String reviewNote;          // Reviewer notes

    @ManyToOne User user;              // Applicant
    @ManyToOne Job job;                // Applied job
    @ManyToOne User reviewedBy;       // Reviewer (recruiter/admin)
}
```

#### FavoriteJob (tbl_favorite_jobs)
```java
@Entity @Table(name = "tbl_favorite_jobs")
public class FavoriteJob extends DateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne User user;              // User who saved
    @ManyToOne Job job;                // Saved job
}

// ENHANCED Job Entity (v1.1)
@Entity @Table(name = "tbl_jobs")
public class Job extends FlagUserDateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String title;              // Job title
    private String slug;               // URL-friendly slug (unique)
    @Column(columnDefinition = "TEXT")
    private String description;        // Full description
    @Column(columnDefinition = "TEXT")
    private String requirement;        // Job requirements
    @Column(columnDefinition = "TEXT")
    private String benefit;            // Job benefits
    private BigDecimal salaryMin;      // Min salary
    private BigDecimal salaryMax;      // Max salary
    private Boolean negotiableSalary;  // Salary negotiable flag
    private Integer experienceYears;    // Required experience
    private LocalDateTime expiredAt;   // Expiration date
    private Boolean published;          // Publication status
    private Boolean deleted;           // Soft delete flag

    @ManyToOne User recruiter;         // Job creator
    @ManyToOne Company company;       // Company posting
    @ManyToMany Set<Skill> skills;     // Required skills
    @OneToMany List<JobApplication> applications;
    @OneToMany List<FavoriteJob> favoriteJobs;
}
```

#### JobApplication (tbl_job_applications)
```java
@Entity @Table(name = "tbl_job_applications")
public class JobApplication extends DateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private StatusEnum status;         // PENDING, REVIEWING, APPROVED, REJECTED
    @Column(columnDefinition = "TEXT")
    private String coverLetter;        // Application letter
    private LocalDateTime appliedAt;    // Application date
    private LocalDateTime reviewedAt;  // Review date
    private String reviewNote;          // Reviewer notes

    @ManyToOne User user;              // Applicant
    @ManyToOne Job job;                // Applied job
    @ManyToOne User reviewedBy;       // Reviewer (recruiter/admin)
}
```

#### FavoriteJob (tbl_favorite_jobs)
```java
@Entity @Table(name = "tbl_favorite_jobs")
public class FavoriteJob extends DateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne User user;              // User who saved
    @ManyToOne Job job;                // Saved job
}
```

#### Resume (tbl_resumes)
```java
@Entity @Table(name = "tbl_resumes")
public class Resume extends UserDateAuditing {
    @Id @GeneratedValue private Long id;
    private String email;
    private String url;              // CV URL
    private StatusEnum status;       // PENDING, REVIEWING, APPROVED, REJECTED

    @ManyToOne User user;
    @ManyToOne Job job;
}
```

#### Skill (tbl_skills)
```java
@Entity @Table(name = "tbl_skills")
public class Skill extends UserDateAuditing {
    @Id @GeneratedValue private Long id;
    private String name;
    private String slug;
    private String description;
    private String level;
    private Boolean active = true;
    private Boolean deleted = false;

    @ManyToMany(mappedBy = "jobs") List<Job> jobs;
    @ManyToMany(mappedBy = "skills") List<Resume> resumes;
    @ManyToMany(mappedBy = "subscribers") List<Subscriber> subscribers;
}
```

#### Subscriber (table_subscribers)
```java
@Entity @Table(name = "table_subscribers")
public class Subscriber extends UserDateAuditing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    private String email;
    private Boolean enabled = true;
    private Boolean deleted = false;
    private LocalDateTime subscribedAt;
    private LocalDateTime lastEmailSentAt;

    @ManyToOne User user;
    @ManyToMany List<Skill> skills;
}
```

#### Role (tbl_roles)
```java
@Entity @Table(name = "tbl_roles")
public class Role {
    @Id @GeneratedValue private Long id;
    private String name;              // ROLE_ADMIN, ROLE_RECRUITER, ROLE_CANDIDATE
    private String description;

    @ManyToMany List<Permission> permissions;
    @OneToMany(mappedBy = "role") List<User> users;
}
```

### Enums

```java
enum GenderEnum { FEMALE, MALE, OTHER }
enum LevelEnum { INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR }
enum StatusEnum { PENDING, REVIEWING, APPROVED, REJECTED }
enum SearchOperation { EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, CONTAINS, STARTS_WITH, ENDS_WITH }
```

### Role Constants

```java
public class RoleConstant {
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String RECRUITER = "ROLE_RECRUITER";
    public static final String CANDIDATE = "ROLE_CANDIDATE";
}
```

---

## 🔐 SECURITY CONFIGURATION

### WebSecurityConfig

```java
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
    .csrf(csrf -> csrf.disable())
    .sessionManagement(SessionCreationPolicy.STATELESS)
    .authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
        .requestMatchers("/api/v1/auth/**").permitAll()
        // User profile - authenticated
        .requestMatchers("/api/v1/user/me/**").authenticated()
        // Admin only
        .requestMatchers("/api/v1/user/**").hasRole("ADMIN")
        .anyRequest().authenticated()
    )
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
}
```

### JWT Flow

```
1. User login → POST /api/v1/auth/login
2. Server validates credentials
3. JwtTokenProvider generates:
   - Access token (15 min expiry)
   - Refresh token (longer expiry)
4. Client stores tokens
5. Subsequent requests: Header "Authorization: Bearer <access_token>"
6. JwtAuthenticationFilter extracts & validates token
7. SecurityContext set, request proceeds
```

### Role-Based Authorization

```java
@PreAuthorize("hasRole('ADMIN')")                    // Only ADMIN
@PreAuthorize("isAuthenticated()")                   // Any logged user
@PreAuthorize("hasRole('ADMIN') or hasRole('RECRUITER')")  // Multiple
```

### Protected Endpoints

| Endpoint Pattern | Access |
|-----------------|--------|
| `/api/v1/auth/**` | Public |
| `/api/v1/user/me/**` | Authenticated |
| `/api/v1/user/**` | ADMIN only |
| `/api/v1/job/**` GET | Authenticated |
| `/api/v1/job/**` POST/PUT/DELETE | RECRUITER, ADMIN |
| `/api/v1/job/*/apply` POST/DELETE | Authenticated |
| `/api/v1/job/*/applications/**` | RECRUITER, ADMIN |
| `/api/v1/job/*/favorite/**` | Authenticated |
| `/api/v1/companies/**` | Role-based |
| `/api/v1/skills/**` GET | Public active skills |
| `/api/v1/skills/**` POST/PUT/PATCH/DELETE | ADMIN only |
| `/swagger-ui/**` | Public |
| `/v3/api-docs/**` | Public |

### Job Module Security Rules

```java
// WebSecurityConfig.java
.requestMatchers(HttpMethod.GET, "/api/v1/job/**").authenticated()
.requestMatchers(HttpMethod.POST, "/api/v1/job/**").hasAnyRole("RECRUITER", "ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/v1/job/**").hasAnyRole("RECRUITER", "ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/v1/job/**").hasAnyRole("RECRUITER", "ADMIN")
.requestMatchers(HttpMethod.POST, "/api/v1/job/*/apply").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/v1/job/*/apply").authenticated()
.requestMatchers("/api/v1/job/*/applications/**").hasAnyRole("RECRUITER", "ADMIN")
.requestMatchers("/api/v1/applications/**").hasAnyRole("RECRUITER", "ADMIN")
.requestMatchers("/api/v1/job/*/favorite/**").authenticated()
.requestMatchers("/api/v1/jobs/favorites").authenticated()
```

### Controller-Level Authorization

```java
@PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")  // Create/Update/Delete jobs
@PreAuthorize("isAuthenticated()")                         // View jobs, Apply, Favorite
@PreAuthorize("hasRole('ADMIN')")                          // Job statistics
```

---

## 🔑 CODING CONVENTIONS

### 1. Response Format - BẮT BUỘC

```java
// Success responses
return VsResponseUtil.success(data);
return VsResponseUtil.success(HttpStatus.CREATED, data);

// Error - throw exception, GlobalExceptionHandler handles
throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
throw new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id});
```

### 2. Exception Messages - Dùng i18n keys

```java
// Always use ErrorMessage class
throw new ConflictException(ErrorMessage.User.ERR_ALREADY_EXISTS_USERNAME);

// NEVER hardcode: "Username already exists"
```

### 3. Validation Messages - Dùng {key}

```java
// ✅ GOOD
@NotBlank(message = "{invalid.general.required}")
@Email(message = "{invalid.email}")
@Size(min = 3, max = 50, message = "{invalid.general}")
@Pattern(regexp = "^$|^(https?://)...", message = "{invalid.general.format}")

// ❌ BAD
@NotBlank(message = "Username is required")
```

### 4. GlobalExceptionHandler - Resolve message keys

```java
@ExceptionHandler(BindException.class)
public ResponseEntity<RestData<?>> handleValidException(BindException ex) {
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String defaultMessage = error.getDefaultMessage();
        String errorMessage;
        // Check if message is a key {key}
        if (defaultMessage != null && defaultMessage.startsWith("{") && defaultMessage.endsWith("}")) {
            errorMessage = messageSource.getMessage(defaultMessage, null, LocaleContextHolder.getLocale());
        } else {
            errorMessage = defaultMessage;
        }
        result.put(fieldName, errorMessage);
    });
}
```

### 5. DTO Pattern

```java
// Request DTO
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Create user request")
public class UserCreateRequest {
    @NotBlank(message = "{invalid.general.required}")
    @Schema(description = "Username", example = "johndoe")
    private String username;
}

// Response DTO
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String username;
}
```

### 6. Pagination Pattern

```java
// Always use PaginationResponseDto<T>
PagingMeta pagingMeta = new PagingMeta(
    page.getTotalElements(),
    page.getTotalPages(),
    pageNum + 1,              // 1-based
    pageSize,
    sortField,
    sortType                  // "ASC" or "DESC"
);
return new PaginationResponseDto<>(pagingMeta, items);
```

### 7. Mapper Pattern (MapStruct)

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    // Ignore fields managed by Spring/JPA
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    User toUser(UserCreateRequest request);

    // Map nested fields
    @Mapping(target = "roleName", source = "user.role.name")
    UserResponse toUserResponse(User user);
}
```

### 8. Specification Pattern

```java
public class UserSpecification {
    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(
                cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase() + "%")
            );
        };
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null) return cb.conjunction();
            Join<User, Role> roleJoin = root.join("role");
            return cb.equal(roleJoin.get("name"), roleName);
        };
    }
}
```

---

## 📝 i18n MESSAGES

### messages_en.properties

```properties
# Exceptions (for throw)
exception.general=Something went wrong
exception.unauthorized=Not authenticated
exception.forbidden=No permission
exception.user.not.found.id=User not found with id: {0}
exception.user.not.found.username=User not found with username: {0}
exception.user.already.exists.username=Username already exists
exception.auth.already.exists.email=Email already exists
exception.role.not.found=Role not found
exception.company.not.found=Company not found

# Validation (for @NotBlank, @Email, etc.)
invalid.general=This field is invalid
invalid.general.format=Invalid format
invalid.general.required=This field is required
invalid.general.not-blank=This field cannot be blank
invalid.password-format=Password must contain min 8 chars, uppercase, lowercase, digit
invalid.email=Email is invalid
invalid.repeat.password=Passwords do not match
invalid.date-format=Invalid date format
```

### messages_vn.properties

```properties
exception.user.already.exists.username=Tên đăng nhập đã tồn tại
exception.auth.already.exists.email=Email này đã tồn tại
exception.role.not.found=Không tìm thấy vai trò
exception.company.not.found=Không tìm thấy công ty

invalid.general.required=Trường này là bắt buộc
invalid.email=Email không hợp lệ
invalid.general.format=Định dạng không hợp lệ
```

---

## 📋 MODULE DEPENDENCIES

```
User
 ├── Role (required)
 └── Company (optional, for RECRUITER)

Job (Enhanced v1.1)
 ├── Company (required)
 ├── Skill (optional, many-to-many)
 ├── User (recruiter - required)
 ├── JobApplication (1:N)
 └── FavoriteJob (1:N)

JobApplication (NEW)
 ├── User (required - applicant)
 ├── Job (required)
 └── User (optional - reviewedBy)

FavoriteJob (NEW)
 ├── User (required)
 └── Job (required)

Resume
 ├── User (required)
 └── Job (required)

Subscriber
 ├── User (required - owner)
 └── Skill (required, many-to-many)

Skill
 └── (standalone)
```

---

## 📦 API ENDPOINTS

### User Module (13 endpoints)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/user` | Create user | ADMIN |
| GET | `/api/v1/user` | Get all users | ADMIN |
| GET | `/api/v1/user/{id}` | Get user by ID | ADMIN/Self |
| PUT | `/api/v1/user/{id}` | Update user | ADMIN/Self |
| DELETE | `/api/v1/user/{id}` | Soft delete | ADMIN |
| GET | `/api/v1/user/me/profile` | Get current profile | USER |
| PUT | `/api/v1/user/me/profile` | Update profile | USER |
| PUT | `/api/v1/user/me/password` | Change password | USER |
| PUT | `/api/v1/user/me/avatar` | Upload avatar | USER |
| PUT | `/api/v1/user/{id}/lock` | Lock account | ADMIN |
| PUT | `/api/v1/user/{id}/unlock` | Unlock account | ADMIN |
| PUT | `/api/v1/user/{id}/role` | Change role | ADMIN |
| GET | `/api/v1/user/statistics` | Get statistics | ADMIN |

### Company Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/companies` | Search companies | Public |
| GET | `/api/v1/companies/{id}` | Get company by ID | Public active/verified or owner/admin |
| POST | `/api/v1/companies` | Create company | ADMIN/RECRUITER |
| PUT | `/api/v1/companies/{id}` | Update company | ADMIN/Owner |
| DELETE | `/api/v1/companies/{id}` | Soft delete company | ADMIN/Owner |
| GET | `/api/v1/companies/me` | Get current recruiter company | RECRUITER/ADMIN |
| PUT | `/api/v1/companies/me` | Update current recruiter company | RECRUITER/ADMIN |
| POST | `/api/v1/companies/{id}/logo` | Upload company logo | ADMIN/Owner |
| POST | `/api/v1/companies/{id}/cover` | Upload company cover image | ADMIN/Owner |
| PATCH | `/api/v1/companies/{id}/enable` | Enable company | ADMIN |
| PATCH | `/api/v1/companies/{id}/disable` | Disable company | ADMIN |
| PATCH | `/api/v1/companies/{id}/approve` | Approve company | ADMIN |
| PATCH | `/api/v1/companies/{id}/reject` | Reject company | ADMIN |
| GET | `/api/v1/companies/{id}/jobs` | Get company jobs | Public active/verified or owner/admin |
| GET | `/api/v1/companies/{id}/statistics` | Get company statistics | ADMIN/Owner |

### Skill Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/skills` | Search active skills with filters/pagination | Public |
| GET | `/api/v1/skills/search` | Alias for skill search | Public |
| GET | `/api/v1/skills/{id}` | Get active skill by ID | Public |
| POST | `/api/v1/skills` | Create skill | ADMIN |
| PUT | `/api/v1/skills/{id}` | Update skill | ADMIN |
| DELETE | `/api/v1/skills/{id}` | Soft delete skill if not in use | ADMIN |
| PATCH | `/api/v1/skills/{id}/enable` | Enable skill | ADMIN |
| PATCH | `/api/v1/skills/{id}/disable` | Disable skill | ADMIN |
| GET | `/api/v1/skills/suggestions?keyword=java&limit=10` | Get active skill suggestions | Public |
| GET | `/api/v1/skills/popular?limit=10` | Get popular active skills by published job usage | Public |

### Auth Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/auth/register` | Register | Public |
| POST | `/api/v1/auth/login` | Login | Public |
| POST | `/api/v1/auth/logout` | Logout | Authenticated |

### Job Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/job` | Create job | RECRUITER, ADMIN |
| GET | `/api/v1/job` | Get all jobs (paginated, filtered) | Authenticated |
| GET | `/api/v1/job/{id}` | Get job by ID | Authenticated |
| PUT | `/api/v1/job/{id}` | Update job | RECRUITER, ADMIN |
| DELETE | `/api/v1/job/{id}` | Soft delete job | RECRUITER, ADMIN |
| PUT | `/api/v1/job/{id}/publish` | Publish job | RECRUITER, ADMIN |
| PUT | `/api/v1/job/{id}/unpublish` | Unpublish job | RECRUITER, ADMIN |
| GET | `/api/v1/job/statistics` | Get job statistics | ADMIN |

### Job Application Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/job/{jobId}/apply` | Apply for job | Authenticated |
| DELETE | `/api/v1/job/{jobId}/apply` | Withdraw application | Authenticated |
| GET | `/api/v1/job/{jobId}/applications` | Get job applicants | RECRUITER, ADMIN |
| GET | `/api/v1/applications/me` | Get my applications | Authenticated |
| PUT | `/api/v1/applications/{applicationId}/status` | Update application status | RECRUITER, ADMIN |

### Favorite Job Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/job/{jobId}/favorite` | Save job to favorites | Authenticated |
| DELETE | `/api/v1/job/{jobId}/favorite` | Remove from favorites | Authenticated |
| GET | `/api/v1/jobs/favorites` | Get my favorite jobs | Authenticated |

### Resume Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/resume` | Upload candidate resume (PDF/DOC/DOCX) | CANDIDATE, ADMIN |
| GET | `/api/v1/resume/me` | Get current user's resumes with pagination/filter | Authenticated |
| GET | `/api/v1/resume/{id}` | Get resume detail | Owner, public resume, ADMIN |
| PUT | `/api/v1/resume/{id}` | Update resume metadata, visibility, skills, default flag | Owner, ADMIN |
| PUT | `/api/v1/resume/{id}/file` | Replace resume file | Owner, ADMIN |
| DELETE | `/api/v1/resume/{id}` | Soft delete resume | Owner, ADMIN |
| PUT | `/api/v1/resume/{id}/default` | Set default resume | Owner, ADMIN |
| GET | `/api/v1/resume/{id}/download` | Get resume download metadata | Owner, public resume, ADMIN |
| GET | `/api/v1/resume/admin` | Admin search all resumes | ADMIN |
| GET | `/api/v1/job/{jobId}/candidates/{candidateId}/resume` | Recruiter view candidate resume after application | Recruiter owning job, ADMIN |
| GET | `/api/v1/job/{jobId}/candidates/{candidateId}/resume/download` | Recruiter get candidate resume download metadata | Recruiter owning job, ADMIN |

### Subscriber Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/subscribers` | Candidate subscribes to job emails by skills | Authenticated |
| PUT | `/api/v1/subscribers/{id}` | Update subscriber email/name/skills/enabled flag | Owner, ADMIN |
| DELETE | `/api/v1/subscribers/{id}` | Soft delete subscriber | Owner, ADMIN |
| GET | `/api/v1/subscribers/{id}` | Get subscriber detail | Owner, RECRUITER, ADMIN |
| GET | `/api/v1/subscribers/me` | Get current user's subscriber | Authenticated |
| PUT | `/api/v1/subscribers/{id}/enable` | Enable subscriber mail delivery | Owner, ADMIN |
| PUT | `/api/v1/subscribers/{id}/disable` | Disable subscriber mail delivery | Owner, ADMIN |
| GET | `/api/v1/subscribers` | Search subscribers with filters/pagination | RECRUITER, ADMIN |
| POST | `/api/v1/subscribers/mail/send` | Manually trigger matching job emails | RECRUITER, ADMIN |

### Job Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/job` | Create job | RECRUITER, ADMIN |
| GET | `/api/v1/job` | Get all jobs (paginated, filtered) | Authenticated |
| GET | `/api/v1/job/{id}` | Get job by ID | Authenticated |
| PUT | `/api/v1/job/{id}` | Update job | RECRUITER, ADMIN |
| DELETE | `/api/v1/job/{id}` | Soft delete job | RECRUITER, ADMIN |
| PUT | `/api/v1/job/{id}/publish` | Publish job | RECRUITER, ADMIN |
| PUT | `/api/v1/job/{id}/unpublish` | Unpublish job | RECRUITER, ADMIN |
| GET | `/api/v1/job/statistics` | Get job statistics | ADMIN |

### Job Application Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/job/{jobId}/apply` | Apply for job | Authenticated |
| DELETE | `/api/v1/job/{jobId}/apply` | Withdraw application | Authenticated |
| GET | `/api/v1/job/{jobId}/applications` | Get job applicants | RECRUITER, ADMIN |
| GET | `/api/v1/applications/me` | Get my applications | Authenticated |
| PUT | `/api/v1/applications/{applicationId}/status` | Update application status | RECRUITER, ADMIN |

### Favorite Job Module

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/job/{jobId}/favorite` | Save job to favorites | Authenticated |
| DELETE | `/api/v1/job/{jobId}/favorite` | Remove from favorites | Authenticated |
| GET | `/api/v1/jobs/favorites` | Get my favorite jobs | Authenticated |

---

## 🔍 SEARCH/FILTER SYNTAX

### Query Parameters

```text
keyword=java                    # Search in name, email
role=ADMIN                      # Filter by role
gender=MALE                     # Filter by gender
minAge=20&maxAge=40            # Age range
companyId=1                    # Filter by company
enabled=true                   # Filter by status
page=0&size=10                 # Pagination
sortBy=createdDate&sortDir=DESC # Sorting
```

### Specification Filter Operators

```java
enum SearchOperation {
    EQUALITY (":", "="),        // field:value
    NEGATION ("!"),             // field!:value
    GREATER_THAN (">"),         // field>value
    LESS_THAN ("<"),            // field<value
    CONTAINS ("*"),             // field:*value*
    STARTS_WITH,                // field:value*
    ENDS_WITH                  // field:*value
}
```

### Example Filters

```text
title:*java*
salaryMin>1000
level:MIDDLE
(location:*hanoi*|location:*danang*)
(status:PENDING|status:REVIEWING)
```

### Resume Filters

```text
filter=title:*java*,atsScore>80,isPublic
title=java
atsScoreMin=80&atsScoreMax=100
skillId=1
uploadedFrom=2026-05-01&uploadedTo=2026-05-16
isDefault=true&isPublic=false
page=0&size=10&sortBy=uploadedAt&sortDir=DESC
```

### Subscriber Filters

```text
filter=email:*gmail*,enabled:true
email=gmail
enabled=true
skillId=1
subscribedFrom=2026-05-01&subscribedTo=2026-05-16
page=0&size=10&sortBy=subscribedAt&sortDir=DESC
```

### Skill Filters

```text
keyword=java
name=java
slug=java
level=backend
active=true
deleted=false
createdFrom=2026-05-01T00:00:00
createdTo=2026-05-16T23:59:59
pageNum=1&pageSize=10&sortBy=createdDate&isAscending=false
```

---

## 💼 BUSINESS FLOWS

### Candidate Flow
1. Register → Create account with CANDIDATE role
2. Complete profile (headline, bio, skills)
3. Upload CV (via Resume entity)
4. Search jobs → Apply job (create Resume)
5. Track application status

### Recruiter Flow
1. Register → Create account with RECRUITER role
2. Create Company profile
3. Create Job postings
4. View applicants (Resumes for their jobs)
5. Review → Approve/Reject applications

### Admin Flow
1. Manage all users (CRUD, lock/unlock)
2. Change user roles
3. View statistics
4. Manage skills
5. Moderate content

### Resume Flow
1. Candidate uploads resume through `/api/v1/resume` with a PDF, DOC or DOCX file.
2. System validates empty file, allowed extension/content type, max size, duplicate title and skill IDs.
3. Resume is uploaded via `UploadFileUtil`; metadata is saved in `tbl_resumes`.
4. Candidate can update metadata, replace file, toggle public/private, set one default resume, search own resumes, download metadata, or soft delete.
5. Admin can manage and search all resumes.
6. Recruiter can view/download a candidate resume only when the candidate has a non-deleted application for the recruiter's job.
7. Resume keeps ATS-ready fields: `summary`, `atsScore`, `parsedContent`, `skills`, `uploadedAt`.

### Subscriber Flow
1. Candidate creates one subscriber config through `/api/v1/subscribers` with email and subscribed skill IDs.
2. System validates email format, duplicate non-deleted email, ownership, and non-empty existing skills.
3. Candidate can update, soft delete, view own subscriber, enable or disable email delivery.
4. Admin can fully manage subscribers; recruiter/admin can search subscribers and trigger matching mail delivery.
5. Scheduled job runs with `subscriber.mail.cron` and checks enabled, non-deleted subscribers.
6. Matching jobs must be published, non-deleted, not expired, share at least one subscriber skill, and be newer than `lastEmailSentAt` or `subscribedAt`.
7. After a successful email, `lastEmailSentAt` is updated to avoid duplicate/spam delivery.

### Assessment Flow
1. Recruiter/admin creates an assessment test for an owned job through `/api/v1/recruiter/jobs/{jobId}/tests`.
2. Recruiter manages questions and options, then publishes the test after validation.
3. Published tests can be assigned to eligible non-rejected `JobApplication` records for the same job.
4. Candidate sees assigned tests through `/api/v1/candidate/tests`, starts an assignment, and submits answers within the configured test window and duration.
5. Multiple choice answers are auto-scored on submit; essay answers are stored for recruiter scoring.
6. Recruiter views assignment results and candidate answers, then scores essay answers through `/api/v1/recruiter/answers/{answerId}/score`.
7. Candidate responses never expose option `correct`; recruiter question responses may include it.

### Skill Flow
1. Public/candidate/recruiter can search active, non-deleted skills through `/api/v1/skills`.
2. Admin manages skills through `/api/v1/skills` create/update/delete and enable/disable endpoints.
3. System normalizes skill names, generates unique slugs, validates duplicate active names/slugs, and stores `active` plus `deleted` status.
4. Delete is soft delete and is blocked when the skill is used by Job, Resume or Subscriber relations.
5. Suggestions return active skills by keyword; popular skills rank active skills by published job usage.

### Personalized Job Feed / Recommendation Flow
1. Guest calls `GET /api/v1/jobs/latest` to view latest published, non-deleted, non-expired jobs from active companies.
2. Candidate logs in and frontend calls `GET /api/v1/candidate/onboarding-status`.
3. If the candidate has no preference, frontend collects desired title, skills, location, work mode, employment type, level, experience years and expected salary range.
4. Candidate creates one preference through `POST /api/v1/candidate/job-preference`; duplicate preference creation is blocked.
5. Candidate can view/update own preference through `GET/PUT /api/v1/candidate/job-preference`.
6. Candidate behavior can be tracked through `POST /api/v1/candidate/jobs/{jobId}/view`, `POST /api/v1/candidate/jobs/{jobId}/click` and `POST /api/v1/candidate/jobs/search-track`.
7. Candidate calls `GET /api/v1/candidate/jobs/recommended`; jobs already applied by the candidate, expired jobs, deleted jobs, unpublished jobs and jobs from inactive/unverified companies are excluded.
8. Recommendations are sorted by `hybridScore DESC`, then `createdDate DESC`. The hybrid formula is `contentScore * 0.50 + behaviorScore * 0.30 + collaborativeScore * 0.20`, configurable through `recommendation.*` properties.
9. Content scoring still uses skill 40%, title 20%, location/work mode 15%, experience 15% and salary 10%.
10. Behavior scoring uses recent view/click/favorite/application/search signals to build skill, title keyword, location and company interests.
11. Collaborative scoring compares skill, applied job, saved job and viewed/clicked job overlap with similar candidates, then boosts jobs those similar users engaged with.
12. Candidate can inspect current behavior profile through `GET /api/v1/candidate/recommendation/behavior-summary`.

### Notification Flow
1. Authenticated users connect to STOMP over WebSocket at `/ws` with `Authorization: Bearer <token>` or `?token=<access_token>`.
2. Clients subscribe to `/user/queue/notifications`; server sends with `convertAndSendToUser(userId, "/queue/notifications", payload)`.
3. Notifications are stored in `tbl_notifications` before realtime push.
4. Users manage their history through `/api/v1/notifications`, `/unread-count`, `/{id}/read`, `/read-all`, and soft delete `DELETE /{id}`.
5. Events are published after existing business actions and handled by `NotificationEventListener` with `@TransactionalEventListener(AFTER_COMMIT)`.
6. Current event coverage: job application created, application status updated, assessment assigned, assessment submitted, company approved/rejected. ATS notification listener exists, but this codebase currently has no ATS Java service class to publish it.

### Elasticsearch Job Search Flow
1. MySQL remains the source of truth for Job data; Elasticsearch index `workhub_jobs` is a read/search index.
2. Search APIs live under `/api/v1/jobs/search` and use `PaginationResponseDto<JobSearchResponse>`.
3. Job documents are mapped by `JobSearchMapper` from `Job`, `Company`, `Recruiter`, and `Skill` relations.
4. `JobSearchService` uses Spring Data Elasticsearch `NativeQuery` with fuzzy multi-match, boosted fields, phrase-prefix matching, highlighting, filters, autocomplete, and reindex.
5. Returned jobs must be `published=true`, `deleted=false`, not expired, and belong to active/verified companies.
6. Job create/update/delete/publish/unpublish publishes search events after MySQL changes; `JobIndexEventListener` syncs index after commit.
7. Company and Skill changes publish reindex events for affected jobs.
8. If Elasticsearch fails during Job write sync, the original Job transaction is not failed; the error is logged. Search can fallback to JPA when `search.job.fallback-to-jpa=true`.

---

## ⚠️ IMPORTANT NOTES

### 1. Company ID is Long, NOT String
```java
// ❌ WRONG
Company company = companyRepository.findById(request.getCompanyId())...

// ✅ CORRECT
Long companyId = Long.parseLong(request.getCompanyId());
Company company = companyRepository.findById(companyId)...
```

### 2. Soft Delete Pattern
```java
// Delete
user.setDeleted(true);
user.setEnabled(false);
userRepository.save(user);

// Find (exclude deleted)
userRepository.findById(id)
    .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
```

### 3. Never Hardcode Error Messages
```java
// ❌ BAD
throw new ConflictException("Username already exists");

// ✅ GOOD
throw new ConflictException(ErrorMessage.User.ERR_ALREADY_EXISTS_USERNAME);
```

### 4. Role Name Prefix ROLE_
```java
// Database: ROLE_ADMIN, ROLE_RECRUITER, ROLE_CANDIDATE
// When comparing: hasRole('ADMIN') - Spring adds ROLE_ prefix
```

### 5. Date Auditing (Don't Map Timestamps)
```java
// MapStruct - DON'T map createdDate/lastModifiedDate
// Spring auto-sets via @CreatedDate/@LastModifiedDate
```

### 6. Password Validation Pattern
```java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
         message = "{invalid.password-format}")
```

---

## 🚀 RUN PROJECT

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Database Setup
```sql
CREATE DATABASE workhub_db;
-- Tables auto-created by JPA (hibernate.hbm2ddl.auto)
```

### Maven Commands
```bash
# Run dev
mvn spring-boot:run

# Build
mvn clean package -DskipTests

# Run JAR
java -jar target/workhub-1.0.jar
```

### Swagger URL
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

### Test Auth
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"Admin123"}'

# Use token
curl http://localhost:8080/api/v1/user \
  -H "Authorization: Bearer <token>"
```

---

## 🔧 ENVIRONMENT CONFIG

### application.yml key configs
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/workhub_db
    username: root
    password: password

jwt:
  secret: <jwt-secret-key>
  expiration: 900000      # 15 min
  refresh-expiration: 86400000  # 24 hours

server:
  port: 8080
```

---

## 📈 FUTURE IMPROVEMENTS

1. **Caching** - Redis for session/token caching
2. **Elasticsearch** - Full-text search for jobs
3. **WebSocket** - Real-time notifications
4. **Email Queue** - Async email sending
5. **File Storage** - Cloud storage (AWS S3) for CVs
6. **Recommendation** - ML-based job recommendations
7. **Microservices** - Split into smaller services
8. **Rate Limiting** - API rate limiting
9. **Unit Tests** - Comprehensive test coverage

---

## 📝 CHANGELOG

### v1.1 (2026-05-15)
- **Job Module**: Full CRUD, publish/unpublish, statistics
- **JobApplication Module**: Apply, withdraw, approve/reject, view applicants
- **FavoriteJob Module**: Save, remove, list favorites
- **JobSpecification**: Advanced filtering with JPA Specification
- **Security Config**: Updated for job/application endpoints
- **i18n**: Complete messages for all modules (EN + VN)

### v1.2 (2026-05-16)
- **Resume Module**: Candidate upload/update/delete/detail/list/default/download APIs.
- **Resume Entity**: Extended `tbl_resumes` with title, file metadata, public/default flags, soft delete, summary, ATS score, parsed content, uploadedAt and skills.
- **Resume Search**: Pagination, sorting and filters for title, ATS score, skill, upload date, default/public flags, plus dynamic filter examples like `title:*java*,atsScore>80,isPublic`.
- **Resume Security**: Owner/admin management, public read access, recruiter access only after candidate applied to the recruiter's job.
- **Resume i18n**: Added EN/VN/VI resume message keys.
- **Postman**: Added `postman/WorkHub_Resume_APIs.postman_collection.json` and `postman/WorkHub_Resume_APIs.postman_environment.json`.

### v1.3 (2026-05-16)
- **Subscriber Module**: Candidate subscribe/update/delete/detail/current-user/enable/disable APIs.
- **Subscriber Entity**: Extended `table_subscribers` with `enabled`, `deleted`, `subscribedAt`, `lastEmailSentAt`, owner `User`, and subscribed skills.
- **Subscriber Search**: Pagination, sorting and filters for email, enabled flag, skill and subscribed date, plus dynamic filter examples like `email:*gmail*,enabled:true`.
- **Subscriber Email Matching**: Added scheduled and manual matching email delivery for new published jobs by subscribed skills.
- **Subscriber Security**: Owner/admin management, recruiter/admin search and mail trigger.
- **Subscriber i18n**: Added EN/VN/VI subscriber message keys.
- **API Documentation**: Added `docs/subscriber-api-documentation.md`.

### v1.4 (2026-05-16)
- **Company Module Upgrade**: Added recruiter-owned company management, admin moderation, public active/verified visibility, logo/cover upload, company jobs listing and company statistics.
- **Company Entity**: Extended `tbl_companies` with `slug`, contact/profile fields, `active`, `verified`, `coverImage`, and owner relation `owner_id -> users.id`.
- **Company Search**: Added `CompanySearchRequest` and `CompanySpecification` with filters for keyword/name/city/country/industry/companySize/active/verified/created date plus pagination/sorting.
- **Company Security**: Public can view only active and verified companies; recruiter can manage only owned/company-linked records; admin can manage all and approve/reject/enable/disable.
- **Company Upload**: `POST /api/v1/companies/{id}/logo` and `/cover` accept multipart `file` and use the existing Cloudinary `UploadFileUtil`.
- **Company Jobs/Statistics**: `GET /api/v1/companies/{id}/jobs` supports public vs owner/admin visibility; `GET /api/v1/companies/{id}/statistics` returns job/application counts by company.
- **URL Standard**: Company APIs are standardized to plural `/api/v1/companies/...`; singular `/api/v1/company/...` routes were removed.
- **API Documentation**: Added `docs/company-api-documentation.md`.

### v1.5 (2026-05-16)
- **Skill Module Upgrade**: Standardized Skill APIs to plural `/api/v1/skills/...` and fixed get-by-id to use `GET /api/v1/skills/{id}`.
- **Skill Entity**: Extended `tbl_skills` with `slug`, `description`, `active`, existing soft delete, and reverse relation to Resume skills.
- **Skill Search**: Added `SkillSearchRequest` and enhanced `SkillSpecification` for keyword/name/slug/level/active/deleted/created date filters with pagination/sorting.
- **Skill Security**: Public can read active, non-deleted skills; admin only can create/update/delete/enable/disable.
- **Skill Suggestions**: Added `GET /api/v1/skills/suggestions?keyword=java&limit=10` for active skill autocomplete.
- **Popular Skills**: Added `GET /api/v1/skills/popular?limit=10`, ranking active skills by published job usage.
- **Skill Delete Rule**: Delete is soft delete and is blocked when a skill is used by Job, Resume or Subscriber records.
- **Skill i18n**: Added EN/VN/VI skill management messages.

### v1.6 (2026-05-17)
- **ATS Resume Screening**: Added AI-powered screening flow for recruiter evaluation of candidate resumes against job descriptions.
- **Application Upgrade**: Extended `tbl_job_applications` with `resume_id`; candidate apply now supports `/api/v1/candidate/jobs/{jobId}/apply` body `{ "resumeId": 1 }` and defaults status to `APPLIED`.
- **ScreeningResult Entity**: Added `tbl_screening_results` with application, total/skill/semantic/experience/education scores, matched/missing/extra skills, raw text and AI summary.
- **AI Worker Client**: Added `AiWorkerClient` using `ai.worker.base-url`; local dev defaults to `ai.worker.mock-enabled=true`, Docker can call FastAPI worker at `http://ai-worker:8000`.
- **Score Calculator**: Added `ScoreCalculatorService` using `totalScore = skillScore * 0.6 + semanticScore * 0.4`, or skill score when semantic score is null/zero.
- **ATS APIs**: Added `GET /api/v1/candidate/applications`, `GET /api/v1/recruiter/jobs/{jobId}/applications`, `POST /api/v1/recruiter/applications/{applicationId}/screen`, `GET /api/v1/recruiter/applications/{applicationId}/screening-result`, and `GET /api/v1/recruiter/jobs/{jobId}/screening-results`.
- **ATS Security**: Candidate can apply/view own applications; recruiter/admin can view job applications, run screening and view ranked results with job ownership checks.
- **AI Worker Skeleton**: Added `ai-worker/` FastAPI service with PDF parsing via pdfplumber, dictionary skill extraction, skill score calculation and semantic score placeholder.
- **Docker Compose**: Added `ai-worker` service and backend environment wiring for `AI_WORKER_BASE_URL`.

### v1.7 (2026-05-18)
- **Recruiter Assessment Module**: Added test, question, option, candidate assignment, and candidate answer entities.
- **Assessment APIs**: Added recruiter endpoints under `/api/v1/recruiter/**` for creating, updating, publishing, closing, assigning, reviewing, and scoring tests.
- **Candidate Assessment APIs**: Added candidate endpoints under `/api/v1/candidate/**` for listing assigned tests, viewing test detail, starting, submitting, and viewing results.
- **Assessment Validation**: Enforces recruiter job ownership, candidate assignment ownership, no duplicate assignments/answers, published-only assignment/start, test window, duration, question/option integrity, and essay score bounds.
- **Assessment Scoring**: Multiple choice answers are auto-scored on submit; essay scoring recalculates assignment total score.
- **Assessment Security**: `/api/v1/recruiter/**` requires RECRUITER or ADMIN; `/api/v1/candidate/**` requires authentication plus service-layer ownership checks.
- **Assessment Documentation**: Added `docs/assessment-postman-checklist.md`.

### v1.8 (2026-05-19)
- **Personalized Job Feed**: Added public `GET /api/v1/jobs/latest` for latest published, active jobs.
- **Candidate Job Preference**: Added `CandidateJobPreference` entity with one preference per candidate and many-to-many skills.
- **Candidate Preference APIs**: Added onboarding status, create, get and update endpoints under `/api/v1/candidate/**`.
- **Job Recommendation APIs**: Added candidate recommended jobs endpoint with match score, matched/missing skills and reason codes.
- **Recommendation Scoring**: MVP algorithm scores skills, title, location/work mode, experience and salary, and excludes already-applied jobs.
- **Recommendation Security/i18n**: Latest jobs are public, candidate endpoints require authentication, and preference errors use EN/VN/VI message keys.
- **Recommendation Documentation**: Added `docs/job-recommendation-api-checklist.md` and `postman/WorkHub_Job_Recommendation_APIs.postman_collection.json`.

### v1.9 (2026-05-24)
- **Realtime Notification Module**: Added `Notification` entity, enums, repository, specification, mapper, service and controller.
- **Notification APIs**: Added authenticated `/api/v1/notifications`, `/api/v1/notifications/unread-count`, `/api/v1/notifications/{id}/read`, `/api/v1/notifications/read-all`, and soft delete endpoint.
- **WebSocket**: Added STOMP over WebSocket endpoint `/ws`, SockJS fallback, JWT handshake validation from `Authorization` header or `?token=`, and user queue `/user/queue/notifications`.
- **Notification Events**: Added event-driven notifications for job application created/status updated, assessment assigned/submitted, and company approved/rejected.
- **Security/i18n**: Added notification security rules and EN/VN/VI notification error keys.
- **Docs/Postman**: Added `docs/notification-websocket-guide.md`, `docs/notification-test-checklist.md`, and `postman/WorkHub_Notification_APIs.postman_collection.json`.

### v2.0 (2026-05-24)
- **Elasticsearch Job Search**: Added Spring Data Elasticsearch-backed Job search index `workhub_jobs`.
- **Search APIs**: Added `GET /api/v1/jobs/search`, `GET /api/v1/jobs/search/autocomplete`, and admin-only `POST /api/v1/jobs/search/reindex`.
- **Search Document**: Added `JobSearchDocument` with title, description, requirement, benefit, location, company, salary, level, employment type, skills, published/deleted/expiry and audit fields.
- **Analyzer Settings**: Added edge-ngram autocomplete analyzer with lowercase/asciifolding support in `elasticsearch/job-settings.json`.
- **Sync Events**: Added job/company/skill search events and `JobIndexEventListener` with after-commit indexing.
- **Fallback**: Search service logs Elasticsearch failures and falls back to JPA Specification when configured.
- **Docker/Config**: Added Elasticsearch 8.13.4 service, `spring.elasticsearch.uris`, and `search.job.*` properties.
- **Docs/Postman**: Added `docs/elasticsearch-job-search-checklist.md` and `postman/WorkHub_Elasticsearch_Job_Search.postman_collection.json`.

### v2.1 (2026-05-24)
- **Hybrid Recommendation Engine v2**: Upgraded candidate recommendations to combine content-based, behavioral and collaborative scores.
- **Behavior Tracking**: Added `JobViewHistory`, `JobClickHistory`, `JobSearchHistory` and optional `JobRecommendationLog` tables.
- **Recommendation APIs**: Added candidate endpoints for tracking job view, job click, search keyword and behavior summary; existing `/api/v1/candidate/jobs/recommended` now returns `contentScore`, `behaviorScore`, `collaborativeScore`, `hybridScore`, `reasonCodes` and `reasonText`.
- **Scoring Config**: Added `recommendation.weights.*`, `recommendation.behavior.*`, `recommendation.collaborative.*` and `recommendation.result.*` properties.
- **Collaborative MVP**: Similar users are selected by Jaccard overlap of skills, applied jobs, saved jobs and viewed/clicked jobs.
- **Docs/Postman**: Added `docs/hybrid-recommendation-engine-v2.md`, `docs/hybrid-recommendation-test-checklist.md` and `postman/WorkHub_Hybrid_Recommendation_V2.postman_collection.json`.

### v1.0 (2024-05-15)
- Initial setup with User Module
- CRUD, Profile, Admin APIs
- JWT Authentication
- MapStruct mappers
- Specification-based search/filter
- i18n error messages

---

**Last Updated**: 2026-05-24
**Author**: Claude Code
