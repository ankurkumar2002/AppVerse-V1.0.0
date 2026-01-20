-- V6__add_identity_fields_to_developers.sql
ALTER TABLE developers
    ADD COLUMN username VARCHAR(100),
    ADD COLUMN first_name VARCHAR(100),
    ADD COLUMN last_name VARCHAR(100);
