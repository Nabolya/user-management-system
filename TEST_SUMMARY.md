# User Management System — Build & Test Summary

## Stack

- Java 17
- Spring Boot 3.3.4 (Web, Data JPA, Validation)
- PostgreSQL (via Spring Data JPA / Hibernate)
- Maven
- Postman (manual testing)

## Architecture

Layered structure:

## Endpoints

| Method | Path              | Description       | Success |
|--------|-------------------|--------------------|---------|
| POST   | /api/users        | Create user        | 201     |
| GET    | /api/users        | Get all users       | 200     |
| GET    | /api/users/{id}   | Get user by ID     | 200     |
| PUT    | /api/users/{id}   | Update user        | 200     |
| DELETE | /api/users/{id}   | Delete user        | 204     |

## Input validation (Bean Validation, enforced via `@Valid`)

- Name: required, 3–50 characters
- Email: required, valid email format
- Age: required, 18–100
- User Type: must be ADMIN or CUSTOMER

Invalid input returns `400 Bad Request` with a `details` array listing each failed field.

## Business rules (enforced in `UserService`)

| Rule | Result |
|---|---|
| Email must be unique across all users | 409 Conflict |
| ADMIN must be at least 21 years old | 400 Bad Request |
| Max 5 ADMIN users allowed at once | 409 Conflict |
| ADMIN users cannot be deleted via the API | 409 Conflict |
| Requesting a non-existent user ID | 404 Not Found |

## Manual test results (Postman, run against local PostgreSQL)

All scenarios below were executed manually and passed:

- ✅ Create a CUSTOMER → 201 Created, user persisted with generated ID
- ✅ Create an ADMIN → 201 Created
- ✅ Get all users → 200 OK, returns array including created users
- ✅ Get user by ID → 200 OK, correct user returned
- ✅ Update a user (PUT) → 200 OK, fields updated correctly
- ✅ Delete a CUSTOMER → 204 No Content
- ✅ Empty/too-short name → 400 Bad Request, field-level message returned
- ✅ Invalid email format → 400 Bad Request, field-level message returned
- ✅ Invalid age (below 18) → 400 Bad Request, field-level message returned
- ✅ Invalid user type value (e.g. "MANAGER") → 400 Bad Request
- ✅ Create a duplicate-email user → 409 Conflict
- ✅ Create an ADMIN under 21 (age 19) → 400 Bad Request
- ✅ Create 5 valid ADMIN users (age ≥ 21, unique emails) → all 201 Created
- ✅ Create a 6th ADMIN → 409 Conflict (max ADMIN cap enforced)
- ✅ Delete an ADMIN user → 409 Conflict (deletion blocked)
- ✅ Get / update / delete a non-existent user ID → 404 Not Found
- ✅ Malformed/non-numeric ID in the URL path → 400 Bad Request

Data was confirmed to persist correctly in PostgreSQL across requests (Hibernate
auto-created the `users` table, including the unique constraint on `email`).

All scenarios listed in the original task's Section 9 (Successful, Validation,
and Business scenarios) have been executed and verified.

## Setup

1. Create the database: `CREATE DATABASE user_management_db;`
2. Update `src/main/resources/application.properties` with your local
   PostgreSQL username/password.
3. Run: `mvn spring-boot:run`
4. API is available at `http://localhost:8080`