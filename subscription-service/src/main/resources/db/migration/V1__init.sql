-- V1__init_subscriptions.sql - Initial schema for Subscription Service

-- Subscription Plans Table
CREATE TABLE subscription_plans (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    billing_interval VARCHAR(20) NOT NULL,       -- MONTHLY, YEARLY, etc.
    billing_interval_count INT NOT NULL DEFAULT 1,
    trial_period_days INT DEFAULT 0,
    status VARCHAR(20) NOT NULL,                 -- ACTIVE, INACTIVE, ARCHIVED
    gateway_plan_price_id VARCHAR(255),          -- e.g., Stripe Price ID
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for subscription_plans
CREATE INDEX idx_subplan_status_v1 ON subscription_plans(status); -- Added _v1 to avoid potential conflict if index name was too generic


-- Subscription Plan Applications Table (Many-to-Many link if an app can be in many plans, and a plan has many apps)
-- Using @ElementCollection in JPA, this table is managed by Hibernate.
-- If you need more complex relationships or additional columns on the link,
-- you'd create an explicit join entity and table.
-- For @ElementCollection of String application_id:
CREATE TABLE subscription_plan_applications (
    subscription_plan_id VARCHAR(36) NOT NULL,
    application_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (subscription_plan_id, application_id), -- Composite primary key
    CONSTRAINT fk_planapps_plan
        FOREIGN KEY(subscription_plan_id)
        REFERENCES subscription_plans(id)
        ON DELETE CASCADE
    -- Note: No FK to an 'applications' table here as 'application_id' is just a string value.
    -- Integrity would be application-level or via eventual consistency.
);
CREATE INDEX idx_planapps_app_id ON subscription_plan_applications(application_id);


-- User Subscriptions Table
CREATE TABLE user_subscriptions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    subscription_plan_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,                    -- PENDING_INITIAL_PAYMENT, TRIALING, ACTIVE, PAST_DUE, CANCELLED, EXPIRED
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NULL,                        -- When the subscription fully ends (if non-renewing or after cancellation period)
    current_period_start_date TIMESTAMP NOT NULL,
    current_period_end_date TIMESTAMP NOT NULL,     -- Next renewal/expiry of current term
    trial_end_date TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancellation_reason TEXT,
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    stored_payment_method_id VARCHAR(36),           -- ID from payment_service.stored_payment_methods
    gateway_subscription_id VARCHAR(255) UNIQUE,    -- e.g., Stripe Subscription ID (sub_xxx), usually unique
    last_successful_payment_id VARCHAR(36),         -- ID from payment_service.payment_transactions
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usersub_plan                         -- Constraint for plan ID (optional if you want loose coupling)
        FOREIGN KEY(subscription_plan_id)
        REFERENCES subscription_plans(id)
        ON DELETE RESTRICT -- Prevent deleting a plan if active subscriptions exist (or SET NULL/DEFAULT if allowed)
);

-- Indexes for user_subscriptions
CREATE INDEX idx_usersub_user_id_v1 ON user_subscriptions(user_id); -- Added _v1
CREATE INDEX idx_usersub_plan_id_v1 ON user_subscriptions(subscription_plan_id); -- Added _v1
CREATE INDEX idx_usersub_status_v1 ON user_subscriptions(status); -- Added _v1
CREATE INDEX idx_usersub_gateway_sub_id_v1 ON user_subscriptions(gateway_subscription_id); -- Added _v1
CREATE INDEX idx_usersub_renewal_check ON user_subscriptions(status, auto_renew, current_period_end_date); -- For renewal job


-- Subscription Events Table (Optional, for auditing)
CREATE TABLE subscription_events (
    id VARCHAR(36) PRIMARY KEY,
    user_subscription_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(50) NOT NULL,             -- CREATED, RENEWED, CANCELLED, etc.
    event_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,                                -- JSON string or longer text
    triggered_by VARCHAR(100),                   -- USER, SYSTEM_RENEWAL, ADMIN
    CONSTRAINT fk_subevent_usersub
        FOREIGN KEY(user_subscription_id)
        REFERENCES user_subscriptions(id)
        ON DELETE CASCADE -- If a subscription is deleted, its events are also deleted
);

-- Indexes for subscription_events
CREATE INDEX idx_subevent_usersub_id_v1 ON subscription_events(user_subscription_id); -- Added _v1
CREATE INDEX idx_subevent_event_type_v1 ON subscription_events(event_type); -- Added _v1