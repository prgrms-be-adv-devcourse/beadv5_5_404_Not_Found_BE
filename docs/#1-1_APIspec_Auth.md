# API Specification - Auth Module

> 도서 이커머스 플랫폼 API 명세서
> Auth API (POST /auth/*)

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
