-- Baseline schema, matching the JPA entities in com.lms.entity as of this
-- migration. Column/table names follow Spring Boot's default snake_case
-- naming strategy plus the explicit @Column/@JoinColumn names already set
-- on the entities (e.g. Course.createdBy -> admin_id).

CREATE TABLE users (
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL
);

CREATE TABLE courses (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255)  NOT NULL,
    description   VARCHAR(1000),
    price         DOUBLE PRECISION,
    video_url     VARCHAR(255),
    thumbnail_url VARCHAR(255),
    admin_id      BIGINT        NOT NULL REFERENCES users(id),
    created_at    TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE enrollments (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT    NOT NULL REFERENCES users(id),
    course_id    BIGINT    NOT NULL REFERENCES courses(id),
    enrolled_at  TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_enrollment_student_course UNIQUE (student_id, course_id)
);
