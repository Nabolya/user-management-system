# User Management System

Spring Boot REST API for managing users (ADMIN / CUSTOMER), backed by PostgreSQL via Spring Data JPA.

## Structure

```
controller/  -> UserController        REST endpoints
service/     -> UserService            business logic + rules
repository/  -> UserRepository         Spring Data JPA
entity/      -> User, UserType         JPA entity + enum
dto/         -> UserRequest, UserResponse   request validation / response shape
exception/   -> custom exceptions + GlobalExceptionHandler (@RestControllerAdvice)
```

## Setup

1. Create the database:
   ```sql
   CREATE DATABASE user_management_db;
   ```
2. Update `src/main/resources/application.properties` with your PostgreSQL username/password.
3. Run:
   ```
   mvn spring-boot:run
   ```
   API starts on `http://localhost:8080`.

## Endpoints

| Method | Path              | Description       | Success |
|--------|-------------------|--------------------|---------|
| POST   | /api/users        | Create user        | 201     |
| GET    | /api/users        | Get all users       | 200     |
| GET    | /api/users/{id}   | Get user by ID     | 200     |
| PUT    | /api/users/{id}   | Update user        | 200     |
| DELETE | /api/users/{id}   | Delete user        | 204     |

### Sample request body

```json
{
  "name": "Youssef Ahmed",
  "email": "youssef@example.com",
  "age": 22,
  "userType": "CUSTOMER"
}
```

## Business rules (enforced in UserService)

- **Unique email** — checked on create and update -> `409 Conflict`
- **ADMIN age ≥ 21** -> `400 Bad Request`
- **Max 5 ADMIN users** — checked on create, and on update only when a CUSTOMER is being promoted to ADMIN -> `409 Conflict`
- **ADMIN cannot be deleted** -> `409 Conflict`

## Error response shape

```json
{
  "timestamp": "2026-08-14T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 99",
  "path": "/api/users/99"
}
```
Validation failures additionally include a `details` array with one message per invalid field.

## Postman testing checklist

**Successful**
- Create a CUSTOMER / ADMIN
- Get all users
- Get user by ID
- Update a user
- Delete a CUSTOMER

**Validation (400)**
- Empty name, invalid email, invalid age, invalid user type

**Business rules**
- Duplicate email -> 409
- ADMIN under 21 -> 400
- 6th ADMIN -> 409
- Delete an ADMIN -> 409
- Get/update/delete a non-existent ID -> 404
