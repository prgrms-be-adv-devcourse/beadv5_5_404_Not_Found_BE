# API Specification - Payment Module

> 도서 이커머스 플랫폼 API 명세서
> Payment API (/payment/*)

---

> ★ = 결제 필수 플로우

## 📌 Payment API

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 예치금 결제 실행 | POST | /payment/orders/{orderId}/pay | ✅ | ★ | 예치금으로 주문 결제 |
| 예치금 충전 준비 | POST | /payment/deposit/charge/ready | ✅ | | PG 결제창 URL 발급 |
| 예치금 충전 승인 | POST | /payment/deposit/charge/confirm | ✅ | | PG 승인 + 예치금 충전 |
| 예치금 내역 조회 | GET | /payment/deposit/history | ✅ | | 충전/사용/환불 내역 |
| 환불 요청 | POST | /payment/{paymentId}/refund | ❌ | | 미구현 |
| PG 웹훅 수신 | POST | /payment/webhook/pg | ❌ | | 미구현 |

### Payment Internal API

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 예치금 차감 | POST | /internal/deposit/deduct | ✅ | | payment-service 내부에서 직접 처리 (외부 호출자 없음) |
| 예치금 환급 | POST | /internal/deposit/refund | ✅ | | 내부 환급 처리 API (외부 공개 환불 API와 별개) |

### Payment → 외부 서비스 내부 호출

| 기능 | Method | Endpoint | 대상 서비스 | 설명 |
|------|--------|----------|------------|------|
| 주문 조회 | GET | /internal/order/{orderId} | order-service | 결제 전 주문 금액/상태 확인 |
| 주문 상태 변경 | POST | /internal/order/{orderId}/status | order-service | 결제 완료 후 PENDING → PAID 변경 |
| 재고 차감 | POST | /internal/products/stock/deduct | product-service | 결제 시 재고 차감 |
| 회원 활성 상태 확인 | GET | /internal/member/{memberId}/active | member-service | 충전 전 회원 상태 확인 |
| 예치금 잔액 조회 | GET | /internal/member/{memberId}/deposit | member-service | 결제/충전 전 잔액 확인 |
| 예치금 차감 동기화 | POST | /internal/member/{memberId}/deposit/deduct | member-service | 결제 후 AFTER_COMMIT 잔액 동기화 |
| 예치금 충전 동기화 | POST | /internal/member/{memberId}/deposit/charge | member-service | 충전/환급 후 AFTER_COMMIT 잔액 동기화 |

> payment-service가 결제 완료 후 order-service 내부 API를 호출하여 주문 상태를 PAID로 변경합니다.
> order-service는 PAID 수신 후 단일 트랜잭션 내에서 PAID→SHIPPING→DELIVERED→PURCHASE_CONFIRMED 처리 및 Kafka 이벤트 발행합니다.

### Notes

- **Payment Purpose**: `PaymentPurpose`는 `DEPOSIT_CHARGE`만 지원합니다. `ORDER_PAY`는 제거되었습니다.
- **Direct Order Payment**: 상품 주문은 예치금 전용입니다. PG 결제는 사용되지 않습니다.
- **PG Usage**: PG(Toss)는 오직 예치금 충전(`POST /payment/deposit/charge/*`)에만 사용됩니다.
- **paymentKey 암호화**: Toss에서 받은 `paymentKey`는 AES-256-GCM으로 암호화하여 DB에 저장됩니다.
- **Stock Deduction**: 재고 차감은 REST 동기 호출로 처리됩니다 (payment-service → product-service). 재고 차감 실패 시 예치금 자동 환급(보상 트랜잭션)됩니다.
- **Settlement Trigger**: 구매확정 시 order-service가 `order.purchase-confirmed` Kafka 이벤트를 발행하면, settlement-service가 소비하여 `settlement_target`을 생성합니다. 정산은 스케줄러(매월 25일) 또는 수동 트리거로 실행됩니다.
- **Deposit Sync Events**: 예치금 충전/차감/환급 시 Spring Event(AFTER_COMMIT)로 member-service 잔액을 비동기 동기화합니다 (`DepositChargedEvent`, `DepositDeductedEvent`, `DepositRefundedEvent`).
- **충전 한도**: 1회 충전 1,000원 이상 500,000원 이하. 보유 예치금 최대 1,000,000원.

---

## `POST /payment/orders/{orderId}/pay` — 예치금 결제 실행

예치금으로 주문을 결제합니다.
내부적으로 예치금 차감 → 재고 차감 → 주문 상태 PAID 변경 순으로 처리되며,
재고 차감 실패 시 보상 트랜잭션(예치금 환급)을 수행합니다.

### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 결제할 주문 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `X-Email-Verified` | boolean | O | 이메일 인증 여부 (gateway가 JWT에서 주입) |

### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "ORDER_PAID",
  "message": "결제가 완료되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "depositUsed": 50000,
    "balanceAfter": 50000
  }
}
```

**2. 클라이언트 오류 — 이메일 미인증**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "EMAIL_NOT_VERIFIED",
  "message": "이메일 인증이 완료된 회원만 이용 가능합니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 예치금 잔액 부족**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "DEPOSIT_INSUFFICIENT_BALANCE",
  "message": "예치금 잔액이 부족합니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 이미 결제된 주문**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ORDER_ALREADY_PAID",
  "message": "이미 결제된 주문입니다.",
  "data": null
}
```

**5. 서버 오류**

Status Code: `500 Internal Server Error`

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "요청을 처리하는 도중 서버에서 문제가 발생했습니다.",
  "data": null
}
```

---

## `POST /payment/deposit/charge/ready` — 예치금 충전 준비

예치금 충전을 준비합니다. 충전 금액을 검증하고, PG 결제창 호출에 필요한 데이터를 반환합니다.
내부적으로 `Payment(purpose=DEPOSIT_CHARGE, status=PENDING)` 레코드를 생성합니다.

### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `amount` | number | O | 충전 금액 (1,000원 이상 500,000원 이하) |

Request Body Example:

```json
{
  "amount": 50000
}
```

### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "DEPOSIT_CHARGE_READY",
  "message": "예치금 충전 준비가 완료되었습니다.",
  "data": {
    "paymentId": "770e8400-e29b-41d4-a716-446655440002",
    "amount": 50000,
    "pgProvider": "TOSS",
    "pgData": {
      "clientKey": "test_ck_xxx",
      "orderId": "DEPOSIT-20260402-A1B2C3D4",
      "amount": 50000,
      "orderName": "예치금 충전",
      "successUrl": "https://example.com/deposit/success",
      "failUrl": "https://example.com/deposit/fail"
    }
  }
}
```

**2. 클라이언트 오류 — 이메일 미인증**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "EMAIL_NOT_VERIFIED",
  "message": "이메일 인증이 완료된 회원만 이용 가능합니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 유효하지 않은 금액**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_CHARGE_AMOUNT",
  "message": "충전 금액은 1,000원 이상 500,000원 이하이어야 합니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 예치금 최대 보유 한도 초과**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "DEPOSIT_BALANCE_EXCEEDS_LIMIT",
  "message": "예치금 최대 보유 한도를 초과합니다.",
  "data": null
}
```

**5. 클라이언트 오류 — 비활성 회원**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "MEMBER_NOT_ACTIVE",
  "message": "활성 상태의 회원만 예치금 충전이 가능합니다.",
  "data": null
}
```

**6. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**7. 서버 오류**

Status Code: `500 Internal Server Error`

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "요청을 처리하는 도중 서버에서 문제가 발생했습니다.",
  "data": null
}
```

---

## `POST /payment/deposit/charge/confirm` — 예치금 충전 승인

PG 결제 승인을 완료하고, 예치금을 충전합니다.
Toss API 호출은 트랜잭션 밖에서 실행되며, 승인 성공 후 DB에 Payment/Deposit 레코드를 저장합니다.

### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `paymentKey` | string | O | Toss에서 발급한 결제 키 |
| `orderId` | string | O | 주문번호 (ready에서 생성한 `pgData.orderId`와 일치해야 함) |
| `amount` | number | O | 결제 금액 (ready에서 확정한 금액과 일치해야 함) |

Request Body Example:

```json
{
  "paymentKey": "tgen_20260402ABCDEF1234567890",
  "orderId": "DEPOSIT-20260402-A1B2C3D4",
  "amount": 50000
}
```

### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "DEPOSIT_CHARGED",
  "message": "예치금 충전이 완료되었습니다.",
  "data": {
    "paymentId": "770e8400-e29b-41d4-a716-446655440002",
    "chargedAmount": 50000,
    "balanceAfter": 100000,
    "pgTransactionId": "toss_txn_20260402ABC",
    "method": "카드",
    "paidAt": "2026-04-02T14:30:00"
  }
}
```

**2. 클라이언트 오류 — 금액 불일치**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "AMOUNT_MISMATCH",
  "message": "결제 금액이 요청 금액과 일치하지 않습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 결제 준비 건을 찾을 수 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PAYMENT_READY_NOT_FOUND",
  "message": "결제 준비 정보를 찾을 수 없습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 이미 승인된 결제**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "PAYMENT_ALREADY_CONFIRMED",
  "message": "이미 승인 처리된 결제입니다.",
  "data": null
}
```

**5. PG 승인 실패**

Status Code: `502 Bad Gateway`

```json
{
  "status": 502,
  "code": "PG_CONFIRM_FAILED",
  "message": "PG 결제 승인에 실패했습니다.",
  "data": null
}
```

**6. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**7. 서버 오류**

Status Code: `500 Internal Server Error`

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "요청을 처리하는 도중 서버에서 문제가 발생했습니다.",
  "data": null
}
```

---

## `GET /payment/deposit/history` — 예치금 충전/사용 내역

예치금 충전/사용/환불 내역을 조회합니다.

### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `type` | string | X | 내역 유형 필터 (`CHARGE`, `USE`, `REFUND`) |
| `page` | number | X | 페이지 번호 (기본값: 0) |
| `size` | number | X | 페이지당 항목 수 (기본값: 20, 최대: 100) |

### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "DEPOSIT_HISTORY_FOUND",
  "message": "예치금 내역을 조회했습니다.",
  "data": {
    "content": [
      {
        "depositId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "type": "CHARGE",
        "amount": 50000,
        "balanceAfter": 50000,
        "description": "예치금 충전",
        "orderId": null,
        "createdAt": "2026-04-02T10:00:00"
      },
      {
        "depositId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
        "type": "USE",
        "amount": 30000,
        "balanceAfter": 20000,
        "description": "주문 결제",
        "orderId": "550e8400-e29b-41d4-a716-446655440001",
        "createdAt": "2026-04-02T11:00:00"
      },
      {
        "depositId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
        "type": "REFUND",
        "amount": 30000,
        "balanceAfter": 50000,
        "description": "주문 취소 환급",
        "orderId": "550e8400-e29b-41d4-a716-446655440001",
        "createdAt": "2026-04-02T15:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

**2. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 잘못된 type 파라미터**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "BAD_REQUEST",
  "message": "잘못된 요청입니다.",
  "data": null
}
```

**4. 서버 오류**

Status Code: `500 Internal Server Error`

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "요청을 처리하는 도중 서버에서 문제가 발생했습니다.",
  "data": null
}
```

---

## `POST /payment/{paymentId}/refund` — 환불 요청 ❌ 미구현

---

## `POST /payment/webhook/pg` — PG 웹훅 수신 ❌ 미구현

---

## Internal API

### `POST /internal/deposit/deduct` — 예치금 차감

payment-service의 결제 실행(`PayOrderService`) 내부에서 직접 처리됩니다. 외부 호출자는 없습니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-Internal-Secret` | string | O | 내부 서비스 인증 시크릿 |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `memberId` | UUID | O | 예치금 차감 대상 회원 ID |
| `orderId` | UUID | O | 차감 원인 주문 ID |
| `amount` | number | O | 차감 금액 |
| `description` | string | X | 차감 사유 |

Request Body Example:

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440000",
  "orderId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 50000,
  "description": "주문 결제"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "DEPOSIT_DEDUCTED",
  "message": "예치금이 차감되었습니다.",
  "data": {
    "depositId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "balanceAfter": 50000
  }
}
```

**2. 클라이언트 오류 — 잔액 부족**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "DEPOSIT_INSUFFICIENT_BALANCE",
  "message": "예치금 잔액이 부족합니다.",
  "data": null
}
```

**3. 인증 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "내부 서비스 인증에 실패했습니다.",
  "data": null
}
```

---

### `POST /internal/deposit/refund` — 예치금 환급

내부 환급 처리 API입니다. 외부 공개 환불 API(`POST /payment/{paymentId}/refund`)와 별개로, payment-service 내부에서 보상 트랜잭션 시 사용됩니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `X-Internal-Secret` | string | O | 내부 서비스 인증 시크릿 |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `memberId` | UUID | O | 환급 대상 회원 ID |
| `orderId` | UUID | O | 환급 원인 주문 ID |
| `amount` | number | O | 환급 금액 |
| `description` | string | X | 환급 사유 |

Request Body Example:

```json
{
  "memberId": "550e8400-e29b-41d4-a716-446655440000",
  "orderId": "550e8400-e29b-41d4-a716-446655440001",
  "amount": 50000,
  "description": "재고 부족으로 인한 결제 취소"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "DEPOSIT_REFUNDED",
  "message": "예치금이 환급되었습니다.",
  "data": {
    "depositId": "d4e5f6a7-b8c9-0123-defa-234567890123",
    "balanceAfter": 100000
  }
}
```

**2. 인증 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "내부 서비스 인증에 실패했습니다.",
  "data": null
}
```

