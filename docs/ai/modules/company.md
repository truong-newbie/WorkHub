# Company Module

## Purpose

Handles recruiter-owned company management, admin moderation, public company visibility, upload of logo/cover images, company job listings, join requests, and company statistics.

## Main Files

- `controller/CompanyController.java`
- `controller/CompanyJoinRequestController.java`
- `service/CompanyService.java`
- `service/CompanyJoinRequestService.java`
- `service/impl/CompanyServiceImpl.java`
- `service/impl/CompanyJoinRequestServiceImpl.java`
- `domain.entity.Company.java`
- `domain.entity.CompanyJoinRequest.java`
- `repository/CompanyRepository.java`
- `repository/CompanyJoinRequestRepository.java`

## Key APIs

- `GET /api/v1/companies`
- `POST /api/v1/companies`
- `GET /api/v1/companies/{id}`
- `PUT /api/v1/companies/{id}`
- `DELETE /api/v1/companies/{id}`
- `GET /api/v1/companies/me`
- `POST /api/v1/companies/{id}/logo`
- `POST /api/v1/companies/{id}/cover`
- `PUT /api/v1/companies/{id}/approve`
- `PUT /api/v1/companies/{id}/reject`
- `GET /api/v1/companies/{id}/jobs`
- `GET /api/v1/companies/{id}/statistics`

## Business Rules

- Public users should see only active and verified companies.
- Recruiters can manage owned/company-linked records.
- Admin can approve/reject/enable/disable all companies.
- Company images use existing upload utility.

## AI Notes

Company ID is `Long`. Preserve plural `/companies` API convention.
