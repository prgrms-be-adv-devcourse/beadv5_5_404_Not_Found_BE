# Enum / 상수값 정의서

> 도서 이커머스 서비스 공통 값 정의
> DTO, Entity, API 명세, ERD, 테스트 코드에서 동일하게 사용할 공통 값 정의
> **문자열 하드코딩 금지, 상태값은 본 문서를 기준으로 통일**

---

## Member 영역

### MemberRole — 회원 권한

| 값 | 설명 | 사용 위치 |
|----|------|----------|
| `USER` | 일반 회원 | 회원가입 기본값 |
| `ADMIN` | 관리자 | 운영자 계정 |
| `SELLER` | 판매자 권한 보유 회원 | 논의 필요 |

### MemberStatus — 회원 상태

| 값 | 설명 | 전이 조건 |
|----|------|----------|
| `ACTIVE` | 정상 이용 가능 | 회원가입 완료 시 기본값 |
| `SUSPENDED` | 이용 정지 | 관리자 제재 |
| `WITHDRAWN` | 탈퇴 | 회원 탈퇴 |

### AuthProvider — 가입 경로

| 값 | 설명 |
|----|------|
| `LOCAL` | 이메일 회원 가입 |

> 최소한 `LOCAL`만 확정해두고, 추후 확장 시 `GOOGLE`, `KAKAO`, `NAVER` 등을 추가

### SellerStatus — 판매자 상태

| 값 | 설명 | 전이 조건 |
|----|------|----------|
| `PENDING` | 승인 대기 | 판매자 신청 시 |
| `APPROVED` | 승인 완료 | 관리자 승인 |
| `SUSPENDED` | 판매 정지 | 관리자 제재 |

---

## Address 영역

### AddressStatus

별도 enum 없이 아래 boolean으로 관리 권장

| 필드 | 값 | 설명 |
|------|-----|------|
| `is_default` | `true` / `false` | 기본 배송지 여부 |
| `is_deleted` | `true` / `false` | 소프트 삭제 여부 |

### AddressLabelType — 배송지 라벨

| 값 | 설명 |
|----|------|
| `HOME` | 집 |
| `OFFICE` | 회사 |
| `ETC` | 기타 |

---

## Product 영역

### BookType — 도서 유형

| 값 | 설명 |
|----|------|
| `NEW` | 신간/새책 |
| `USED` | 중고도서 |

### ProductStatus — 상품 상태

| 값 | 설명 | 전이 조건 |
|----|------|----------|
| `PENDING_REVIEW` | 검수 대기 | 상품 등록 직후 |
| `ACTIVE` | 판매 가능 | 검수 승인 |
| `INACTIVE` | 판매 중지 | 운영 제재/판매 중단 |
| `SOLD_OUT` | 품절 | 재고 0 (시스템 자동 전환) |

**허용 상태 전환 (관리자 수동 변경 기준)**

| 현재 상태 | 전환 가능 상태 | 비고 |
|-----------|--------------|------|
| `PENDING_REVIEW` | `ACTIVE`, `INACTIVE` | 검수 승인/거부 |
| `ACTIVE` | `INACTIVE` | 판매 중단 |
| `INACTIVE` | `ACTIVE` | 판매 재개 |
| `SOLD_OUT` | 불가 | 재고 복원 시 시스템이 자동으로 `ACTIVE` 전환 |

---

## Review 영역

> 리뷰는 텍스트 내용 없이 **별점(1~5)만** 등록합니다. 주문 완료된 상품에 대해 회원당 1개의 별점만 등록 가능합니다.

### Review 정책 상수값

| 상수 | 값 | 설명 |
|------|-----|------|
| `REVIEW_RATING_MIN` | `1` | 최소 별점 |
| `REVIEW_RATING_MAX` | `5` | 최대 별점 |

> 별점 허용 값은 `1, 2, 3, 4, 5` 이다.

---

## Cart 영역

### Cart 정책값

| 상수 | 값 | 설명 |
|------|-----|------|
| `CART_PER_MEMBER_LIMIT` | `1` | 회원당 장바구니 개수 |
| `CART_ITEM_MIN_QUANTITY` | `1` | 장바구니 상품 최소 수량 |
| `CART_ITEM_MAX_QUANTITY` | `99` | 장바구니 상품 최대 수량 |

---

## Order 영역

### OrderStatus — 주문 상태

| 값 | 설명 | 전이 조건 |
|----|------|----------|
| `PENDING_PAYMENT` | 결제 대기 | 주문 생성 시 기본값 |
| `CONFIRMED` | 주문 확정 | 결제 완료 시 |
| `SHIPPING` | 배송 중 | 배송 시작 시 |
| `DELIVERED` | 배송 완료 | 배송 완료 시 |
| `PURCHASE_CONFIRMED` | 구매확정 | 수동 확정 또는 배송 완료 후 7일 경과 시 자동 전환 |
| `CANCELLED` | 주문 취소 | 취소 완료 시 |

### OrderItemStatus — 주문상품 상태

> ERD에 `ORDER_ITEM.status`가 따로 있으니 주문 전체 상태와 분리

| 값 | 설명 |
|----|------|
| `PENDING_PAYMENT` | 결제 대기 |
| `CONFIRMED` | 주문 확정 |
| `PREPARING` | 출고 준비 |
| `SHIPPED` | 발송 |
| `DELIVERED` | 배송 완료 |
| `CANCELLED` | 취소 |
| `REFUNDED` | 환불 완료 |

---

## Shipment 영역

### ShipmentStatus — 배송 상태

| 값 | 설명 |
|----|------|
| `PREPARING` | 배송 준비중 |
| `SHIPPED` | 발송 완료 |
| `IN_TRANSIT` | 배송 중 |
| `DELIVERED` | 배송 완료 |
| `RETURNED` | 반송 |

---

## Payment 영역

### PaymentMethodType — 결제 방식 구분

| 값 | 설명 |
|----|------|
| `DEPOSIT` | 예치금 결제 |
| `PG` | 외부 PG 결제 |

### PgProvider — PG사 구분

| 값 | 설명 |
|----|------|
| `TOSS` | 현재 기본 PG |

### PaymentStatus — 결제 상태

| 값 | 설명 |
|----|------|
| `PENDING` | 결제 요청 생성 |
| `COMPLETED` | 결제 승인 완료 |
| `FAILED` | 결제 실패 |
| `CANCELLED` | 결제 취소 |

---

### PaymentPurpose — 결제 목적

| 값 | 설명 |
|----|------|
| `ORDER_PAY` | 주문 결제 |
| `DEPOSIT_CHARGE` | 예치금 충전 |

---

## Deposit 영역

### DepositType — 예치금 변동 유형

| 값 | 설명 |
|----|------|
| `CHARGE` | 예치금 충전 |
| `USE` | 예치금 사용 (주문 시 차감) |
| `REFUND` | 예치금 환불 (주문 취소 시 반환) |

---

## Refund 영역

### RefundStatus

| 값 | 설명 |
|----|------|
| `PENDING` | 환불 처리중 |
| `COMPLETED` | 환불 완료 |
| `FAILED` | 환불 실패 |

---

## Settlement 영역

### SettlementTargetStatus — 정산 대상 상태

| 값 | 설명 | 전이 조건 |
|----|------|----------|
| `PENDING` | 정산 대기 | 구매확정 시 생성 기본값 |
| `SETTLED` | 정산 완료 | 월 정산 배치 실행 후 settlement에 연결 시 |

### SettlementStatus — 정산 결과 상태

| 값 | 설명 | 전이 조건 |
|----|------|----------|
| `PENDING` | 정산 실행 전 | 배치 시작 시 생성 기본값 |
| `COMPLETED` | 정산 완료 | 판매자별 집계 및 계좌 조회 성공 |
| `FAILED` | 정산 실패 | 계좌 조회 실패 또는 집계 오류 시 |

---

## Boolean / Flag 로 처리할 값들

| 필드 | 값 |
|------|-----|
| `email_verified` | `true` / `false` |
| `is_default` | `true` / `false` |
| `is_deleted` | `true` / `false` |
| `is_active` | `true` / `false` |

---

## 서비스 정책 상수값

### 비밀번호 정책

| 상수 | 권장값 | 설명 |
|------|--------|------|
| `PASSWORD_MIN_LENGTH` | `8` | 최소 길이 |
| `PASSWORD_MAX_LENGTH` | `20` | 최대 길이 |
| `PASSWORD_PATTERN` | `^(?=.*[a-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=]{8,20}$` | 영문 소문자+숫자 포함 권장 |

### 이름 정책

| 상수 | 권장값 | 설명 |
|------|--------|------|
| `NAME_MIN_LENGTH` | `2` | 최소 길이 |
| `NAME_MAX_LENGTH` | `20` | 최대 길이 |

### 연락처 정책

| 상수 | 권장값 | 설명 |
|------|--------|------|
| `PHONE_PATTERN` | `^01[0-9]-?\\d{3,4}-?\\d{4}$` | 휴대폰 번호 형식 |

### 배송지 정책

| 상수 | 값 | 설명 |
|------|-----|------|
| `ADDRESS_LABEL_MAX_LENGTH` | `50` | 배송지 라벨 최대 길이 |
| `RECIPIENT_MAX_LENGTH` | `100` | 수령인 최대 길이 |
| `ZIPCODE_LENGTH` | `10` | 우편번호 최대 길이 |
| `ADDRESS1_MAX_LENGTH` | `255` | 기본 주소 최대 길이 |
| `ADDRESS2_MAX_LENGTH` | `255` | 상세 주소 최대 길이 |
| `MAX_ADDRESS_COUNT` | `10` | 회원당 최대 배송지 개수 |

### 예치금 정책

| 상수 | 권장값 | 설명 |
|------|--------|------|
| `DEPOSIT_MIN_CHARGE_AMOUNT` | `1000` | 최소 충전 금액 |
| `DEPOSIT_MAX_CHARGE_AMOUNT` | `500000` | 1회 최대 충전 |
| `DEPOSIT_MAX_BALANCE` | `1000000` | 최대 보유 한도 |

### 상품 정책

| 상수 | 값 | 설명 |
|------|-----|------|
| `PRODUCT_MIN_PRICE` | `0` | 최소 가격 |
| `PRODUCT_MAX_PRICE` | `1000000` | 최대 가격 |
| `PRODUCT_MIN_STOCK` | `0` | 최소 재고 |
| `PRODUCT_MAX_STOCK` | `9999` | 최대 재고 |
| `PRODUCT_TITLE_MAX_LENGTH` | `300` | 제목 최대 길이 |
| `AUTHOR_MAX_LENGTH` | `200` | 저자 최대 길이 |
| `PUBLISHER_MAX_LENGTH` | `100` | 출판사 최대 길이 |

### 주문 정책

| 상수 | 값 | 설명 |
|------|-----|------|
| `ORDER_NUMBER_MAX_LENGTH` | `30` | 주문번호 최대 길이 |
| `IDEMPOTENCY_KEY_MAX_LENGTH` | `100` | 멱등키 최대 길이 |
| `ORDER_ITEM_MAX_QUANTITY` | `99` | 주문상품 최대 수량 |

### JWT 정책

| 상수 | 권장값 | 설명 |
|------|--------|------|
| `ACCESS_TOKEN_EXPIRE_MINUTES` | `30` | Access Token 유효 시간 |
| `REFRESH_TOKEN_EXPIRE_DAYS` | `14` | Refresh Token 유효 시간 |

### 탈퇴/소프트삭제 정책

| 상수 | 권장값 | 설명 |
|------|--------|------|
| `WITHDRAWAL_RETENTION_DAYS` | `30` | 탈퇴 회원 정보 보관 기간 |
| `ADDRESS_SOFT_DELETE_RETENTION_DAYS` | `30` | 배송지 소프트 삭제 보관 기간 |