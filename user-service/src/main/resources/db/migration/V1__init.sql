-- V1__Create_users_table.sql
-- Defines the initial schema for the 'users' table based on the current User.java entity model.

CREATE TABLE users (
    id CHAR(36) NOT NULL,                           -- For java.util.UUID mapped as a string by JPA
    keycloak_user_id VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    first_name VARCHAR(100) NULL,
    last_name VARCHAR(100) NULL,
    phone VARCHAR(20) NULL,
    role VARCHAR(20) NOT NULL,                       -- For Role enum (EnumType.STRING)
    status VARCHAR(20) NOT NULL,                     -- For UserStatus enum (EnumType.STRING)
    deactivated_by_admin BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at DATETIME NULL,                     -- For LocalDateTime
    created_at DATETIME NOT NULL,                    -- For LocalDateTime
    updated_at DATETIME NOT NULL,                    -- For LocalDateTime

    PRIMARY KEY (id),
    CONSTRAINT uk_users_keycloak_user_id UNIQUE (keycloak_user_id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Optional: Add other indexes if anticipated to be frequently queried upon
-- CREATE INDEX idx_users_status ON users(status);
-- CREATE INDEX idx_users_role ON users(role);