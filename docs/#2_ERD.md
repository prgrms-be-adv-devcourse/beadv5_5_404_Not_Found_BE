# ERD (Entity Relationship Diagram)

## 상세 ERD (테이블 컬럼 포함)

```mermaid
erDiagram
    MEMBER {
        UUID id PK
        VARCHAR_255 email UK
        VARCHAR_255 password_hash
        VARCHAR_100 name
        VARCHAR_20 phone
        ENUM role
        ENUM status
        INT point_balance
        INT deposit_balance "예치금 잔액 (이벤트로 동기화)"
        BOOLEAN email_verified
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    REFRESH_TOKEN {
        UUID id PK
        UUID member_id FK
        VARCHAR_512 token_hash "저장 시 해싱 필수"
        VARCHAR_255 user_agent
        VARCHAR_45 ip_address
        BOOLEAN is_revoked
        TIMESTAMP expires_at
        TIMESTAMP created_at
        TIMESTAMP last_used_at
    }
    TOKEN_BLACKLIST {
        BIGINT id PK "AUTO_INCREMENT"
        VARCHAR_255 jti UK "Access Token 고유 식별자"
        TIMESTAMP expires_at "해당 토큰 만료 시각"
        TIMESTAMP created_at "블랙리스트 등록 시각"
    }
    ADDRESS {
        UUID id PK
        UUID member_id FK
        VARCHAR_50 label
        VARCHAR_100 recipient
        VARCHAR_20 phone
        VARCHAR_10 zipcode
        VARCHAR_255 address1
        VARCHAR_255 address2
        BOOLEAN is_default
        BOOLEAN is_deleted
    }
    SELLER {
        UUID id PK
        UUID member_id FK
        VARCHAR_20 business_number UK
        VARCHAR_100 shop_name
        VARCHAR_10 bank_code
        VARCHAR_50 bank_account
        VARCHAR_100 account_holder
        DECIMAL_5_2 commission_rate
        ENUM status
        TIMESTAMP approved_at
    }
    CATEGORY {
        UUID id PK
        UUID parent_id FK
        VARCHAR_100 name
        VARCHAR_100 slug UK
        INT depth
        INT sort_order
        BOOLEAN is_active
    }
    PROCESSED_EVENTS {
        VARCHAR event_id PK "Kafka eventId (UUID 문자열), 중복 처리 방지"
        TIMESTAMP processed_at "처리 완료 시각"
    }
    PRODUCT {
        UUID id PK
        UUID seller_id FK
        UUID category_id FK
        VARCHAR_20 isbn UK
        VARCHAR_300 title
        VARCHAR_200 author
        VARCHAR_100 publisher
        INT price
        INT quantity
        BIGINT version
        ENUM book_type
        ENUM status
        DECIMAL_3_2 avg_rating
        INT review_count
        TIMESTAMP created_at
    }
    REVIEW {
        UUID id PK
        UUID product_id FK
        UUID member_id FK
        UUID order_id FK
        SMALLINT rating "1~5 별점"
        TIMESTAMP created_at
    }
    CART {
        UUID id PK
        UUID member_id FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    CART_ITEM {
        UUID id PK
        UUID cart_id FK
        UUID product_id FK
        INT quantity
        TIMESTAMP added_at
    }
    DEPOSIT {
        UUID id PK
        UUID member_id FK
        UUID payment_id FK "충전 시 결제 건 참조 (CHARGE일 때)"
        UUID order_id FK "사용 시 주문 참조 (USE일 때)"
        ENUM type "CHARGE | USE | REFUND"
        INT amount "변동 금액"
        INT balance_after "변동 후 잔액 스냅샷"
        VARCHAR_255 description
        TIMESTAMP created_at
    }
    ORDER {
        UUID id PK
        VARCHAR_30 order_number UK
        UUID member_id FK
        ENUM status "PENDING | PAID | CONFIRMED | SHIPPING | DELIVERED | PURCHASE_CONFIRMED | CANCELLED"
        INT total_amount
        INT deposit_used "예치금 차감액"
        JSONB shipping_snapshot
        VARCHAR_100 idempotency_key UK
        TIMESTAMP confirmed_at "구매확정 시각 (PURCHASE_CONFIRMED 전환 시 기록, NULL 허용)"
        TIMESTAMP created_at
    }
    ORDER_ITEM {
        UUID id PK
        UUID order_id FK
        UUID product_id FK
        UUID seller_id FK
        VARCHAR_300 product_title
        INT unit_price
        INT quantity
        INT subtotal
        ENUM status
    }
    SHIPMENT {
        UUID id PK
        UUID order_id FK
        VARCHAR_50 carrier
        VARCHAR_50 tracking_number
        ENUM status
        TIMESTAMP shipped_at
        TIMESTAMP delivered_at
    }
    PAYMENT {
        UUID id PK
        UUID member_id FK "예치금 충전을 수행하는 회원"
        ENUM pg_provider
        INT amount
        ENUM status
        VARCHAR_200 pg_transaction_id UK
        VARCHAR_500 payment_key
        ENUM method
        ENUM purpose "DEPOSIT_CHARGE"
        TIMESTAMP paid_at
        VARCHAR_100 idempotency_key UK
    }
    REFUND {
        UUID id PK
        UUID payment_id FK
        UUID order_item_id FK
        INT amount
        VARCHAR_255 reason
        ENUM status
        VARCHAR_200 pg_refund_id
        TIMESTAMP refunded_at
    }
    SETTLEMENT_TARGET {
        UUID id PK
        UUID order_id FK
        UUID seller_id FK
        BIGINT total_amount "해당 주문에서 이 판매자 매출"
        TIMESTAMP confirmed_at "구매확정 시각"
        UUID settlement_id FK "nullable - 정산 완료 후 연결"
        ENUM status "PENDING | SETTLED"
    }
    SETTLEMENT {
        UUID id PK
        UUID seller_id FK
        DATE period_start "집계 시작일"
        DATE period_end "집계 종료일"
        BIGINT total_sales_amount "총 매출"
        BIGINT fee_amount "수수료"
        BIGINT net_amount "순 정산 금액"
        TIMESTAMP settled_at "정산 실행 시각"
        ENUM status "PENDING | COMPLETED | FAILED"
    }

    MEMBER ||--o{ ADDRESS : has
    MEMBER ||--o| SELLER : becomes
    MEMBER ||--|| CART : owns
    MEMBER ||--o{ ORDER : places
    MEMBER ||--o{ REVIEW : writes
    MEMBER ||--o{ DEPOSIT : "충전/사용 이력"
    MEMBER ||--o{ REFRESH_TOKEN : has
    MEMBER ||--o{ PAYMENT : "예치금 충전"

    CATEGORY ||--o{ CATEGORY : parent_of
    CATEGORY ||--o{ PRODUCT : classifies

    SELLER ||--o{ PRODUCT : sells
    SELLER ||--o{ ORDER_ITEM : fulfills
    SELLER ||--o{ SETTLEMENT_TARGET : "정산 대상"
    SELLER ||--o{ SETTLEMENT : receives

    PRODUCT ||--o{ CART_ITEM : added_as
    PRODUCT ||--o{ ORDER_ITEM : ordered_as
    PRODUCT ||--o{ REVIEW : reviewed_for

    CART ||--o{ CART_ITEM : contains

    ORDER ||--o{ ORDER_ITEM : contains
    ORDER ||--|| SHIPMENT : ships_with
    ORDER ||--o{ REVIEW : verified_by
    ORDER ||--o{ DEPOSIT : "예치금 사용 이력"

    PAYMENT ||--o{ REFUND : refunded_by
    PAYMENT ||--o{ DEPOSIT : "충전 결제 참조"

    ORDER ||--o{ SETTLEMENT_TARGET : "구매확정 시 생성"
    SETTLEMENT ||--o{ SETTLEMENT_TARGET : "월 정산 시 연결"

    ORDER_ITEM ||--o{ REFUND : refunded_item
```

---

## 관계 중심 ERD (개요)

```mermaid
erDiagram
    MEMBER ||--o{ ADDRESS : has
    MEMBER ||--|| CART : owns
    MEMBER ||--o{ ORDER : places
    MEMBER ||--o| SELLER : is
    MEMBER ||--o{ DEPOSIT : has
    MEMBER ||--o{ REFRESH_TOKEN : has
    MEMBER ||--o{ PAYMENT : "charges deposit"

    CATEGORY ||--o{ PRODUCT : has
    SELLER ||--o{ PRODUCT : sells
    CART ||--o{ CART_ITEM : has
    PRODUCT ||--o{ CART_ITEM : in
    ORDER ||--o{ ORDER_ITEM : has
    PRODUCT ||--o{ ORDER_ITEM : in
    SELLER ||--o{ ORDER_ITEM : fulfills
    ORDER ||--o{ SHIPMENT : ships
    ORDER ||--o{ DEPOSIT : uses
    PAYMENT ||--o{ REFUND : has
    PAYMENT ||--o{ DEPOSIT : charges
    ORDER_ITEM ||--o{ REFUND : for
    ORDER ||--o{ SETTLEMENT_TARGET : confirmed
    SETTLEMENT ||--o{ SETTLEMENT_TARGET : settles
    SELLER ||--o{ SETTLEMENT_TARGET : targets
    SELLER ||--o{ SETTLEMENT : gets
```
