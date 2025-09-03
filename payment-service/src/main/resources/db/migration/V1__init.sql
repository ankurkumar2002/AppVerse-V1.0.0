-- V1__init_payments.sql - Initial schema for Payment Service

-- Payment Transactions Table
CREATE TABLE payment_transactions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    payer_email VARCHAR(255),
    reference_id VARCHAR(255) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_gateway VARCHAR(50) NOT NULL,
    gateway_transaction_id VARCHAR(255) UNIQUE, -- Gateway's main transaction/charge ID
    gateway_payment_intent_id VARCHAR(255),    -- e.g., Stripe PaymentIntent ID
    payment_method_type VARCHAR(50),
    payment_method_details VARCHAR(255),         -- Masked details
    status VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    error_message TEXT,                          -- Can be long
    gateway_error_code VARCHAR(100),
    metadata TEXT,                               -- Store as JSON string
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for payment_transactions
CREATE INDEX idx_pt_user_id ON payment_transactions(user_id);
CREATE INDEX idx_pt_reference_id_type ON payment_transactions(reference_id, reference_type);
CREATE INDEX idx_pt_gateway_tx_id ON payment_transactions(gateway_transaction_id);
CREATE INDEX idx_pt_status ON payment_transactions(status);


-- Stored Payment Methods Table
CREATE TABLE stored_payment_methods (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    payment_gateway VARCHAR(50) NOT NULL,
    gateway_customer_id VARCHAR(255),
    gateway_payment_method_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,                   -- CARD, BANK_ACCOUNT
    brand VARCHAR(50),
    last4 VARCHAR(4),
    expiry_month INT,
    expiry_year INT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    billing_details_snapshot TEXT,               -- Store as JSON string
    status VARCHAR(50) NOT NULL,                 -- ACTIVE, EXPIRED, REMOVED
    expires_at TIMESTAMP NULL,                   -- For card expiry
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_spm_gateway_pm_id_gateway (gateway_payment_method_id, payment_gateway) -- Ensure gateway PM ID is unique per gateway
);

-- Indexes for stored_payment_methods
CREATE INDEX idx_spm_user_id_gateway ON stored_payment_methods(user_id, payment_gateway);
CREATE INDEX idx_spm_gateway_customer_id ON stored_payment_methods(gateway_customer_id);