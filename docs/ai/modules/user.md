# User Module

## Purpose

Handles user profile, admin user management, role changes, account lock/unlock, avatar upload, and statistics.

## Main Files

- `controller/UserController.java`
- `service/UserService.java`
- `service/impl/UserServiceImpl.java`
- `domain.entity.User.java`
- `repository/UserRepository.java`
- `domain.mapper.UserMapper.java`

## Entity Notes

`User.id` is a `String` UUID. User has role, company, profile fields, OAuth provider fields, soft delete, and enabled flags.

## Security

- Current profile APIs require authentication.
- Admin management requires admin role.
- Service layer validates user existence and deleted status.

## AI Notes

Do not assume numeric user IDs. Always treat user IDs as strings.
