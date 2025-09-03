-- V1__Create_cart_tables.sql

CREATE TABLE carts (
    id BINARY(16) NOT NULL, -- Or BINARY(16) if you prefer native UUID storage and configure JPA for it
    user_id VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL, -- DATETIME(6) for microsecond precision with Instant
    updated_at DATETIME(6) NOT NULL,
    -- cart_status VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT uk_cart_user_id UNIQUE (user_id)
);

CREATE TABLE cart_items (
    id BINARY(16) NOT NULL, -- Or BINARY(16)
    cart_id BINARY(16) NOT NULL, -- Or BINARY(16), must match carts.id type
    application_id VARCHAR(255) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    is_free BOOLEAN NOT NULL,
    thumbnail_url VARCHAR(512) NULL,
    -- developer_id VARCHAR(255) NULL,
    added_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cart_item_to_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE
);

-- Optional indexes
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_application_id ON cart_items (application_id);