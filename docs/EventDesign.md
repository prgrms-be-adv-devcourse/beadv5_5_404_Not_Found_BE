# 이벤트 설계 문서

> MSA 환경에서 서비스 간 비동기 통신은 두 가지 방식을 사용합니다:
> 1. **Kafka**: 재고 관련 이벤트만 (StockDeductedEvent, StockRestoredEvent)
> 2. **Spring Event (ApplicationEventPublisher)**: 각 서비스 내부 도메인 이벤트

---

## 1. Kafka Events (서비스 간 비동기 통신)

재고 차감/복원 및 정산 관련 이벤트를 Kafka를 통해 비동기로 처리됩니다.

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `StockDeductedEvent` | Order | Product | `product.stock-deducted` | 주문 시 재고 차감 반영 |
| `StockRestoredEvent` | Order | Product | `product.stock-restored` | 주문 취소 시 재고 복구 |
| `PurchaseConfirmedEvent` | Order | Payment | `order.purchase-confirmed` | 구매확정 → 정산 대상 생성 트리거 |
| `OrderDeliveredEvent` | Order | Review | `order.delivered` | 배송 완료 후 리뷰 작성 가능 |

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

| 이벤트명 | 발행처 | 목적 |
|---------|--------|------|
| `ReviewCreatedEvent` | Review Service | 리뷰 등록 시 상품 평균 평점 및 리뷰 수 업데이트 |
| `ReviewUpdatedEvent` | Review Service | 리뷰 수정 시 상품 평균 평점 재계산 |
| `ReviewDeletedEvent` | Review Service | 리뷰 삭제 시 상품 평균 평점 및 리뷰 수 업데이트 |

### Member Service

| 이벤트명 | 발행처 | 목적 |
|---------|--------|------|
| `MemberRegisteredEvent` | Member Service | 회원가입 완료 후 인증 메일 발송 |
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
