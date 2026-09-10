# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A small LMS (Learning Management System) with two deployable units — see `REQUIREMENTS.md` at the repo root for the full requirements/architecture doc, ADRs, and known-risk register. Don't duplicate that content here; this file is command/orientation-focused.

- `lms-backend/` — Spring Boot 3.3.4 (Java 17) REST API. No automated tests exist yet.
- `frontend/` — Next.js 16 (App Router, TypeScript, Tailwind v4), a pure client of the API (no server-side session, no BFF).

## Commands

### Backend — run from `lms-backend/`

```bash
# Build
./mvnw clean install          # or `mvn clean install` if using a system Maven

# Run the app (starts on port 8080)
./mvnw spring-boot:run

# Run tests (none exist yet, but this is the standard entry point)
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

The database is Supabase-hosted PostgreSQL (direct connection, `db.<ref>.supabase.co:5432`, `sslmode=require`) — see `src/main/resources/application.properties`. Direct connections are IPv6-only unless the project has the IPv4 add-on; if it won't connect, switch to the Session Pooler host from the Supabase dashboard instead. The password is never hardcoded — it's read from the `DB_PASSWORD` environment variable (`spring.datasource.password=${DB_PASSWORD}`); export it before running, or use an untracked `application-local.properties` with `--spring.profiles.active=local`.

Schema is managed by **Flyway**, not Hibernate — `spring.jpa.hibernate.ddl-auto=validate` means Hibernate only checks entities against the schema and never mutates it. Migrations live in `src/main/resources/db/migration/` (`V1__init_schema.sql` is the baseline). To change the schema: add a new `Vn__description.sql` file there first, then update the matching entity to agree with it — never the other way around, and never edit an already-applied migration file. Flyway runs automatically on startup; `spring.flyway.baseline-on-migrate=true` lets it adopt a database that already has this schema (e.g. one seeded by hand or by a prior `ddl-auto=update` run) without erroring.

### Frontend — run from `frontend/`

```bash
cp .env.local.example .env.local   # NEXT_PUBLIC_API_URL, defaults to http://localhost:8080
npm install
npm run dev      # dev server
npm run build    # production build (also the TypeScript check)
npx eslint src   # lint
```

The frontend needs the backend running to load real data; without it, pages render their error/loading states.

## Architecture

Standard layered Spring Boot structure under `com.lms`:

- `controller/` — REST endpoints (`/api/auth`, `/api/courses`, `/api/enrollments`). Controllers are thin; they delegate to services and pull the authenticated user's identity from Spring's `Authentication` (the principal name is the user's email — see auth flow below).
- `service/` — business logic. `AuthService`, `CourseService`, `EnrollmentService`.
- `repository/` — Spring Data JPA repositories.
- `entity/` — JPA entities: `User` (with `Role` enum: `ADMIN`/`STUDENT`), `Course`, `Enrollment` (join entity with a unique `(student_id, course_id)` constraint).
- `dto/` — request/response DTOs for the controller layer (`LoginRequest`, `RegisterRequest`, `RegisterResponse`, `CourseRequest`, `CourseResponse`, `EnrollmentResponse`).
- `security/` — JWT-based stateless auth (see below).
- `resources/db/migration/` — Flyway SQL migrations; the schema's source of truth (see "Commands" above).

### Auth flow

1. `AuthController` `/api/auth/register` and `/api/auth/login` are public. `AuthService` hashes passwords with `BCryptPasswordEncoder` and validates credentials. Public self-registration always creates a `STUDENT` account — `RegisterRequest` has no `role` field, and `AuthService.register` hardcodes `Role.STUDENT`. There is currently no endpoint to provision an `ADMIN`; one must be inserted directly into the `users` table (bcrypt-hashed password) until that's built.
2. On successful login, `JwtUtil.generateToken(email, role)` issues a JWT (HS256, 1-hour expiry) with the user's email as subject and role as a claim.
3. `JwtAuthFilter` (a `OncePerRequestFilter`) runs on every request, reads the `Authorization: Bearer <token>` header, validates it via `JwtUtil`, and if valid populates the `SecurityContext` with a `UsernamePasswordAuthenticationToken` whose principal is the user's **email** and whose authority is `ROLE_<role>`.
4. `SecurityConfig` wires the filter chain: stateless sessions, CSRF disabled, `/api/auth/**` and `GET /api/courses/**` are public, everything else requires authentication. `@EnableMethodSecurity` is on, so controllers use `@PreAuthorize("hasRole('ADMIN')")` / `hasRole('STUDENT')` for role gating (e.g. only `ADMIN` can create/update/delete courses, only `STUDENT` can enroll).
5. Downstream code identifies the caller via `Authentication#getName()`, which is the email — services re-look-up the `User` entity by email from there (e.g. `AuthService`/`CourseService`/`EnrollmentService` all do `userRepository.findByEmail(auth.getName())`).

**Important known gap:** `JwtUtil` generates a new random signing key (`Keys.secretKeyFor(...)`) on every application restart, and it's a comment in the code itself — this invalidates all previously issued tokens on restart and is explicitly flagged as needing to be replaced with a fixed secret from config/env before any real deployment. Keep this in mind if a task touches JWT signing.

### Access control nuance: course visibility

`GET /api/courses/{id}` is public, but `CourseService.getCourseForViewer` conditionally strips `videoUrl` from the response unless the caller is either the admin who created the course or a student enrolled in it (checked via `EnrollmentRepository.existsByStudentAndCourse`). This gating logic lives only in the service method, not in `@PreAuthorize` — don't assume unauthenticated `Authentication` will be non-null here; it's `null`-checked explicitly.

### Error handling

There is no global `@ControllerAdvice`/exception handler. Services throw plain `RuntimeException` with a message (e.g. "Course not found", "Invalid credentials", "Already enrolled in this course") and these currently propagate as raw 500s. Follow this existing pattern for consistency unless asked to introduce structured error handling.

### CORS

The frontend calls this API directly from the browser (no proxy), so it needs an explicit CORS allowlist — `SecurityConfig` registers a `CorsConfigurationSource` from `app.cors.allowed-origins` (`application.properties`, comma-separated). Add a new origin there whenever the frontend runs somewhere new (a different dev port, a deployed URL) — without it, requests fail silently in the browser (blocked client-side) even though they work fine from curl/Postman, since CORS is a browser-enforced restriction the server has to opt into per-origin.

### Response shape discipline

Every course- or enrollment-returning endpoint maps to a DTO (`CourseResponse`, `EnrollmentResponse`, `RegisterResponse`) rather than serializing JPA entities directly — this was a deliberate fix (see `REQUIREMENTS.md` §7, R3/R4) after raw-entity responses were found to leak nested `User.password` (bcrypt hash) and to over-expose `videoUrl`. `CourseService.toCourseResponse(Course, boolean includeVideoUrl)` controls whether `videoUrl` is included: `false` for anything list-shaped or public (`getAllCourses`), `true` only for the admin who just created/updated the course. `getCourseForViewer` has its own enrollment/ownership-gated logic for the single-course detail view — don't reuse `toCourseResponse` there. When adding a new endpoint that returns a `Course` or `Enrollment`, map it to a DTO the same way — never return the entity directly.
