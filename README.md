# StoryLoom Auth Service

A dedicated **authentication and user management microservice** for the StoryLoom platform. Built with Spring Boot 4, this service handles user registration, email verification via OTP, JWT-based authentication (access & refresh tokens), password reset, and session management.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Authentication Flow](#authentication-flow)
- [Security Considerations](#security-considerations)
- [Running with Low Memory](#running-with-low-memory)

---

## Overview

StoryLoom Auth Service is a stateless, RESTful microservice responsible for all authentication-related operations within the StoryLoom ecosystem. It registers with **Eureka Service Discovery** and can communicate with other services via **OpenFeign**. Authentication is enforced via JWT tokens — no server-side sessions are used.

## Tech Stack

| Layer              | Technology                                      |
| ------------------ | ----------------------------------------------- |
| **Runtime**        | Java 21                                         |
| **Framework**      | Spring Boot 4.0.6                               |
| **Cloud**          | Spring Cloud 2025.1.1 (Eureka Client, OpenFeign) |
| **Security**       | Spring Security + JWT (jjwt 0.12.7) + BCrypt    |
| **Database**       | PostgreSQL                                      |
| **ORM**            | Spring Data JPA / Hibernate                     |
| **Email**          | Spring Boot Mail (SMTP / Gmail)                 |
| **Build Tool**     | Maven                                           |
| **Utilities**      | Lombok, Jakarta Validation                      |

## Architecture

```
┌──────────────┐       ┌──────────────────────────────────────────┐
│   Client     │──────▶│           StoryLoom Auth Service           │
│  (Frontend)  │       │               (Port 8088)                 │
└──────────────┘       │                                          │
                       │  ┌─────────┐  ┌────────┐  ┌────────────┐  │
┌──────────────┐       │  │Controller│─▶│Service│─▶│Repository  │  │
│  Eureka      │◀──────│  └─────────┘  └────────┘  └─────┬──────┘  │
│  (Port 8761) │       │                                   │         │
└──────────────┘       │                              ┌────▼────┐   │
                       │                              │PostgreSQL│   │
┌──────────────┐       │                              └─────────┘   │
│ Other        │◀──────│  (via OpenFeign)                           │
│ Microservices│       │                              ┌─────────┐   │
└──────────────┘       │                              │  Gmail  │   │
                       │                              │  (SMTP) │   │
                       │                              └─────────┘   │
                       └──────────────────────────────────────────┘
```

## Features

- **User Registration** — Create an account with username, email, and password
- **Email Verification (OTP)** — 6-digit OTP sent to the user's email; account remains disabled until verified
- **OTP Resend** — Request a new OTP if the previous one expired or wasn't received
- **JWT Authentication** — Stateless authentication using access tokens (15 min) and refresh tokens (7 days)
- **Token Refresh** — Obtain a new access token using a valid refresh token
- **Password Reset** — Request a reset OTP via email, verify it, then set a new password
- **Logout** — Invalidate all refresh tokens for the user on logout
- **Scheduled Cleanup** — Automatically purge expired OTP records every hour
- **Service Discovery** — Auto-registers with Eureka for inter-service communication
- **Lightweight Tuning** — Optimized thread pool and lazy initialization for minimal resource usage

## API Reference

All endpoints are prefixed with the service base URL: `http://localhost:8088`

### 🔓 Public Endpoints (no authentication required)

| Method | Endpoint                 | Description                     |
| ------ | ------------------------ | ------------------------------- |
| POST   | `/register`              | Register a new user account     |
| POST   | `/verify`                | Verify email with OTP           |
| POST   | `/login`                 | Authenticate and receive tokens |
| POST   | `/resend-otp`            | Resend email verification OTP   |
| POST   | `/resetPassword-Request` | Request a password reset OTP    |

### 🔒 Protected Endpoints (Bearer token required)

| Method | Endpoint        | Description                   |
| ------ | --------------- | ----------------------------- |
| POST   | `/refresh`      | Refresh an access token       |
| POST   | `/resetPassword` | Reset password using OTP      |
| POST   | `/logout`       | Logout and invalidate tokens  |

---

### 1. Register

```
POST /register
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword",
  "phone": "+1234567890"
}
```

**Response (200 OK):**
```json
{
  "message": "Registration successful! Please check your email for OTP.",
  "email": "john@example.com"
}
```

> **Note:** The user cannot log in until their email is verified via OTP.

---

### 2. Verify Email

```
POST /verify
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response (200 OK):**
```json
{
  "message": "Email verified successfully!"
}
```

---

### 3. Login

```
POST /login
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "securepassword"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

---

### 4. Refresh Token

```
POST /refresh
Authorization: Bearer <refreshToken>
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

---

### 5. Resend OTP

```
POST /resend-otp
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "OTP resent successfully!"
}
```

---

### 6. Request Password Reset

```
POST /resetPassword-Request
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Response (200 OK):**
```
"Reset OTP sent successfully!"
```

---

### 7. Reset Password

```
POST /resetPassword
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "otp": "654321",
  "newPassword": "newsecurepassword"
}
```

**Response (200 OK):**
```
"Password changed successfully!"
```

---

### 8. Logout

```
POST /logout
Authorization: Bearer <accessToken>
```

**Response (200 OK):**
```json
{
  "message": "Logout successful!"
}
```

---

## Database Schema

The service manages four PostgreSQL tables (auto-created via Hibernate `ddl-auto=update`):

### `users`
| Column           | Type         | Constraints                    |
| ---------------- | ------------ | ------------------------------ |
| id               | BIGINT       | PRIMARY KEY, AUTO_INCREMENT     |
| username         | VARCHAR      | UNIQUE, NOT NULL                |
| password         | VARCHAR      | NOT NULL (BCrypt hashed)        |
| email            | VARCHAR      | UNIQUE, NOT NULL                |
| phone            | VARCHAR      | NOT NULL                        |
| email_verified   | BOOLEAN      | DEFAULT false                   |
| verification_otp | VARCHAR      |                                |
| otp_generated_time | TIMESTAMP |                                |

### `otp_verifications`
| Column         | Type         | Constraints                    |
| -------------- | ------------ | ------------------------------ |
| id             | BIGINT       | PRIMARY KEY, AUTO_INCREMENT     |
| email          | VARCHAR      |                                |
| otp            | VARCHAR      |                                |
| generated_time | TIMESTAMP    |                                |
| expiry_time    | TIMESTAMP    |                                |
| verified       | BOOLEAN      | DEFAULT false                   |
| user_id        | BIGINT       | FOREIGN KEY → users(id)         |

### `refresh_tokens`
| Column      | Type         | Constraints                    |
| ----------- | ------------ | ------------------------------ |
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT     |
| token       | VARCHAR      | UNIQUE, NOT NULL                |
| expiry_date | TIMESTAMP    | NOT NULL                        |
| user_id     | BIGINT       | FOREIGN KEY → users(id)         |

### `password_reset`
| Column         | Type         | Constraints                    |
| -------------- | ------------ | ------------------------------ |
| id             | BIGINT       | PRIMARY KEY, AUTO_INCREMENT     |
| email          | VARCHAR      |                                |
| otp            | VARCHAR      |                                |
| generated_time | TIMESTAMP    |                                |
| expiry_time    | TIMESTAMP    |                                |
| used           | BOOLEAN      | DEFAULT false                   |
| user_id        | BIGINT       | FOREIGN KEY → users(id)         |

---

## Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven** (or use the included `mvnw` wrapper)
- **PostgreSQL** running on `localhost:5432`
- **Eureka Server** running on `localhost:8761` (if using service discovery)
- **Gmail account** with an App Password for email sending

### 1. Clone the Repository

```bash
git clone <repository-url>
cd storyloom-auth-service
```

### 2. Create the Database

Create a PostgreSQL database for the service:

```sql
CREATE DATABASE storyloom_auth_db;
```

### 3. Configure Application Properties

Copy and edit `src/main/resources/application.properties`:

```properties
spring.application.name=storyloom-auth-service
server.port=8088

# Database
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/storyloom-auth-db
spring.datasource.username=<your-db-username>
spring.datasource.password=<your-db-password>

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# Eureka Client
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
eureka.instance.prefer-ip-address=true
eureka.instance.ip-address=127.0.0.1

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<your-gmail-address>
spring.mail.password=<your-gmail-app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.email.from=<your-gmail-address>

# OTP
otp.expiration.minutes=5

# JWT
jwt.secret=<your-base64-encoded-secret-key>
jwt.expiration=900000        # 15 minutes
jwt.refresh.expiration=604800000  # 7 days
```

> ⚠️ **Important:** Never commit real credentials to version control. Use environment variables or a secrets manager in production.

### 4. Build and Run

Using Maven wrapper:

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Or build the JAR first:

```bash
./mvnw clean package -DskipTests
java -jar target/storyloom-auth-service-0.0.1-SNAPSHOT.jar
```

The service will start on **port 8088** and register itself with Eureka at `http://localhost:8761`.

---

## Configuration

### JWT Token Expiration

| Token Type      | Default Duration | Property                      |
| --------------- | ---------------- | ----------------------------- |
| Access Token    | 15 minutes       | `jwt.expiration`              |
| Refresh Token   | 7 days           | `jwt.refresh.expiration`     |

### OTP Expiration

| Setting           | Default | Property                |
| ----------------- | ------- | ----------------------- |
| OTP validity      | 5 min   | `otp.expiration.minutes` |
| Cleanup schedule  | Hourly  | Hardcoded (cron)         |

### Gmail App Password

To send emails via Gmail, you need to generate an **App Password**:

1. Go to your Google Account → **Security**
2. Enable **2-Step Verification**
3. Go to **App Passwords** → Create a new app password
4. Use that 16-character password as `spring.mail.password`

---

## Project Structure

```
src/main/java/com/example/storyloom_auth_service/
├── StoryloomAuthServiceApplication.java     # Main entry point
├── config/
│   ├── SecurityConfig.java                 # Spring Security config & filter chain
│   └── JwtFilter.java                       # JWT authentication filter
├── controller/
│   └── UserController.java                  # REST API endpoints
├── dto/
│   ├── LoginRequest.java                    # Login request DTO
│   ├── RefreshTokenRequest.java             # Refresh token request DTO
│   ├── ResetRequest.java                    # Password reset DTO (email + otp + new password)
│   └── ResetRequestEmail.java              # Password reset request DTO (email only)
├── model/
│   ├── auth/
│   │   ├── OtpVerification.java            # Email verification OTP entity
│   │   ├── PasswordReset.java              # Password reset OTP entity
│   │   └── RefreshToken.java               # Refresh token entity
│   └── user/
│       ├── User.java                       # User entity
│       └── UserPrincipal.java              # Spring Security UserDetails wrapper
├── repository/
│   └── auth/
│       ├── OtpVerificationRepo.java        # OTP verification repository
│       ├── RefreshTokenRepo.java           # Refresh token repository
│       ├── ResetPasswordRepo.java           # Password reset repository
│       └── UserRepo.java                   # User repository
└── service/
    ├── EmailService.java                   # Email sending (SMTP)
    ├── OtpService.java                     # OTP generation, verification & cleanup
    ├── jwt/
    │   ├── JwtService.java                 # JWT creation, parsing & validation
    │   └── RefreshTokenService.java        # Refresh token CRUD & expiration logic
    └── user/
        ├── MyUserService.java              # UserDetailsService implementation
        └── UserService.java                # User business logic (register, verify, reset)
```

---

## Authentication Flow

### Registration & Verification
```
Client                          Auth Service                     Database
  │                                  │                              │
  ├── POST /register ──────────────▶│                              │
  │                                  ├── Save user (unverified) ──▶│
  │                                  ├── Generate 6-digit OTP ───▶│
  │◀── "Check your email for OTP" ──┤                              │
  │                                  │                              │
  ├── POST /verify ────────────────▶│                              │
  │                                  ├── Validate OTP ────────────▶│
  │                                  ├── Mark user as verified ──▶│
  │◀── "Email verified!" ──────────┤                              │
```

### Login & Token Management
```
Client                          Auth Service                     Database
  │                                  │                              │
  ├── POST /login ─────────────────▶│                              │
  │                                  ├── Authenticate credentials  │
  │                                  ├── Generate access token     │
  │                                  ├── Generate refresh token ──▶│
  │◀── accessToken + refreshToken ──┤                              │
  │                                  │                              │
  ├── POST /refresh ──────────────▶│  (Authorization: refresh token)
  │                                  ├── Validate refresh token ──▶│
  │                                  ├── Generate new access token │
  │◀── new accessToken ────────────┤                              │
  │                                  │                              │
  ├── POST /logout ───────────────▶│  (Authorization: access token)
  │                                  ├── Extract username from JWT │
  │                                  ├── Delete refresh tokens ──▶│
  │◀── "Logout successful!" ───────┤                              │
```

### Password Reset
```
Client                          Auth Service                  Email / DB
  │                                  │                              │
  ├── POST /resetPassword-Request ─▶│                              │
  │                                  ├── Generate reset OTP ──────▶│ (DB)
  │                                  ├── Send OTP via email ─────▶│ (Gmail)
  │◀── "Reset OTP sent!" ──────────┤                              │
  │                                  │                              │
  ├── POST /resetPassword ────────▶│                              │
  │                                  ├── Validate reset OTP ─────▶│ (DB)
  │                                  ├── Update password ────────▶│ (DB)
  │                                  ├── Invalidate refresh tokens▶│ (DB)
  │◀── "Password changed!" ────────┤                              │
```

---

## Security Considerations

- **Passwords** are hashed using **BCrypt** with strength 12 before storage
- **JWT tokens** are signed with **HMAC-SHA256** using a Base64-encoded secret key
- **CSRF** is disabled (stateless API — no cookies are used)
- **Sessions** are stateless (`SessionCreationPolicy.STATELESS`) — no server-side session storage
- **One refresh token per user** — creating a new one invalidates the previous
- **Password change** automatically invalidates all existing refresh tokens
- **OTP records** are automatically cleaned up on a hourly schedule
- Users **cannot log in** until their email is verified (`isEnabled()` checks `emailVerified`)

> ⚠️ **Production recommendations:**
> - Move all secrets (DB password, JWT secret, Gmail credentials) to environment variables or a vault
> - Use HTTPS in production
> - Consider adding rate limiting on OTP-related endpoints
> - Replace the HS256 JWT secret with a longer, securely generated key

---

## Running with Low Memory

The service is tuned for low resource usage. To run with constrained memory:

```bash
java -Xms64m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -jar target/storyloom-auth-service-0.0.1-SNAPSHOT.jar
```

Additional optimizations already configured:
- **Lazy initialization** enabled (`spring.main.lazy-initialization=true`)
- **Reduced Tomcat threads** (max 20, min-spare 5)
- **Actuator endpoints** disabled by default

---

## License

This project is part of the **StoryLoom** platform.

---
