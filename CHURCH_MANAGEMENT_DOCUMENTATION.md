# ⛪ Church Management System — Backend Documentation

> **A comprehensive Spring Boot REST API for managing church members, groups, events, donations, attendance, and more.**

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [System Architecture](#2-system-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Features](#4-features)
5. [Folder Structure](#5-folder-structure)
6. [Database Design](#6-database-design)
7. [Entity Descriptions](#7-entity-descriptions)
8. [API Documentation](#8-api-documentation)
9. [Authentication & Authorization](#9-authentication--authorization)
10. [Exception Handling](#10-exception-handling)
11. [Validation](#11-validation)
12. [Logging & Monitoring](#12-logging--monitoring)
13. [Configuration](#13-configuration)
14. [Testing](#14-testing)
15. [Performance Optimization](#15-performance-optimization)
16. [Security Best Practices](#16-security-best-practices)
17. [Deployment Guide](#17-deployment-guide)
18. [CI/CD Pipeline](#18-cicd-pipeline)
19. [Future Improvements](#19-future-improvements)
20. [Conclusion](#20-conclusion)

---

## 1. Project Overview

### Purpose

The **Church Management System** is a full-featured backend REST API built with Spring Boot, designed to digitize and streamline the administrative and community operations of a church organization. It replaces manual, paper-based processes with a centralized, secure, and scalable system.

### Business Problem It Solves

Managing a growing church congregation involves tracking hundreds of members, coordinating group activities, scheduling events, managing finances (tithes and offerings), and maintaining communication — all of which are error-prone and time-consuming when done manually. This system provides a unified platform that:

- Eliminates scattered spreadsheets for member data
- Automates birthday and notification reminders
- Provides transparent financial tracking (donations vs. expenditures)
- Enables role-based access for admins and regular members
- Supports a public-facing gallery and event listing for the church website

### Key Features

- JWT-secured authentication and registration
- Member profile management with photo uploads
- Church groups (ministries) with membership constraints
- Event management with registration tracking
- Attendance tracking (event check-ins and meeting headcounts)
- Donation and expenditure recording with financial analytics
- Gallery management (hero carousel, landing page, public gallery)
- Automated birthday email notifications via scheduled tasks
- Password reset via email with expiring tokens
- File upload with image resizing (Thumbnailator)

### Target Users

| User Type | Description |
|-----------|-------------|
| **Admin** | Church staff/leaders with full CRUD access to all modules |
| **Member (USER)** | Regular church members who can view events, manage their own profile, donate, and join groups |
| **Public** | Unauthenticated visitors who can view events and the public gallery |

---

## 2. System Architecture

### Architecture Style

The application follows a **Layered (N-Tier) Monolithic Architecture** with clear separation of concerns across well-defined layers. This is suitable for a mid-sized church organization and can be evolved into microservices if scale demands.

```
┌──────────────────────────────────────────────────────────┐
│                     CLIENT (Frontend / API Consumer)      │
└────────────────────────────┬─────────────────────────────┘
                             │ HTTP Requests (JWT in Header)
┌────────────────────────────▼─────────────────────────────┐
│               Spring Security Filter Chain                │
│     JwtAuthenticationFilter → AuthenticationProvider     │
└────────────────────────────┬─────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│                  Controller Layer                         │
│   (REST endpoints, request mapping, input validation)     │
└────────────────────────────┬─────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│                   Service Layer                           │
│   (Business logic, transaction management, mapping)       │
└────────────────────────────┬─────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│                 Repository Layer                          │
│      (Spring Data JPA interfaces, custom queries)         │
└────────────────────────────┬─────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│               PostgreSQL Database                         │
└──────────────────────────────────────────────────────────┘
```

### Layer Descriptions

**Controller Layer** — Handles incoming HTTP requests, maps them to service calls, and returns appropriate HTTP responses. Uses `@RestController`, `@PreAuthorize` for method-level security, and DTOs for request/response shaping.

**Service Layer** — Contains all business logic. Each service is responsible for a single domain (Members, Events, Donations, etc.). Transactions are managed here using `@Transactional`.

**Repository Layer** — Spring Data JPA repositories that abstract database access. Custom JPQL queries are written where needed (e.g., birthday lookups by month and day).

**Security Layer** — `JwtAuthenticationFilter` intercepts every request, extracts and validates the JWT, and populates the `SecurityContextHolder`. `SecurityConfig` defines which endpoints are public vs. protected.

**Scheduled Tasks** — `BirthdayNotificationTask` runs daily at 8:00 AM to send email notifications for members with birthdays in 3 days.

### Request Flow

```
1. Client sends HTTP request with Bearer token in Authorization header
2. JwtAuthenticationFilter extracts and validates JWT
3. SecurityContextHolder is populated with the authenticated user
4. Request reaches the appropriate Controller
5. Controller delegates to the Service layer
6. Service executes business logic and calls Repository(ies)
7. Repository queries PostgreSQL via JPA/Hibernate
8. Service maps entity to DTO
9. Controller wraps DTO in ResponseEntity and returns HTTP response
```

---

## 3. Technology Stack

| Technology | Purpose | Version |
|------------|---------|---------|
| **Java** | Primary programming language | 21 |
| **Spring Boot** | Application framework | 3.2.4 |
| **Spring Security** | Authentication & Authorization | (Boot managed) |
| **Spring Data JPA** | Database ORM abstraction | (Boot managed) |
| **Hibernate** | JPA implementation / ORM | (Boot managed) |
| **PostgreSQL** | Relational database | Latest |
| **JJWT** | JSON Web Token generation & validation | 0.11.5 |
| **Lombok** | Boilerplate code reduction | 1.18.34 |
| **Thumbnailator** | Image resizing on upload | 0.4.21 |
| **Apache Commons IO** | File extension utilities | 2.13.0 |
| **Thymeleaf** | HTML email template rendering | (Boot managed) |
| **Spring Mail** | SMTP email sending | (Boot managed) |
| **Spring Actuator** | Health and monitoring endpoints | (Boot managed) |
| **Maven** | Build & dependency management | 3.9.9 |
| **Docker** | Containerization | Latest |
| **BCrypt** | Password hashing | (Spring managed) |

---

## 4. Features

### Member Management
Full CRUD operations for church members. Admin-only create/update/delete. Members can update their own profiles via a dedicated `/profile` endpoint. Profile photo upload with automatic image resizing (max 1080×1080px). Pre-registration linking — if a member was added by admin before they created an account, registration automatically links them via phone number.

### Authentication & Account Management
JWT-based stateless authentication. Registration with optional admin flag. Password reset via a time-limited (1 hour) email token. Profile completion tracking (`profileComplete` flag). Secure password updates with old-password verification.

### Church Groups (Ministries)
Admin can create, update, and delete groups with optional images. Members can join/leave groups. A hard business rule limits each member to a maximum of 2 groups. Group membership counts are returned dynamically.

### Event Management
Admin manages church events (services, programs, outreaches) with start/end times, location, and images. Members can register/unregister for events. Registered member count and IDs are returned with each event.

### Attendance Tracking
Two attendance models:
- **Event Attendance** — Members check in to a specific event. Stored with timestamp and optional notes.
- **Meeting Attendance** — Admin logs aggregate headcounts for any meeting by name and date (for services without individual member tracking).

### Donations & Expenditures
Admin logs donations manually (tithe, offerings, special funds). Members can submit their own donations via `/contribute`. Full financial analytics: total income, total expenditure, net revenue, and year-by-year breakdown. Expenditure categories include Utility, Salary, Maintenance, and Charity.

### Gallery Management
Images can be flagged as: Public Gallery, Hero Carousel (homepage slider), or Show on Landing page. Separate endpoints for public/admin access. Supports sort ordering.

### File Storage
All uploaded images (member photos, event images, group images, gallery) are stored on the local filesystem under the `uploads/` directory. Images are resized on upload using Thumbnailator to conserve storage. Old files are deleted on replacement.

### Automated Birthday Notifications
A scheduled task runs every day at 8:00 AM, finds members with birthdays 3 days from the current date, and sends a formatted HTML email to all configured recipients.

### Email Notifications
HTML emails rendered via Thymeleaf templates for: password reset links and birthday reminders.

---

## 5. Folder Structure

```
church-management-backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/churchmanagement/backend/
│       │       ├── BackendApplication.java        # Spring Boot entry point, enables scheduling
│       │       ├── config/                        # Application configuration classes
│       │       │   ├── ApplicationConfig.java     # Beans: UserDetailsService, AuthProvider, PasswordEncoder
│       │       │   ├── AppProperties.java         # Typed config properties (@ConfigurationProperties)
│       │       │   ├── DataInitializer.java       # Seeds the default admin on startup
│       │       │   ├── JwtProperties.java         # JWT secret and expiration config
│       │       │   ├── SecurityConfig.java        # HTTP security rules, CORS, filter chain
│       │       │   └── WebConfig.java             # Static resource handler for /uploads/**
│       │       ├── controller/                    # REST controllers — one per domain
│       │       │   ├── AuthController.java
│       │       │   ├── MemberController.java
│       │       │   ├── ChurchGroupController.java
│       │       │   ├── EventController.java
│       │       │   ├── AttendanceController.java
│       │       │   ├── MeetingAttendanceController.java
│       │       │   ├── DonationController.java
│       │       │   ├── ExpenditureController.java
│       │       │   ├── GalleryController.java
│       │       │   ├── AnalyticsController.java
│       │       │   └── UserController.java
│       │       ├── dto/                           # Data Transfer Objects (request/response shapes)
│       │       │   ├── AuthRequest.java
│       │       │   ├── AuthResponse.java
│       │       │   ├── RegisterRequest.java
│       │       │   ├── MemberDto.java
│       │       │   ├── ChurchGroupDto.java
│       │       │   ├── EventDto.java
│       │       │   ├── AttendanceDto.java
│       │       │   ├── MeetingAttendanceRequest.java
│       │       │   ├── DonationDto.java
│       │       │   ├── ExpenditureDto.java
│       │       │   ├── GalleryImageDto.java
│       │       │   ├── UploadResponseDto.java
│       │       │   ├── UserDto.java
│       │       │   └── PasswordUpdateRequest.java
│       │       ├── exceptions/                    # Custom exceptions and global handler
│       │       │   ├── GlobalExceptionHandler.java
│       │       │   ├── FailedToStoreFileException.java
│       │       │   └── MaximumFileSizeException.java
│       │       ├── model/                         # JPA entities
│       │       │   ├── User.java
│       │       │   ├── Member.java
│       │       │   ├── ChurchGroup.java
│       │       │   ├── Event.java
│       │       │   ├── Attendance.java
│       │       │   ├── MeetingAttendance.java
│       │       │   ├── Donation.java
│       │       │   ├── Expenditure.java
│       │       │   ├── GalleryImage.java
│       │       │   ├── PasswordResetToken.java
│       │       │   └── Role.java                  # Enum: ADMIN, USER
│       │       ├── repository/                    # Spring Data JPA repositories
│       │       │   ├── UserRepository.java
│       │       │   ├── MemberRepository.java
│       │       │   ├── ChurchGroupRepository.java
│       │       │   ├── EventRepository.java
│       │       │   ├── AttendanceRepository.java
│       │       │   ├── MeetingAttendanceRepository.java
│       │       │   ├── DonationRepository.java
│       │       │   ├── ExpenditureRepository.java
│       │       │   ├── GalleryImageRepository.java
│       │       │   └── PasswordResetTokenRepository.java
│       │       ├── security/                      # JWT infrastructure
│       │       │   ├── JwtService.java            # Token generation and validation
│       │       │   └── JwtAuthenticationFilter.java # Per-request JWT processing
│       │       ├── service/                       # Business logic layer
│       │       │   ├── AuthService.java
│       │       │   ├── MemberService.java
│       │       │   ├── ChurchGroupService.java
│       │       │   ├── EventService.java
│       │       │   ├── AttendanceService.java
│       │       │   ├── MeetingAttendanceService.java  # Interface
│       │       │   ├── MeetingAttendanceServiceImpl.java
│       │       │   ├── DonationService.java
│       │       │   ├── ExpenditureService.java
│       │       │   ├── GalleryService.java
│       │       │   ├── AnalyticsService.java
│       │       │   ├── FileStorageService.java
│       │       │   ├── EmailService.java
│       │       │   └── UserService.java
│       │       └── task/
│       │           └── BirthdayNotificationTask.java  # @Scheduled daily task
│       └── resources/
│           ├── application.yml                    # Main configuration file
│           └── templates/
│               └── emails/                        # Thymeleaf HTML email templates
│                   ├── birthday-notification.html
│                   └── password-reset.html
├── uploads/                                       # Runtime file storage (gitignored)
├── Dockerfile                                     # Multi-stage Docker build
└── pom.xml                                        # Maven build descriptor
```

---

## 6. Database Design

### Database Choice

**PostgreSQL** was chosen for its robustness, support for complex relational queries, EXTRACT() functions (used for birthday queries), and strong ACID compliance — critical for financial data integrity.

### Schema Overview

The schema consists of 10 tables managed automatically by Hibernate (`ddl-auto: update`).

```
users ────────────── members (1:1)
                        │
                        ├──── group_members (M:M) ──── church_groups
                        │
                        ├──── event_registrations (M:M) ──── events
                        │                                        │
                        │                                    attendance (M:M join with notes)
                        │
                        └──── donations (1:M)

expenditures (standalone)
gallery_images (standalone)
meeting_attendances (standalone)
password_reset_tokens ──── users (1:1)
```

### Join Tables

| Join Table | Left Entity | Right Entity | Purpose |
|------------|------------|--------------|---------|
| `group_members` | `church_groups` | `members` | Tracks which members belong to which groups |
| `event_registrations` | `events` | `members` | Tracks event pre-registrations |

### Key Constraints & Notes

- `users.email` is unique and non-null
- `members.user_id` foreign key allows NULL (for pre-registered members without accounts)
- `password_reset_tokens.token` is unique
- `expenditures.amount` and `donations.amount` use `BigDecimal` for financial precision
- `@PrePersist` hooks auto-set timestamps on `Attendance`, `Donation`, `Expenditure`, and `GalleryImage`

---

## 7. Entity Descriptions

### User

The authentication entity. Implements Spring Security's `UserDetails`.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `email` | String | Unique login identifier |
| `password` | String | BCrypt-hashed password |
| `role` | Role (Enum) | `ADMIN` or `USER` |
| `profileComplete` | boolean | Whether the member has completed their profile |
| `createdAt` | LocalDateTime | Set automatically on persist |
| `memberProfile` | Member | One-to-one relationship |

**Relationship:** One `User` → One `Member` (bidirectional, `User` is not the owner)

---

### Member

The core domain entity representing a church member.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `firstName`, `lastName` | String | Full name |
| `phone` | String | Used for pre-registration linking |
| `address` | String | Home address |
| `dateOfBirth` | LocalDate | Used for birthday scheduling |
| `joinedDate` | LocalDate | When they joined the church |
| `profileImageUrl` | String | Relative URL to stored image |
| `gender` | String | Member gender |
| `membershipStatus` | String | e.g., member, worker, leader |
| `maritalStatus` | String | Single / Married |
| `emergencyContact` | String | Emergency contact info |
| `spouseName` | String | For married members |
| `childrenData` | String | JSON string of children details |
| `profession` | String | Occupation |
| `user` | User | Linked auth account (nullable) |

**Relationships:**
- Many-to-Many with `ChurchGroup` (via `group_members`)
- One-to-Many with `Donation`
- One-to-Many with `Attendance`

---

### ChurchGroup

Represents a ministry, department, or small group within the church.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `name` | String | Group name (required) |
| `description` | String | Group description |
| `imageUrl` | String | Cover image URL |
| `category` | String | Group category (defaults to "General") |
| `meetingSchedule` | String | e.g., "Tuesdays 7PM" |
| `members` | List\<Member\> | Many-to-many members |

**Business Rule:** A member may belong to a maximum of 2 groups simultaneously.

---

### Event

Represents church events such as services, conferences, or outreach programs.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `title` | String | Event name |
| `description` | String (TEXT) | Full description |
| `location` | String | Venue |
| `imageUrl` | String (1000) | Event banner URL |
| `startTime` | LocalDateTime | Event start |
| `endTime` | LocalDateTime | Event end |
| `registeredMembers` | List\<Member\> | Pre-registered attendees |
| `attendanceRecords` | List\<Attendance\> | Actual check-in records |

---

### Attendance

Records an individual member's check-in at a specific event.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `event` | Event | FK to event |
| `member` | Member | FK to member |
| `checkInTime` | LocalDateTime | Auto-set on persist if not provided |
| `notes` | String | e.g., "Brought 2 guests" |

---

### MeetingAttendance

Tracks aggregate headcounts for general church meetings (not tied to individual members).

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `meetingName` | String | Name of the meeting (required) |
| `meetingDate` | LocalDate | Date of meeting (required) |
| `attendeeCount` | Integer | Number of attendees (≥ 0) |

---

### Donation

Records a financial contribution by a member or anonymous donor.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `amount` | BigDecimal | Donation amount (required) |
| `fund` | String | e.g., Tithe, Building Fund, Missions |
| `paymentMethod` | String | Cash, Bank Transfer, Card |
| `transactionId` | String | External payment reference |
| `donationDate` | LocalDateTime | Defaults to now |
| `member` | Member | Donor (nullable for anonymous) |

---

### Expenditure

Records a church expense for financial tracking.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `title` | String | Expense label (required) |
| `description` | String | Details |
| `amount` | BigDecimal | Expense amount (required) |
| `category` | String | e.g., Utility, Salary, Maintenance |
| `date` | LocalDateTime | Defaults to now |

---

### GalleryImage

An image in the church's media gallery.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `title` | String | Optional caption |
| `description` | String | Optional description |
| `imageUrl` | String | File path URL (required) |
| `isHeroCarousel` | boolean | Show on homepage slider |
| `isPublic` | boolean | Visible in public gallery |
| `isShowOnLanding` | boolean | Show on landing page (max 5) |
| `sortOrder` | int | Display ordering |
| `uploadedAt` | LocalDateTime | Auto-set on persist |

---

### PasswordResetToken

Stores a time-limited token for password reset flows.

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Primary key |
| `token` | String | UUID token (unique) |
| `user` | User | One-to-one with User |
| `expiryDate` | LocalDateTime | Token expires after 1 hour |

---

## 8. API Documentation

**Base URL:** `http://localhost:8081/api/v1`

**Authentication:** All protected endpoints require the header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

### Authentication Endpoints

| Method | URL | Description | Auth Required |
|--------|-----|-------------|--------------|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/authenticate` | Login and receive JWT | No |
| POST | `/auth/forgot-password` | Request password reset email | No |
| POST | `/auth/reset-password` | Submit new password with token | No |

#### POST /auth/register

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "securePass123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+233244000000",
  "admin": false
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john.doe@example.com",
  "role": "USER",
  "memberId": 12,
  "profileImageUrl": null,
  "profileComplete": false,
  "profileLinked": false
}
```

**Error `400`:**
```json
{ "success": false, "message": "Email already in use" }
```

---

#### POST /auth/authenticate

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "securePass123"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john.doe@example.com",
  "role": "USER",
  "memberId": 12,
  "profileImageUrl": "/uploads/members/12/img-uuid.jpg",
  "profileComplete": true,
  "profileLinked": false
}
```

---

#### POST /auth/forgot-password

**Request Body:**
```json
{ "email": "john.doe@example.com" }
```

**Response `200 OK`:** Empty body. Email is sent with reset link.

---

#### POST /auth/reset-password

**Request Body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "newSecurePass456"
}
```

**Response `200 OK`:** Empty body.

---

### Member Endpoints

| Method | URL | Description | Auth Required | Role |
|--------|-----|-------------|--------------|------|
| GET | `/members` | Get all members | Yes | Any |
| GET | `/members/{id}` | Get member by ID | Yes | Any |
| POST | `/members` | Create a member | Yes | ADMIN |
| PUT | `/members/{id}` | Full update a member | Yes | ADMIN |
| PUT | `/members/{id}/profile` | Member self-updates profile | Yes | Any |
| POST | `/members/{memberId}/profile-image` | Upload profile photo | Yes | Any |
| DELETE | `/members/{id}` | Delete a member | Yes | ADMIN |
| GET | `/members/{id}/groups` | Get member's groups | Yes | Any |

#### GET /members (Response `200 OK`)

```json
[
  {
    "id": 12,
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+233244000000",
    "address": "123 Church Street",
    "dateOfBirth": "1990-05-14",
    "joinedDate": "2020-01-01",
    "profileImageUrl": "/uploads/members/12/img-abc.jpg",
    "userId": 5,
    "email": "john.doe@example.com",
    "accountCreatedAt": "2024-01-15T10:00:00",
    "groupNames": ["Youth Ministry", "Choir"],
    "gender": "Male",
    "membershipStatus": "worker",
    "maritalStatus": "Married",
    "emergencyContact": "+233244111111",
    "spouseName": "Jane Doe",
    "childrenData": "[{\"name\":\"Junior\",\"age\":5}]",
    "profession": "Software Engineer"
  }
]
```

#### POST /members/{memberId}/profile-image

**Request:** `multipart/form-data`

| Field | Type | Description |
|-------|------|-------------|
| `profileImage` | File | JPEG/PNG/WebP image (max 10MB) |
| `dateOfBirth` | Date (ISO) | Optional, updates DOB |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Profile image uploaded successfully",
  "profileImageUrl": "/uploads/members/12/img-newuuid.jpg",
  "uploadedAt": "2025-05-08T14:30:00"
}
```

---

### Church Group Endpoints

| Method | URL | Description | Auth | Role |
|--------|-----|-------------|------|------|
| GET | `/groups` | Get all groups | Yes | Any |
| POST | `/groups` | Create a group | Yes | ADMIN |
| PUT | `/groups/{id}` | Update a group | Yes | ADMIN |
| DELETE | `/groups/{id}` | Delete a group | Yes | ADMIN |
| POST | `/groups/upload-image` | Upload group image | Yes | ADMIN |
| POST | `/groups/{groupId}/members/{memberId}` | Member joins group | Yes | Any |
| DELETE | `/groups/{groupId}/members/{memberId}` | Member leaves group | Yes | Any |

#### POST /groups (Request Body)

```json
{
  "name": "Youth Ministry",
  "description": "For members aged 18-35",
  "imageUrl": "/uploads/groups/uuid/img-xxx.jpg",
  "meetingSchedule": "Saturdays 4PM",
  "category": "Youth"
}
```

**Response `200 OK`:**
```json
{
  "id": 3,
  "name": "Youth Ministry",
  "description": "For members aged 18-35",
  "imageUrl": "/uploads/groups/uuid/img-xxx.jpg",
  "meetingSchedule": "Saturdays 4PM",
  "category": "Youth",
  "memberCount": 0,
  "memberIds": []
}
```

**Error `400`** (member joins 3rd group):
```json
{ "success": false, "message": "Members can only join a maximum of 2 groups" }
```

---

### Event Endpoints

| Method | URL | Description | Auth | Role |
|--------|-----|-------------|------|------|
| GET | `/events` | Get all events | No | Public |
| POST | `/events` | Create an event | Yes | ADMIN |
| PUT | `/events/{id}` | Update an event | Yes | ADMIN |
| DELETE | `/events/{id}` | Delete an event | Yes | ADMIN |
| POST | `/events/upload-image` | Upload event image | Yes | ADMIN |
| POST | `/events/{eventId}/register/{memberId}` | Register for event | Yes | Any |
| DELETE | `/events/{eventId}/register/{memberId}` | Unregister from event | Yes | Any |

#### POST /events (Request Body)

```json
{
  "title": "Annual Convention 2025",
  "description": "Our flagship annual gathering",
  "location": "Main Auditorium",
  "imageUrl": "/uploads/events/uuid/img-xxx.jpg",
  "startTime": "2025-12-01T09:00:00",
  "endTime": "2025-12-03T18:00:00"
}
```

---

### Attendance Endpoints

| Method | URL | Description | Auth |
|--------|-----|-------------|------|
| GET | `/attendance/event/{eventId}` | Event attendance list | Yes |
| GET | `/attendance/member/{memberId}` | Member's attendance history | Yes |
| POST | `/attendance/checkin` | Check member into event | Yes |
| POST | `/attendance` | Log meeting headcount | Yes (ADMIN) |
| GET | `/attendance` | Get all meeting attendances | Yes (ADMIN) |
| PUT | `/attendance/{id}` | Update meeting attendance | Yes (ADMIN) |
| DELETE | `/attendance/{id}` | Delete meeting attendance | Yes (ADMIN) |

#### POST /attendance/checkin (Request Body)

```json
{
  "eventId": 5,
  "memberId": 12,
  "notes": "Brought 2 guests",
  "checkInTime": "2025-12-01T09:15:00"
}
```

---

### Donation Endpoints

| Method | URL | Description | Auth | Role |
|--------|-----|-------------|------|------|
| GET | `/donations` | Get all donations | Yes | ADMIN |
| GET | `/donations/member/{memberId}` | Member's donations | Yes | Any |
| POST | `/donations` | Admin logs a donation | Yes | ADMIN |
| POST | `/donations/contribute` | Member submits a donation | Yes | Any |

#### POST /donations/contribute (Request Body)

```json
{
  "amount": 200.00,
  "fund": "Tithe",
  "paymentMethod": "Mobile Money",
  "transactionId": "MM-20250508-001",
  "memberId": 12
}
```

---

### Expenditure Endpoints

| Method | URL | Description | Auth | Role |
|--------|-----|-------------|------|------|
| GET | `/expenditures` | Get all expenditures | Yes | ADMIN |
| POST | `/expenditures` | Create expenditure | Yes | ADMIN |
| DELETE | `/expenditures/{id}` | Delete expenditure | Yes | ADMIN |

#### POST /expenditures (Request Body)

```json
{
  "title": "Electricity Bill - May",
  "description": "Monthly utility payment",
  "amount": 450.00,
  "category": "Utility",
  "date": "2025-05-01T00:00:00"
}
```

---

### Gallery Endpoints

| Method | URL | Description | Auth |
|--------|-----|-------------|------|
| GET | `/gallery/public` | Public gallery images | No |
| GET | `/gallery/landing` | Landing page images (max 5) | No |
| GET | `/gallery/hero` | Hero carousel images | No |
| GET | `/gallery` | All images (admin) | Yes |
| POST | `/gallery` | Upload gallery image | Yes |
| PUT | `/gallery/{id}` | Update image metadata | Yes |
| DELETE | `/gallery/{id}` | Delete image | Yes |

#### POST /gallery (multipart/form-data)

| Field | Type | Default |
|-------|------|---------|
| `file` | File | Required |
| `title` | String | Optional |
| `description` | String | Optional |
| `isPublic` | boolean | `true` |
| `isHeroCarousel` | boolean | `false` |
| `isShowOnLanding` | boolean | `false` |
| `sortOrder` | int | `0` |

---

### Analytics Endpoints

| Method | URL | Description | Auth | Role |
|--------|-----|-------------|------|------|
| GET | `/analytics/finance-summary` | Financial overview | Yes | ADMIN |

**Response `200 OK`:**
```json
{
  "totalDonations": 15000.00,
  "totalExpenditures": 8500.00,
  "netRevenue": 6500.00,
  "yearlyBreakdown": {
    "2024": { "income": 10000.00, "expense": 5000.00 },
    "2025": { "income": 5000.00, "expense": 3500.00 }
  }
}
```

---

### User Management Endpoints

| Method | URL | Description | Auth | Role |
|--------|-----|-------------|------|------|
| GET | `/users` | Get all users | Yes | ADMIN |
| GET | `/users/{id}` | Get user by ID | Yes | ADMIN or self |
| PATCH | `/users/{id}/role` | Change user role | Yes | ADMIN |
| DELETE | `/users/{id}` | Delete user | Yes | ADMIN |
| PUT | `/users/{id}/password` | Change password | Yes | Any |
| POST | `/users/{id}/complete-profile` | Mark profile complete | Yes | Any |

---

### Curl Examples

**Login:**
```bash
curl -X POST http://localhost:8081/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@church.com","password":"adminpass"}'
```

**Get all members (authenticated):**
```bash
curl -X GET http://localhost:8081/api/v1/members \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Upload a profile image:**
```bash
curl -X POST http://localhost:8081/api/v1/members/12/profile-image \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -F "profileImage=@/path/to/photo.jpg" \
  -F "dateOfBirth=1990-05-14"
```

---

## 9. Authentication & Authorization

### JWT Flow

```
1. User POSTs credentials to /auth/authenticate
2. AuthService validates credentials via AuthenticationManager
3. JwtService generates a signed HS256 JWT (24-hour expiry by default)
4. Client stores token and sends it in the Authorization header
5. JwtAuthenticationFilter intercepts each request:
   a. Extracts token from "Bearer <token>"
   b. Calls JwtService.extractUsername() to get email
   c. Loads UserDetails via UserDetailsService
   d. Calls JwtService.isTokenValid() to verify signature and expiry
   e. Populates SecurityContextHolder if valid
6. @PreAuthorize annotations enforce role-level access per method
```

### JWT Token Structure

```
Header:  { "alg": "HS256" }
Payload: { "sub": "user@email.com", "iat": ..., "exp": ... }
Signature: HMAC-SHA256(base64(header) + "." + base64(payload), SECRET)
```

### Roles & Permissions

| Endpoint Category | ADMIN | USER (authenticated) | Public |
|-------------------|-------|---------------------|--------|
| Auth endpoints | ✅ | ✅ | ✅ |
| View events | ✅ | ✅ | ✅ |
| View public gallery | ✅ | ✅ | ✅ |
| View members | ✅ | ✅ | ❌ |
| Create/Edit/Delete members | ✅ | ❌ | ❌ |
| Manage groups | ✅ | ❌ (join/leave only) | ❌ |
| Manage events | ✅ | ❌ (register only) | ❌ |
| Donations (all) | ✅ | Own only | ❌ |
| Expenditures | ✅ | ❌ | ❌ |
| Analytics | ✅ | ❌ | ❌ |
| Gallery management | ✅ | ❌ | ❌ |
| Meeting attendance | ✅ | ❌ | ❌ |

### Spring Security Configuration Summary

- CSRF is **disabled** (stateless JWT API — no session cookies)
- CORS is configured for `http://localhost:5173` and `http://localhost:3000`
- Session management is **STATELESS**
- Method-level security is enabled with `@EnableMethodSecurity`
- `@PreAuthorize("hasRole('ADMIN')")` guards all admin endpoints

### Default Admin Seeding

On application startup, `DataInitializer` checks whether the configured admin email exists. If not, it creates both the `User` and associated `Member` profile automatically, ensuring the system is always accessible.

---

## 10. Exception Handling

All exceptions are caught and formatted by `GlobalExceptionHandler` annotated with `@RestControllerAdvice`.

### Exception Mapping

| Exception | HTTP Status | Use Case |
|-----------|------------|----------|
| `IllegalArgumentException` | 400 Bad Request | Invalid input (e.g., duplicate email, bad token) |
| `RuntimeException` | 400 Bad Request | Business rule violations (e.g., "Member not found") |
| `MailException` | 500 Internal Server Error | Email sending failures |
| `Exception` (catch-all) | 500 Internal Server Error | Unexpected errors |

### Error Response Format

All error responses follow a consistent JSON structure:

```json
{
  "success": false,
  "message": "Human-readable error description"
}
```

### Custom Exceptions

| Exception Class | Extends | Thrown When |
|----------------|---------|-------------|
| `FailedToStoreFileException` | RuntimeException | IOException during file write |
| `MaximumFileSizeException` | RuntimeException | Upload exceeds 10MB limit |

---

## 11. Validation

### Jakarta Validation

Request DTOs use Jakarta Bean Validation annotations. The `MeetingAttendanceRequest` is the most formally validated DTO:

```java
@NotBlank(message = "Meeting name is required")
private String meetingName;

@NotNull(message = "Meeting date is required")
private LocalDate meetingDate;

@NotNull @PositiveOrZero
private Integer attendeeCount;
```

Controllers use `@Valid` on `@RequestBody` parameters to trigger validation. Validation failures are caught by the `GlobalExceptionHandler`.

### File Validation

`FileStorageService` validates:
- **Content type** — Only `image/jpeg`, `image/png`, `image/gif`, `image/webp` are accepted
- **File size** — Maximum 10MB enforced before processing
- Both validations throw `IllegalArgumentException` which returns a 400 response

### Business Rule Validation

Applied at the service layer (not annotation-based):
- Member joining more than 2 groups throws `IllegalStateException`
- Deleting or modifying the default admin account throws `RuntimeException`

---

## 12. Logging & Monitoring

### Application Logging

Lombok's `@Slf4j` annotation is used in service and task classes. Key logged events include:

- Default admin creation on startup (`DataInitializer`)
- Birthday notification task execution and results (`BirthdayNotificationTask`)
- Admin profile update operations (`MemberService`)
- Email sending successes and failures

Example log output:
```
INFO  - Checking for upcoming birthdays...
INFO  - Birthday notification sent to pastor@church.com for member: John Doe
ERROR - Failed to send birthday notification to xxx for member: Jane Smith
```

### Spring Boot Actuator

The project includes `spring-boot-starter-actuator`. Default endpoints such as `/actuator/health` and `/actuator/info` are available for health checks in deployment environments.

### SQL Logging

`spring.jpa.show-sql: true` and `format_sql: true` are enabled for development, printing all generated SQL to the console. This should be disabled in production by using a profile-specific configuration.

---

## 13. Configuration

### application.yml Overview

All sensitive values are injected through environment variables:

| Config Key | Environment Variable | Description |
|------------|---------------------|-------------|
| `spring.datasource.password` | `DB_PASSWORD` | PostgreSQL password |
| `spring.mail.username` | `MAIL_USERNAME` | Gmail SMTP username |
| `spring.mail.password` | `MAIL_PASSWORD` | Gmail App Password |
| `jwt.secret` | `SECRET_KEY` | Base64-encoded HS256 key (min 32 bytes) |
| `app.default-admin.email` | `ADMIN_EMAIL` | Default admin email address |
| `app.default-admin.password` | `ADMIN_PASSWORD` | Default admin password |
| `app.default-admin.first-name` | `ADMIN_FIRSTNAME` | Admin first name |
| `app.default-admin.last-name` | `ADMIN_LASTNAME` | Admin last name |

### Typed Configuration Beans

`AppProperties` uses `@ConfigurationProperties(prefix = "app")` to bind nested YAML values (admin credentials and notification email lists) into a type-safe Java class, avoiding scattered `@Value` annotations.

`JwtProperties` similarly binds `jwt.secret` and `jwt.expiration`.

### File Upload Limits

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### Scheduled Task Thread Pool

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 5
```

---

## 14. Testing

> The project ships with the Spring Boot Test and Spring Security Test dependencies, providing the foundation for the following testing strategies.

### Unit Testing

Services should be tested in isolation using **Mockito** to mock repositories:

```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void shouldReturnMemberById() {
        Member member = Member.builder().id(1L).firstName("John").build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        
        MemberDto result = memberService.getMemberById(1L);
        
        assertThat(result.getFirstName()).isEqualTo("John");
    }
}
```

### Integration Testing

Spring's `@SpringBootTest` with `MockMvc` can test the full stack from HTTP request to database:

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnTokenOnValidLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
```

### Security Testing

`@WithMockUser(roles = "ADMIN")` from `spring-security-test` allows testing role-based access without a live JWT.

### Recommended Test Coverage Areas

- `AuthService.register()` — valid registration, duplicate email
- `AuthService.resetPassword()` — valid token, expired token
- `ChurchGroupService.joinGroup()` — success case, 3rd group rejection
- `MemberService.deleteMember()` — default admin protection
- `FileStorageService.storeFile()` — invalid type, oversized file
- `BirthdayNotificationTask` — correct date targeting

---

## 15. Performance Optimization

### Lazy Loading

All `@ManyToOne` relationships use `FetchType.LAZY` by default (JPA default for collections). `Attendance.event` and `Attendance.member` are explicitly `LAZY` to avoid N+1 issues when loading attendance lists.

### DTO Projection

The service layer maps entities to DTOs before returning data. This prevents accidental serialization of entire object graphs (e.g., a `Member`'s donations, attendance records, and groups are not loaded unless explicitly needed).

### `@JsonIgnore`

Applied on all bidirectional relationship fields (`Member.groups`, `Member.donations`, `Event.attendanceRecords`, etc.) to prevent circular serialization and reduce response payload size.

### Stream Processing

All list-based queries use Java Streams for mapping, filtering, and limiting (e.g., `getLandingGallery()` streams, filters for `isPublic && isShowOnLanding`, sorts, limits to 5, and maps to DTOs without loading everything into memory unnecessarily).

### Image Optimization

Thumbnailator resizes all uploaded images to a maximum of 1080×1080 pixels while preserving aspect ratio. This significantly reduces stored file size and serves faster to clients.

### Database Indexing

`users.email` has a `UNIQUE` constraint which implicitly creates an index — the most frequently queried column (on every authenticated request). `password_reset_tokens.token` is similarly unique-indexed.

### Future: Caching

Redis caching with `@Cacheable` can be added for frequently accessed, rarely changing data such as the public gallery and group listings.

---

## 16. Security Best Practices

### Password Security

All passwords are hashed with **BCrypt** (`BCryptPasswordEncoder`) before storage. Raw passwords are never stored or logged. Old password verification is required before a password change.

### CSRF

CSRF protection is **disabled** because the API uses stateless JWT authentication without session cookies. CSRF attacks are session-cookie-based and do not apply here.

### CORS

CORS is explicitly configured to allow only trusted origins (`localhost:5173`, `localhost:3000`). Allowed methods, headers, and credentials are strictly defined. Production deployments must update allowed origins to the live frontend URL.

### JWT Security

- Tokens are signed with HMAC-SHA256 using a Base64-encoded secret key
- Token expiry is enforced on every request
- Expired/invalid tokens are silently rejected by the filter; the request continues unauthenticated (protected endpoints then return 403)

### Default Admin Protection

The default admin account is protected at multiple layers:
- Cannot be deleted via `UserService.deleteUser()`
- Password cannot be changed via `UserService.updatePassword()`
- Member profile cannot be deleted via `MemberService.deleteMember()`

### File Upload Security

- Content type whitelist: only image types accepted
- File size capped at 10MB
- Files stored outside the web root (`uploads/` mapped via `WebMvcConfigurer`) — no executable files are served
- Old files deleted on replacement to prevent orphaned file accumulation

### Input Sanitization

Jakarta Validation annotations ensure `@NotBlank`, `@NotNull`, and `@PositiveOrZero` constraints are enforced at the API boundary. Business-level checks (e.g., group limit) are enforced in the service layer.

---

## 17. Deployment Guide

### Prerequisites

- Java 21 JDK
- Maven 3.9+
- PostgreSQL 14+
- Docker (optional)

### Local Development Setup

**1. Clone the repository:**
```bash
git clone https://github.com/your-org/church-management-backend.git
cd church-management-backend
```

**2. Create the PostgreSQL database:**
```sql
CREATE DATABASE church_management;
```

**3. Set environment variables:**
```bash
export DB_PASSWORD=your_postgres_password
export MAIL_USERNAME=your_gmail@gmail.com
export MAIL_PASSWORD=your_gmail_app_password
export SECRET_KEY=your_base64_encoded_256bit_secret
export ADMIN_EMAIL=admin@yourchurch.com
export ADMIN_PASSWORD=adminSecurePass
export ADMIN_FIRSTNAME=Church
export ADMIN_LASTNAME=Admin
```

**4. Build and run:**
```bash
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8081`.

---

### Docker Setup

**Build the image:**
```bash
docker build -t church-management-backend .
```

**Run with environment variables:**
```bash
docker run -d \
  -p 8081:8081 \
  -e DB_PASSWORD=your_password \
  -e MAIL_USERNAME=your_email@gmail.com \
  -e MAIL_PASSWORD=your_app_password \
  -e SECRET_KEY=your_base64_secret \
  -e ADMIN_EMAIL=admin@church.com \
  -e ADMIN_PASSWORD=adminpass \
  -e ADMIN_FIRSTNAME=Church \
  -e ADMIN_LASTNAME=Admin \
  --name church-backend \
  church-management-backend
```

**Docker Compose (recommended for local dev with PostgreSQL):**

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: church_management
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  backend:
    build: .
    ports:
      - "8081:8081"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/church_management
      DB_PASSWORD: ${DB_PASSWORD}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      SECRET_KEY: ${SECRET_KEY}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      ADMIN_FIRSTNAME: ${ADMIN_FIRSTNAME}
      ADMIN_LASTNAME: ${ADMIN_LASTNAME}
    volumes:
      - ./uploads:/app/uploads

volumes:
  pgdata:
```

```bash
docker-compose up -d
```

---

### Production Checklist

- [ ] Set `spring.jpa.show-sql: false`
- [ ] Update `allowedOrigins` in `SecurityConfig` to production frontend URL
- [ ] Store `uploads/` on a persistent volume or migrate to cloud storage (e.g., AWS S3)
- [ ] Use a secrets manager (AWS Secrets Manager, HashiCorp Vault) instead of plain environment variables
- [ ] Configure a reverse proxy (Nginx) with HTTPS/TLS
- [ ] Enable Spring profiles: `spring.profiles.active=prod`
- [ ] Set `jwt.expiration` appropriately for production (e.g., 3600000 for 1 hour)
- [ ] Configure database connection pooling (HikariCP — included with Spring Boot)

---

## 18. CI/CD Pipeline

### GitHub Actions Workflow

Create `.github/workflows/ci.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: church_management_test
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: test_password
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build and Test
        env:
          DB_PASSWORD: test_password
          SECRET_KEY: ${{ secrets.SECRET_KEY }}
          ADMIN_EMAIL: test@admin.com
          ADMIN_PASSWORD: testpass
          ADMIN_FIRSTNAME: Test
          ADMIN_LASTNAME: Admin
          MAIL_USERNAME: test@mail.com
          MAIL_PASSWORD: testmailpass
        run: mvn -B clean verify

  docker-build:
    needs: build-and-test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      - uses: actions/checkout@v4

      - name: Build Docker Image
        run: docker build -t church-management-backend:${{ github.sha }} .

      - name: Push to Registry
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker push church-management-backend:${{ github.sha }}
```

### Pipeline Stages

| Stage | Trigger | Action |
|-------|---------|--------|
| **Build & Test** | Every push and PR | Compiles, runs tests with a real PostgreSQL service container |
| **Docker Build** | Push to `main` only | Builds and pushes Docker image |
| **Deploy** | Post Docker push | Can trigger deployment to Railway, Render, or a VPS via SSH |

---

## 19. Future Improvements

### Near-Term

- **Cloud File Storage (AWS S3 / Cloudinary)** — Replace local filesystem storage with a CDN-backed cloud store to enable horizontal scaling and persistent storage across deployments
- **Refresh Token Support** — Add longer-lived refresh tokens alongside short-lived access tokens for better security UX
- **Pagination** — Add `Pageable` support to member and donation list endpoints for large congregations
- **Soft Deletes** — Add `deletedAt` timestamp to members and users instead of hard deletion, preserving historical data
- **Donation Receipts** — Email PDF receipts to members upon donation submission

### Medium-Term

- **Role Granularity** — Introduce additional roles (e.g., `STAFF`, `LEADER`) with more fine-grained permissions
- **Push Notifications** — Integrate Firebase Cloud Messaging (FCM) for event reminders and announcements
- **Member Directory PDF** — Generate downloadable member directories using JasperReports or iText
- **Financial Reports** — Export monthly/yearly financial reports as Excel or PDF
- **Tithe Calculator** — Help members compute suggested tithe amounts based on income
- **Redis Caching** — Cache gallery images, group listings, and public events to reduce database load

### Long-Term

- **Microservices Extraction** — Extract Finance, Gallery, and Notification into independent services communicating via Kafka or RabbitMQ
- **Mobile App Integration** — Expose GraphQL endpoint for flexible mobile querying
- **OAuth2 / Social Login** — Allow members to sign in with Google accounts
- **Multi-Tenant Support** — Extend the platform to manage multiple church branches under one umbrella
- **Analytics Dashboard** — Deeper reporting: attendance trends over time, donation growth rates, group engagement metrics

---

## 20. Conclusion

The **Church Management System Backend** is a well-structured, production-aware Spring Boot REST API that addresses the real administrative challenges facing a growing church community. It demonstrates solid software engineering practices:

- **Clean layered architecture** with clear separation between controllers, services, repositories, and security
- **Stateless JWT authentication** with role-based access control enforced at both the HTTP filter and method level
- **Domain-driven design** with rich entities (Member, Event, Group, Donation) that map closely to real-world church operations
- **Automated workflows** through scheduled tasks for birthday notifications
- **Extensible file management** with type validation, size enforcement, and automatic image optimization
- **Financial integrity** using `BigDecimal` for all monetary values and structured analytics
- **Developer-friendly configuration** with typed properties beans, environment-variable-driven secrets, and a self-seeding admin account

The system is designed to be immediately deployable via Docker and can be extended with cloud storage, caching, mobile notifications, and multi-tenancy as the church grows. It serves as a strong foundation for a full-stack church management platform.

---

*Documentation generated for Church Management System Backend — Spring Boot 3.2.4 / Java 21*
