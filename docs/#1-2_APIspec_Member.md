# API Specification - Member Module

> 도서 이커머스 플랫폼 API 명세서
> Member API (/member/*)

---

## 📌 Member API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 내 정보 조회 | GET | /member/me | 회원 정보 조회 |
| 내 정보 수정 | PATCH | /member/me | 회원 정보 수정 |
| 예치금 잔액 조회 | GET | /member/me/deposit | 보유 예치금 조회 |
| 회원 탈퇴 | DELETE | /member/me | 계정 삭제 |
| 배송지 목록 조회 | GET | /member/address | 배송지 리스트 |
| 배송지 추가 | POST | /member/address | 배송지 등록 |
| 배송지 수정 | PATCH | /member/address/{addressId} | 배송지 수정 |
| 배송지 삭제 | DELETE | /member/address/{addressId} | 배송지 삭제 |
| 판매자 등록 신청 | POST | /member/seller | 판매자 신청 |
| 판매자 정보 조회 | GET | /member/seller/{memberId} | 판매자 정보 |
| 판매자 승인/거절 | PATCH | /member/admin/seller/{memberId} | 관리자 승인 |

### `GET /member/me` — 내 정보 조회

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
  "code": "MEMBER_INFO_FETCH_SUCCESS",
  "message": "내 정보 조회에 성공했습니다.",
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "01012345678",
    "role": "USER",
    "sellerRegistered": false,
    "createdAt": "2026-03-18T10:00:00"
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

### `PATCH /member/me` — 내 정보 수정

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | X | 변경할 이름 |
| `phone` | string | X | 변경할 휴대폰 번호 |
| `password` | string | X | 변경할 비밀번호 |

Request Body Example:

```json
{
  "name": "김길동",
  "phone": "01099998888"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "MEMBER_UPDATE_SUCCESS",
  "message": "회원 정보가 수정되었습니다.",
  "data": {
    "memberId": 1,
    "name": "김길동",
    "phone": "01099998888"
  }
}
```

**2. 클라이언트 오류 — 잘못된 요청**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "phone": "휴대폰 번호 형식이 올바르지 않습니다."
  }
}
```

**3. 클라이언트 오류 — 인증 실패**

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

### `GET /member/me/deposit` — 예치금 잔액 조회

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
  "code": "DEPOSIT_BALANCE_FOUND",
  "message": "예치금 잔액을 조회했습니다.",
  "data": {
    "memberId": 1,
    "depositBalance": 50000
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

### `DELETE /member/me` — 회원 탈퇴

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `password` | string | O | 본인 확인용 비밀번호 |

Request Body Example:

```json
{
  "password": "Password123!"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "MEMBER_DELETE_SUCCESS",
  "message": "회원 탈퇴가 완료되었습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 비밀번호 불일치**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "INVALID_PASSWORD",
  "message": "비밀번호가 일치하지 않습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 인증 실패**

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

### `GET /member/address` — 배송지 목록 조회

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
  "code": "ADDRESS_LIST_FETCH_SUCCESS",
  "message": "배송지 목록 조회에 성공했습니다.",
  "data": [
    {
      "addressId": 1,
      "recipient": "홍길동",
      "phone": "01012345678",
      "zipcode": "06236",
      "address1": "서울특별시 강남구",
      "address2": "101동 1001호",
      "isDefault": true
    },
    {
      "addressId": 2,
      "recipient": "홍길동",
      "phone": "01011112222",
      "zipcode": "04524",
      "address1": "서울특별시 중구",
      "address2": "202호",
      "isDefault": false
    }
  ]
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

### `POST /member/address` — 배송지 추가

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `recipient` | string | O | 수령인 이름 |
| `phone` | string | O | 연락처 |
| `zipcode` | string | O | 우편번호 |
| `address1` | string | O | 기본 주소 |
| `address2` | string | X | 상세 주소 |
| `isDefault` | boolean | X | 기본 배송지 여부 |

Request Body Example:

```json
{
  "recipient": "홍길동",
  "phone": "01012345678",
  "zipcode": "06236",
  "address1": "서울특별시 강남구 테헤란로 1",
  "address2": "101동 1001호",
  "isDefault": true
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "ADDRESS_CREATE_SUCCESS",
  "message": "배송지가 등록되었습니다.",
  "data": {
    "addressId": 1
  }
}
```

**2. 클라이언트 오류 — 잘못된 요청**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "zipcode": "우편번호 형식이 올바르지 않습니다."
  }
}
```

**3. 클라이언트 오류 — 인증 실패**

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

### `PATCH /member/address/{addressId}` — 배송지 수정

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `addressId` | number | O | 수정할 배송지 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `recipient` | string | X | 수령인 이름 |
| `phone` | string | X | 연락처 |
| `zipcode` | string | X | 우편번호 |
| `address1` | string | X | 기본 주소 |
| `address2` | string | X | 상세 주소 |
| `isDefault` | boolean | X | 기본 배송지 여부 |

Request Body Example:

```json
{
  "recipient": "김길동",
  "isDefault": true
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "ADDRESS_UPDATE_SUCCESS",
  "message": "배송지 정보가 수정되었습니다.",
  "data": {
    "addressId": 1
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 배송지**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ADDRESS_NOT_FOUND",
  "message": "배송지를 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ADDRESS_ACCESS_DENIED",
  "message": "해당 배송지에 접근할 권한이 없습니다.",
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

### `DELETE /member/address/{addressId}` — 배송지 삭제

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `addressId` | number | O | 삭제할 배송지 ID |

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
  "code": "ADDRESS_DELETE_SUCCESS",
  "message": "배송지가 삭제되었습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 존재하지 않는 배송지**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ADDRESS_NOT_FOUND",
  "message": "배송지를 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ADDRESS_ACCESS_DENIED",
  "message": "해당 배송지에 접근할 권한이 없습니다.",
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

### `POST /member/seller` — 판매자 등록 신청

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `shopName` | string | O | 상점명 |
| `businessNumber` | string | O | 사업자등록번호 |
| `bankCode` | string | O | 정산 은행 코드 |
| `bankAccount` | string | O | 정산 계좌번호 |
| `accountHolder` | string | O | 예금주명 |

Request Body Example:

```json
{
  "shopName": "북하이브 스토어",
  "businessNumber": "1234567890",
  "bankCode": "004",
  "bankAccount": "123456789012",
  "accountHolder": "홍길동"
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "SELLER_APPLY_SUCCESS",
  "message": "판매자 등록 신청이 완료되었습니다.",
  "data": {
    "memberId": 1,
    "sellerStatus": "PENDING"
  }
}
```

**2. 클라이언트 오류 — 중복 신청**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "SELLER_APPLICATION_ALREADY_EXISTS",
  "message": "이미 판매자 신청이 존재합니다.",
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
    "businessNumber": "사업자등록번호 형식이 올바르지 않습니다."
  }
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

### `GET /member/seller/{memberId}` — 판매자 정보 조회

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `memberId` | number | O | 조회할 회원 ID |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SELLER_INFO_FETCH_SUCCESS",
  "message": "판매자 정보 조회에 성공했습니다.",
  "data": {
    "memberId": 1,
    "shopName": "북하이브 스토어",
    "businessNumber": "1234567890",
    "bankCode": "004",
    "bankAccount": "123456789012",
    "accountHolder": "홍길동",
    "sellerStatus": "APPROVED"
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 판매자 정보**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "SELLER_NOT_FOUND",
  "message": "판매자 정보를 찾을 수 없습니다.",
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

### `PATCH /member/admin/seller/{memberId}` — 판매자 승인/거절

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `memberId` | number | O | 승인/거절할 회원 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Role` | string | O | 관리자 권한 식별용 헤더 또는 토큰 내 권한 정보 |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `status` | string | O | 처리 결과 (`APPROVED`, `SUSPENDED`) |
| `reason` | string | X | 거절 사유 |

Request Body Example:

```json
{
  "status": "APPROVED"
}
```

또는

```json
{
  "status": "SUSPENDED",
  "reason": "사업자등록번호 확인이 필요합니다."
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SELLER_STATUS_UPDATE_SUCCESS",
  "message": "판매자 상태가 변경되었습니다.",
  "data": {
    "memberId": 1,
    "sellerStatus": "APPROVED"
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 신청**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "SELLER_APPLICATION_NOT_FOUND",
  "message": "판매자 신청 정보를 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 권한 없음**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "ACCESS_DENIED",
  "message": "관리자만 접근할 수 있습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 잘못된 상태값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_SELLER_STATUS",
  "message": "유효하지 않은 판매자 상태값입니다. (허용값: APPROVED, SUSPENDED)",
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
