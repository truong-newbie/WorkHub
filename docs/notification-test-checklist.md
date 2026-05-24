# Notification Test Checklist

1. Login as candidate, recruiter, and admin.
2. Open WebSocket as recruiter and subscribe to `/user/queue/notifications`.
3. Candidate applies for a job.
4. Recruiter receives a realtime notification.
5. Recruiter calls `GET /api/v1/notifications` and sees the saved notification.
6. Recruiter marks it read with `PUT /api/v1/notifications/{id}/read`.
7. `GET /api/v1/notifications/unread-count` decreases.
8. Open WebSocket as candidate and subscribe to `/user/queue/notifications`.
9. Recruiter updates application status.
10. Candidate receives a realtime notification.
11. Recruiter assigns an assessment test to candidate.
12. Candidate receives an assessment assigned notification.
13. Candidate submits assessment.
14. Recruiter receives an assessment submitted notification.
15. Admin approves or rejects a company.
16. Company owner/recruiters receive company moderation notification.
17. User A cannot mark User B's notification as read.
