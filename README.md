# Chaletta Performance — Backend API

A Spring Boot REST API for automated tracking of Warcraft III DotA performance statistics. Fetches match data from the RGC ladder, stores it in MySQL, and exposes endpoints for the frontend.

---

## Stack

- Java 21
- Spring Boot 4
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- MySQL
- Scheduled Tasks (match sync every 15 seconds)
- Jackson (JSON parsing)

---

## Prerequisites

- Java 21+
- Maven
- MySQL 8+

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/youruser/chalettaperformance.git
cd chalettaperformance
```

### 2. Create the database

```sql
CREATE DATABASE chalettaperformancedb;
```

### 3. Configure properties

Open `src/main/resources/application.properties` and fill in all `YOUR_*` values:

```
spring.datasource.url=jdbc:mysql://localhost:3306/chalettaperformancedb
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
jwt.secret=YOUR_SECRET_KEY_AT_LEAST_32_CHARACTERS_LONG
app.cors.allowed-origins=http://localhost:5173
app.default-user.username=YOUR_ADMIN_USERNAME
app.default-user.password=YOUR_ADMIN_PASSWORD
app.rgc.room=YOUR_ROOM_ID
```

### 4. Run

```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.
On first run, the default admin user will be created automatically.

---

## API Endpoints

### Public
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/home` | Full homepage data (summary, leaderboard, heroes, titles, recent matches) |
| GET | `/api/stats/overall` | Overall player statistics |
| GET | `/api/stats/weekly?from=&to=` | Weekly player statistics |
| GET | `/api/stats/heroes` | Hero statistics with best player per hero |
| GET | `/api/stats/heroes/player/{username}` | Hero stats for a specific player |
| GET | `/api/matches?page=&size=&from=&to=` | Paginated match history |
| GET | `/api/matches/{id}` | Single match |
| GET | `/api/match-players/match/{gameId}` | Players in a match |
| GET | `/api/match-players/player/{playerId}` | Matches for a player |
| GET | `/api/players` | All registered players |
| GET | `/api/players/{id}` | Single player |
| GET | `/api/weekly-titles/week/{weekStart}` | Titles for a specific week |
| GET | `/api/weekly-titles/range?from=&to=` | Titles within a date range |

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and receive JWT token |

### Admin (requires `ROLE_ADMIN`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | List all users |
| POST | `/admin/users` | Create a user |
| PUT | `/admin/users/{id}` | Update a user |
| DELETE | `/admin/users/{id}` | Delete a user |
| GET | `/admin/title-definitions` | List title definitions |
| POST | `/admin/title-definitions` | Create a title definition |
| PUT | `/admin/title-definitions/{id}` | Update a title definition |
| DELETE | `/admin/title-definitions/{id}` | Delete a title definition |
| POST | `/admin/tools/assign-titles?weekStart=YYYY-MM-DD` | Manually trigger weekly title assignment |

---

## Authentication

All protected endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer YOUR_JWT_TOKEN
```

Obtain a token by calling `POST /auth/login` with your credentials.

---

## Roles

| Role | Access |
|------|--------|
| `ROLE_ADMIN` | Full access to all endpoints |
| `ROLE_MANAGER` | Access to `/manager/**` and `/api/**` |
| `ROLE_USER` | Access to `/api/**` only |

---

## Scheduler

The application automatically syncs match data from the RGC API every 15 seconds.
Only games from the current year with 6+ players are stored.
Games containing at least one registered player are saved.

Weekly titles are assigned every Sunday at midnight based on `title_definitions`.

---

## Project Structure

```
src/main/java/com/chaletta/chalettaperformance/
├── config/          — Security, CORS, data initializer
├── controller/      — REST controllers
├── dto/             — Data transfer objects
│   └── stats/       — Stats DTOs
├── model/           — JPA entities
├── repository/      — Spring Data repositories
├── scheduler/       — Scheduled tasks
├── security/        — JWT filter and utilities
└── service/         — Business logic
    └── external/    — RGC API sync services
```