-- V1__init.sql - Initial schema for Order Service

-- Customer Orders Table
CREATE TABLE customer_orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    order_total DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_transaction_id VARCHAR(255),
    payment_status VARCHAR(50),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Or DATETIME(6)
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Or DATETIME(6)
    completed_at TIMESTAMP NULL -- Or DATETIME(6) NULL
);

-- Indexes for customer_orders
CREATE INDEX idx_customer_orders_user_id ON customer_orders(user_id);
CREATE INDEX idx_customer_orders_status ON customer_orders(order_status);
CREATE INDEX idx_customer_orders_payment_transaction_id ON customer_orders(payment_transaction_id);


-- Order Items Table
CREATE TABLE order_items (
    id VARCHAR(36) PRIMARY KEY,
    customer_order_id VARCHAR(36) NOT NULL,
    application_id VARCHAR(255) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    application_version VARCHAR(50),
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    total_price DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    subscription_plan_id VARCHAR(255),
    fulfillment_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Or DATETIME(6)
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Or DATETIME(6)
    CONSTRAINT fk_order_items_customer_order
        FOREIGN KEY(customer_order_id)
        REFERENCES customer_orders(id)
        ON DELETE CASCADE
);

-- Indexes for order_items
CREATE INDEX idx_order_items_customer_order_id ON order_items(customer_order_id);
CREATE INDEX idx_order_items_application_id ON order_items(application_id);