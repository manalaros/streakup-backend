# StreakUp: Usage and Consumption Guide

## 1. Project overview

StreakUp is a Spring Boot 4.1 API built with Java 17, PostgreSQL, Spring Data JPA, Spring Security, and JWT. It manages users, activities, streaks, friendships, and a weekly leaderboard.

The OpenAPI specification is available in [`openapi.yaml`](./openapi.yaml). You can open it in [Swagger Editor](https://editor.swagger.io/) or import it into Postman.

## 2. Running the backend

### Docker

From the project root:

```bash
cp .env.example .env
docker compose up --build
```

The API is available at `http://localhost:8080` and PostgreSQL at `localhost:5433`.

Stop the services with:

```bash
docker compose down
```

### Local PostgreSQL

If PostgreSQL is already available at `127.0.0.1:5433`:

```bash
./mvnw spring-boot:run
```

The application uses `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `APP_TIMEZONE`. Hibernate maintains the schema with `spring.jpa.hibernate.ddl-auto=update`.

Check the API:

```bash
curl -i http://localhost:8080/api/health
```

It should return `200` with `StreakUp backend is running`.

Run the automated tests:

```bash
./mvnw test -q
```

## 3. Authentication flow

1. Register a user with `POST /api/users`.
2. Log in with `POST /api/auth/login`.
3. Store the `token` field securely.
4. Send the token on every protected request:

```http
Authorization: Bearer <TOKEN>
```

The token lasts 24 hours by default (`JWT_EXPIRATION_MINUTES=1440`). Store it in the iOS Keychain or Flutter Secure Storage, never as plain text.

## 4. Basic examples

### Register

```bash
curl -i -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"username":"maria","email":"maria@example.com","password":"password123"}'
```

### Log in

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"maria@example.com","password":"password123"}'
```

### Create an activity

```bash
curl -i -X POST http://localhost:8080/api/activities \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"type":"RUNNING","date":"2026-09-20","durationMinutes":30,"distanceKm":5.2,"notes":"Morning workout"}'
```

Allowed activity types are `WALKING`, `RUNNING`, `CYCLING`, `SWIMMING`, `GYM`, `STRENGTH`, `YOGA`, `MEDITATION`, `STUDY`, `READING`, and `OTHER`. Dates use `YYYY-MM-DD`; the statistics week runs from Monday to Sunday according to `APP_TIMEZONE`.

### Query data

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/me
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/activities
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/statistics
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/leaderboard/weekly
```

## 5. Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Health check |
| `POST` | `/api/users` | Register |
| `POST` | `/api/auth/register` | Alternative registration endpoint |
| `POST` | `/api/auth/login` | Login and JWT |
| `GET` | `/api/users/me` | Authenticated user |
| `GET/POST` | `/api/activities` | List/create activities |
| `GET/PUT/PATCH/DELETE` | `/api/activities/{id}` | Get/update/delete an activity |
| `GET` | `/api/statistics` | Own statistics |
| `GET` | `/api/leaderboard/weekly` | Weekly leaderboard |
| `GET` | `/api/friends/search?q=...` | Search users |
| `POST` | `/api/friends/requests/{userId}` | Send a friend request |
| `GET` | `/api/friends/requests` | Received requests |
| `POST` | `/api/friends/requests/{requestId}/accept` | Accept a request |
| `GET` | `/api/friends` | Accepted friends |
| `GET` | `/api/users/{id}/activities` | Own or accepted friend's activities |
| `GET` | `/api/users/{id}/statistics` | Own or accepted friend's statistics |

Another user's activities and statistics are available only to accepted friends. The leaderboard contains the authenticated user and accepted friends.

## 6. Responses and errors

| Code | Meaning |
| ---: | --- |
| `200` | Successful query or update |
| `201` | Resource created |
| `204` | Resource deleted |
| `400` | Invalid JSON or validation error |
| `401` | Missing/invalid token or invalid credentials |
| `403` | Resource is not visible to the user |
| `404` | Resource not found |
| `409` | Conflict or duplicate |

Errors use this shape:

```json
{
  "timestamp": "2026-09-20T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/activities",
  "fields": {
    "durationMinutes": "must be greater than or equal to 1"
  }
}
```

A client should clear the session and return to the login screen after receiving `401`.

## 7. iOS client

Use `URLSession` or Alamofire, `Codable` models, and Keychain for the JWT. Configure the decoder for `createdAt` and `updatedAt` as ISO-8601. An activity's `date` is a `yyyy-MM-dd` string, not a timestamp.

URLs:

| Environment | Base URL |
| --- | --- |
| iOS Simulator | `http://localhost:8080` |
| Physical iPhone | `http://YOUR_COMPUTER_IP:8080` |
| Production | `https://api.example.com` |

The phone and computer must be on the same local network during testing.

Suggested structure:

```text
Models/
Networking/
Services/
Storage/
Features/
```

Centralize requests in an `APIClient` that adds the Bearer token and maps HTTP errors to a shared error type.

## 8. Flutter client

Recommended dependencies:

```yaml
dependencies:
  dio: ^5.0.0
  flutter_secure_storage: ^9.0.0
```

URLs:

| Environment | Base URL |
| --- | --- |
| Android Emulator | `http://10.0.2.2:8080` |
| iOS Simulator | `http://localhost:8080` |
| Physical device | `http://YOUR_COMPUTER_IP:8080` |

Use a Dio interceptor to add:

```http
Authorization: Bearer <TOKEN>
```

Use `flutter_secure_storage` for the token and clear the session after a `401`. Use HTTPS in production; HTTP should be limited to local development.

## 9. Running continuously

During development, everything runs on your computer: the backend, PostgreSQL, and optionally the simulators. To keep it running for free, you can leave Docker Compose running, use your own computer/Raspberry Pi, or deploy to a free service subject to its limits. Production also requires HTTPS, backups, secret rotation, persistent storage, and monitoring.

## 10. Initial data

With the API running, populate demo data with:

```bash
./scripts/seed-data.sh
```

The script creates two demo users, several activities, and an accepted friendship. Override the URL with `API_URL=http://localhost:8080 ./scripts/seed-data.sh`.
