# Kotlin Spring REST API Starter

A reusable REST API starter with **Spring Boot 3.4.5 + Kotlin 2.1.21 + Java 21**.

## Features

- Spring MVC + Jakarta Validation
- PostgreSQL + Spring Data JPA + Flyway
- JWT login/register (HS256), BCrypt password hashing
- Per-user authorization for sample Notes CRUD
- Consistent success/error responses and HTTP status codes
- Trace ID header (`X-Trace-Id`), paginated endpoints
- Swagger UI / OpenAPI, Actuator health
- Docker Compose for local PostgreSQL
- JUnit 5 unit tests + GitHub Actions CI

> **Starter, not production-ready identity platform.** Registration is open for demo purposes. Before production add rate limiting, email verification, refresh-token policy, key rotation, secret management, audit logging, stronger integration tests, and an explicit multi-tenant authorization model.

## Prerequisites

- JDK 21 (`java -version`)
- Docker Desktop with Compose
- macOS/Linux: `curl`, `unzip` (Windows: PowerShell)
- Internet access on first build to download Gradle and Maven dependencies

This repository includes a **small Gradle bootstrap script** named `gradlew` (not the official Gradle wrapper JAR). It downloads Gradle 8.14.3 on first run into ignored `.gradle-dist/`. For stronger supply-chain controls, replace it with the official Gradle Wrapper and verify checksums before production use.

## Run locally

```bash
git clone https://github.com/YOUR_USERNAME/kotlin-spring-rest-starter.git
cd kotlin-spring-rest-starter
chmod +x gradlew
docker compose up -d
./gradlew bootRun
```

No GitHub repository yet? Download/unzip the project and run the last three commands inside its folder.

- API health: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

Default database credentials are **local development only**. Set environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` for deployment. JWT_SECRET must be 32+ UTF-8 bytes and must be randomly generated.

## Test with curl

Register:

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"ChangeMe123!"}'
```

Login:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"ChangeMe123!"}'
```

Copy `data.accessToken` from the login response, then:

```bash
export TOKEN='PASTE_ACCESS_TOKEN_HERE'
curl -i -X POST http://localhost:8080/api/v1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"First note","content":"Hello Kotlin"}'
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/notes
```

Other Notes routes: `GET /api/v1/notes/{id}`, `PUT /api/v1/notes/{id}`, `DELETE /api/v1/notes/{id}`.

## Build / test

```bash
./gradlew test
./gradlew bootJar
```

Jar: `build/libs/kotlin-spring-rest-starter-0.1.0.jar`

## API example

Success: `{"success":true,"data":{"id":1,"title":"First note","content":"Hello Kotlin"}}`

Error: `{"success":false,"error":{"code":"NOTE_NOT_FOUND","message":"Note not found","details":[]},"traceId":"..."}`

## Security and architecture notes

- Access tokens expire after 60 minutes; no refresh token in MVP.
- Notes are scoped to the authenticated user (`owner_id`).
- Tenant isolation is **not implemented**. For a multi-tenant SaaS, add tenant membership, building assignment, and repository-level authorization before using real customer data.
- Register/login endpoints are public; disable public registration or add invitation/email verification before deploying publicly.
- API keys/passwords must never be committed to GitHub.
- No external AI API keys or paid services are required.

MIT license.
