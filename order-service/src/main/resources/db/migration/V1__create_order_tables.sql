-- V1: order-service 초기 스키마
-- 프로파일별 동작:
--   운영: Flyway가 이 스크립트 실행 (ddl-auto: validate)
--   로컬: Flyway 실행 후 Hibernate validate
--   테스트: H2 + ddl-auto: create-drop (Flyway 비활성)

CREATE TABLE IF NOT EXISTS cart (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cart_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (cart_id, product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(30) NOT NULL UNIQUE,
    member_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount INT NOT NULL,
    shipping_fee INT NOT NULL,
    deposit_used INT NOT NULL DEFAULT 0,
    shipping_snapshot JSONB,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    address_id UUID,
    cart_item_ids TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    delivered_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    seller_id UUID NOT NULL,
    product_title VARCHAR(300) NOT NULL,
    unit_price INT NOT NULL,
    quantity INT NOT NULL,
    subtotal INT NOT NULL,
    status VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS shipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    carrier VARCHAR(50),
    tracking_number VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP
);
