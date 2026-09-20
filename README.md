# StreakUp backend

Spring Boot API for accounts, activities, streaks, friendships, and weekly leaderboards.

## Documentation

- [OpenAPI specification](docs/openapi.yaml)
- [Guide for running and consuming the API](docs/consumption-guide.md)

## Run locally

```bash
cp .env.example .env
docker compose up --build
```

The API is available at `http://localhost:8080`; PostgreSQL is exposed on port `5433`.

## API

- `POST /api/users` and `POST /api/auth/login`
- `GET|POST /api/activities`, `PUT|DELETE /api/activities/{id}`
- `GET /api/statistics` and `GET /api/leaderboard/weekly`
- `GET /api/friends/search?q=`, `POST /api/friends/requests/{userId}`
- `GET /api/friends/requests`, `POST /api/friends/requests/{id}/accept`
- `GET /api/friends`
- `GET /api/users/{id}/activities` and `/api/users/{id}/statistics` for accepted friends

Authenticated endpoints use `Authorization: Bearer <token>`. Activity dates are interpreted using
`APP_TIMEZONE`, and the weekly period runs Monday through Sunday.

## Seed data

With the API running, create demo users, activities, and an accepted friendship:

```bash
./scripts/seed-data.sh
```

The demo credentials are `demo.alice@example.com` and `demo.bob@example.com`, both
with password `password123`. Override the API URL with `API_URL=...`.
