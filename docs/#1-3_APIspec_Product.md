# API Specification - Product Module

> 도서 이커머스 플랫폼 API 명세서
> Product API (/products/*)

---

## 📌 Product API

> ★ = 상품선택 → 결제완료 → 정산완료 필수 플로우

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 상품 목록 조회 | GET | /products | ✅ | ★ | 상품 리스트 조회 (비인증 허용) |
| 상품 상세 조회 | GET | /products/{productId} | ✅ | ★ | 상품 상세 (비인증 허용) |
| 상품 등록 | POST | /products | ✅ | ★ | 상품 등록 (SELLER 전용) |
| 상품 수정 | PATCH | /products/{productId} | ✅ | | 상품 정보 수정 (SELLER 전용) |
| 상품 상태 변경 | PATCH | /products/{productId}/status | ✅ | ★ | ACTIVE 전환 (ADMIN 전용) |
| 카테고리 조회 | GET | /products/categories | ✅ | | 카테고리 목록 |
| 카테고리 추가 | POST | /products/categories | ✅ | | 카테고리 등록 (ADMIN 전용) |

> 재고 조회/수정 API는 별도 엔드포인트 없이 상품 등록/수정 시 `quantity` 필드로 관리.
> 재고 차감: `POST /internal/products/stock/deduct` (payment-service 전용).
> 재고 복원: UseCase 구현됨, HTTP 엔드포인트 미노출.

---

### `GET /products` — 상품 목록 조회

상품 목록을 조회합니다. ID 목록으로 필터링을 지원합니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `ids` | UUID[] | X | 조회할 상품 ID 목록 (미입력 시 전체 조회) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "상품 목록 조회에 성공했습니다.",
  "data": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "sellerId": "550e8400-e29b-41d4-a716-446655440001",
      "categoryId": "550e8400-e29b-41d4-a716-446655440002",
      "isbn": "9788966261208",
      "title": "클린 코드",
      "author": "로버트 C. 마틴",
      "publisher": "인사이트",
      "price": 33000,
      "quantity": 50,
      "bookType": "NEW",
      "status": "ACTIVE",
      "avgRating": 4.5,
      "reviewCount": 12,
      "createdAt": "2026-03-01T09:00:00"
    }
  ]
}
```

**2. 클라이언트 오류 — 잘못된 파라미터**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_PARAMETER",
  "message": "잘못된 요청 파라미터입니다.",
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

### `GET /products/{productId}` — 상품 상세 조회

특정 상품의 상세 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | UUID | O | 조회할 상품 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "상품 상세 조회에 성공했습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261208",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 33000,
    "quantity": 50,
    "bookType": "NEW",
    "status": "ACTIVE",
    "avgRating": 4.5,
    "reviewCount": 12,
    "createdAt": "2026-03-01T09:00:00"
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 상품**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "해당 상품을 찾을 수 없습니다.",
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

### `POST /products` — 상품 등록

새로운 도서 상품을 등록합니다. SELLER 권한 필요.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `categoryId` | UUID | O | 카테고리 ID |
| `isbn` | string | O | ISBN (최대 20자) |
| `title` | string | O | 도서 제목 (최대 300자) |
| `author` | string | O | 저자 (최대 200자) |
| `publisher` | string | O | 출판사 (최대 100자) |
| `price` | number | O | 판매 가격 (0 이상) |
| `quantity` | number | O | 초기 재고 수량 (0 이상) |
| `bookType` | string | O | 도서 유형 (`NEW` \| `USED`) |

Request Body Example:

```json
{
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "isbn": "9788966261208",
  "title": "클린 코드",
  "author": "로버트 C. 마틴",
  "publisher": "인사이트",
  "price": 33000,
  "quantity": 50,
  "bookType": "NEW"
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "PRODUCT_REGISTER_SUCCESS",
  "message": "상품이 성공적으로 등록되었습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

**2. 클라이언트 오류 — 중복 ISBN**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ISBN_DUPLICATE",
  "message": "이미 등록된 ISBN입니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 존재하지 않는 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CATEGORY_NOT_FOUND",
  "message": "해당 카테고리를 찾을 수 없습니다.",
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

**5. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `PATCH /products/{productId}` — 상품 수정

기존 상품 정보를 수정합니다. 변경하고자 하는 필드만 전달합니다. SELLER 권한 필요 (본인 상품만 수정 가능).

> ISBN, bookType 변경 불가.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | UUID | O | 수정할 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `categoryId` | UUID | X | 카테고리 ID |
| `title` | string | X | 도서 제목 (최대 300자) |
| `author` | string | X | 저자 (최대 200자) |
| `publisher` | string | X | 출판사 (최대 100자) |
| `price` | number | X | 판매 가격 (0 이상) |
| `quantity` | number | X | 재고 수량 (0 이상) |

Request Body Example:

```json
{
  "price": 30000,
  "quantity": 100
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PRODUCT_UPDATE_SUCCESS",
  "message": "상품 정보가 성공적으로 수정되었습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261208",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 30000,
    "quantity": 100,
    "bookType": "NEW",
    "status": "ACTIVE",
    "avgRating": 4.5,
    "reviewCount": 12,
    "createdAt": "2026-03-01T09:00:00"
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 상품**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "해당 상품을 찾을 수 없습니다.",
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
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `PATCH /products/{productId}/status` — 상품 상태 변경

상품의 판매 상태를 변경합니다. ADMIN 권한 필요.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | UUID | O | 상태를 변경할 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `status` | string | O | 변경할 상태 (`PENDING_REVIEW` \| `ACTIVE` \| `INACTIVE` \| `SOLD_OUT`) |

Request Body Example:

```json
{
  "status": "ACTIVE"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PRODUCT_STATUS_CHANGE_SUCCESS",
  "message": "상품 상태가 성공적으로 변경되었습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261208",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 33000,
    "quantity": 50,
    "bookType": "NEW",
    "status": "ACTIVE",
    "avgRating": 0.0,
    "reviewCount": 0,
    "createdAt": "2026-03-01T09:00:00"
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 상품**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "해당 상품을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 유효하지 않은 상태 전환**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_STATUS_TRANSITION",
  "message": "유효하지 않은 상태 전환입니다.",
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

**5. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `GET /products/categories` — 카테고리 조회

상품 카테고리 목록을 트리 구조로 조회합니다.

#### Request

(Query Parameter 없음)

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "카테고리 목록 조회에 성공했습니다.",
  "data": [
    {
      "categoryId": "550e8400-e29b-41d4-a716-446655440010",
      "name": "국내도서",
      "parentId": null,
      "depth": 0,
      "sortOrder": 1,
      "children": [
        {
          "categoryId": "550e8400-e29b-41d4-a716-446655440011",
          "name": "소설",
          "parentId": "550e8400-e29b-41d4-a716-446655440010",
          "depth": 1,
          "sortOrder": 1,
          "children": []
        }
      ]
    }
  ]
}
```

**2. 서버 오류**

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

### `POST /products/categories` — 카테고리 추가

새로운 상품 카테고리를 추가합니다. ADMIN 권한 필요.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | O | 카테고리명 (최대 50자) |
| `parentId` | UUID | X | 부모 카테고리 ID (미입력 시 최상위 카테고리로 생성) |

Request Body Example:

```json
{
  "name": "컴퓨터/IT",
  "parentId": null
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "CATEGORY_CREATE_SUCCESS",
  "message": "카테고리가 성공적으로 추가되었습니다.",
  "data": {
    "categoryId": "550e8400-e29b-41d4-a716-446655440020",
    "name": "컴퓨터/IT",
    "parentId": null,
    "depth": 0
  }
}
```

**2. 클라이언트 오류 — 중복 슬러그**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "CATEGORY_SLUG_DUPLICATE",
  "message": "동일한 슬러그의 카테고리가 이미 존재합니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 존재하지 않는 부모 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CATEGORY_NOT_FOUND",
  "message": "부모 카테고리를 찾을 수 없습니다.",
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

**5. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 작업에 대한 권한이 없습니다.",
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

## 📌 Internal API (서비스 간 통신 전용)

> `X-Internal-Secret` 헤더 인증 필요. 외부 노출 없음.

### `POST /internal/products/stock/deduct` — 재고 차감

payment-service가 결제 완료 후 재고를 차감합니다.

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `items` | object[] | O | 차감 대상 목록 |
| `items[].productId` | UUID | O | 상품 ID |
| `items[].quantity` | number | O | 차감 수량 (1 이상) |

Request Body Example:

```json
{
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    }
  ]
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "STOCK_DEDUCT_SUCCESS",
  "message": "재고가 성공적으로 차감되었습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 재고 부족**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "재고가 부족합니다.",
  "data": null
}
```
