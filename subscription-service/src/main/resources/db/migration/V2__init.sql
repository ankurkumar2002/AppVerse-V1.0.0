-- V2__add_app_dev_to_plans.sql
-- Adds application_id and developer_id to the subscription_plans table
-- and creates necessary indexes.

-- Add new columns to the subscription_plans table
ALTER TABLE subscription_plans
ADD COLUMN application_id VARCHAR(36) NULL AFTER gateway_plan_price_id, -- Or wherever you want it positioned
ADD COLUMN developer_id VARCHAR(255) NULL AFTER application_id;

-- Note on existing data:
-- If you have existing subscription_plans in your database that were meant to be platform-wide
-- (not tied to a specific app/developer), these new columns will be NULL for those rows.
-- You might want to update them to have a placeholder value if NULL is not desired,
-- or make the columns NOT NULL if all plans must be associated.
-- Example:
-- UPDATE subscription_plans
-- SET application_id = 'PLATFORM_DEFAULT', developer_id = 'PLATFORM_ADMIN'
-- WHERE application_id IS NULL AND developer_id IS NULL;
-- For now, allowing NULL is simpler if you have existing data.

-- Create indexes for the new columns
CREATE INDEX idx_subplan_application_id_v2 ON subscription_plans(application_id);
CREATE INDEX idx_subplan_developer_id_v2 ON subscription_plans(developer_id);

-- Optional: If you want plan names to be unique per application per developer,
-- and you previously had a UNIQUE index only on 'name', you'd need to:
-- 1. Drop the old unique index on 'name'.
-- 2. Create a new composite unique index.
-- This step depends on your existing constraints and if you have data that would violate a new stricter constraint.
-- Example (if you had 'idx_subplan_name' as a UNIQUE index on 'name' only):
-- -- ALTER TABLE subscription_plans DROP INDEX idx_subplan_name;
-- -- CREATE UNIQUE INDEX uk_subplan_name_app_dev ON subscription_plans(name, application_id, developer_id);
-- For now, we'll assume the uniqueness is primarily handled by application logic or can be added later if needed.
-- Your entity has @Index(name = "idx_subplan_name_app_dev", columnList = "name, applicationId, developerId", unique = true)
-- Hibernate will try to create this if ddl-auto is 'update' or 'create', but with 'none', Flyway must do it.
-- If your V1 script created a unique index on 'name' only, you'd need to drop it before adding a composite one.
-- If V1 did not create a unique index on 'name', then Hibernate might try (and fail if 'name' is not globally unique).
-- Best to define unique constraints in Flyway.
-- Let's assume V1 created: CREATE INDEX idx_subplan_name ON subscription_plans(name) UNIQUE;
-- To change it:
-- -- ALTER TABLE subscription_plans DROP INDEX idx_subplan_name; -- (or the actual name of your unique index on 'name')
-- -- CREATE UNIQUE INDEX uk_subplan_name_app_dev ON subscription_plans(name, application_id, developer_id);
-- For simplicity now, we are just adding non-unique indexes on the new columns.
-- If your business logic requires `name` to be unique PER `application_id` and `developer_id`,
-- and you also want a database constraint, you'd add the composite unique index.