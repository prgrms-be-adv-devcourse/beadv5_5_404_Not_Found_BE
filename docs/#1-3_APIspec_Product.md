# API Specification - Product Module

> 도서 이커머스 플랫폼 API 명세서
> Product API (/product/*)

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
| 카테고리 추가 | POST | /products/categories | ✅ | | 카테고리 등록 |

> 설계 변경: 재고 조회/수정 API는 별도 엔드포인트 없이 상품 등록/수정 시 quantity 필드로 관리. 재고 차감은 `POST /internal/products/stock/deduct` (payment-service 전용). 재고 복원 엔드포인트(`/internal/products/stock/restore`)는 미구현.

### `GET /product` — 상품 목록 조회

상품 목록을 조회합니다. 페이지네이션과 필터링을 지원합니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `page` | number | X | 페이지 번호 (기본값: 1) |
| `limit` | number | X | 페이지당 항목 수 (기본값: 20, 최대: 100) |
| `categoryId` | number | X | 카테고리 ID로 필터링 |
| `keyword` | string | X | 상품명 검색 키워드 |
| `status` | string | X | 상품 상태 필터 (`ACTIVE`, `SOLD_OUT`, `INACTIVE`, `PENDING_REVIEW`) |
| `sort` | string | X | 정렬 기준 (`latest`, `price_asc`, `price_desc`, `name`) (기본값: `latest`) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "상품 목록 조회에 성공했습니다.",
  "data": {
    "products": [
      {
        "productId": 1,
        "name": "오가닉 코튼 티셔츠",
        "price": 29000,
        "thumbnailUrl": "https://example.com/images/product1_thumb.jpg",
        "status": "ACTIVE",
        "categoryId": 3,
        "categoryName": "상의",
        "createdAt": "2026-03-01T09:00:00Z"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalItems": 98,
      "limit": 20
    }
  }
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

### `GET /product/{productId}` — 상품 상세 조회

특정 상품의 상세 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | number | O | 조회할 상품 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "상품 상세 조회에 성공했습니다.",
  "data": {
    "productId": 1,
    "name": "오가닉 코튼 티셔츠",
    "description": "100% 유기농 면으로 제작된 프리미엄 티셔츠입니다.",
    "price": 29000,
    "discountPrice": 25000,
    "status": "ACTIVE",
    "categoryId": 3,
    "categoryName": "상의",
    "imageUrls": [
      "https://example.com/images/product1_1.jpg",
      "https://example.com/images/product1_2.jpg"
    ],
    "thumbnailUrl": "https://example.com/images/product1_thumb.jpg",
    "stock": 150,
    "options": [
      {
        "optionId": 1,
        "name": "사이즈",
        "values": ["S", "M", "L", "XL"]
      },
      {
        "optionId": 2,
        "name": "색상",
        "values": ["화이트", "블랙", "네이비"]
      }
    ],
    "createdAt": "2026-03-01T09:00:00Z",
    "updatedAt": "2026-03-15T14:30:00Z"
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

**3. 클라이언트 오류 — 잘못된 파라미터**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_PRODUCT_ID",
  "message": "유효하지 않은 상품 ID입니다.",
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

### `POST /product` — 상품 등록

새로운 상품을 등록합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | O | 상품명 (최대 100자) |
| `description` | string | O | 상품 설명 |
| `price` | number | O | 상품 가격 (0 이상) |
| `discountPrice` | number | X | 할인 가격 (`price`보다 작아야 함) |
| `categoryId` | number | O | 카테고리 ID |
| `imageUrls` | string[] | O | 상품 이미지 URL 배열 (최소 1개) |
| `thumbnailUrl` | string | O | 썸네일 이미지 URL |
| `stock` | number | O | 초기 재고 수량 (0 이상) |
| `options` | object[] | X | 상품 옵션 배열 |
| `options[].name` | string | O | 옵션명 (예: "사이즈") |
| `options[].values` | string[] | O | 옵션값 배열 (예: ["S", "M", "L"]) |

Request Body Example:

```json
{
  "name": "오가닉 코튼 티셔츠",
  "description": "100% 유기농 면으로 제작된 프리미엄 티셔츠입니다.",
  "price": 29000,
  "discountPrice": 25000,
  "categoryId": 3,
  "imageUrls": [
    "https://example.com/images/product1_1.jpg",
    "https://example.com/images/product1_2.jpg"
  ],
  "thumbnailUrl": "https://example.com/images/product1_thumb.jpg",
  "stock": 150,
  "options": [
    {
      "name": "사이즈",
      "values": ["S", "M", "L", "XL"]
    },
    {
      "name": "색상",
      "values": ["화이트", "블랙", "네이비"]
    }
  ]
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "PRODUCT_CREATED",
  "message": "상품이 성공적으로 등록되었습니다.",
  "data": {
    "productId": 1
  }
}
```

**2. 클라이언트 오류 — 필수 필드 누락**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "MISSING_REQUIRED_FIELD",
  "message": "필수 입력 항목이 누락되었습니다.",
  "data": {
    "missingFields": ["name", "price"]
  }
}
```

**3. 클라이언트 오류 — 유효하지 않은 데이터**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_FIELD_VALUE",
  "message": "유효하지 않은 입력값입니다.",
  "data": {
    "errors": [
      {
        "field": "discountPrice",
        "message": "할인 가격은 정가보다 작아야 합니다."
      }
    ]
  }
}
```

**4. 클라이언트 오류 — 존재하지 않는 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CATEGORY_NOT_FOUND",
  "message": "해당 카테고리를 찾을 수 없습니다.",
  "data": null
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
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `PATCH /product/{productId}` — 상품 수정

기존 상품 정보를 수정합니다. 변경하고자 하는 필드만 전달합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | number | O | 수정할 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | X | 상품명 (최대 100자) |
| `description` | string | X | 상품 설명 |
| `price` | number | X | 상품 가격 (0 이상) |
| `discountPrice` | number | X | 할인 가격 (`price`보다 작아야 함) |
| `categoryId` | number | X | 카테고리 ID |
| `imageUrls` | string[] | X | 상품 이미지 URL 배열 |
| `thumbnailUrl` | string | X | 썸네일 이미지 URL |
| `options` | object[] | X | 상품 옵션 배열 |

Request Body Example:

```json
{
  "name": "프리미엄 오가닉 코튼 티셔츠",
  "price": 32000,
  "discountPrice": 28000
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PRODUCT_UPDATED",
  "message": "상품 정보가 성공적으로 수정되었습니다.",
  "data": {
    "productId": 1,
    "updatedFields": ["name", "price", "discountPrice"]
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

**3. 클라이언트 오류 — 유효하지 않은 데이터**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_FIELD_VALUE",
  "message": "유효하지 않은 입력값입니다.",
  "data": {
    "errors": [
      {
        "field": "price",
        "message": "가격은 0 이상이어야 합니다."
      }
    ]
  }
}
```

**4. 클라이언트 오류 — 수정할 필드 없음**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "NO_FIELDS_TO_UPDATE",
  "message": "수정할 항목이 없습니다.",
  "data": null
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
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `PATCH /product/{productId}/status` — 상품 상태 변경

상품의 판매 상태를 변경합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | number | O | 상태를 변경할 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `status` | string | O | 변경할 상태 (`ACTIVE`, `SOLD_OUT`, `INACTIVE`, `PENDING_REVIEW`) |

Request Body Example:

```json
{
  "status": "SOLD_OUT"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "STATUS_UPDATED",
  "message": "상품 상태가 성공적으로 변경되었습니다.",
  "data": {
    "productId": 1,
    "previousStatus": "ACTIVE",
    "currentStatus": "SOLD_OUT"
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

**3. 클라이언트 오류 — 유효하지 않은 상태값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_STATUS",
  "message": "유효하지 않은 상태값입니다. (허용값: PENDING_REVIEW, ACTIVE, INACTIVE, SOLD_OUT)",
  "data": null
}
```

**4. 클라이언트 오류 — 동일한 상태로 변경 시도**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "SAME_STATUS",
  "message": "현재 상태와 동일한 상태로는 변경할 수 없습니다.",
  "data": null
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
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `GET /product/{productId}/inventory` — 재고 조회

특정 상품의 재고 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | number | O | 조회할 상품 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "재고 조회에 성공했습니다.",
  "data": {
    "productId": 1,
    "productName": "오가닉 코튼 티셔츠",
    "totalStock": 150,
    "optionStocks": [
      {
        "optionId": 1,
        "optionName": "S / 화이트",
        "stock": 30
      },
      {
        "optionId": 2,
        "optionName": "M / 화이트",
        "stock": 50
      },
      {
        "optionId": 3,
        "optionName": "L / 블랙",
        "stock": 70
      }
    ],
    "updatedAt": "2026-03-18T10:00:00Z"
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

### `PATCH /product/{productId}/inventory` — 재고 수정

특정 상품의 재고를 수정합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `productId` | number | O | 수정할 상품 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `stocks` | object[] | O | 재고 수정 배열 |
| `stocks[].optionId` | number | O | 옵션 ID |
| `stocks[].stock` | number | O | 변경할 재고 수량 (0 이상) |

Request Body Example:

```json
{
  "stocks": [
    {
      "optionId": 1,
      "stock": 50
    },
    {
      "optionId": 2,
      "stock": 100
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
  "code": "INVENTORY_UPDATED",
  "message": "재고가 성공적으로 수정되었습니다.",
  "data": {
    "productId": 1,
    "updatedStocks": [
      {
        "optionId": 1,
        "stock": 50
      },
      {
        "optionId": 2,
        "stock": 100
      }
    ]
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

**3. 클라이언트 오류 — 존재하지 않는 옵션**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "OPTION_NOT_FOUND",
  "message": "해당 옵션을 찾을 수 없습니다.",
  "data": {
    "invalidOptionIds": [99]
  }
}
```

**4. 클라이언트 오류 — 유효하지 않은 재고 수량**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_STOCK_VALUE",
  "message": "재고 수량은 0 이상이어야 합니다.",
  "data": null
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
  "message": "해당 작업에 대한 권한이 없습니다.",
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

### `GET /product/category` — 카테고리 조회

상품 카테고리 목록을 조회합니다.

#### Request

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `parentId` | number | X | 부모 카테고리 ID (미입력 시 최상위 카테고리 조회) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "카테고리 조회에 성공했습니다.",
  "data": {
    "categories": [
      {
        "categoryId": 1,
        "name": "의류",
        "parentId": null,
        "depth": 0,
        "children": [
          {
            "categoryId": 2,
            "name": "상의",
            "parentId": 1,
            "depth": 1,
            "children": []
          },
          {
            "categoryId": 3,
            "name": "하의",
            "parentId": 1,
            "depth": 1,
            "children": []
          }
        ]
      },
      {
        "categoryId": 4,
        "name": "액세서리",
        "parentId": null,
        "depth": 0,
        "children": []
      }
    ]
  }
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

### `POST /product/category` — 카테고리 추가

새로운 상품 카테고리를 추가합니다.

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
| `parentId` | number | X | 부모 카테고리 ID (미입력 시 최상위 카테고리로 생성) |

Request Body Example:

```json
{
  "name": "반팔 티셔츠",
  "parentId": 2
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "CATEGORY_CREATED",
  "message": "카테고리가 성공적으로 추가되었습니다.",
  "data": {
    "categoryId": 5,
    "name": "반팔 티셔츠",
    "parentId": 2,
    "depth": 2
  }
}
```

**2. 클라이언트 오류 — 중복 카테고리명**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "DUPLICATE_CATEGORY_NAME",
  "message": "동일한 이름의 카테고리가 이미 존재합니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 존재하지 않는 부모 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PARENT_CATEGORY_NOT_FOUND",
  "message": "부모 카테고리를 찾을 수 없습니다.",
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
    "missingFields": ["name"]
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
  "message": "해당 작업에 대한 권한이 없습니다.",
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
