-- Update existing lowercase values (if any)
UPDATE developers SET role = 'DEVELOPER' WHERE role = 'developer';

-- Alter the default value
ALTER TABLE developers
MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'DEVELOPER';
