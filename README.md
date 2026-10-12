# Field Notes Backend

A REST API for managing field notes, customers, sites, photos, and offline data sync.

## Stack

- Java 17 and Spring Boot 3.3.5
- PostgreSQL, Spring Data JPA, and Flyway
- JWT authentication

## Setup

Requirements: JDK 17+, Maven, and PostgreSQL.

Create a database:

```sql
CREATE DATABASE fieldnotes;
```

Set connection details as needed (defaults: `postgres` / `postgres`):

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/fieldnotes"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "your-password"
$env:JWT_SECRET = "use-a-secure-secret-of-at-least-32-bytes"
```

Run the application:

```powershell
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. Flyway applies database migrations on startup. For production, set a secure `JWT_SECRET`; do not use the development default.

Run tests and verification:

```powershell
mvn clean verify
```

## API

Protected endpoints require `Authorization: Bearer <token>`.

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register an account |
| `POST` | `/api/auth/login` | Log in and receive a JWT |
| `GET` | `/api/customers` | List customers |
| `PUT` / `DELETE` | `/api/customers/{id}` | Create/update or delete a customer |
| `GET` | `/api/sites?customerId={uuid}` | List sites; optionally filter by customer |
| `PUT` / `DELETE` | `/api/sites/{id}` | Create/update or delete a site |
| `GET` | `/api/notes?q={text}&status={status}&siteId={uuid}` | Search and filter notes |
| `PUT` / `DELETE` | `/api/notes/{id}` | Create/update or delete a note |
| `POST` | `/api/notes/{id}/photo` | Upload a photo (`multipart/form-data`, field `file`) |
| `GET` | `/api/photos/{filename}` | Retrieve a photo |
| `GET` | `/api/sync/pull?since={timestamp}` | Pull changes since an ISO-8601 timestamp |

IDs are UUIDs. Deletes are soft deletes; sync includes deleted records. Note statuses are `DRAFT`, `IN_PROGRESS`, and `COMPLETED`. Photo uploads accept JPG, JPEG, PNG, or WebP up to 10 MB.

## API docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
