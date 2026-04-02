# API Specification - Order Module

> 도서 이커머스 플랫폼 API 명세서
> Order API (/order/*)

---

## 📌 Order API

> ★ = 상품선택 → 결제완료 → 정산완료 필수 플로우

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 장바구니 조회 | GET | /order/cart | ✅ | ★ | 장바구니 조회 |
| 장바구니 상품 추가 | POST | /order/cart/item | ✅ | ★ | 상품 추가 |
| 장바구니 수량 수정 | PATCH | /order/cart/item/{cartItemId} | ✅ | | 수량 변경 |
| 장바구니 항목 삭제 | DELETE | /order/cart/item/{cartItemId} | ✅ | | 항목 삭제 |
| 장바구니 비우기 | DELETE | /order/cart | ✅ | | 전체 삭제 |
| 결제 페이지 정보 조회 | GET | /order/checkout | ✅ | ★ | 상품+배송지+잔액 조회 |
| 주문 생성 | POST | /order | ✅ | ★ | 주문 생성 (PENDING 상태, 결제 미포함) |
| 주문 목록 조회 | GET | /order | ✅ | | 주문 리스트 |
| 주문 상세 조회 | GET | /order/{orderId} | ✅ | | 주문 상세 |
| 주문 취소 | POST | /order/{orderId}/cancel | 🟡 | | 주문 취소 (PENDING 🟢 / PAID·CONFIRMED 예치금 환급 🟢 + 재고 복원 🔴 STUB) |
| 구매확정 | POST | /order/{orderId}/confirm | ✅ | | 구매확정 (DELIVERED → PURCHASE_CONFIRMED). 🟡 현재 PAID 시 자동 전이 운영 중 |
| 반품 신청 | POST | /order/{orderId}/return | ✅ | | 반품 요청 |
| 송장 등록/배송 정보 수정 | PATCH | /order/{orderId}/shipment | ✅ | | 🟡 배송 모듈 미분리 — order-service 내 포함 |

### Notes

- **Cart Item Stock Check**: 장바구니에 상품을 추가할 때는 재고를 확인하지 않습니다. 어떤 상품이든 추가할 수 있습니다.
- **Cart Item Latest Info**: 장바구니 조회 시 상품의 최신 가격과 재고 정보를 함께 반환합니다. 가격 변동 또는 품절 여부를 표시합니다.
- **Cart**: 장바구니는 회원 전용 기능입니다. 비회원 장바구니는 지원하지 않습니다.
- **Order Status**: 주문은 생성 시점에 PENDING 상태입니다. 결제 완료(`POST /payment/orders/{orderId}/pay`) 후 PAID로 전환됩니다. 🟡 **현재 PAID → DELIVERED → PURCHASE_CONFIRMED 자동 전이 운영 중** (배송 모듈 미분리). 유효한 상태: PENDING, PAID, CONFIRMED(🟡 미사용), SHIPPING, DELIVERED, PURCHASE_CONFIRMED, CANCELLED
- **Order-Payment Separation**: `POST /order`는 주문 정보만 생성(PENDING)합니다. 실제 결제(재고 차감 + 예치금 차감)는 payment-service의 `POST /payment/orders/{orderId}/pay`가 담당합니다.
- **Purchase Confirm**: 배송 완료(DELIVERED) 후 구매자가 수동 확정하거나, 7일 경과 시 스케줄러가 자동으로 PURCHASE_CONFIRMED로 전환합니다. 전환 시 PurchaseConfirmedEvent(Kafka)를 발행하여 Settlement 서비스가 정산 대상을 생성합니다.
- **Payment**: 모든 상품 결제는 예치금만 사용합니다. PG는 예치금 충전(POST /payment/deposit/charge/*) 시에만 사용됩니다.
- **Stock**: 재고 차감은 REST 동기 호출로 처리됩니다 (payment-service → product-service). 🔴 재고 복원 엔드포인트(`POST /internal/products/stock/restore`)는 미노출 — order-service STUB(log.warn만 출력).

---

### `GET /order/cart` — 장바구니 조회

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CART_FETCH_SUCCESS",
  "message": "장바구니 조회에 성공했습니다.",
  "data": {
    "cartId": "550e8400-e29b-41d4-a716-446655440010",
    "items": [
      {
        "cartItemId": "550e8400-e29b-41d4-a716-446655440011",
        "productId": "550e8400-e29b-41d4-a716-446655440101",
        "quantity": 2
      },
      {
        "cartItemId": "550e8400-e29b-41d4-a716-446655440012",
        "productId": "550e8400-e29b-41d4-a716-446655440102",
        "quantity": 1
      }
    ]
  }
}
```

**2. 클라이언트 오류 — 장바구니 식별 정보 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "CART_IDENTIFIER_MISSING",
  "message": "장바구니 식별 정보가 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 장바구니 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CART_NOT_FOUND",
  "message": "장바구니를 찾을 수 없습니다.",
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

### `POST /order/cart/item` — 장바구니 상품 추가

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `productId` | number | O | 장바구니에 담을 상품 ID |
| `quantity` | number | O | 담을 수량 |

Request Body Example:

```json
{
  "productId": 101,
  "quantity": 2
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "CART_ITEM_CREATE_SUCCESS",
  "message": "장바구니에 상품이 추가되었습니다.",
  "data": {
    "cartItemId": "550e8400-e29b-41d4-a716-446655440011",
    "productId": "550e8400-e29b-41d4-a716-446655440101",
    "quantity": 2
  }
}
```

**2. 클라이언트 오류 — 장바구니 식별 정보 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "CART_IDENTIFIER_MISSING",
  "message": "장바구니 식별 정보가 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 잘못된 요청**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "quantity": "수량은 1 이상이어야 합니다."
  }
}
```

**4. 클라이언트 오류 — 상품 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "상품을 찾을 수 없습니다.",
  "data": null
}
```

**5. 클라이언트 오류 — 장바구니 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CART_NOT_FOUND",
  "message": "장바구니를 찾을 수 없습니다.",
  "data": null
}
```

**6. 서버 오류**

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

### `PATCH /order/cart/item/{cartItemId}` — 장바구니 수량 수정

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cartItemId` | UUID | O | 수정할 장바구니 항목 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `quantity` | number | O | 변경할 수량 |

Request Body Example:

```json
{
  "quantity": 3
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CART_ITEM_UPDATE_SUCCESS",
  "message": "장바구니 수량이 수정되었습니다.",
  "data": {
    "cartItemId": "550e8400-e29b-41d4-a716-446655440011",
    "quantity": 3
  }
}
```

**2. 클라이언트 오류 — 장바구니 식별 정보 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "CART_IDENTIFIER_MISSING",
  "message": "장바구니 식별 정보가 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 잘못된 요청**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "quantity": "수량은 1 이상이어야 합니다."
  }
}
```

**4. 클라이언트 오류 — 장바구니 항목 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CART_ITEM_NOT_FOUND",
  "message": "장바구니 항목을 찾을 수 없습니다.",
  "data": null
}
```

**5. 클라이언트 오류 — 접근 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "CART_ITEM_ACCESS_DENIED",
  "message": "해당 장바구니 항목에 접근할 권한이 없습니다.",
  "data": null
}
```

**6. 서버 오류**

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

### `DELETE /order/cart/item/{cartItemId}` — 장바구니 항목 삭제

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cartItemId` | UUID | O | 삭제할 장바구니 항목 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CART_ITEM_DELETE_SUCCESS",
  "message": "장바구니 항목이 삭제되었습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 장바구니 식별 정보 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "CART_IDENTIFIER_MISSING",
  "message": "장바구니 식별 정보가 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 장바구니 항목 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CART_ITEM_NOT_FOUND",
  "message": "장바구니 항목을 찾을 수 없습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 접근 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "CART_ITEM_ACCESS_DENIED",
  "message": "해당 장바구니 항목에 접근할 권한이 없습니다.",
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

### `DELETE /order/cart` — 장바구니 비우기

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CART_CLEAR_SUCCESS",
  "message": "장바구니가 비워졌습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 장바구니 식별 정보 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "CART_IDENTIFIER_MISSING",
  "message": "장바구니 식별 정보가 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 장바구니 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CART_NOT_FOUND",
  "message": "장바구니를 찾을 수 없습니다.",
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

### `GET /order/checkout` — 결제 페이지 정보 조회

결제 페이지 로드 시 필요한 상품 정보, 배송지, 예치금 정보를 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cartItemIds` | string | X | 선택 구매할 장바구니 항목 ID (쉼표로 구분) |
| `productId` | UUID | X | 단일 상품 ID (productId + quantity로 직구매) |
| `quantity` | number | X | 상품 수량 (productId와 함께 사용) |

#### Response

**1. 요청 성공 — 장바구니 아이템 기반**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CHECKOUT_READY",
  "message": "결제 페이지 정보를 조회했습니다.",
  "data": {
    "checkoutId": "chk_20260320_001",
    "items": [
      {
        "cartItemId": "550e8400-e29b-41d4-a716-446655440011",
        "productId": "550e8400-e29b-41d4-a716-446655440101",
        "productName": "도서 A",
        "price": 15000,
        "quantity": 2,
        "subtotal": 30000,
        "imageUrl": "https://example.com/book-a.jpg"
      },
      {
        "cartItemId": "550e8400-e29b-41d4-a716-446655440012",
        "productId": "550e8400-e29b-41d4-a716-446655440102",
        "productName": "도서 B",
        "price": 22000,
        "quantity": 1,
        "subtotal": 22000,
        "imageUrl": "https://example.com/book-b.jpg"
      }
    ],
    "totalAmount": 52000,
    "shippingFee": 3000,
    "addresses": [
      {
        "addressId": "550e8400-e29b-41d4-a716-446655440001",
        "recipient": "홍길동",
        "phone": "01012345678",
        "address": "서울특별시 강남구 테헤란로 1 101동 1001호",
        "isDefault": true
      }
    ],
    "depositBalance": 50000
  }
}
```

**2. 요청 성공 — 단일 상품 직구매**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CHECKOUT_READY",
  "message": "결제 페이지 정보를 조회했습니다.",
  "data": {
    "checkoutId": "chk_20260320_002",
    "items": [
      {
        "productId": "550e8400-e29b-41d4-a716-446655440101",
        "productName": "도서 A",
        "price": 15000,
        "quantity": 1,
        "subtotal": 15000,
        "imageUrl": "https://example.com/book-a.jpg"
      }
    ],
    "totalAmount": 15000,
    "shippingFee": 2500,
    "addresses": [
      {
        "addressId": "550e8400-e29b-41d4-a716-446655440001",
        "recipient": "홍길동",
        "phone": "01012345678",
        "address": "서울특별시 강남구 테헤란로 1 101동 1001호",
        "isDefault": true
      }
    ],
    "depositBalance": 50000
  }
}
```

**3. 클라이언트 오류 — 선택 아이템 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "NO_ITEMS_SELECTED",
  "message": "구매할 상품을 선택해주세요.",
  "data": null
}
```

**4. 클라이언트 오류 — 인증 실패**

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

### `POST /order` — 주문 생성 + 예치금 결제

장바구니 또는 선택된 상품으로 주문을 생성합니다. 서버가 합계를 계산하고 재고를 검증한 후 예치금 차감, 재고 차감, 주문 생성을 동시에 처리합니다(단일 트랜잭션).

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `items` | object[] | O | 주문 항목 배열 (최소 1개) |
| `items[].productId` | UUID | O | 상품 ID |
| `items[].quantity` | number | O | 수량 (1 이상) |
| `items[].cartItemId` | UUID | X | 장바구니 항목 ID (장바구니 구매 시) |
| `addressId` | UUID | O | 배송지 ID |

Request Body Example:

```json
{
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440101",
      "quantity": 2,
      "cartItemId": "550e8400-e29b-41d4-a716-446655440011"
    },
    {
      "productId": "550e8400-e29b-41d4-a716-446655440102",
      "quantity": 1,
      "cartItemId": "550e8400-e29b-41d4-a716-446655440012"
    }
  ],
  "addressId": "550e8400-e29b-41d4-a716-446655440001"
}
```

또는 직구매:

```json
{
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440101",
      "quantity": 1
    }
  ],
  "addressId": "550e8400-e29b-41d4-a716-446655440001"
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "ORDER_CREATED",
  "message": "주문이 완료되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655441001",
    "orderNumber": "20260320-000001",
    "orderStatus": "PAID",
    "items": [
      {
        "orderItemId": "550e8400-e29b-41d4-a716-446655440201",
        "productId": "550e8400-e29b-41d4-a716-446655440101",
        "productName": "도서 A",
        "price": 15000,
        "quantity": 2,
        "subtotal": 30000
      },
      {
        "orderItemId": "550e8400-e29b-41d4-a716-446655440202",
        "productId": "550e8400-e29b-41d4-a716-446655440102",
        "productName": "도서 B",
        "price": 22000,
        "quantity": 1,
        "subtotal": 22000
      }
    ],
    "totalAmount": 52000,
    "shippingFee": 3000,
    "depositUsed": 55000,
    "recipient": "홍길동",
    "address": "서울특별시 강남구 테헤란로 1 101동 1001호",
    "createdAt": "2026-03-20T10:00:00Z"
  }
}
```

**2. 클라이언트 오류 — 재고 부족**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "재고가 부족합니다.",
  "data": {
    "insufficientItems": [
      {
        "productId": "550e8400-e29b-41d4-a716-446655440101",
        "productName": "도서 A",
        "requestedQuantity": 10,
        "availableStock": 5
      }
    ]
  }
}
```

**3. 클라이언트 오류 — 예치금 부족**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INSUFFICIENT_DEPOSIT",
  "message": "예치금 잔액이 부족합니다.",
  "data": {
    "requiredAmount": 55000,
    "availableDeposit": 30000
  }
}
```

**4. 클라이언트 오류 — 상품 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "상품을 찾을 수 없습니다.",
  "data": {
    "invalidProductIds": ["550e8400-e29b-41d4-a716-446655440999"]
  }
}
```

**5. 클라이언트 오류 — 배송지 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ADDRESS_NOT_FOUND",
  "message": "배송지를 찾을 수 없습니다.",
  "data": null
}
```

**6. 클라이언트 오류 — 배송지 접근 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ADDRESS_ACCESS_DENIED",
  "message": "해당 배송지에 접근할 권한이 없습니다.",
  "data": null
}
```

**7. 클라이언트 오류 — 빈 주문**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "EMPTY_ORDER",
  "message": "주문할 상품이 없습니다.",
  "data": null
}
```

**8. 클라이언트 오류 — 인증 실패**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
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

### `GET /order` — 주문 목록 조회

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `status` | string | X | 주문 상태 필터 (`PAID`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `PURCHASE_CONFIRMED`, `CANCELLED`) |
| `page` | number | X | 페이지 번호 |
| `size` | number | X | 페이지 크기 |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "ORDER_LIST_FETCH_SUCCESS",
  "message": "주문 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "orderId": "550e8400-e29b-41d4-a716-446655441001",
        "orderNumber": "20260318-000001",
        "orderStatus": "CONFIRMED",
        "totalAmount": 55000,
        "createdAt": "2026-03-18T10:00:00"
      },
      {
        "orderId": "550e8400-e29b-41d4-a716-446655441002",
        "orderNumber": "20260317-000014",
        "orderStatus": "DELIVERED",
        "totalAmount": 22000,
        "createdAt": "2026-03-17T15:10:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

**2. 클라이언트 오류 — 인증 실패**

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

### `GET /order/{orderId}` — 주문 상세 조회

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 조회할 주문 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "ORDER_DETAIL_FETCH_SUCCESS",
  "message": "주문 상세 조회에 성공했습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655441001",
    "orderNumber": "20260318-000001",
    "orderStatus": "CONFIRMED",
    "recipient": "홍길동",
    "phone": "01012345678",
    "address": "서울특별시 강남구 테헤란로 1 101동 1001호",
    "items": [
      {
        "orderItemId": "550e8400-e29b-41d4-a716-446655440201",
        "productId": "550e8400-e29b-41d4-a716-446655440101",
        "productName": "도서 A",
        "price": 15000,
        "quantity": 2,
        "subtotal": 30000
      }
    ],
    "paymentAmount": 33000,
    "shippingFee": 3000,
    "createdAt": "2026-03-18T10:00:00"
  }
}
```

**2. 클라이언트 오류 — 주문 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ORDER_ACCESS_DENIED",
  "message": "해당 주문에 접근할 권한이 없습니다.",
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

### `POST /order/{orderId}/cancel` — 주문 취소

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 취소할 주문 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `reason` | string | O | 취소 사유 |
| `orderItemIds` | array | O | 취소 대상 주문 항목 ID 목록 |

Request Body Example:

```json
{
  "reason": "상품 파손",
  "orderItemIds": ["550e8400-e29b-41d4-a716-446655440201", "550e8400-e29b-41d4-a716-446655440202"]
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "RETURN_REQUEST_SUCCESS",
  "message": "반품 신청이 접수되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655441001",
    "returnStatus": "PENDING",
    "orderItemIds": ["550e8400-e29b-41d4-a716-446655440201", "550e8400-e29b-41d4-a716-446655440202"]
  }
}
```

**2. 클라이언트 오류 — 주문 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 반품 불가 상태**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ORDER_CANNOT_BE_RETURNED",
  "message": "현재 주문 상태에서는 반품 신청이 불가능합니다.",
  "data": {
    "currentStatus": "CONFIRMED"
  }
}
```

**4. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ORDER_ACCESS_DENIED",
  "message": "해당 주문에 접근할 권한이 없습니다.",
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

### `POST /order/{orderId}/confirm` — 구매확정

> 🟡 **현재 자동 전이 운영 중** — 배송 모듈 미분리 상태이므로 결제 완료(PAID) 시 DELIVERED → PURCHASE_CONFIRMED까지 자동 전이. 아래는 배송 모듈 분리 후 사용될 수동 확정 API.

배송 완료(DELIVERED) 상태의 주문을 구매확정(PURCHASE_CONFIRMED)으로 전환합니다. 전환 시 `PurchaseConfirmedEvent`(Kafka)를 발행하여 Settlement 서비스가 정산 대상(`settlement_target`)을 생성합니다.

> 자동 확정: 배송 완료 후 7일 경과 시 스케줄러가 자동 전환합니다. 이 API는 수동 확정용입니다.

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 구매확정할 주문 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PURCHASE_CONFIRM_SUCCESS",
  "message": "구매확정이 완료되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655441001",
    "orderStatus": "PURCHASE_CONFIRMED",
    "confirmedAt": "2026-03-27T10:00:00Z"
  }
}
```

**2. 클라이언트 오류 — 주문 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 구매확정 불가 상태**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ORDER_CANNOT_BE_CONFIRMED",
  "message": "배송 완료(DELIVERED) 상태에서만 구매확정이 가능합니다.",
  "data": {
    "currentStatus": "CONFIRMED"
  }
}
```

**4. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ORDER_ACCESS_DENIED",
  "message": "해당 주문에 접근할 권한이 없습니다.",
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

### `POST /order/{orderId}/return` — 반품 신청

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 반품 신청할 주문 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `reason` | string | O | 반품 사유 |
| `orderItemIds` | array | O | 반품 대상 주문 항목 ID 목록 |

Request Body Example:

```json
{
  "reason": "상품 파손",
  "orderItemIds": ["550e8400-e29b-41d4-a716-446655440201", "550e8400-e29b-41d4-a716-446655440202"]
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "RETURN_REQUEST_SUCCESS",
  "message": "반품 신청이 접수되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655441001",
    "returnStatus": "PENDING",
    "orderItemIds": ["550e8400-e29b-41d4-a716-446655440201", "550e8400-e29b-41d4-a716-446655440202"]
  }
}
```

**2. 클라이언트 오류 — 주문 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 반품 불가 상태**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ORDER_CANNOT_BE_RETURNED",
  "message": "현재 주문 상태에서는 반품 신청이 불가능합니다.",
  "data": {
    "currentStatus": "CONFIRMED"
  }
}
```

**4. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ORDER_ACCESS_DENIED",
  "message": "해당 주문에 접근할 권한이 없습니다.",
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

### `PATCH /order/{orderId}/shipment` — 송장 등록/배송 정보 수정

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 배송 정보를 수정할 주문 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `carrier` | string | O | 택배사명 |
| `trackingNumber` | string | O | 송장 번호 |
| `shipmentStatus` | string | X | 배송 상태 (`PREPARING`, `SHIPPED`, `IN_TRANSIT`, `DELIVERED`, `RETURNED`) |

Request Body Example:

```json
{
  "carrier": "CJ대한통운",
  "trackingNumber": "1234567890",
  "shipmentStatus": "SHIPPED"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SHIPMENT_UPDATE_SUCCESS",
  "message": "배송 정보가 수정되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655441001",
    "carrier": "CJ대한통운",
    "trackingNumber": "1234567890",
    "shipmentStatus": "SHIPPED"
  }
}
```

**2. 클라이언트 오류 — 주문 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "SHIPMENT_ACCESS_DENIED",
  "message": "배송 정보를 수정할 권한이 없습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 잘못된 배송 상태**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_SHIPMENT_STATUS",
  "message": "유효하지 않은 배송 상태값입니다.",
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

## 📌 Internal Order API

> 서비스 간 내부 통신 전용 API입니다. 외부(클라이언트)에서 직접 호출하지 않습니다.

| 기능 | Method | Endpoint | 설명 | 호출 서비스 |
|------|--------|----------|------|------------|
| 주문 상태 변경 | POST | /internal/order/{orderId}/status | PENDING → PAID 상태 변경 | payment-service |

### `POST /internal/order/{orderId}/status` — 주문 상태 변경

payment-service가 결제 완료 후 주문 상태를 PENDING → PAID로 변경합니다.

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | UUID | O | 상태를 변경할 주문 ID |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `status` | string | O | 변경할 상태 (`PAID`) |
| `depositUsed` | number | O | 예치금 차감액 |

Request Body Example:

```json
{
  "status": "PAID",
  "depositUsed": 55000
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "ORDER_STATUS_UPDATED",
  "message": "주문 상태가 변경되었습니다.",
  "data": {
    "orderId": "550e8400-e29b-41d4-a716-446655440001",
    "status": "PAID"
  }
}
```

**2. 주문 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "해당 주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 상태 전환 불가**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "INVALID_ORDER_STATUS_TRANSITION",
  "message": "현재 주문 상태에서 해당 상태로 전환할 수 없습니다.",
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
