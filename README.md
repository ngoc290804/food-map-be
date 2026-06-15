# Food Map Backend Base

Base backend Spring Boot scaffolded from the PDF `Base-be-hoan-thien-java-spring-boot.pdf`.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Swagger / OpenAPI
- Maven

## Main Structure

- `common`: standard response, exceptions, base entity, utils
- `config`: CORS, JWT properties, upload properties, OpenAPI
- `security`: JWT filter, token provider, security config, user details
- `modules/auth`: register, login, me
- `modules/user`: entities and repositories for `User`, `Role`
- `modules/restaurant`: CRUD + search + paging
- `modules/menu`: CRUD + filter + list by restaurant
- `modules/chatbot`: ask endpoint and chat history
- `modules/recommendation`: simple suggestion endpoints
- `modules/upload`: local image upload and file serving

## Main APIs

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/restaurants`
- `GET /api/restaurants/{id}`
- `POST /api/restaurants`
- `PUT /api/restaurants/{id}`
- `DELETE /api/restaurants/{id}`
- `GET /api/menu-items`
- `GET /api/menu-items/{id}`
- `GET /api/restaurants/{restaurantId}/menu-items`
- `POST /api/menu-items`
- `PUT /api/menu-items/{id}`
- `DELETE /api/menu-items/{id}`
- `POST /api/chatbot/ask`
- `GET /api/chatbot/history`
- `POST /api/recommendations/suggest`
- `GET /api/recommendations/popular`
- `POST /api/uploads/image`

## Configuration

Default config is in `src/main/resources/application.yml`.

Secrets are not committed to Git. For local development, copy `.env.example` to `.env` and fill in real values. For Render, set the same values in the service Environment settings.

Required environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`

Optional environment variables:

- `PORT`
- `JWT_EXPIRATION`
- `UPLOAD_DIR`
- `CORS_ALLOWED_ORIGINS`
- `CLOUDINARY_FOLDER`
- `NOMINATIM_USER_AGENT`
- `OPENAI_API_KEY`
- `OPENAI_MODEL`
- `OPENAI_BASE_URL`
- `OPENAI_MAX_RESTAURANTS`

## Database

The backend uses PostgreSQL. Production is configured through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, so Neon or another managed PostgreSQL provider can be used without code changes.

Use the JDBC URL format for Spring Boot:

```properties
DB_URL=jdbc:postgresql://your-host/your-database?sslmode=require
```

Do not commit real database passwords or API keys. Keep them in `.env` locally and in Render Environment Variables in production.

## Run

1. Install Java 17+.
2. Copy `.env.example` to `.env` and fill in real values.
3. Make sure the configured PostgreSQL database already has the required schema/data, or enable and run Flyway migrations intentionally.
4. Run `.\mvnw.cmd spring-boot:run` on Windows or `./mvnw spring-boot:run` on macOS/Linux.
5. Open Swagger at `http://localhost:8080/swagger-ui.html`.

## Render

- Build command: `./mvnw clean package -DskipTests`
- Start command: `java -jar target/backend-1.0.0.jar`
- Runtime: Java 17, pinned by `system.properties`
- Set all required environment variables in Render before deploying.
- Set `CORS_ALLOWED_ORIGINS` to the real frontend URL, for example `https://your-frontend.onrender.com`.

## Build

- Windows: `.\mvnw.cmd clean package`
- macOS/Linux: `./mvnw clean package`
- If you want to use the global `mvn` command instead of the wrapper, reopen the terminal after setting up Maven so the updated `PATH` is reloaded.

## Suggested Next Steps

- Add refresh token and logout
- Add `createdBy` and `updatedBy` audit
- Add dedicated mapper layer if you want stricter separation
- Add service/controller tests
- Move chatbot and recommendation logic to real AI integrations
