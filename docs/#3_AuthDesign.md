# 인증/인가 설계 문서

> 도서 이커머스 플랫폼 | JWT 기반 | MSA (Member / Product / Order / Payment / Review)
> Gateway: Spring Cloud Gateway (토큰 검증) | Service Discovery: Eureka Server

---

## 1. 개요

사용자 인증 방식 및 권한 관리를 정의한 문서입니다.

**Member Service**가 회원가입, 로그인, 토큰 발급/재발급/로그아웃 등 인증 기능을 전담합니다. **Spring Cloud Gateway**는 발급된 토큰의 검증만 수행하며, 인가(role/email_verified 등)는 각 서비스 내부에서 처리합니다.

비회원은 상품 목록/상세 조회만 가능하며, 장바구니를 포함한 모든 서비스 이용은 로그인 및 이메일 인증이 필요합니다.

> Redis는 현재 사용하지 않습니다. 추후 트래픽 증가 시 도입을 논의할 예정입니다.

### 인증/인가 책임 분리

| 구성 요소 | 역할 |
|----------|------|
| **Member Service** | 회원가입, 로그인, 토큰 발급/재발급, 로그아웃, REFRESH_TOKEN/TOKEN_BLACKLIST 관리 |
| **Spring Cloud Gateway** | JWT 서명 검증, 만료 확인, 블랙리스트 조회, 공개 API 바이패스, 서비스 라우팅 |
| **각 도메인 서비스** | email_verified 확인, role 기반 인가, SELLER 상태 확인 등 비즈니스 인가 |
| **Eureka Server** | 서비스 등록/탐색, Gateway의 동적 라우팅 지원 |

### 사용자 유형

| 유형 | 설명 |
|------|------|
| **Guest** (비회원) | 상품 목록/상세 조회만 가능 |
| **USER** (이메일 미인증) | 상품 조회만 허용, 장바구니 포함 그 외 전체 차단 |
| **USER** (이메일 인증 완료) | 장바구니, 주문, 결제, 배송지 관리, 예치금, 마이페이지, 리뷰 |
| **SELLER** | USER 권한 포함 + 상품 등록/수정, 재고 관리, 정산 조회 (`SELLER.status = APPROVED` 필수) |
| **ADMIN** | 회원 관리, 판매자 심사, 상품 검수, 계정 정지, 정산 관리 |

---

## 2. 인증 방식

### 2.1 토큰 구조

**Access Token**

| 항목 | 값 |
|------|-----|
| 만료시간 | 15~30분 |
| 저장 위치 | 클라이언트 메모리 (Authorization 헤더로 전송) |
| 용도 | API 요청 시 인증 |

**Refresh Token**

| 항목 | 값 |
|------|-----|
| 만료시간 | 15~30일 |
| 저장 위치 | DB (`REFRESH_TOKEN` 테이블, Member DB) |
| 용도 | Access Token 재발급 |

### 2.2 Access Token Payload

```json
{
  "sub": "member_id (UUID)",
  "jti": "token 고유 ID (UUID)",
  "role": "USER | SELLER | ADMIN",
  "email_verified": true,
  "iat": 1700000000,
  "exp": 1700001800
}
```

| 필드 | 설명 |
|------|------|
| `sub` | 회원 ID |
| `jti` | 토큰 고유 식별자. 블랙리스트 등록 시 사용 |
| `role` | 권한 (USER / SELLER / ADMIN) |
| `email_verified` | 이메일 인증 여부. `false`이면 상품 조회 외 전체 차단 |
| `iat` / `exp` | 발급 시간 / 만료 시간 |

> `seller_id`와 `seller_status`는 페이로드에 포함하지 않습니다. 판매자 상태는 관리자가 실시간으로 변경할 수 있으므로, 판매자 API 진입 시마다 DB에서 `SELLER.status = APPROVED`를 별도 조회합니다.

### 2.3 Refresh Token 저장 (REFRESH_TOKEN 테이블)

Refresh Token 원문은 저장하지 않고 SHA-256 해시값만 저장합니다. Member DB에 위치합니다.

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | PK |
| `member_id` | UUID | `MEMBER.id` 참조 |
| `token_hash` | VARCHAR(512) | SHA-256 해시값 |
| `user_agent` | VARCHAR | 클라이언트 User-Agent |
| `ip_address` | VARCHAR | 요청 IP (IPv4/IPv6) |
| `is_revoked` | BOOLEAN | 폐기 여부. 로그아웃/정지/탈퇴 시 `true` |
| `expires_at` | TIMESTAMP | 만료 시각 |
| `created_at` | TIMESTAMP | 발급 시각 |
| `last_used_at` | TIMESTAMP | 마지막 사용 시각. 재발급 시 갱신 |

### 2.4 Access Token 블랙리스트 (TOKEN_BLACKLIST 테이블)

로그아웃, 회원 정지, 탈퇴 시 해당 Access Token의 `jti`를 블랙리스트 테이블에 등록해 즉각 차단합니다. Member DB에 위치하며, Gateway가 Member Service API를 통해 조회합니다.

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT | PK (AUTO_INCREMENT) |
| `jti` | VARCHAR(255) | Access Token 고유 식별자 (UNIQUE) |
| `expires_at` | TIMESTAMP | 해당 Access Token의 만료 시각 |
| `created_at` | TIMESTAMP | 블랙리스트 등록 시각 |

> 만료된 블랙리스트 항목은 스케줄러가 주기적으로 삭제합니다 (`expires_at < NOW()`).

---

## 3. Token Rotation 및 탈취 감지

재발급 시마다 Refresh Token을 교체합니다. 이미 사용된 구 토큰으로 재발급을 시도하면 탈취로 간주하고 해당 회원의 전체 Refresh Token을 즉시 무효화합니다.

```
재발급 요청 수신 (Member Service)
→ 토큰 해시로 DB 조회 → is_revoked = true 확인 (이미 교체된 토큰)
→ 탈취로 판단
→ 해당 member_id의 REFRESH_TOKEN 전체 is_revoked = true
→ 401 반환 → 재로그인 유도
```

---

## 4. 인증 흐름

### 4.1 회원가입

```
Client → Gateway → Member Service

1. 이메일, 비밀번호, 이름, 연락처 전송
2. 이메일 중복 검증 (MEMBER.email UNIQUE)
3. 비밀번호 유효성 검증
   - 8자 이상 20자 이하
   - 영문 대소문자 + 숫자 + 특수문자 각 1자 이상 포함
4. 비밀번호 BCrypt 해싱 저장
5. MEMBER 생성: role = USER, status = ACTIVE, email_verified = false
6. Access Token + Refresh Token 발급 (Member Service에서 발급)
7. Refresh Token SHA-256 해시 → REFRESH_TOKEN INSERT
8. Access Token은 응답 바디, Refresh Token은 HttpOnly Cookie로 반환
9. 이메일 인증 이벤트 발행 (Spring Event) → 인증 메일 발송
```

> 가입 직후 `email_verified = false` 상태이므로 이메일 인증 완료 전까지 상품 조회 외 모든 기능은 사용할 수 없습니다.

### 4.2 로그인

```
Client → Gateway → Member Service

1. 이메일 + 비밀번호 전송
2. MEMBER.status 검증: ACTIVE만 통과, WITHDRAWN/SUSPENDED는 401 반환
3. BCrypt 검증: 입력 비밀번호 vs MEMBER.password_hash
4. Access Token + Refresh Token 발급 (Member Service에서 발급)
5. REFRESH_TOKEN INSERT
6. Access Token은 응답 바디, Refresh Token은 HttpOnly Cookie로 반환
```

> 동시 로그인을 허용합니다. 기기별로 독립된 Refresh Token이 발급되며, 동일 `member_id`로 여러 행이 공존할 수 있습니다. 만료된 행은 스케줄러가 주기적으로 삭제합니다.

### 4.3 토큰 재발급

```
Client → Gateway → Member Service

1. 만료된 Access Token으로 API 호출 → Gateway에서 401 응답
2. Refresh Token으로 POST /auth/refresh 요청
3. Member Service: Refresh Token SHA-256 해시 → DB 조회
4. is_revoked = false, expires_at > NOW() 검증 → 실패 시 401 반환
5. 기존 Refresh Token 행 is_revoked = true (구 토큰 폐기)
6. 새 Access Token + 새 Refresh Token 발급 (Token Rotation)
7. 새 Refresh Token INSERT
8. 새 토큰 반환
```

### 4.4 로그아웃

```
Client → Gateway → Member Service

1. POST /auth/logout 요청
2. Member Service: 해당 Refresh Token 행 is_revoked = true
3. 현재 Access Token의 jti → TOKEN_BLACKLIST 테이블에 INSERT
4. 클라이언트: Access Token 메모리에서 제거
```

> 특정 기기만 로그아웃하는 경우 해당 기기의 Refresh Token만 처리합니다. 전체 기기 로그아웃이라면 해당 `member_id`의 모든 Refresh Token을 `is_revoked = true`로 변경합니다.

---

## 5. 인가

### 5.1 API 접근 규칙

**인증 없이 접근 가능 (비회원 포함)**

| 경로 | 라우팅 대상 | 설명 |
|------|-----------|------|
| `GET /product` | Product Service | 상품 목록 조회 |
| `GET /product/{productId}` | Product Service | 상품 상세 조회 |
| `GET /product/category/**` | Product Service | 카테고리 조회 |
| `POST /auth/register` | Member Service | 회원가입 |
| `POST /auth/login` | Member Service | 로그인 |
| `POST /auth/refresh` | Member Service | 토큰 재발급 |

> 비회원은 상품 목록/상세 조회만 가능합니다. 리뷰 조회, 장바구니, 주문 등 그 외 모든 기능은 로그인이 필요합니다.

**로그인 + 이메일 인증 필요 (USER 이상)**

| 경로 | 설명 |
|------|------|
| `POST /auth/logout` | 로그아웃 |
| `/member/me/**` | 내 정보, 배송지, 예치금 |
| `/order/cart/**` | 장바구니 |
| `/order/**` | 주문 |
| `/payment/**` | 결제/환불 |
| `GET /review/product/{productId}` | 상품별 별점 조회 |
| `POST /review` | 별점 등록 |
| `PATCH /review/{reviewId}` | 별점 수정 |
| `DELETE /review/{reviewId}` | 별점 삭제 |

**SELLER 전용 (`SELLER.status = APPROVED` 추가 확인)**

| 경로 | 설명 |
|------|------|
| `POST /product` | 상품 등록 |
| `PATCH /product/{productId}` | 상품 수정 |
| `PATCH /product/{productId}/inventory` | 재고 관리 |
| `GET /payment/settlement/me` | 내 정산 조회 |
| `GET /payment/settlement/{settlementId}` | 정산 상세 조회 |
| `POST /payment/settlement/payout` | 출금 신청 |

**ADMIN 전용**

| 경로 | 설명 |
|------|------|
| `PATCH /member/admin/seller/{memberId}` | 판매자 승인/거절 |
| `GET /payment/commission` | 수수료 정책 조회 |
| `POST /payment/commission` | 수수료 정책 등록 |

### 5.2 인증/인가 처리 순서

```
요청 수신 (Spring Cloud Gateway)
→ Eureka에서 대상 서비스 인스턴스 탐색
→ 공개 API 여부 확인 → 해당하면 인증 없이 서비스로 라우팅
→ Authorization 헤더 확인 → 없으면 401 반환
→ Access Token 서명 검증 + 만료 확인 (Gateway에서 수행)
→ Member Service에 TOKEN_BLACKLIST jti 조회 → 등록되어 있으면 401 반환
→ 해당 서비스로 라우팅 (JWT 페이로드 헤더로 전달)

각 서비스 내부:
→ email_verified 확인 → false이면 403 반환
→ role 확인 → 접근 권한 없으면 403 반환
→ SELLER API 진입 시 Member Service에 SELLER.status = APPROVED 확인 (REST API) → 불충족 시 403 반환
→ 비즈니스 로직 실행
```

### 5.3 판매자 API 이중 검증

JWT 페이로드의 `role = SELLER` 확인만으로는 부족합니다. 판매자 상태는 실시간으로 변경될 수 있으므로 매 요청마다 Member Service를 통해 `SELLER.status = APPROVED`를 별도 확인합니다. 상태가 `PENDING` 또는 `SUSPENDED`이면 403 Forbidden을 반환합니다.

---

## 6. 비밀번호 정책

| 항목 | 규칙 |
|------|------|
| 길이 | 8자 이상 20자 이하 |
| 복잡도 | 영문 대소문자 + 숫자 + 특수문자 각 1자 이상 포함 필수 |
| 저장 | BCrypt 해싱 (cost factor 10 이상 권장) |
| 변경 | 현재 비밀번호 확인 후 변경 허용 |

---

## 7. 계정 보안 처리

### 7.1 회원 정지 (SUSPENDED)

```
1. 관리자가 Member Service를 통해 MEMBER.status = SUSPENDED로 변경
2. 해당 member_id의 REFRESH_TOKEN 전체 is_revoked = true
3. 현재 Access Token의 jti → TOKEN_BLACKLIST INSERT
4. 이후 API 요청: Gateway의 블랙리스트 조회에서 즉시 차단
5. 이후 재발급 시도: MEMBER.status 검증에서 차단
```

### 7.2 회원 탈퇴 (WITHDRAWN)

```
1. 사용자가 Member Service에 탈퇴 요청
2. MEMBER.status = WITHDRAWN
3. 해당 member_id의 REFRESH_TOKEN 전체 is_revoked = true
4. 현재 Access Token의 jti → TOKEN_BLACKLIST INSERT
5. 이후 로그인 시도: MEMBER.status 검증에서 401 반환
```
