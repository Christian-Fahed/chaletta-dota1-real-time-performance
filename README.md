# Chaletta Performance — Backend API

> ⚠️ **Disclaimer:** This project was built purely out of curiosity and for personal use within a private gaming community. It is not affiliated with, endorsed by, or connected to RankedGaming.com or any related entity. We make no claim of ownership over any data retrieved from external APIs. Use at your own risk. The authors take no legal responsibility for any misuse of this software or its data.

---

Chaletta Performance is a self-hosted statistics tracker for a **single private Warcraft III DotA room** (Room 107) on the RGC ladder. It automatically pulls match data from the RGC public API, persists it in a local MySQL database, and serves it through a REST API consumed by a custom frontend and admin dashboard.

The project was built to give a small group of players a dedicated space to track their performance, compete for weekly titles, and relive their match history — something the RGC platform itself does not offer at this level of detail.

**This is not a general-purpose tool.** It is hardcoded for a specific room and a specific set of registered players. If a match contains none of the registered players, it is ignored entirely.
---

## Stack

- Java 21
- Spring Boot 4
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- MySQL 8
- Scheduled Tasks (match sync every 15 seconds, weekly title assignment every Sunday)
- Jackson (JSON parsing)
- Lombok

---

## Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8+

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/Christian-Fahed/chalettaperformance.git
cd chalettaperformance
```

### 2. Create the database

```sql
CREATE DATABASE chalettaperformancedb;
```

### 3. Configure properties

Open `src/main/resources/application.properties` and fill in all `YOUR_*` values:

```properties
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
On first run, the default admin user is created automatically.

---

## Database Schema

The application auto-creates all tables on first run via `spring.jpa.hibernate.ddl-auto=update`.

| Table | Description |
|-------|-------------|
| `users` | Admin panel users with roles |
| `players` | Registered DotA players tracked from RGC |
| `matches` | Match records fetched from RGC API |
| `match_players` | Per-player stats for each match (kills, deaths, assists, hero) |
| `title_definitions` | Admin-defined weekly award titles and their metrics |
| `weekly_titles` | Assigned weekly titles per player per week |

---

## API Endpoints

### Public — No authentication required

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/home` | Full homepage data (summary, leaderboard, heroes, titles, recent matches) |
| GET | `/api/stats/overall` | Overall player statistics sorted by win rate |
| GET | `/api/stats/weekly?from=&to=` | Weekly player statistics for a Unix timestamp range |
| GET | `/api/stats/heroes` | Hero statistics with best player per hero |
| GET | `/api/stats/heroes/player/{username}` | All hero stats for a specific player |
| GET | `/api/stats/leaderboard/kills` | Kills leaderboard |
| GET | `/api/matches?page=&size=&from=&to=` | Paginated match history with optional date filter |
| GET | `/api/matches/{id}` | Single match by game ID |
| GET | `/api/match-players/match/{gameId}` | All players and stats for a specific match |
| GET | `/api/match-players/player/{playerId}` | All matches for a specific player |
| GET | `/api/players` | All registered players |
| GET | `/api/players/{id}` | Single player by ID |
| GET | `/api/weekly-titles` | All weekly titles |
| GET | `/api/weekly-titles/player/{playerId}` | Weekly titles for a specific player |
| GET | `/api/weekly-titles/week/{weekStart}` | Titles for a specific week (YYYY-MM-DD) |
| GET | `/api/weekly-titles/range?from=&to=` | Titles within a date range (YYYY-MM-DD) |

### Auth — No authentication required

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user (`username`, `password`, `role`) |
| POST | `/auth/login` | Login and receive JWT token |

### Admin — Requires `ROLE_ADMIN`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | List all users |
| POST | `/admin/users` | Create a user |
| PUT | `/admin/users/{id}` | Update a user |
| DELETE | `/admin/users/{id}` | Delete a user |
| GET | `/admin/title-definitions` | List all title definitions |
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
| `ROLE_ADMIN` | Full access to all endpoints including `/admin/**` |
| `ROLE_MANAGER` | Access to `/manager/**` and `/api/**` |
| `ROLE_USER` | Access to `/api/**` only |

---

## Scheduler

| Task | Schedule | Description |
|------|----------|-------------|
| Match sync | Every 15 seconds | Fetches new games from RGC API and stores them |
| Weekly titles | Every Sunday at midnight | Assigns titles based on title definitions |

**Sync rules:**
- Only games from the current year are stored
- Games with less than 6 players are skipped
- Games with no registered players are skipped
- Sync stops when a game that already exists in the DB is encountered

**Title assignment rules:**
- Metrics supported: `kills`, `deaths`, `assists`, `wins`, `losses`, `games_played`
- Aggregations supported: `SUM`, `MAX`, `MIN`
- `min_games` threshold must be met for a player to qualify

---

## Project Structure

```
src/
└── main/
    ├── java/com/chaletta/chalettaperformance/
    │   │
    │   ├── ChalettaperformanceApplication.java   — Entry point, @EnableScheduling
    │   │
    │   ├── config/
    │   │   ├── AppConfig.java                    — RestTemplate bean
    │   │   ├── CorsConfig.java                   — CORS filter
    │   │   ├── DataInitializer.java              — Creates default admin user on startup
    │   │   ├── GlobalExceptionHandler.java       — Handles BadCredentialsException etc.
    │   │   └── SecurityConfig.java               — JWT filter chain, role-based access
    │   │
    │   ├── controller/
    │   │   ├── AuthController.java               — /auth/register, /auth/login
    │   │   ├── AdminToolsController.java         — /admin/tools/assign-titles
    │   │   ├── MatchController.java              — /api/matches
    │   │   ├── MatchPlayerController.java        — /api/match-players
    │   │   ├── PlayerController.java             — /api/players
    │   │   ├── StatsController.java              — /api/stats
    │   │   ├── HomeController.java               — /api/home
    │   │   ├── TitleDefinitionController.java    — /admin/title-definitions
    │   │   ├── UserController.java               — /admin/users
    │   │   └── WeeklyTitleController.java        — /api/weekly-titles
    │   │
    │   ├── dto/
    │   │   ├── AuthResponse.java                 — token, username, role
    │   │   ├── LoginRequest.java                 — username, password
    │   │   ├── RegisterRequest.java              — username, password, role
    │   │   └── stats/
    │   │       ├── HomePageDto.java              — Full home page payload
    │   │       ├── HeroPlayerStatsDto.java       — Hero stats with best player
    │   │       ├── LeaderboardEntryDto.java      — Single leaderboard entry
    │   │       ├── OverallSummaryDto.java        — Global summary stats
    │   │       ├── PlayerOverallStatsDto.java    — Per-player overall stats
    │   │       ├── PlayerWeeklyStatsDto.java     — Per-player weekly stats
    │   │       ├── RecentMatchDto.java           — Recent match summary
    │   │       ├── RecentMatchPlayerDto.java     — Player entry in recent match
    │   │       └── WeeklyTitleDto.java           — Weekly title entry
    │   │
    │   ├── model/
    │   │   ├── Match.java                        — game_id, started_at, duration, winner, status
    │   │   ├── MatchPlayer.java                  — match, player, hero, kills, deaths, assists
    │   │   ├── Player.java                       — player_id, uuid, username
    │   │   ├── TitleDefinition.java              — title_name, metric, aggregation, min_games
    │   │   ├── User.java                         — id, username, password, role (ADMIN/MANAGER/USER)
    │   │   └── WeeklyTitle.java                  — player, week_start, title_name, value
    │   │
    │   ├── repository/
    │   │   ├── MatchPlayerRepository.java        — Custom JPQL queries for stats aggregation
    │   │   ├── MatchRepository.java              — Pagination, date range filtering
    │   │   ├── PlayerRepository.java             — findByUuid, totalPlayers
    │   │   ├── TitleDefinitionRepository.java    — findByTitleName
    │   │   ├── UserRepository.java               — findByUsername
    │   │   └── WeeklyTitleRepository.java        — findByWeekStart, findByWeekStartBetween
    │   │
    │   ├── scheduler/
    │   │   └── DataScheduler.java                — @Scheduled match sync + weekly title assignment
    │   │
    │   ├── security/
    │   │   ├── JwtFilter.java                    — OncePerRequestFilter, validates Bearer token
    │   │   ├── JwtUtil.java                      — generateToken, extractUsername, validateToken
    │   │   └── UserDetailsServiceImpl.java       — Loads user from DB for Spring Security
    │   │
    │   └── service/
    │       ├── AuthService.java                  — register, login
    │       ├── HomePageService.java              — Assembles full home page DTO
    │       ├── MatchPlayerService.java           — CRUD for match players
    │       ├── MatchService.java                 — Paginated match retrieval
    │       ├── PlayerService.java                — CRUD for players
    │       ├── StatsService.java                 — Overall stats, weekly stats, hero stats
    │       ├── TitleDefinitionService.java       — CRUD for title definitions
    │       ├── UserService.java                  — CRUD for users with password encoding
    │       ├── WeeklyTitleAssignmentService.java — Assigns weekly titles based on definitions
    │       ├── WeeklyTitleService.java           — Read weekly titles
    │       └── external/
    │           ├── ExternalApiClient.java        — HTTP POST to RGC API, returns JsonNode
    │           ├── GameSyncService.java          — Orchestrates full sync loop with pagination
    │           ├── MatchIngestionService.java    — Creates and saves Match entities
    │           ├── MatchPlayerIngestionService.java — Creates and saves MatchPlayer entities
    │           └── PlayerIngestionService.java   — Resolves or creates Player entities
    │
    └── resources/
        ├── application.properties                        — Fill in YOUR_* values before running
        └── application-production.properties.example    — Template for production deployment
```
