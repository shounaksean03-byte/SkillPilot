# Skillpilot

Skillpilot is a small Learning Management System with a Java Spring Boot backend and a Next.js frontend. It supports course browsing, enrollment, and admin course management.

## Project overview

- Backend: Spring Boot 3.3.4, Java 17, PostgreSQL, JWT authentication
- Frontend: Next.js 16, TypeScript, Tailwind CSS
- Database: Supabase-hosted PostgreSQL via Flyway-managed schema
- Main apps:
  - LMS API at `lms-backend/`
  - Client web app at `frontend/`

## Features

- User registration and login
- Role-based access control (`ADMIN` and `STUDENT`)
- Public course listing and course detail views
- Student course enrollment
- Admin course create, update, and delete flows
- JWT-authenticated API requests
- DTO-based API responses to avoid leaking sensitive data

## Repository structure

```text
Skillpilot/
├── AGENTS.md
├── CLAUDE.md
├── REQUIREMENTS.md
├── README.md
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── ...
├── lms-backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
└── skills-lock.json
```

## Backend setup

From `lms-backend/`:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The API runs on port `8080` by default.

Required environment for the database:

```bash
export DB_PASSWORD="your-db-password"
```

The application expects PostgreSQL connection values defined in `src/main/resources/application.properties` and uses Flyway for schema management.

### Backend test command

```bash
./mvnw test
```

## Frontend setup

From `frontend/`:

```bash
cp .env.local.example .env.local
npm install
npm run dev
```

The frontend also supports:

```bash
npm run build
npx eslint src
```

## Authentication and security

- Public routes include auth endpoints and public course reads.
- JWTs are issued after successful login.
- API requests include the token in the `Authorization: Bearer <token>` header.
- Security policies enforce role checks for admin/student actions.

## Useful references

- `REQUIREMENTS.md` — product and architecture requirements
- `AGENTS.md` — repository guidance and command reference
- `CLAUDE.md` — additional project notes for Claude Code usage

## Notes

The backend currently uses a JWT secret generated at startup, which is a known deployment concern. Before production use, it should be replaced with a stable configured secret from environment variables or config management.
