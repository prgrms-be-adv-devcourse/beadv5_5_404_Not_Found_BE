# API Specification - Settlement Module

> 도서 이커머스 플랫폼 API 명세서
> Settlement API (/api/settlements/*, /internal/settlements/*)

---

## 📌 Settlement API

> ★ = 상품선택 → 결제완료 → 정산완료 필수 플로우

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 내 정산 조회 | GET | /api/settlements/me | ✅ | ★ | 판매자 정산 내역 조회 (SELLER 전용) |
| 월 정산 수동 실행 | POST | /internal/settlements/execute | ✅ | ★ | 관리자 수동 트리거 (Internal) |

---

## 📌 정산 흐름 요약

```
구매확정 (Order Service)
  → Kafka: order.purchase-confirmed
  → Settlement Service: SettlementTarget 생성 (PENDING)
  → 매월 25일 00:00 스케줄러 자동 실행 (또는 /internal/settlements/execute 수동 트리거)
  → 전월 PENDING SettlementTarget 집계
  → Settlement 생성 (수수료 차감 후 netAmount 계산)
  → SettlementTarget 상태 SETTLED로 갱신
```

---

### `GET /api/settlements/me` — 내 정산 조회

로그인한 판매자의 정산 완료 내역을 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token (SELLER 전용) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SETTLEMENT_LIST_GET_SUCCESS",
  "message": "정산 내역 조회에 성공했습니다.",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "sellerId": "550e8400-e29b-41d4-a716-446655440001",
      "periodStart": "2026-03-01",
      "periodEnd": "2026-03-31",
      "totalSalesAmount": 330000,
      "feeAmount": 33000,
      "netAmount": 297000,
      "settledAt": "2026-04-25T00:00:00",
      "status": "COMPLETED"
    }
  ]
}
```

**2. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 작업에 대한 권한이 없습니다.",
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

## 📌 Internal API (서비스 간 통신 / 관리자 수동 트리거)

> `X-Internal-Secret` 헤더 인증 필요. 외부 노출 없음.

### `POST /internal/settlements/execute` — 월 정산 수동 실행

특정 월의 정산을 수동으로 실행합니다. 스케줄러 미실행 시 관리자가 직접 트리거합니다.

> 미래 월 지정 불가 (`@PastOrPresent` 검증).

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `targetMonth` | string | O | 정산 대상 월 (`yyyy-MM` 형식, 현재 월 이하) |

Request Body Example:

```json
{
  "targetMonth": "2026-03"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

(body 없음)

**2. 클라이언트 오류 — 미래 월 지정**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_PARAMETER",
  "message": "미래 월은 정산할 수 없습니다.",
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

## 📌 스케줄러 명세

| 항목 | 값 |
|------|-----|
| 실행 시각 | 매월 25일 00:00:00 |
| 대상 월 | 실행 시점 전월 (`YearMonth.now().minusMonths(1)`) |
| 중복 방지 | ShedLock (`lockAtMostFor: 1h`, `lockAtLeastFor: 10m`) |
| Lock 이름 | `monthly-settlement` |
| 수수료율 | `application.yml` — `settlement.fee-rate: 0.03` (코드 수정 없이 변경 가능) |

---

> Enum 정의 (SettlementStatus, SettlementTargetStatus) 는 [EnumSpec.md](EnumSpec.md) 참조
