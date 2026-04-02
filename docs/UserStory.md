# 유저 스토리

> Service_Overview.md 기준 · 5개 서비스 (Member / Product / Order / Payment / Settlement)
> ★ = 필수 플로우 · 🟢 구현 완료 · 🟡 부분 구현 · 🔴 미구현

---

## 1️⃣ Member (회원) 도메인

### 인증

```
US-M01: 회원가입 ★ 🟢
As a 신규 사용자
I want 이메일, 비밀번호, 이름, 연락처로 회원가입하고 싶다
So that 서비스에서 내 계정을 생성하고 주문과 배송 정보를 관리할 수 있다
API: POST /auth/register
구현: emailVerified=true 임시 설정 (이메일 인증 기능 미구현)

US-M02: 로그인 ★ 🟢
As a 기존 회원
I want 이메일과 비밀번호로 로그인하고 싶다
So that 내 계정에 접속해 쇼핑 서비스를 이용할 수 있다
API: POST /auth/login
구현: JWT Access Token(30분) / Refresh Token(14일) 발급

US-M03: 토큰 재발급 🟢
As a 로그인된 회원
I want Access Token이 만료되었을 때 재발급 받고 싶다
So that 다시 로그인하지 않고 서비스를 계속 이용할 수 있다
API: POST /auth/refresh

US-M04: 로그아웃 🟢
As a 로그인된 회원
I want 로그아웃하고 싶다
So that 내 계정을 안전하게 종료할 수 있다
API: POST /auth/logout
구현: Access Token 블랙리스트 등록 + Refresh Token 폐기
```

### 회원 정보 관리

```
US-M05: 내 정보 조회 🟢
As a 회원
I want 내 이름, 연락처 등 회원 정보를 조회하고 싶다
So that 현재 등록된 내 계정 정보를 확인할 수 있다
API: GET /member/me

US-M06: 회원 정보 수정 🟢
As a 회원
I want 내 이름, 연락처, 비밀번호를 수정하고 싶다
So that 내 계정 정보를 최신 상태로 유지할 수 있다
API: PATCH /member/me

US-M07: 회원 탈퇴 🟢
As a 회원
I want 서비스를 탈퇴하고 싶다
So that 더 이상 내 계정을 사용하지 않을 때 이용을 종료할 수 있다
API: DELETE /member/me
구현: status → WITHDRAWN, 비밀번호 확인 필수
```

### 배송지 관리

```
US-M08: 배송지 추가 ★ 🟢
As a 회원
I want 여러 배송지를 저장하고 싶다
So that 주문 시 배송 정보를 매번 새로 입력하지 않아도 된다
API: POST /member/address

US-M09: 배송지 수정/삭제 🟢
As a 회원
I want 저장된 배송지를 수정하거나 삭제하고 싶다
So that 배송지 정보를 최신 상태로 유지할 수 있다
API: PATCH /member/address/{id}, DELETE /member/address/{id}
구현: 삭제는 소프트 삭제 (is_deleted)
```

### 예치금

```
US-M10: 예치금 잔액 조회 ★ 🟢
As a 회원
I want 내 예치금 잔액을 조회하고 싶다
So that 주문 또는 결제 시 사용할 수 있는 금액을 확인할 수 있다
API: GET /member/me/deposit
구현: 잔액은 member-service 보관, payment-service AFTER_COMMIT 이벤트로 동기화
```

### 판매자 등록

```
US-M11: 판매자 등록 신청 ★ 🟢
As a 회원
I want 사업자 정보와 정산 계좌 정보를 등록해 판매자로 신청하고 싶다
So that 상품을 판매할 수 있는 권한을 요청할 수 있다
API: POST /member/seller
구현: 사업자등록번호 중복 검증, 계좌 정보 AES 암호화 저장

US-M12: 판매자 승인 ★ 🟢
As a 관리자
I want 판매자 신청을 승인/거절하고 싶다
So that 적격한 판매자만 상품을 판매할 수 있다
API: PATCH /member/admin/seller/{memberId}
구현: 승인 시 SellerApprovedEvent → role SELLER 자동 전환

US-M13: 판매자 정보 조회 🟢
As a 판매자
I want 내 판매자 등록 상태를 확인하고 싶다
So that 승인 대기, 승인 완료, 정지 여부를 확인할 수 있다
API: GET /member/seller/{memberId}
```

---

## 2️⃣ Product (상품) 도메인

### 상품 관리 (판매자)

```
US-P01: 상품 등록 ★ 🟢
As a 승인된 판매자
I want ISBN, 제목, 저자, 출판사, 가격, 재고, 도서 유형, 카테고리를 입력해 상품을 등록하고 싶다
So that 내 상품을 플랫폼에서 판매할 수 있다
API: POST /products
구현: 등록 시 PENDING_REVIEW 상태, member-service에서 판매자 승인 확인

US-P02: 상품 ACTIVE 전환 ★ 🟢
As a 관리자
I want 등록된 상품을 검수 후 판매 가능 상태로 전환하고 싶다
So that 적격한 상품만 구매자에게 노출된다
API: PATCH /products/{id}/status

US-P03: 상품 정보 수정 🟢
As a 판매자
I want 내가 등록한 상품의 가격, 재고, 카테고리 등을 수정하고 싶다
So that 상품 정보를 최신 상태로 유지할 수 있다
API: PATCH /products/{id}
```

### 상품 조회 (구매자)

```
US-P04: 상품 목록 조회 ★ 🟢
As a 사용자 (비회원 포함)
I want 상품 목록을 조회하고 싶다
So that 원하는 책을 찾을 수 있다
API: GET /products
구현: 비인증 허용, 페이지네이션/필터링 지원

US-P05: 상품 상세 조회 ★ 🟢
As a 사용자 (비회원 포함)
I want 도서의 기본 정보, 가격, 재고 상태, 평균 평점, 리뷰 수를 확인하고 싶다
So that 구매 여부를 판단할 수 있다
API: GET /products/{id}
구현: 비인증 허용, avgRating/reviewCount는 기본값 0 (review-service 미구현)
```

---

## 3️⃣ Order (주문) 도메인

### 장바구니 (order-service 소속)

```
US-O01: 장바구니 상품 담기 ★ 🟢
As a 구매자
I want 재고 상태와 관계없이 모든 상품을 장바구니에 담고 싶다
So that 품절 상품도 함께 담아서 나중에 구매 여부를 결정할 수 있다
API: POST /order/cart/item
구현: 회원 전용, 재고 검증 없이 담기 가능

US-O02: 장바구니 조회 ★ 🟢
As a 구매자
I want 장바구니를 조회하고 싶다
So that 담은 상품 목록을 확인할 수 있다
API: GET /order/cart
구현: cartItemId, productId, quantity만 반환 (상품 상세 정보는 미포함)

US-O03: 장바구니 수량 변경/삭제 🟢
As a 구매자
I want 장바구니 상품의 수량을 변경하거나 삭제하고 싶다
So that 주문 전에 상품 목록을 정리할 수 있다
API: PATCH /order/cart/item/{id}, DELETE /order/cart/item/{id}, DELETE /order/cart
```

### 주문

```
US-O04: 결제 페이지 조회 ★ 🟢
As a 구매자
I want 주문 전에 상품, 배송지, 예치금 잔액을 한 화면에서 확인하고 싶다
So that 결제 정보를 확인한 뒤 주문을 진행할 수 있다
API: GET /order/checkout?cartItemIds= 또는 GET /order/checkout?productId=&quantity=
구현: order-service → member-service (배송지, 예치금 조회)

US-O05: 주문 생성 ★ 🟢
As a 구매자
I want 장바구니 상품 또는 바로구매를 통해 주문을 생성하고 싶다
So that 결제 단계로 진행할 수 있다
API: POST /order
구현: PENDING 상태 생성, 서버에서 총 금액 계산, idempotencyKey 중복 방지, 30분 미결제 시 자동 취소

US-O06: 주문 조회 🟢
As a 구매자
I want 내 주문 목록과 주문 상태를 조회하고 싶다
So that 주문이 정상적으로 처리되고 있는지 확인할 수 있다
API: GET /order, GET /order/{id}

US-O07: 주문 취소 🟡
As a 구매자
I want 결제 전 또는 배송 전에 주문을 취소하고 싶다
So that 불필요한 주문을 취소하고 예치금을 돌려받을 수 있다
API: POST /order/{id}/cancel
구현:
- PENDING 취소: 상태 변경만 🟢
- PAID/CONFIRMED 취소: 예치금 환급 🟢 + 재고 복원 🔴 (STUB — product-service 엔드포인트 미구현)

US-O08: 구매확정 🟢
As a 구매자
I want 배송 완료된 주문을 구매확정하고 싶다
So that 판매자 정산이 진행될 수 있다
API: POST /order/{id}/confirm
구현:
- DELIVERED 상태에서만 가능
- 구매확정 후 환불 불가
- 배송 완료 후 7일 경과 시 AutoConfirmScheduler가 자동 전환
- PurchaseConfirmedEvent Kafka 발행 → settlement-service 정산 대상 생성
- 🟡 현재 PAID 시 DELIVERED → PURCHASE_CONFIRMED 자동 전이 운영 중 (배송 모듈 미분리)

US-O09: 송장 등록 / 배송 상태 수정 🟢
As a 판매자/관리자
I want 주문의 송장번호와 배송 상태를 관리하고 싶다
So that 구매자가 배송 현황을 확인할 수 있다
API: PATCH /order/{id}/shipment
구현: 🟡 배송 모듈 미분리 — 현재 order-service 내 shipment 엔드포인트로 처리 중

US-O10: 반품 신청 🟢
As a 구매자
I want 배송된 상품에 대해 반품을 신청하고 싶다
So that 불량/오배송 등의 문제를 해결할 수 있다
API: POST /order/{id}/return
```

---

## 4️⃣ Payment (결제) 도메인

### 결제

```
US-PA01: 주문 결제 ★ 🟢
As a 구매자
I want 주문 금액을 예치금으로 결제하고 싶다
So that 보유한 예치금으로 상품을 구매할 수 있다
API: POST /payment/orders/{orderId}/pay
구현:
- payment-service가 예치금 차감(member) → 재고 차감(product) → 주문 PAID(order) 순서 처리
- 예치금 부족 시 재고 차감 없이 즉시 실패
- 재고 부족 시 예치금 환급(보상 트랜잭션) 후 실패
- 결제 성공 시 order-service 내부에서 PAID→PURCHASE_CONFIRMED 자동 전이
```

### 예치금 충전 (PG 연동)

```
US-PA02: 예치금 충전 🟢
As a 회원
I want 토스 결제를 통해 예치금을 충전하고 싶다
So that 이후 주문 결제 시 예치금을 사용할 수 있다
API: POST /payment/deposit/charge/ready → POST /payment/deposit/charge/confirm
구현:
- ready: Toss 결제창 URL 발급, PAYMENT(PENDING) 레코드 생성
- confirm: Toss 승인 → DB 커밋 → DepositChargedEvent(AFTER_COMMIT) → member-service 잔액 동기화
- 충전 한도: 최소 1,000원, 최대 500,000원, 보유 한도 1,000,000원

US-PA03: 예치금 내역 조회 🟢
As a 회원
I want 예치금의 충전, 사용, 환불 내역을 확인하고 싶다
So that 예치금 흐름을 파악할 수 있다
API: GET /payment/deposit/history
구현: 타입 필터(CHARGE/USE/REFUND), 페이징 지원
```

### 환불

```
US-PA04: 환불 처리 🔴
As a 구매자
I want 취소된 주문에 대해 PG 환불을 받고 싶다
So that 내가 결제한 금액을 돌려받을 수 있다
API: POST /payment/{paymentId}/refund (미구현)
현재: 예치금 환급은 order-service → member-service POST /internal/member/{id}/deposit/charge로 처리 중
```

---

## 5️⃣ Settlement (정산) 도메인

> 정산 트리거: 구매확정(PURCHASE_CONFIRMED) 시 PurchaseConfirmedEvent(Kafka) → Settlement 서비스가 settlement_target 생성 → 매월 25일 스케줄러 배치로 정산 실행 (수수료 3%)

```
US-ST01: 정산 대상 생성 🟡
As a 시스템
I want 구매확정 시 자동으로 정산 대상을 생성하고 싶다
So that 월별 정산 배치 실행 시 집계 대상을 확보할 수 있다
구현: Kafka Consumer(order.purchase-confirmed) → settlement_target 생성
상태: 발행-수신 연결 완료, E2E 검증 필요
멱등성: ProcessedEventJpaEntity eventId 기반 중복 방지 (TODO: INSERT ON CONFLICT 전환 필요)

US-ST02: 정산 내역 조회 ★ 🟢
As a 판매자
I want 내 판매 건의 정산 내역을 조회하고 싶다
So that 수익 현황을 확인할 수 있다
API: GET /api/settlements/me
구현: SELLER 권한 필수, 판매자별 정산 이력 반환

US-ST03: 월 정산 실행 ★ 🟢
As a 시스템/관리자
I want 매월 25일에 전월 구매확정 건을 집계하여 정산을 실행하고 싶다
So that 판매자에게 수수료를 제외한 정산금을 지급할 수 있다
API: POST /internal/settlements/execute (수동 실행)
구현:
- SettlementScheduler: 매월 25일 자동 실행 (ShedLock 적용)
- 집계: 전월 1일~말일 구매확정 건
- 수수료: 3% (settlement.fee-rate=0.03)
- member-service에서 판매자 계좌 정보 조회 (REST)
- 성공: SettlementCompletedEvent / 실패: SettlementFailedEvent

US-ST04: 출금 신청 🔴
As a 판매자
I want 정산 가능한 금액에 대해 출금을 신청하고 싶다
So that 정산금을 내 계좌로 받을 수 있다
API: POST /payment/settlement/payout (미구현)
```

---

## 📊 유저 스토리 요약

| 도메인 | 총 개수 | 🟢 구현 | 🟡 부분 | 🔴 미구현 |
|--------|---------|---------|---------|----------|
| Member | 13 | 13 | 0 | 0 |
| Product | 5 | 5 | 0 | 0 |
| Order | 10 | 9 | 1 | 0 |
| Payment | 4 | 3 | 0 | 1 |
| Settlement | 4 | 2 | 1 | 1 |
| **합계** | **36** | **32** | **2** | **2** |

> review-service는 모듈 미존재로 유저 스토리에서 제외. 추후 구현 시 별도 추가.
