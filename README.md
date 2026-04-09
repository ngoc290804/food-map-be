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

Default config is in [application.yml](/D:/food-map-be/src/main/resources/application.yml).

Supported environment overrides:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `UPLOAD_DIR`

## Database

The backend uses local PostgreSQL:

- Host: `localhost`
- Port: `5432`
- Database: `food_map_db`
- Username: `postgres`

## Run

1. Install Java 17+.
2. Start PostgreSQL and make sure database `food_map_db` exists.
3. Update JWT config if needed.
4. Run `.\mvnw.cmd spring-boot:run` on Windows or `./mvnw spring-boot:run` on macOS/Linux.
5. Open Swagger at `http://localhost:8080/swagger-ui.html`.

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
