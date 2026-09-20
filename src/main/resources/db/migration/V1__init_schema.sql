-- Users & auth
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Geography / venue hierarchy
CREATE TABLE city (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE theater (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT NOT NULL REFERENCES city(id),
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255)
);

CREATE TABLE screen (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT NOT NULL REFERENCES theater(id),
    name VARCHAR(50) NOT NULL
);

CREATE TABLE seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screen_id BIGINT NOT NULL REFERENCES screen(id),
    seat_row VARCHAR(4) NOT NULL,
    seat_number INT NOT NULL,
    seat_type VARCHAR(16) NOT NULL,
    UNIQUE(screen_id, seat_row, seat_number)
);

-- Catalog
CREATE TABLE movie (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    duration_minutes INT NOT NULL,
    language VARCHAR(50),
    genre VARCHAR(50)
);

CREATE TABLE pricing_tier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    regular_seat_price DECIMAL(10,2) NOT NULL,
    premium_seat_price DECIMAL(10,2) NOT NULL,
    weekend_surcharge_percent DECIMAL(5,2) NOT NULL DEFAULT 0
);

-- Shows
CREATE TABLE show (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL REFERENCES movie(id),
    screen_id BIGINT NOT NULL REFERENCES screen(id),
    pricing_tier_id BIGINT NOT NULL REFERENCES pricing_tier(id),
    show_time TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED'
);

-- Per-show seat snapshot: the concurrency-critical table
CREATE TABLE show_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES show(id),
    seat_id BIGINT NOT NULL REFERENCES seat(id),
    status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
    hold_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE(show_id, seat_id)
);
CREATE INDEX idx_show_seat_hold ON show_seat(hold_id);

-- Discounts
CREATE TABLE discount_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(16) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    max_usage INT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0
);

-- Holds
CREATE TABLE booking_hold (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES show(id),
    customer_id BIGINT NOT NULL REFERENCES app_user(id),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE show_seat ADD CONSTRAINT fk_show_seat_hold FOREIGN KEY (hold_id) REFERENCES booking_hold(id);

-- Bookings
CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hold_id BIGINT NOT NULL REFERENCES booking_hold(id),
    show_id BIGINT NOT NULL REFERENCES show(id),
    customer_id BIGINT NOT NULL REFERENCES app_user(id),
    discount_code_id BIGINT REFERENCES discount_code(id),
    base_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'CONFIRMED',
    booked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP
);

CREATE TABLE booking_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES booking(id),
    show_seat_id BIGINT NOT NULL REFERENCES show_seat(id),
    price_at_booking DECIMAL(10,2) NOT NULL,
    UNIQUE(booking_id, show_seat_id)
);

CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES booking(id),
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    transaction_ref VARCHAR(64) NOT NULL,
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refunds
CREATE TABLE refund_policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    min_hours_before_show INT NOT NULL,
    refund_percent DECIMAL(5,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE refund (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE REFERENCES booking(id),
    refund_policy_id BIGINT REFERENCES refund_policy(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Notifications
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES booking(id),
    type VARCHAR(32) NOT NULL,
    channel VARCHAR(16) NOT NULL DEFAULT 'EMAIL',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);
