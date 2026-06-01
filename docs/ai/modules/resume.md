# Resume Module

## Purpose

Handles candidate resume upload, update, file replacement, soft delete, default resume, public resume visibility, recruiter access after application, and resume parsing queue trigger.

## Main Files

- `controller/ResumeController.java`
- `service/ResumeService.java`
- `service/impl/ResumeServiceImpl.java`
- `domain.entity.Resume.java`
- `repository/ResumeRepository.java`
- `domain.mapper.ResumeMapper.java`

## Key APIs

- `POST /api/v1/resume`
- `PUT /api/v1/resume/{id}`
- `PUT /api/v1/resume/{id}/file`
- `DELETE /api/v1/resume/{id}`
- `GET /api/v1/resume/{id}`
- `GET /api/v1/resume/me`
- `GET /api/v1/resume/admin`
- `PUT /api/v1/resume/{id}/default`
- `GET /api/v1/job/{jobId}/candidates/{candidateId}/resume`

## Entity Notes

Resume includes file metadata, public/default flags, soft delete, summary, ATS score, parsed content, uploaded time, owner user, optional job, and skills.

## Queue Integration

After upload or file replacement, `ResumeServiceImpl` publishes `ResumeParsingJobMessage`. The consumer loads the resume and skips if `parsedContent` already exists.

Resume documents are uploaded to Cloudinary with `resource_type=raw` so ATS
workers can download PDF, DOC, and DOCX files from their persisted URL. Existing
documents uploaded under another Cloudinary resource type may need to be
uploaded again.

## AI Notes

Do not break upload response timing. Resume parsing should remain asynchronous.
