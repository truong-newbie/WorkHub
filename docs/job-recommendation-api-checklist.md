# Personalized Job Feed / Job Recommendation API

Base URL: `http://localhost:8080/api/v1`

## Flow hoạt động

1. Guest gọi `GET /jobs/latest` để xem các job mới nhất đã publish, chưa bị xóa, chưa hết hạn.
2. Candidate login thành công, frontend gọi `GET /candidate/onboarding-status`.
3. Nếu `hasJobPreference=false`, frontend chuyển candidate sang màn hình nhập nhu cầu tìm việc.
4. Candidate submit `POST /candidate/job-preference`.
5. Sau khi tạo preference, candidate gọi `GET /candidate/jobs/recommended`.
6. Các lần đăng nhập sau, nếu `hasJobPreference=true`, frontend gọi thẳng API recommended jobs.

## 1. Latest Jobs

`GET /jobs/latest?page=0&size=10&sortBy=createdDate&sortDir=DESC`

Access: public.

Response mong đợi:

```json
{
  "status": "SUCCESS",
  "data": {
    "meta": {
      "totalElements": 2,
      "totalPages": 1,
      "pageNum": 1,
      "pageSize": 10,
      "sortBy": "createdDate",
      "sortType": "DESC"
    },
    "items": [
      {
        "jobId": 10,
        "title": "Java Backend Developer",
        "companyName": "ABC Tech",
        "location": "Ha Noi",
        "salaryMin": "800",
        "salaryMax": "1500",
        "experienceYears": 1,
        "employmentType": "FULL_TIME",
        "createdDate": "2026-05-19T10:00:00"
      }
    ]
  }
}
```

## 2. Onboarding Status

`GET /candidate/onboarding-status`

Header: `Authorization: Bearer <candidate_token>`

Response khi chưa có preference:

```json
{
  "status": "SUCCESS",
  "data": {
    "hasJobPreference": false,
    "requiredPreference": true
  }
}
```

Response khi đã có preference:

```json
{
  "status": "SUCCESS",
  "data": {
    "hasJobPreference": true,
    "requiredPreference": false
  }
}
```

## 3. Create Preference

`POST /candidate/job-preference`

Header: `Authorization: Bearer <candidate_token>`

Request body:

```json
{
  "desiredJobTitle": "Java Backend Developer",
  "preferredLocation": "Ha Noi",
  "workMode": "HYBRID",
  "employmentType": "FULL_TIME",
  "candidateLevel": "JUNIOR",
  "experienceYears": 1,
  "expectedSalaryMin": 800,
  "expectedSalaryMax": 1500,
  "skillIds": [1, 2, 3]
}
```

Response mong đợi: `201 Created`

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 1,
    "candidateId": "candidate-user-id",
    "desiredJobTitle": "Java Backend Developer",
    "preferredLocation": "Ha Noi",
    "workMode": "HYBRID",
    "employmentType": "FULL_TIME",
    "candidateLevel": "JUNIOR",
    "experienceYears": 1,
    "expectedSalaryMin": 800,
    "expectedSalaryMax": 1500,
    "skills": [
      {
        "id": 1,
        "name": "Java",
        "slug": "java"
      }
    ]
  }
}
```

## 4. Get My Preference

`GET /candidate/job-preference`

Header: `Authorization: Bearer <candidate_token>`

Response mong đợi: giống response create preference.

## 5. Update Preference

`PUT /candidate/job-preference`

Header: `Authorization: Bearer <candidate_token>`

Request body:

```json
{
  "desiredJobTitle": "Spring Boot Developer",
  "preferredLocation": "Remote",
  "workMode": "REMOTE",
  "employmentType": "FULL_TIME",
  "candidateLevel": "MIDDLE",
  "experienceYears": 3,
  "expectedSalaryMin": 1200,
  "expectedSalaryMax": 2500,
  "skillIds": [1, 2, 4]
}
```

Response mong đợi: preference đã cập nhật.

## 6. Recommended Jobs

`GET /candidate/jobs/recommended?page=0&size=10`

Header: `Authorization: Bearer <candidate_token>`

Response mong đợi:

```json
{
  "status": "SUCCESS",
  "data": {
    "meta": {
      "totalElements": 20,
      "totalPages": 2,
      "pageNum": 1,
      "pageSize": 10,
      "sortBy": "matchScore",
      "sortType": "DESC"
    },
    "items": [
      {
        "jobId": 10,
        "title": "Java Backend Developer",
        "companyName": "ABC Tech",
        "location": "Ha Noi",
        "salaryMin": "800",
        "salaryMax": "1500",
        "experienceYears": 1,
        "employmentType": "FULL_TIME",
        "matchScore": 86.5,
        "matchedSkills": ["Java", "Spring Boot"],
        "missingSkills": ["Docker"],
        "matchReasons": [
          "recommendation.reason.skill.match",
          "recommendation.reason.title.match",
          "recommendation.reason.location.match",
          "recommendation.reason.experience.match",
          "recommendation.reason.salary.match"
        ],
        "createdDate": "2026-05-19T10:00:00"
      }
    ]
  }
}
```

## Checklist test

- Guest gọi `GET /jobs/latest` thành công.
- Candidate chưa có preference gọi onboarding status trả `hasJobPreference=false`.
- Candidate tạo preference thành công.
- Candidate tạo preference lần 2 bị `409 Conflict`.
- Candidate gọi onboarding status sau khi tạo trả `hasJobPreference=true`.
- Candidate get my preference thành công.
- Candidate update preference thành công.
- Candidate gọi recommended jobs thành công.
- Recommended jobs không chứa job candidate đã apply.
- Recommended jobs không chứa job expired, deleted hoặc chưa publish.
- Recommended jobs sort theo `matchScore DESC`.
- `expectedSalaryMax < expectedSalaryMin` bị lỗi.
- `skillIds` không tồn tại hoặc inactive bị lỗi.
- Guest không gọi được `/candidate/jobs/recommended`.
- Candidate chưa có preference gọi recommended jobs bị lỗi required preference.
