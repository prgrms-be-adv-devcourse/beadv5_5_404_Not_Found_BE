# 이벤트 설계 문서

> MSA 환경에서 서비스 간 비동기 통신은 Apache Kafka를 사용합니다.
> 각 이벤트는 Kafka Topic으로 발행되며, 관심 있는 서비스가 해당 Topic을 구독합니다.

---

## 1. 핵심 이벤트 목록

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `OrderCreatedEvent` | Order | Payment | `order.created` | 주문 생성 사실 전달, 결제 준비 시작 |
| `PaymentApprovedEvent` | Payment | Order, Product | `payment.approved` | 주문 확정, 재고 차감 반영 |
| `PaymentFailedEvent` | Payment | Order | `payment.failed` | 결제 실패 반영, 주문 상태 정리 |
| `OrderCancelRequestedEvent` | Order | Payment | `order.cancel-requested` | 주문 취소 요청 전달, 환불 시작 |
| `RefundCompletedEvent` | Payment | Order, Product | `refund.completed` | 환불 완료 반영, 주문 취소 확정, 재고 복구 |

---

## 2. 추가 이벤트 목록

### 회원 도메인 (Member)

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `MemberRegisteredEvent` | Member | (이메일 발송) | `member.registered` | 회원가입 완료 후 인증 메일 발송 |
| `MemberEmailVerifiedEvent` | Member | (내부 처리) | `member.email-verified` | 이메일 인증 완료 반영 |
| `SellerApprovedEvent` | Member | Product | `seller.approved` | 판매자 승인 완료 사실 전달 |
| `SellerSuspendedEvent` | Member | Product | `seller.suspended` | 판매자 정지 시 상품 판매 제한 반영 |

### 상품 도메인 (Product)

| 이벤트명 | Producer | Consumer | Topic | 목적 | 비고 |
|---------|----------|----------|-------|------|------|
| `ProductCreatedEvent` | Product | (운영/알림) | `product.created` | 상품 등록 완료 상태 반영 | 미구현 — 소비하는 서비스 없음 (외부 알림 서비스 미정) |
| `ProductApprovedEvent` | Product | (노출 처리) | `product.approved` | 상품 검수 승인 후 판매 가능 반영 | 미구현 — 소비하는 서비스 없음 (별도 노출 처리 서비스 미정) |
| `ProductSoldOutEvent` | Product | Order | `product.sold-out` | 품절 상태 반영, 주문 제한 | 미구현 — Order가 주문 생성 시 REST로 재고 검증하므로 당장 불필요, Order 서비스 개발 시 추가 |
| `StockDeductedEvent` | Product | (운영 추적) | `product.stock-deducted` | 재고 차감 완료 사실 추적 | 미구현 — 운영 모니터링 목적, 소비 서비스 없음 |
| `StockRestoredEvent` | Product | (운영 추적) | `product.stock-restored` | 재고 복구 완료 사실 추적 | 미구현 — 운영 모니터링 목적, 소비 서비스 없음 |

### 리뷰 도메인 (Review)

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `ReviewCreatedEvent` | Review | Product | `review.created` | 리뷰 등록 시 상품 평균 평점 및 리뷰 수 업데이트 |
| `ReviewUpdatedEvent` | Review | Product | `review.updated` | 리뷰 수정 시 상품 평균 평점 재계산 |
| `ReviewDeletedEvent` | Review | Product | `review.deleted` | 리뷰 삭제 시 상품 평균 평점 및 리뷰 수 업데이트 |

### 결제 도메인 (Payment)

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `RefundFailedEvent` | Payment | Order | `refund.failed` | 환불 실패 시 주문 상태 보류/재처리 판단 |
| `SettlementTargetCreatedEvent` | Payment | (내부 처리) | `settlement.target-created` | 정산 대상 생성 |
| `SettlementCompletedEvent` | Payment | (내부 / 조회성) | `settlement.completed` | 정산 완료 반영 |
| `SettlementFailedEvent` | Payment | (내부 처리) | `settlement.failed` | 정산 실패 추적 |

### 주문 도메인 (Order)

| 이벤트명 | Producer | Consumer | Topic | 목적 |
|---------|----------|----------|-------|------|
| `OrderCanceledEvent` | Order | (필요 시 확장) | `order.canceled` | 환불 완료 후 주문 최종 취소 확정 알림 |
| `OrderDeliveredEvent` | Order | Review, Payment | `order.delivered` | 배송 완료 후 리뷰 가능, 정산 기준점 |

---

## 3. Kafka Topic 네이밍 규칙

```
{도메인}.{이벤트 동사/상태}
```

예시: `order.created`, `payment.approved`, `review.created`, `seller.approved`

---

## 4. 이벤트 메시지 공통 구조

```json
{
  "eventId": "UUID",
  "eventType": "PaymentApprovedEvent",
  "timestamp": "2026-03-23T12:00:00Z",
  "payload": {
    // 이벤트별 데이터
  }
}
```

각 서비스의 Kafka Producer/Consumer는 헥사고날 구조의 adapter 레이어에 위치합니다.

```
adapter/
├── in/kafka/    ← Consumer (이벤트 수신)
└── out/kafka/   ← Producer (이벤트 발행)
```

---

## 5. 이벤트별 Payload 상세

### PaymentApprovedEvent
- **Topic**: `payment.approved`
- **Producer**: Payment
- **Consumer**: Order (주문 확정), Product (재고 차감)

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "PaymentApprovedEvent",
  "timestamp": "2026-03-23T12:00:00",
  "payload": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "memberId": "550e8400-e29b-41d4-a716-446655440002",
    "orderItems": [
      {
        "productId": "550e8400-e29b-41d4-a716-446655440003",
        "quantity": 2
      }
    ]
  }
}
```

---

### RefundCompletedEvent
- **Topic**: `refund.completed`
- **Producer**: Payment
- **Consumer**: Order (주문 취소 확정), Product (재고 복원)

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "RefundCompletedEvent",
  "timestamp": "2026-03-23T12:00:00",
  "payload": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "memberId": "550e8400-e29b-41d4-a716-446655440002",
    "orderItems": [
      {
        "productId": "550e8400-e29b-41d4-a716-446655440003",
        "quantity": 2
      }
    ]
  }
}
```
