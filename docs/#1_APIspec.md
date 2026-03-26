# API Specification

> 도서 이커머스 플랫폼 API 명세서
> MSA 5개 서비스: Member / Product / Review / Order / Payment

---

## 📌 Auth API

> Member Service 내부에서 처리하되, API 경로는 `/auth/*`로 독립 구분합니다.

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 회원가입 | POST | /auth/register | 신규 회원 생성 + 토큰 발급 |
| 로그인 | POST | /auth/login | JWT 발급 |
| 토큰 재발급 | POST | /auth/refresh | Access Token 재발급 |
| 로그아웃 | POST | /auth/logout | 로그아웃 처리 |

### `POST /auth/register` — 회원가입

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `email` | string | O | 회원 이메일 |
| `password` | string | O | 비밀번호 |
| `name` | string | O | 회원 이름 |
| `phone` | string | O | 휴대폰 번호 |

Request Body Example:

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "홍길동",
  "phone": "01012345678"
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "MEMBER_REGISTER_SUCCESS",
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "name": "홍길동"
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
    "email": "이메일 형식이 올바르지 않습니다.",
    "password": "비밀번호는 8자 이상이어야 합니다."
  }
}
```

**3. 클라이언트 오류 — 중복 이메일**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "이미 사용 중인 이메일입니다.",
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

### `POST /auth/login` — 로그인

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `email` | string | O | 회원 이메일 |
| `password` | string | O | 비밀번호 |

Request Body Example:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "LOGIN_SUCCESS",
  "message": "로그인에 성공했습니다.",
  "data": {
    "memberId": 1,
    "name": "홍길동",
    "role": "USER"
  }
}
```

**2. 클라이언트 오류 — 로그인 실패**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "INVALID_CREDENTIALS",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 탈퇴 또는 비활성 계정**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "MEMBER_INACTIVE",
  "message": "비활성화된 계정입니다.",
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

### `POST /auth/refresh` — 토큰 재발급

#### Request

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `refreshToken` | string | O | 재발급에 사용할 refresh token |

Request Body Example:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "TOKEN_REFRESH_SUCCESS",
  "message": "토큰이 재발급되었습니다.",
  "data": null
}
```

**2. 클라이언트 오류 — 유효하지 않은 토큰**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "INVALID_REFRESH_TOKEN",
  "message": "유효하지 않은 리프레시 토큰입니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 만료된 토큰**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "EXPIRED_REFRESH_TOKEN",
  "message": "리프레시 토큰이 만료되었습니다.",
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

### `POST /auth/logout` — 로그아웃

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `refreshToken` | string | O | 로그아웃 시 무효화할 refresh token |

Request Body Example:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "LOGOUT_SUCCESS",
  "message": "로그아웃이 완료되었습니다.",
  "data": null
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

## 📌 Product API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 상품 목록 조회 | GET | /products | 상품 리스트 조회, `?ids=` 파라미터로 배치 조회 가능 |
| 상품 상세 조회 | GET | /products/{productId} | 상품 상세 조회 |
| 상품 등록 | POST | /products | 상품 등록 |
| 상품 수정 | PATCH | /products/{productId} | 상품 정보 수정 (판매자) |
| 상품 상태 변경 | PATCH | /products/{productId}/status | 판매 상태 변경 |
| 카테고리 목록 조회 | GET | /products/categories | 카테고리 트리 조회 |
| 카테고리 추가 | POST | /products/categories | 카테고리 등록 |


### `GET /products` — 상품 목록 조회

상품 목록을 조회합니다. `ids` 파라미터로 특정 상품들을 배치 조회할 수 있습니다.

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
  "code": "PRODUCT_LIST_GET_SUCCESS",
  "message": "상품 목록 조회에 성공했습니다.",
  "data": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "sellerId": "550e8400-e29b-41d4-a716-446655440001",
      "categoryId": "550e8400-e29b-41d4-a716-446655440002",
      "isbn": "9788966261840",
      "title": "클린 코드",
      "author": "로버트 C. 마틴",
      "publisher": "인사이트",
      "price": 33000,
      "quantity": 50,
      "bookType": "NEW",
      "status": "ACTIVE",
      "avgRating": 4.5,
      "reviewCount": 128,
      "createdAt": "2026-03-01T09:00:00"
    }
  ]
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
  "code": "PRODUCT_GET_SUCCESS",
  "message": "상품 조회에 성공했습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261840",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 33000,
    "quantity": 50,
    "bookType": "NEW",
    "status": "ACTIVE",
    "avgRating": 4.5,
    "reviewCount": 128,
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

---

### `POST /products` — 상품 등록

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
| `sellerId` | UUID | O | 판매자 ID |
| `categoryId` | UUID | O | 카테고리 ID |
| `isbn` | string | O | ISBN (최대 20자) |
| `title` | string | O | 도서 제목 (최대 300자) |
| `author` | string | O | 저자 (최대 200자) |
| `publisher` | string | O | 출판사 (최대 100자) |
| `price` | integer | O | 가격 (0 이상) |
| `quantity` | integer | O | 초기 재고 수량 (0 이상) |
| `bookType` | string | O | 도서 유형 (`NEW`, `USED`) |

Request Body Example:

```json
{
  "sellerId": "550e8400-e29b-41d4-a716-446655440001",
  "categoryId": "550e8400-e29b-41d4-a716-446655440002",
  "isbn": "9788966261840",
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
  "message": "상품이 등록되었습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261840",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 33000,
    "quantity": 50,
    "bookType": "NEW",
    "status": "PENDING_REVIEW",
    "createdAt": "2026-03-01T09:00:00"
  }
}
```

**2. 클라이언트 오류 — 유효하지 않은 입력값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "price": "가격은 0 이상이어야 합니다.",
    "isbn": "ISBN은 필수입니다."
  }
}
```

**3. 클라이언트 오류 — ISBN 중복**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "PRODUCT_ISBN_DUPLICATE",
  "message": "이미 등록된 ISBN입니다.",
  "data": null
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

---

### `PATCH /products/{productId}` — 상품 수정

기존 상품 정보를 수정합니다. 변경하고자 하는 필드만 전달합니다.

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
| `price` | integer | X | 가격 (0 이상) |
| `quantity` | integer | X | 재고 수량 (0 이상) |

Request Body Example:

```json
{
  "title": "클린 코드 (개정판)",
  "price": 35000,
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
  "message": "상품이 수정되었습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261840",
    "title": "클린 코드 (개정판)",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 35000,
    "quantity": 100,
    "bookType": "NEW",
    "status": "ACTIVE",
    "avgRating": 4.5,
    "reviewCount": 128,
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

**3. 클라이언트 오류 — 유효하지 않은 입력값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "price": "가격은 0 이상이어야 합니다."
  }
}
```

---

### `PATCH /products/{productId}/status` — 상품 상태 변경

상품의 판매 상태를 변경합니다.

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
| `status` | string | O | 변경할 상태 (`PENDING_REVIEW`, `ACTIVE`, `INACTIVE`, `SOLD_OUT`) |

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
  "message": "상품 상태가 변경되었습니다.",
  "data": {
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "sellerId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryId": "550e8400-e29b-41d4-a716-446655440002",
    "isbn": "9788966261840",
    "title": "클린 코드",
    "author": "로버트 C. 마틴",
    "publisher": "인사이트",
    "price": 33000,
    "quantity": 50,
    "bookType": "NEW",
    "status": "ACTIVE",
    "avgRating": 4.5,
    "reviewCount": 128,
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

**3. 클라이언트 오류 — 유효하지 않은 입력값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "status": "유효하지 않은 상태값입니다."
  }
}
```

---

### `GET /products/categories` — 카테고리 목록 조회

카테고리 목록을 계층형 트리 구조로 조회합니다.

#### Request

없음

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CATEGORY_LIST_GET_SUCCESS",
  "message": "카테고리 목록 조회에 성공했습니다.",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "name": "국내도서",
      "slug": "domestic",
      "depth": 0,
      "sortOrder": 1,
      "children": [
        {
          "id": "550e8400-e29b-41d4-a716-446655440011",
          "name": "소설",
          "slug": "domestic-novel",
          "depth": 1,
          "sortOrder": 1,
          "children": []
        },
        {
          "id": "550e8400-e29b-41d4-a716-446655440012",
          "name": "자기계발",
          "slug": "domestic-self-help",
          "depth": 1,
          "sortOrder": 2,
          "children": []
        }
      ]
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440013",
      "name": "외국도서",
      "slug": "foreign",
      "depth": 0,
      "sortOrder": 2,
      "children": []
    }
  ]
}
```

---

### `POST /products/categories` — 카테고리 추가

새로운 카테고리를 추가합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `parentId` | UUID | X | 부모 카테고리 ID (미입력 시 최상위 카테고리로 생성) |
| `name` | string | O | 카테고리명 (최대 100자) |
| `slug` | string | O | 슬러그 (최대 100자, URL 식별자) |
| `sortOrder` | integer | O | 정렬 순서 (0 이상) |

Request Body Example:

```json
{
  "parentId": "550e8400-e29b-41d4-a716-446655440010",
  "name": "판타지",
  "slug": "domestic-fantasy",
  "sortOrder": 3
}
```

#### Response

**1. 요청 성공**

Status Code: `201 Created`

```json
{
  "status": 201,
  "code": "CATEGORY_CREATE_SUCCESS",
  "message": "카테고리가 등록되었습니다.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "parentId": "550e8400-e29b-41d4-a716-446655440010",
    "name": "판타지",
    "slug": "domestic-fantasy",
    "depth": 1,
    "sortOrder": 3
  }
}
```

**2. 클라이언트 오류 — slug 중복**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "CATEGORY_SLUG_DUPLICATE",
  "message": "이미 사용 중인 슬러그입니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 존재하지 않는 부모 카테고리**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "CATEGORY_NOT_FOUND",
  "message": "해당 카테고리를 찾을 수 없습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 유효하지 않은 입력값**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "name": "카테고리명은 필수입니다.",
    "slug": "슬러그는 필수입니다."
  }
}
```

---

## 📌 Review API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 별점 등록 | POST | /review | 주문 상품에 별점(1~5) 등록 |
| 내 별점 수정 | PATCH | /review/{reviewId} | 별점 수정 |
| 내 별점 삭제 | DELETE | /review/{reviewId} | 별점 삭제 |
| 상품별 별점 조회 | GET | /review/product/{productId} | 상품의 별점 목록 조회 |

---

## 📌 Order API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 장바구니 조회 | GET | /order/cart | 장바구니 조회 |
| 장바구니 상품 추가 | POST | /order/cart/item | 상품 추가 |
| 장바구니 수량 수정 | PATCH | /order/cart/item/{cartItemId} | 수량 변경 |
| 장바구니 항목 삭제 | DELETE | /order/cart/item/{cartItemId} | 항목 삭제 |
| 장바구니 비우기 | DELETE | /order/cart | 전체 삭제 |
| 주문 생성 | POST | /order | 주문 생성 |
| 주문 목록 조회 | GET | /order | 주문 리스트 |
| 주문 상세 조회 | GET | /order/{orderId} | 주문 상세 |
| 주문 취소 | POST | /order/{orderId}/cancel | 주문 취소 |
| 반품 신청 | POST | /order/{orderId}/return | 반품 요청 |
| 송장 등록/배송 정보 수정 | PATCH | /order/{orderId}/shipment | 송장/배송 수정 |

### `GET /order/cart` — 장바구니 조회

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | X | 회원 사용 시 Bearer access token |
| `X-Cart-Token` | string | X | 비회원 사용 시 장바구니 식별 토큰 |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "CART_FETCH_SUCCESS",
  "message": "장바구니 조회에 성공했습니다.",
  "data": {
    "cartId": 1,
    "items": [
      {
        "cartItemId": 1,
        "productId": 101,
        "productName": "도서 A",
        "price": 15000,
        "quantity": 2,
        "stock": 10,
        "imageUrl": "https://example.com/book-a.jpg",
        "sellerId": 11
      },
      {
        "cartItemId": 2,
        "productId": 102,
        "productName": "도서 B",
        "price": 22000,
        "quantity": 1,
        "stock": 3,
        "imageUrl": "https://example.com/book-b.jpg",
        "sellerId": 12
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
| `Authorization` | string | X | 회원 사용 시 Bearer access token |
| `X-Cart-Token` | string | X | 비회원 사용 시 장바구니 식별 토큰 |

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
    "cartItemId": 1,
    "productId": 101,
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

**6. 클라이언트 오류 — 재고 부족**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "재고가 부족합니다.",
  "data": {
    "availableStock": 1
  }
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

### `PATCH /order/cart/item/{cartItemId}` — 장바구니 수량 수정

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cartItemId` | number | O | 수정할 장바구니 항목 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | X | 회원 사용 시 Bearer access token |
| `X-Cart-Token` | string | X | 비회원 사용 시 장바구니 식별 토큰 |

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
    "cartItemId": 1,
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

**6. 클라이언트 오류 — 재고 부족**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "재고가 부족합니다.",
  "data": {
    "availableStock": 2
  }
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

### `DELETE /order/cart/item/{cartItemId}` — 장바구니 항목 삭제

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `cartItemId` | number | O | 삭제할 장바구니 항목 ID |

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | X | 회원 사용 시 Bearer access token |
| `X-Cart-Token` | string | X | 비회원 사용 시 장바구니 식별 토큰 |

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
| `Authorization` | string | X | 회원 사용 시 Bearer access token |
| `X-Cart-Token` | string | X | 비회원 사용 시 장바구니 식별 토큰 |

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

### `POST /order` — 주문 생성

> 상세 Request / Response 추후 추가 예정

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
| `status` | string | X | 주문 상태 필터 (`PENDING_PAYMENT`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`) |
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
        "orderId": 1001,
        "orderNumber": "20260318-000001",
        "orderStatus": "CONFIRMED",
        "totalAmount": 55000,
        "createdAt": "2026-03-18T10:00:00"
      },
      {
        "orderId": 1002,
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
| `orderId` | number | O | 조회할 주문 ID |

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
    "orderId": 1001,
    "orderNumber": "20260318-000001",
    "orderStatus": "CONFIRMED",
    "recipient": "홍길동",
    "phone": "01012345678",
    "address": "서울특별시 강남구 테헤란로 1 101동 1001호",
    "items": [
      {
        "orderItemId": 1,
        "productId": 101,
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
| `orderId` | number | O | 취소할 주문 ID |

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
  "orderItemIds": [1, 2]
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
    "orderId": 1001,
    "returnStatus": "PENDING",
    "orderItemIds": [1, 2]
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

### `POST /order/{orderId}/return` — 반품 신청

#### Request

Path Variable:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | number | O | 반품 신청할 주문 ID |

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
  "orderItemIds": [1, 2]
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
    "orderId": 1001,
    "returnStatus": "PENDING",
    "orderItemIds": [1, 2]
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
| `orderId` | number | O | 배송 정보를 수정할 주문 ID |

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
    "orderId": 1001,
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

## 📌 Payment API

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 결제 준비 | POST | /payment/ready | 결제 요청 준비 |
| 결제 승인 | POST | /payment/confirm | 결제 승인 |
| 결제 상세 조회 | GET | /payment/{paymentId} | 결제 정보 |
| 주문 기준 결제 조회 | GET | /payment/order/{orderId} | 주문 기준 조회 |
| 내 결제 내역 조회 | GET | /payment/me | 사용자 결제 리스트 |
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

### `POST /payment/ready` — 결제 준비

주문 결제를 준비합니다. 주문 금액을 검증하고, 예치금 사용 여부를 확인한 뒤, PG 결제창 호출에 필요한 데이터를 반환합니다. 내부적으로 PAYMENT(status=PENDING) 레코드를 생성합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |
| `Content-Type` | string | O | `application/json` |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `orderId` | UUID | O | 주문 ID |
| `useDeposit` | boolean | X | 예치금 사용 여부 (기본값: false) |
| `depositAmount` | number | X | 예치금 사용 금액 (useDeposit=true일 때 필수, 0 이상, 보유 잔액 이하) |

Request Body Example:

```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "useDeposit": true,
  "depositAmount": 10000
}
```

#### Response

**1. 요청 성공 — PG 결제 필요**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PAYMENT_READY",
  "message": "결제 준비가 완료되었습니다.",
  "data": {
    "paymentId": "660e8400-e29b-41d4-a716-446655440001",
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "orderName": "클린 코드 외 2건",
    "totalAmount": 35000,
    "depositUsed": 10000,
    "pgPaymentAmount": 25000,
    "pgRequired": true,
    "pgProvider": "TOSS",
    "pgData": {
      "clientKey": "test_ck_xxx",
      "orderId": "ORDER-20260320-ABC123",
      "amount": 25000,
      "orderName": "클린 코드 외 2건",
      "successUrl": "https://example.com/payment/success",
      "failUrl": "https://example.com/payment/fail"
    }
  }
}
```

Response Body 필드:

| 필드 | 타입 | 설명 |
|------|------|------|
| `paymentId` | UUID | 생성된 PAYMENT 레코드 ID |
| `orderId` | UUID | 주문 ID |
| `orderName` | string | 주문 표시명 (상품명 + 외 N건) |
| `totalAmount` | number | 주문 총 금액 |
| `depositUsed` | number | 예치금 차감액 |
| `pgPaymentAmount` | number | PG 실결제 금액 (totalAmount - depositUsed) |
| `pgRequired` | boolean | PG 결제창 호출 필요 여부 |
| `pgProvider` | string | PG사 식별자 (TOSS) |
| `pgData` | object | PG 결제창 호출에 필요한 데이터 (PG사별 상이) |

**2. 요청 성공 — 예치금 전액 결제 (PG 불필요)**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PAYMENT_COMPLETED_BY_DEPOSIT",
  "message": "예치금으로 결제가 완료되었습니다.",
  "data": {
    "paymentId": "660e8400-e29b-41d4-a716-446655440001",
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "orderName": "클린 코드 외 2건",
    "totalAmount": 35000,
    "depositUsed": 35000,
    "pgPaymentAmount": 0,
    "pgRequired": false,
    "pgProvider": null,
    "pgData": null
  }
}
```

**3. 클라이언트 오류 — 주문을 찾을 수 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "주문 정보를 찾을 수 없습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 예치금 잔액 부족**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INSUFFICIENT_DEPOSIT",
  "message": "예치금 잔액이 부족합니다.",
  "data": null
}
```

**5. 클라이언트 오류 — 이미 결제된 주문**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ORDER_ALREADY_PAID",
  "message": "이미 결제가 완료된 주문입니다.",
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

### `POST /payment/confirm` — 결제 승인

PG 결제 승인을 완료합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Content-Type` | string | O | `application/json` |
| `Authorization` | string | O | Bearer access token |

Request Body:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `orderId` | number | O | 주문 ID |
| `paymentMethodId` | number | O | 결제수단 ID |
| `amount` | number | O | 결제 금액 (1 이상) |
| `currency` | string | X | 통화 코드 (기본값: `KRW`) |

Request Body Example:

```json
{
  "orderId": 1001,
  "paymentMethodId": 5,
  "amount": 54000,
  "currency": "KRW"
}
```

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "PAYMENT_REQUESTED",
  "message": "결제가 성공적으로 요청되었습니다.",
  "data": {
    "paymentId": 3001,
    "orderId": 1001,
    "amount": 54000,
    "currency": "KRW",
    "paymentStatus": "COMPLETED",
    "paidAt": "2026-03-18T14:30:00Z"
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
    "missingFields": ["orderId", "amount"]
  }
}
```

**3. 클라이언트 오류 — 유효하지 않은 결제 금액**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_AMOUNT",
  "message": "결제 금액이 유효하지 않습니다.",
  "data": null
}
```

**4. 클라이언트 오류 — 주문 금액 불일치**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "AMOUNT_MISMATCH",
  "message": "결제 금액이 주문 금액과 일치하지 않습니다.",
  "data": {
    "expectedAmount": 54000,
    "requestedAmount": 50000
  }
}
```

**5. 클라이언트 오류 — 존재하지 않는 주문**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "해당 주문을 찾을 수 없습니다.",
  "data": null
}
```

**6. 클라이언트 오류 — 존재하지 않는 결제수단**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PAYMENT_METHOD_NOT_FOUND",
  "message": "해당 결제수단을 찾을 수 없습니다.",
  "data": null
}
```

**7. 클라이언트 오류 — 이미 결제된 주문**

Status Code: `409 Conflict`

```json
{
  "status": 409,
  "code": "ALREADY_PAID",
  "message": "이미 결제가 완료된 주문입니다.",
  "data": null
}
```

**8. 결제 실패 (PG사 응답 오류)**

Status Code: `402 Payment Required`

```json
{
  "status": 402,
  "code": "PAYMENT_FAILED",
  "message": "결제 처리에 실패했습니다.",
  "data": {
    "pgErrorCode": "CARD_DECLINED",
    "pgErrorMessage": "카드 결제가 거절되었습니다."
  }
}
```

**9. 인증 오류**

Status Code: `401 Unauthorized`

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "data": null
}
```

**10. 서버 오류**

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

### `GET /payment/{paymentId}` — 결제 상세 조회

특정 결제 건의 상세 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `paymentId` | number | O | 조회할 결제 ID |

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
  "code": "SUCCESS",
  "message": "결제 상세 조회에 성공했습니다.",
  "data": {
    "paymentId": 3001,
    "orderId": 1001,
    "userId": 501,
    "amount": 54000,
    "currency": "KRW",
    "paymentStatus": "COMPLETED",
    "paymentMethod": {
      "paymentMethodId": 5,
      "type": "CARD",
      "cardCompany": "신한카드",
      "cardNumber": "****-****-****-1234"
    },
    "pgTransactionId": "pg_txn_abc123def456",
    "paidAt": "2026-03-18T14:30:00Z",
    "refund": null,
    "createdAt": "2026-03-18T14:29:55Z"
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

**3. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 결제 정보에 대한 접근 권한이 없습니다.",
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

### `GET /payment/order/{orderId}` — 주문 기준 결제 조회

특정 주문에 대한 결제 정보를 조회합니다.

#### Request

Path Parameter:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `orderId` | number | O | 조회할 주문 ID |

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
  "code": "SUCCESS",
  "message": "주문 기준 결제 조회에 성공했습니다.",
  "data": {
    "orderId": 1001,
    "payments": [
      {
        "paymentId": 3001,
        "amount": 54000,
        "currency": "KRW",
        "paymentStatus": "COMPLETED",
        "paymentMethod": {
          "type": "CARD",
          "cardCompany": "신한카드",
          "cardNumber": "****-****-****-1234"
        },
        "paidAt": "2026-03-18T14:30:00Z"
      }
    ],
    "totalPaidAmount": 54000,
    "refundedAmount": 0
  }
}
```

**2. 클라이언트 오류 — 존재하지 않는 주문**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "ORDER_NOT_FOUND",
  "message": "해당 주문을 찾을 수 없습니다.",
  "data": null
}
```

**3. 클라이언트 오류 — 결제 내역 없음**

Status Code: `404 Not Found`

```json
{
  "status": 404,
  "code": "PAYMENT_NOT_FOUND",
  "message": "해당 주문에 대한 결제 내역이 없습니다.",
  "data": null
}
```

**4. 권한 오류**

Status Code: `403 Forbidden`

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "해당 주문의 결제 정보에 대한 접근 권한이 없습니다.",
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

### `GET /payment/me` — 내 결제 내역 조회

로그인한 사용자의 결제 내역을 조회합니다.

#### Request

Request Header:

| 헤더 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `Authorization` | string | O | Bearer access token |

Query String:

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `page` | number | X | 페이지 번호 (기본값: 1) |
| `limit` | number | X | 페이지당 항목 수 (기본값: 20, 최대: 100) |
| `status` | string | X | 결제 상태 필터 (`PENDING`, `COMPLETED`, `FAILED`, `CANCELLED`) |
| `startDate` | string | X | 조회 시작일 (형식: `YYYY-MM-DD`) |
| `endDate` | string | X | 조회 종료일 (형식: `YYYY-MM-DD`) |

#### Response

**1. 요청 성공**

Status Code: `200 OK`

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "결제 내역 조회에 성공했습니다.",
  "data": {
    "payments": [
      {
        "paymentId": 3001,
        "orderId": 1001,
        "amount": 54000,
        "currency": "KRW",
        "paymentStatus": "COMPLETED",
        "paymentMethod": {
          "type": "CARD",
          "cardCompany": "신한카드",
          "cardNumber": "****-****-****-1234"
        },
        "paidAt": "2026-03-18T14:30:00Z"
      },
      {
        "paymentId": 3002,
        "orderId": 1002,
        "amount": 32000,
        "currency": "KRW",
        "paymentStatus": "COMPLETED",
        "paymentMethod": {
          "type": "CARD",
          "cardCompany": "국민카드",
          "cardNumber": "****-****-****-5678"
        },
        "paidAt": "2026-03-17T10:15:00Z"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 3,
      "totalItems": 47,
      "limit": 20
    }
  }
}
```

**2. 클라이언트 오류 — 잘못된 날짜 형식**

Status Code: `400 Bad Request`

```json
{
  "status": 400,
  "code": "INVALID_DATE_FORMAT",
  "message": "날짜 형식이 올바르지 않습니다. (형식: YYYY-MM-DD)",
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
| `status` | string | X | 정산 상태 필터 (`PENDING`, `COMPLETED`) |
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