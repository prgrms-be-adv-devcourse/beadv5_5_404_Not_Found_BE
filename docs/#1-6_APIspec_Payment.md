# API Specification - Payment Module

> 도서 이커머스 플랫폼 API 명세서
> Payment API (/payment/*)

---

## 📌 Payment API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 환불 요청 | POST | /payment/{paymentId}/refund | 환불 처리 |
| 예치금 충전 준비 | POST | /payment/deposit/charge/ready | 충전 요청 |
| 예치금 충전 승인 | POST | /payment/deposit/charge/confirm | 충전 승인 |
| 예치금 내역 조회 | GET | /payment/deposit/history | 사용 내역 |
| PG 웹훅 수신 | POST | /payment/webhook/pg | 외부 결제 콜백 |
| 내 정산 조회 | GET | /payment/settlement/me | 정산 조회 |
| 정산 상세 조회 | GET | /payment/settlement/{settlementId} | 정산 상세 |
| 출금 신청 | POST | /payment/settlement/payout | 출금 요청 |
| 정산 요약 | GET | /payment/settlement/summary | 요약 |
| 수수료 정책 등록 | POST | /payment/commission | 정책 등록 |
| 수수료 정책 조회 | GET | /payment/commission | 정책 조회 |

### Payment → Order Internal 호출

| 기능 | Method | Endpoint | 설명 | 대상 서비스 |
|------|--------|----------|------|------------|
| 주문 상태 변경 | POST | /internal/order/{orderId}/status | 결제 완료 후 PENDING → PAID 변경 | order-service |

> payment-service가 결제 실행(`POST /payment/orders/{orderId}/pay`) 완료 후, order-service의 내부 API를 호출하여 주문 상태를 PAID로 변경하고 depositUsed를 전달합니다.

Request Body:

```json
{
  "status": "PAID",
  "depositUsed": 55000
}
```

### Notes

- **Payment Purpose**: PaymentPurpose는 DEPOSIT_CHARGE만 지원합니다. ORDER_PAY는 제거되었습니다.
- **Direct Order Payment**: 상품 주문은 예치금 전용입니다. PG 결제는 사용되지 않습니다.
- **PG Usage**: PG는 오직 예치금 충전(POST /payment/deposit/charge/*)에만 사용됩니다.
- **Stock Events**: 재고 차감/복원은 Kafka를 통해 처리됩니다. (StockDeductedEvent, StockRestoredEvent)
- **Settlement Trigger**: 구매확정 시 Order 서비스가 `PurchaseConfirmedEvent`(Kafka)를 발행하면, Payment 서비스가 consume하여 `settlement_target`을 생성합니다. 정산은 스케줄러 배치(매월 25일)로 실행됩니다.
- **Other Events**: 예치금 충전/환불 완료 등 내부 이벤트는 Spring Event로 처리됩니다.

---

### `POST /payment/{paymentId}/refund` — 환불 요청

결제 건에 대한 환불을 요청합니다. 전액 환불 및 부분 환불을 지원합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `paymentId` | number | O | 환불할 결제 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `refundAmount` | number | X | 환불 금액 (미입력 시 전액 환불) |
| `reason` | string | O | 환불 사유 |

Request Body Example:

```json
{
  "refundAmount": 25000,
  "reason": "상품 불량으로 인한 부분 환불 요청"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "REFUND_REQUESTED",
  "message": "환불이 성공적으로 요청되었습니다.",
  "data": {
    "refundId": 7001,
    "paymentId": 3001,
    "refundAmount": 25000,
    "refundStatus": "PROCESSING",
    "reason": "상품 불량으로 인한 부분 환불 요청",
    "requestedAt": "2026-03-18T15:00:00Z"
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 결제**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PAYMENT_NOT_FOUND",
  "message": "해당 결제 건을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 환불 금액 초과**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "REFUND_AMOUNT_EXCEEDED",
  "message": "환불 가능 금액을 초과했습니다.",
  "data": {
    "refundableAmount": 54000,
    "requestedAmount": 60000
  }
}
```

**4. 클라이언트 오류 — 환불 불가 상태**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "REFUND_NOT_ALLOWED",
  "message": "현재 결제 상태에서는 환불할 수 없습니다.",
  "data": {
    "currentStatus": "FAILED"
  }
}
```

**5. 클라이언트 오류 — 환불 사유 누락**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "MISSING_REQUIRED_FIELD",
  "message": "환불 사유를 입력해주세요.",
  "data": {
    "missingFields": ["reason"]
  }
}
```

**6. PG사 환불 처리 실패**

Status Code: `502 Bad Gateway`

```json
{
  "status": 502,
  "code": "PG_REFUND_FAILED",
  "message": "PG사 환불 처리에 실패했습니다.",
  "data": {
    "pgErrorCode": "REFUND_TIMEOUT",
    "pgErrorMessage": "환불 요청 시간이 초과되었습니다."
  }
}
```

**7. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**8. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 결제 건에 대한 환불 권한이 없습니다.",
  "data": null
}
```

**9. 서버 오류**

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

### `POST /payment/deposit/charge/ready` — 예치금 충전 준비

예치금 충전을 준비합니다. 충전 금액을 검증하고, PG 결제창 호출에 필요한 데이터를 반환합니다. 내부적으로 PAYMENT(purpose=DEPOSIT_CHARGE, status=PENDING) 레코드를 생성합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `amount` | number | O | 충전 금액 (최소 1,000원 이상) |

Request Body Example:

```json
{
  "amount": 50000
}
```

#### Response

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
      "orderId": "DEPOSIT-20260320-XYZ789",
      "amount": 50000,
      "orderName": "예치금 충전",
      "successUrl": "https://example.com/deposit/success",
      "failUrl": "https://example.com/deposit/fail"
    }
  }
}
```

**2. 클라이언트 오류 — 유효하지 않은 금액**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_CHARGE_AMOUNT",
  "message": "충전 금액은 1,000원 이상이어야 합니다.",
  "data": null
}
```

**3. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
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

### `POST /payment/deposit/charge/confirm` — 예치금 충전 승인

PG 결제 승인을 완료하고, 예치금을 충전합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `paymentKey` | string | O | 토스페이먼츠에서 발급한 결제 키 |
| `orderId` | string | O | 주문번호 (ready에서 생성한 값과 일치해야 함) |
| `amount` | number | O | 결제 금액 (ready에서 확정한 금액과 일치해야 함) |

Request Body Example:

```json
{
  "paymentKey": "tgen_20260320ABCDEF1234567890",
  "orderId": "DEPOSIT-20260320-XYZ789",
  "amount": 50000
}
```

#### Response

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
    "pgTransactionId": "toss_txn_20260320ABC",
    "method": "CARD",
    "paidAt": "2026-03-20T14:30:00"
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
  "message": "PG사 결제 승인에 실패했습니다.",
  "data": {
    "pgErrorCode": "REJECT_CARD_COMPANY",
    "pgErrorMessage": "카드사에서 거절되었습니다."
  }
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

### `GET /payment/deposit/history` — 예치금 충전/사용 내역

예치금 충전/사용/환불 내역을 조회합니다.

#### Request

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

#### Response

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
        "depositId": 1,
        "type": "CHARGE",
        "amount": 50000,
        "balanceAfter": 50000,
        "description": "예치금 충전",
        "createdAt": "2026-03-19T10:00:00Z"
      },
      {
        "depositId": 2,
        "type": "USE",
        "amount": 30000,
        "balanceAfter": 20000,
        "description": "주문 #ORD-20260319-001 차감",
        "orderId": 1001,
        "createdAt": "2026-03-19T11:00:00Z"
      },
      {
        "depositId": 3,
        "type": "REFUND",
        "amount": 30000,
        "balanceAfter": 50000,
        "description": "주문 #ORD-20260319-001 환불 복원",
        "orderId": 1001,
        "createdAt": "2026-03-19T15:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

**2. 클라이언트 오류 — 유효하지 않은 내역 유형**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_DEPOSIT_TYPE",
  "message": "유효하지 않은 예치금 내역 유형입니다.",
  "data": {
    "allowedTypes": ["CHARGE", "USE", "REFUND"]
  }
}
```

**3. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
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

### `POST /payment/webhook/pg` — PG 웹훅 수신

PG사로부터 결제 상태 변경 알림을 수신합니다. PG사 서버에서 호출하는 엔드포인트입니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `X-PG-Signature` | string | O | PG사 웹훅 서명 (요청 위변조 검증용) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `eventType` | string | O | 이벤트 유형 (`PAYMENT_APPROVED`, `PAYMENT_FAILED`, `PAYMENT_CANCELLED`, `REFUND_COMPLETED`) |
| `pgTransactionId` | string | O | PG사 거래 ID |
| `merchantOrderId` | string | O | 가맹점 주문 ID |
| `amount` | number | O | 금액 |
| `currency` | string | O | 통화 코드 |
| `status` | string | O | PG사 결제 상태 |
| `timestamp` | string | O | 이벤트 발생 시각 (ISO 8601) |
| `metadata` | object | X | 추가 메타데이터 |

Request Body Example:

```json
{
  "eventType": "PAYMENT_APPROVED",
  "pgTransactionId": "pg_txn_abc123def456",
  "merchantOrderId": "order_1001",
  "amount": 54000,
  "currency": "KRW",
  "status": "SUCCESS",
  "timestamp": "2026-03-18T14:30:00Z",
  "metadata": {
    "cardCompany": "신한카드",
    "approvalNumber": "12345678"
  }
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "WEBHOOK_RECEIVED",
  "message": "웹훅이 성공적으로 수신되었습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 서명 검증 실패**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "INVALID_SIGNATURE",
  "message": "웹훅 서명 검증에 실패했습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 중복 이벤트**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "DUPLICATE_EVENT",
  "message": "이미 처리된 이벤트입니다.",
  "data": {
    "pgTransactionId": "pg_txn_abc123def456"
  }
}
```

**4. 클라이언트 오류 — 잘못된 이벤트 형식**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_WEBHOOK_PAYLOAD",
  "message": "웹훅 요청 형식이 올바르지 않습니다.",
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

### `GET /payment/settlement/me` — 내 정산 조회

로그인한 판매자의 정산 내역을 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token (판매자) |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `page` | number | X | 페이지 번호 (기본값: 1) |
| `limit` | number | X | 페이지당 항목 수 (기본값: 20, 최대: 100) |
| `status` | string | X | 정산 상태 필터 (`PENDING`, `COMPLETED`, `FAILED`) |
| `startDate` | string | X | 조회 시작일 (형식: `YYYY-MM-DD`) |
| `endDate` | string | X | 조회 종료일 (형식: `YYYY-MM-DD`) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "정산 내역 조회에 성공했습니다.",
  "data": {
    "settlements": [
      {
        "settlementId": 8001,
        "settlementPeriod": {
          "startDate": "2026-03-01",
          "endDate": "2026-03-15"
        },
        "totalSalesAmount": 1500000,
        "commissionAmount": 150000,
        "netAmount": 1350000,
        "status": "COMPLETED",
        "settledAt": "2026-03-18T00:00:00Z"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalItems": 90,
      "limit": 20
    }
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

**3. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "판매자만 정산 내역을 조회할 수 있습니다.",
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

### `GET /payment/settlement/{settlementId}` — 정산 상세 조회

특정 정산 건의 상세 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `settlementId` | number | O | 조회할 정산 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token (판매자) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "정산 상세 조회에 성공했습니다.",
  "data": {
    "settlementId": 8001,
    "sellerId": 201,
    "settlementPeriod": {
      "startDate": "2026-03-01",
      "endDate": "2026-03-15"
    },
    "totalSalesAmount": 1500000,
    "refundedAmount": 50000,
    "commissionAmount": 150000,
    "commissionRate": 10.0,
    "netAmount": 1300000,
    "status": "COMPLETED",
    "paymentDetails": [
      {
        "paymentId": 3001,
        "orderId": 1001,
        "amount": 54000,
        "commission": 5400,
        "netAmount": 48600,
        "paidAt": "2026-03-05T14:30:00Z"
      }
    ],
    "settledAt": "2026-03-18T00:00:00Z",
    "paidOutAt": null
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 정산**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "SETTLEMENT_NOT_FOUND",
  "message": "해당 정산 건을 찾을 수 없습니다.",
  "data": null
}
```

**3. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 정산 정보에 대한 접근 권한이 없습니다.",
  "data": null
}
```

**4. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
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

### `POST /payment/settlement/payout` — 출금 신청

정산된 금액의 출금을 신청합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token (판매자) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `settlementIds` | number[] | O | 출금 대상 정산 ID 배열 |
| `bankCode` | string | O | 출금 은행 코드 |
| `bankAccount` | string | O | 출금 계좌번호 |
| `accountHolder` | string | O | 예금주명 |

Request Body Example:

```json
{
  "settlementIds": [8001, 8002],
  "bankCode": "004",
  "bankAccount": "123-456-789012",
  "accountHolder": "홍길동"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PAYOUT_REQUESTED",
  "message": "출금이 성공적으로 신청되었습니다.",
  "data": {
    "payoutId": 9001,
    "totalAmount": 2650000,
    "bankCode": "004",
    "bankAccount": "***-***-***012",
    "accountHolder": "홍길동",
    "payoutStatus": "PROCESSING",
    "estimatedArrival": "2026-03-20T00:00:00Z",
    "requestedAt": "2026-03-18T16:00:00Z"
  }
}
```

**2. 클라이언트 오류 — 출금 가능 금액 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "NO_PAYABLE_AMOUNT",
  "message": "출금 가능한 정산 금액이 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 정산 미완료 상태**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "SETTLEMENT_NOT_READY",
  "message": "아직 정산이 완료되지 않은 건이 포함되어 있습니다.",
  "data": {
    "pendingSettlementIds": [8002]
  }
}
```

**4. 클라이언트 오류 — 잘못된 계좌 정보**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_ACCOUNT_INFO",
  "message": "계좌 정보가 유효하지 않습니다.",
  "data": null
}
```

**5. 클라이언트 오류 — 최소 출금 금액 미달**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "BELOW_MINIMUM_PAYOUT",
  "message": "최소 출금 금액(1,000원) 이상부터 출금할 수 있습니다.",
  "data": {
    "minimumAmount": 1000,
    "requestedAmount": 500
  }
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

**7. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "출금 신청 권한이 없습니다.",
  "data": null
}
```

**8. 서버 오류**

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

### `GET /payment/settlement/summary` — 정산 요약

판매자의 정산 현황 요약 정보를 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token (판매자) |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `period` | string | X | 조회 기간 (`week`, `month`, `quarter`, `year`) (기본값: `month`) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "정산 요약 조회에 성공했습니다.",
  "data": {
    "period": "month",
    "totalSalesAmount": 5200000,
    "totalRefundedAmount": 200000,
    "totalCommission": 500000,
    "totalNetAmount": 4500000,
    "pendingSettlement": 1200000,
    "availablePayout": 3300000,
    "paidOutAmount": 0,
    "settlementCount": {
      "pending": 2,
      "settled": 5,
      "paidOut": 0
    }
  }
}
```

**2. 클라이언트 오류 — 잘못된 기간 값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_PERIOD",
  "message": "유효하지 않은 조회 기간입니다. (허용값: week, month, quarter, year)",
  "data": null
}
```

**3. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**4. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "판매자만 정산 요약을 조회할 수 있습니다.",
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

### `POST /payment/commission` — 수수료 정책 등록

수수료 정책을 등록합니다. 관리자 전용 API입니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token (관리자) |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | O | 수수료 정책명 |
| `type` | string | O | 수수료 유형 (`PERCENTAGE`, `FIXED`) |
| `rate` | number | 조건부 | 수수료율 (%) (type이 `PERCENTAGE`일 때 필수, 0~100) |
| `fixedAmount` | number | 조건부 | 고정 수수료 금액 (type이 `FIXED`일 때 필수) |
| `categoryId` | number | X | 적용 카테고리 ID (미입력 시 전체 카테고리 적용) |
| `effectiveFrom` | string | O | 적용 시작일 (형식: `YYYY-MM-DD`) |
| `effectiveTo` | string | X | 적용 종료일 (형식: `YYYY-MM-DD`, 미입력 시 무기한) |

Request Body Example:

```json
{
  "name": "기본 판매 수수료",
  "type": "PERCENTAGE",
  "rate": 10.0,
  "categoryId": null,
  "effectiveFrom": "2026-04-01",
  "effectiveTo": null
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "COMMISSION_CREATED",
  "message": "수수료 정책이 성공적으로 등록되었습니다.",
  "data": {
    "commissionId": 101,
    "name": "기본 판매 수수료",
    "type": "PERCENTAGE",
    "rate": 10.0,
    "effectiveFrom": "2026-04-01"
  }
}
```

**2. 클라이언트 오류 — 기간 중복**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "COMMISSION_PERIOD_OVERLAP",
  "message": "동일 카테고리에 적용 기간이 중복되는 수수료 정책이 존재합니다.",
  "data": {
    "conflictingCommissionId": 99
  }
}
```

**3. 클라이언트 오류 — 유효하지 않은 수수료율**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_COMMISSION_RATE",
  "message": "수수료율은 0에서 100 사이여야 합니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 필수 필드 누락**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "MISSING_REQUIRED_FIELD",
  "message": "필수 입력 항목이 누락되었습니다.",
  "data": {
    "missingFields": ["name", "type"]
  }
}
```

**5. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**6. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "관리자만 수수료 정책을 등록할 수 있습니다.",
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

### `GET /payment/commission` — 수수료 정책 조회

수수료 정책 목록을 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `categoryId` | number | X | 특정 카테고리의 수수료 정책 필터링 |
| `activeOnly` | boolean | X | 현재 적용 중인 정책만 조회 (기본값: `true`) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "수수료 정책 조회에 성공했습니다.",
  "data": {
    "commissions": [
      {
        "commissionId": 101,
        "name": "기본 판매 수수료",
        "type": "PERCENTAGE",
        "rate": 10.0,
        "fixedAmount": null,
        "categoryId": null,
        "categoryName": "전체",
        "effectiveFrom": "2026-04-01",
        "effectiveTo": null,
        "isActive": true
      },
      {
        "commissionId": 102,
        "name": "전자기기 수수료",
        "type": "PERCENTAGE",
        "rate": 8.0,
        "fixedAmount": null,
        "categoryId": 10,
        "categoryName": "전자기기",
        "effectiveFrom": "2026-04-01",
        "effectiveTo": "2026-12-31",
        "isActive": true
      }
    ]
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

**3. 서버 오류**

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
