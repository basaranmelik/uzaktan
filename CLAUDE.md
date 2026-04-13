# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start the database (required before running the app)
docker-compose up -d

# Run the application
./mvnw spring-boot:run

# Build
./mvnw clean package

# Run tests
./mvnw test
```

## Architecture

**Uzaktan** is a Spring Boot 4.0.5 online learning platform (Java 21, Maven, SQL Server via Azure SQL Edge on port 1433).

### Layer Status

| Layer | Status |
|-------|--------|
| `model/` | Implemented — JPA entities with Lombok (User, Course, Enrollment, Certificate, Assignment, AssignmentSubmission, CourseVideo, VideoWatch, Address) |
| `repository/` | Implemented — Spring Data JPA interfaces for all entities |
| `controller/` | Implemented — HomeController, AuthController, CourseController, EnrollmentController, AssignmentController, CertificateController, ProfileController, VideoController, FileDownloadController, AdminController, TeacherController |
| `service/` | Implemented — interfaces + impl/ for UserService, CourseService, EnrollmentService, AssignmentService, CertificateService, CourseVideoService, FileStorageService |
| `dto/` | Implemented — request/ and response/ DTOs for all entities |
| `exception/` | Implemented — GlobalExceptionHandler, ResourceNotFoundException, DuplicateEnrollmentException, DuplicateSubmissionException, CourseFullException, UnauthorizedActionException |
| `mapper/` | Implemented — UserMapper, CourseMapper, EnrollmentMapper, AssignmentMapper, CertificateMapper |
| `templates/` | Implemented — Thymeleaf templates (home, auth, course, admin, hocam, odev, video, profile, error pages, fragments) |

### Domain Model

Core entities and their relationships:
- `User` — has roles (`ADMIN`, `TEACHER`, `USER`), embedded `Address`, one-to-many `Certificate`, `Enrollment`, `AssignmentSubmission`
- `Course` — has `CourseCategory` enum (9 categories), `CourseStatus` enum, ManyToOne `instructor` (User), one-to-many `Enrollment`, `Assignment`, `CourseVideo`, `Certificate`
- `Enrollment` — join entity between `User` and `Course`, tracks `EnrollmentStatus` and `progressPercentage`
- `Certificate` — references both `User` and `Course`, unique `certificateCode`
- `Assignment` — belongs to `Course`, has one-to-many `AssignmentSubmission`
- `AssignmentSubmission` — join entity between `Assignment` and `User`, tracks `SubmissionStatus`, file upload support
- `CourseVideo` — belongs to `Course`, ordered by `orderIndex`, file-based video storage
- `VideoWatch` — tracks which `User` watched which `CourseVideo`

### Security

`SecurityConfig` uses **database-backed authentication** via `CustomUserDetailsService` with BCrypt(12) password encoding.

- Roles: `ADMIN`, `TEACHER`, `USER`
- Login via email (`/giris`), role-based redirect after login (admin → `/admin`, teacher → `/hocam/panel`, user → `/panom`)
- Open redirect protection on login success handler
- Session fixation protection + max 1 concurrent session
- `@PreAuthorize` on `AdminController` and `TeacherController`

Public paths: `/`, `/home`, `/egitimler`, `/kayit-ol`, `/giris`, `/hakkimizda`, `/iletisim`, `/sertifika/dogrula/**`, `/css/**`, `/js/**`, `/images/**`, `/error/**`

### Tech Stack

- **View layer**: Thymeleaf + Spring Security Thymeleaf dialect
- **Frontend**: Bootstrap 5 (static assets in `src/main/resources/static/`)
- **Mail**: Spring Mail configured (smtp.gmail.com) but credentials not set
- **Database**: SQL Server JDBC; credentials in `application.properties` (`sa` / `verYs3cret`)
- **File Storage**: Local file storage (`~/guzem-uploads`), max upload 500MB
- **Data Init**: `DataInitializer` seeds default admin/teacher/student accounts on startup
