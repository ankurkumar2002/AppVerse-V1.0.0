-- V3__remove_plan_name_global_uniqueness.sql
-- Removes ONLY the global unique constraint/index from the 'name' column
-- in the subscription_plans table.
-- The 'name' COLUMN ITSELF and ALL ITS DATA WILL REMAIN.

-- Step 1: Drop the unique index that enforces the UNIQUE constraint on the 'name' column.
-- For MySQL, if `name VARCHAR(150) NOT NULL UNIQUE` was used in V1,
-- the index is often automatically named `name`.
ALTER TABLE subscription_plans
DROP INDEX name;

-- VERIFICATION (Important!):
-- If the above `DROP INDEX name;` command fails with an error like
-- "ERROR 1091 (42000): Can't DROP 'name'; check that column/key exists"
-- or a similar message indicating the index 'name' wasn't found,
-- it means MySQL named the unique index something else.
-- In that case, you MUST:
--   1. Connect to your MySQL database.
--   2. Run: `SHOW CREATE TABLE subscription_plans;`
--   3. Find the line that looks like: `UNIQUE KEY `actual_unique_index_name_for_name_column` (`name`)`
--   4. Then, replace the `DROP INDEX` command above with:
--      `ALTER TABLE subscription_plans DROP INDEX `actual_unique_index_name_for_name_column`;`