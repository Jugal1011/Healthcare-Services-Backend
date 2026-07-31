# Healthcare Services Backend

1. **Users** — CRUD, Spring Security + JWT, roles `ADMIN` / `USER`, signup, login, forgot password (OTP via mobile), reset password.
2. **Patients** — CRUD, with structured `Address` (line1, line2, city, state, country, pincode).
3. **Medicines** — CRUD catalog.
4. **Visits** — a patient can have many visits; each visit stores the visit date, the list of prescribed medicines (FK to Medicine), and a list of uploaded report files.

## Tech stack (all free / open-source)

| Concern | Library |
|---|---|
| Web | Spring Boot Web (MVC) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL (default) — H2 in-memory option included for zero-setup testing |
| Auth | Spring Security + `jjwt` (JWT, Apache-2.0) |
| Password hashing | BCrypt (built into Spring Security) |
| Validation | Jakarta Bean Validation |
| DTO mapping | ModelMapper (used minimally; most mapping is explicit for clarity) |
| API docs | springdoc-openapi (Swagger UI) |
| Boilerplate reduction | Lombok |
| OTP delivery | Pluggable `SmsService` — defaults to `LogSmsServiceImpl` (logs OTP to console, **free, no account needed**). A `TwilioSmsServiceImpl` stub is included for real SMS delivery on a Twilio free-trial account — just flip `twilio.enabled=true` and add the Twilio SDK dependency (instructions in that file). |
| File storage | Local filesystem (`uploads/reports`) — free, no cloud account required. Swap `FileStorageService` for S3/R2 later if you need cloud storage. |

No paid service is required to run this end-to-end.

## Project layout

```
src/main/java/com/hospital/management/
  config/       SecurityConfig, OpenApiConfig, ModelMapperConfig, DataSeeder
  entity/       User, Role, Patient, Address, Medicine, Visit, Report, PasswordResetOtp
  repository/   Spring Data JPA repositories
  security/     JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService
  dto/          Request/response DTOs per module (auth, user, patient, medicine, visit)
  service/      Business logic
  controller/   REST controllers
  exception/    Custom exceptions + GlobalExceptionHandler (RFC-ish error JSON)
```

## Running it

### 1. Database
By default it points at PostgreSQL:
```
jdbc:postgresql://localhost:5432/hospital_db
```
Unlike MySQL, Postgres won't auto-create the database for you — create it once before first run:
```bash
psql -U postgres -c "CREATE DATABASE hospital_db;"
```
Then either match the `username`/`password` in `application.yml` to your local Postgres user, or override via env vars. The schema itself (tables/columns) is auto-created/updated via `ddl-auto: update`.

**No Postgres handy?** Open `application.yml` and swap the `datasource` block to the commented-out H2 block — everything else works unchanged (H2 dependency is already in `pom.xml`).

### 2. Build & run
```bash
mvn clean install
mvn spring-boot:run
```
App starts on `http://localhost:8080`.

### 3. Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 4. Default admin
On first boot, `DataSeeder` creates:
```
email: admin@hospital.com
password: Admin@123
```
**Change this password immediately** in any real deployment (or delete `DataSeeder` once you have a real admin provisioning process).

## Auth flow

| Endpoint | Method | Auth | Notes |
|---|---|---|---|
| `/api/auth/signup` | POST | Public | Always creates role `USER` |
| `/api/auth/login` | POST | Public | Returns `accessToken` + `refreshToken` |
| `/api/auth/forgot-password` | POST | Public | Body: `{ "mobileNumber": "..." }` — generates OTP, sends via `SmsService` |
| `/api/auth/reset-password` | POST | Public | Body: `{ "mobileNumber", "otp", "newPassword" }` |

Send the JWT on subsequent requests:
```
Authorization: Bearer <accessToken>
```

## Endpoint summary

- `GET/PUT/DELETE /api/users/**` — **ADMIN only**. `GET /api/users/me` — any authenticated user (own profile).
- `/api/patients/**` — full CRUD, any authenticated user. `GET /api/patients/search?keyword=`
- `/api/medicines/**` — full CRUD, any authenticated user. `GET /api/medicines/search?name=`
- `/api/visits/**` — create/update/delete visit (`patientId`, `visitDate`, `notes`, `medicineIds[]`), `GET /api/visits/patient/{patientId}` for a patient's full visit history.
  - `POST /api/visits/{id}/reports` (multipart `file`) — upload a report to a visit.
  - `GET /api/visits/{visitId}/reports/{reportId}/download` — download a report.
  - `DELETE /api/visits/{visitId}/reports/{reportId}` — remove a report.

Role check is enforced both at the `SecurityConfig` filter-chain level and via `@PreAuthorize` on the user-management controller (defense in depth). Patients/medicines/visits are open to any authenticated user (`USER` or `ADMIN`) since typical hospital staff need to manage them — tighten with `@PreAuthorize` per-method if you want ADMIN-only writes.
