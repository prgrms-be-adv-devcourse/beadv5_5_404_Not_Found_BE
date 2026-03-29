# 이벤트 설계 문서

> MSA 환경에서 서비스 간 비동기 통신은 두 가지 방식을 사용합니다:
> 1. **Kafka**: 재고 및 정산 관련 이벤트 (StockDeductedEvent, StockRestoredEvent, PurchaseConfirmedEvent)
> 2. **Spring Event (ApplicationEventPublisher)**: 각 서비스 내부 도메인 이벤트

---

## 1. Kafka Events (서비스 간 비동기 통신)

재고 차감/복원 및 정산 관련 이벤트를 Kafka를 통해 비동기로 처리됩니다.

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `StockDeductedEvent` | Order | Product | `product.stock-deducted` | 주문 시 재고 차감 반영 |
| `StockRestoredEvent` | Order | Product | `product.stock-restored` | 주문 취소 시 재고 복구 |
| `StockDeductFailedEvent` | Product | Order, Payment | `product.stock-deduct-failed` | 낙관적 락 충돌로 재고 차감 실패 시 보상 트랜잭션 트리거 | 미구현 — payment-service 환불 구현 완료 후 적용 |
| `PurchaseConfirmedEvent` | Order | Payment | `order.purchase-confirmed` | 구매확정 → 정산 대상 생성 트리거 |

> **삭제된 이벤트:**
> - ~~`OrderDeliveredEvent`~~: 리뷰 작성 가능 여부는 Review 서비스가 리뷰 작성 시점에 Order 서비스에 REST로 구매 이력을 확인하는 방식으로 대체. Kafka 불필요.
> - ~~`PaymentApprovedEvent`~~: 예치금 결제는 주문 생성 트랜잭션 내에서 REST 동기 호출로 처리되므로 별도 이벤트 불필요.
> - ~~`RefundCompletedEvent` (Kafka)~~: 환불 흐름은 Order가 REST로 주도하며, Payment 내부 후처리는 Spring Event로 충분.

---

## 2. Spring Events (도메인 내부 비동기 처리)

각 서비스 내부에서 Spring ApplicationEventPublisher를 사용하여 도메인 이벤트를 처리합니다.

### Order Service

| 이벤트명 | 발행처 | 목적 |
|---------|--------|------|
| `OrderCancelledEvent` | Order Service | 주문 취소 완료, 배송 중단 처리 |

### Payment Service

| 이벤트명 | 발행처 | 목적 |
|---------|--------|------|
| `DepositChargedEvent` | Payment Service | 예치금 충전 완료, 회원 잔액 반영 |
| `RefundCompletedEvent` | Payment Service | 환불 완료, 예치금 복원 및 재고 복구 트리거 |
| `SettlementCompletedEvent` | Payment Service | 정산 완료 반영 |
| `SettlementFailedEvent` | Payment Service | 정산 실패 추적 |

### Review Service

> 리뷰 등록/수정/삭제 시 상품 평점 업데이트는 Review → Product REST 동기 호출로 처리. Spring Event/Kafka 사용하지 않음.

### Member Service

| 이벤트명 | 발행처 | 목적 |
|---------|--------|------|
| `MemberRegisteredEvent` | Member Service | 회원가입 완료 후 인증 메일 발송 (내부 Spring Event) |
| `SellerApprovedEvent` | Member Service | 판매자 승인 완료 처리 |

---

## 3. Kafka Topic 네이밍 규칙

```
{도메인}.{이벤트 동사/상태}
```

예시: `product.stock-deducted`, `product.stock-restored`

---

## 4. 이벤트 메시지 공통 구조

```json
{
  "eventId": "UUID",
  "eventType": "StockDeductedEvent",
  "timestamp": "2026-03-23T12:00:00Z",
  "payload": {
    // 이벤트별 데이터
  }
}
```

### Kafka Events 메시지 구조

**StockDeductedEvent**
```json
{
  "eventId": "UUID",
  "eventType": "StockDeductedEvent",
  "timestamp": "2026-03-23T12:00:00Z",
  "payload": {
    "orderId": "UUID",
    "productId": "UUID",
    "quantity": 2,
    "reason": "ORDER_CREATED"
  }
}
```

**StockRestoredEvent**
```json
{
  "eventId": "UUID",
  "eventType": "StockRestoredEvent",
  "timestamp": "2026-03-23T12:00:00Z",
  "payload": {
    "orderId": "UUID",
    "productId": "UUID",
    "quantity": 2,
    "reason": "ORDER_CANCELLED"
  }
}
```

### Spring Event 메시지 구조

Spring Event는 내부 도메인 이벤트로 JSON 직렬화가 필요하지 않습니다.

> `SettlementTargetCreatedEvent`는 Kafka 이벤트로 발행하지 않는다. Payment 서비스 내부에서 `PurchaseConfirmedEvent` 처리 시 `settlement_target`을 직접 생성한다.

각 서비스의 Kafka Producer/Consumer는 헥사고날 구조의 adapter 레이어에 위치합니다.

```
adapter/
├── in/kafka/    ← Consumer (Kafka 이벤트 수신)
└── out/kafka/   ← Producer (Kafka 이벤트 발행)
```

Spring Event는 별도의 adapter 없이 ApplicationEventPublisher와 EventListener를 사용합니다.

---

## 5. 삭제된 Kafka 이벤트 (설계 변경 이력)

| 이벤트 | 삭제 사유 |
|--------|----------|
| `PaymentApprovedEvent` | 예치금 결제는 주문 생성 트랜잭션 내 REST 동기 호출로 처리. 별도 Kafka 이벤트 불필요 |
| `RefundCompletedEvent` (Kafka) | 환불 흐름은 Order가 REST로 주도. Payment 내부 후처리는 Spring Event(`RefundCompletedEvent`)로 처리 |
| `OrderDeliveredEvent` | 리뷰 작성 가능 여부는 Review → Order REST 구매 이력 확인으로 대체 |
