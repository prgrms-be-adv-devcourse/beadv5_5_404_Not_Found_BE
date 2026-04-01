# API Specification

> 도서 이커머스 플랫폼 API 명세서
> MSA 5개 서비스: Member / Product / Order / Payment / Settlement (+ Gateway, Eureka)
> 각 API의 상세 Request/Response는 모듈별 명세서 참조 (#1-1 ~ #1-6)

> ★ = 상품선택 → 결제완료 → 정산완료 필수 플로우

---

## 📌 Auth API

> Member Service 내부에서 처리하되, API 경로는 `/auth/*`로 독립 구분합니다.
> 상세: [#1-1_APIspec_Auth.md](#1-1_APIspec_Auth.md)

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 회원가입 | POST | /auth/register | ✅ | ★ | 신규 회원 생성 + 토큰 발급 |
| 로그인 | POST | /auth/login | ✅ | ★ | JWT 발급 |
| 토큰 재발급 | POST | /auth/refresh | ✅ | | Access Token 재발급 |
| 로그아웃 | POST | /auth/logout | ✅ | | 로그아웃 처리 |

---

## 📌 Member API

> 상세: [#1-2_APIspec_Member.md](#1-2_APIspec_Member.md)

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 내 정보 조회 | GET | /member/me | ✅ | | 회원 정보 조회 |
| 내 정보 수정 | PATCH | /member/me | ✅ | | 회원 정보 수정 |
| 예치금 잔액 조회 | GET | /member/me/deposit | ✅ | ★ | 보유 예치금 조회 |
| 회원 탈퇴 | DELETE | /member/me | ✅ | | 계정 삭제 |
| 배송지 목록 조회 | GET | /member/address | ✅ | | 배송지 리스트 |
| 배송지 추가 | POST | /member/address | ✅ | ★ | 배송지 등록 |
| 배송지 수정 | PATCH | /member/address/{addressId} | ✅ | | 배송지 수정 |
| 배송지 삭제 | DELETE | /member/address/{addressId} | ✅ | | 배송지 삭제 |
| 판매자 등록 신청 | POST | /member/seller | ✅ | ★ | 판매자 신청 |
| 판매자 정보 조회 | GET | /member/seller/{memberId} | ✅ | | 판매자 정보 |
| 판매자 승인/거절 | PATCH | /member/admin/seller/{memberId} | ✅ | ★ | 관리자 승인 |

---

## 📌 Product API

> 상세: [#1-3_APIspec_Product.md](#1-3_APIspec_Product.md)

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 상품 목록 조회 | GET | /products | ✅ | ★ | 상품 리스트 조회 (비인증 허용) |
| 상품 상세 조회 | GET | /products/{productId} | ✅ | ★ | 상품 상세 (비인증 허용) |
| 상품 등록 | POST | /products | ✅ | ★ | 상품 등록 (SELLER 전용) |
| 상품 수정 | PATCH | /products/{productId} | ✅ | | 상품 정보 수정 (SELLER 전용) |
| 상품 상태 변경 | PATCH | /products/{productId}/status | ✅ | ★ | ACTIVE 전환 (ADMIN 전용) |
| 카테고리 조회 | GET | /products/categories | ✅ | | 카테고리 목록 |
| 카테고리 추가 | POST | /products/categories | ✅ | | 카테고리 등록 |

---

## 📌 Review API (review-service 미구현)

> 상세: [#1-4_APIspec_Review.md](#1-4_APIspec_Review.md)
> review-service 모듈 자체가 미구현. product-service에 avgRating, reviewCount 필드만 존재 (기본값 0, 읽기 전용).

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 별점 등록 | POST | /review | ❌ | | 주문 상품에 별점(1~5) 등록 |
| 내 별점 수정 | PATCH | /review/{reviewId} | ❌ | | 별점 수정 |
| 내 별점 삭제 | DELETE | /review/{reviewId} | ❌ | | 별점 삭제 |
| 상품별 별점 조회 | GET | /review/product/{productId} | ❌ | | 상품의 별점 목록 조회 |

---

## 📌 Order API

> 상세: [#1-5_APIspec_Order.md](#1-5_APIspec_Order.md)

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 장바구니 조회 | GET | /order/cart | ✅ | ★ | 장바구니 조회 |
| 장바구니 상품 추가 | POST | /order/cart/item | ✅ | ★ | 상품 추가 |
| 장바구니 수량 수정 | PATCH | /order/cart/item/{cartItemId} | ✅ | | 수량 변경 |
| 장바구니 항목 삭제 | DELETE | /order/cart/item/{cartItemId} | ✅ | | 항목 삭제 |
| 장바구니 비우기 | DELETE | /order/cart | ✅ | | 전체 삭제 |
| 결제 페이지 조회 | GET | /order/checkout | ✅ | ★ | 상품+배송지+잔액 조회 |
| 주문 생성 | POST | /order | ✅ | ★ | 주문 생성 (PENDING) |
| 주문 목록 조회 | GET | /order | ✅ | | 주문 리스트 |
| 주문 상세 조회 | GET | /order/{orderId} | ✅ | | 주문 상세 |
| 주문 취소 | POST | /order/{orderId}/cancel | 🟡 | | 주문 취소 (PENDING 🟢 / PAID·CONFIRMED 재고 복원 STUB 🔴) |
| 구매확정 | POST | /order/{orderId}/confirm | ✅ | | 구매확정 (DELIVERED → PURCHASE_CONFIRMED) |
| 반품 신청 | POST | /order/{orderId}/return | ✅ | | 반품 요청 |
| 송장 등록/배송 정보 수정 | PATCH | /order/{orderId}/shipment | ✅ | | 송장/배송 수정 |

---

## 📌 Payment API

> 상세: [#1-6_APIspec_Payment.md](#1-6_APIspec_Payment.md)

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 예치금 결제 실행 | POST | /payment/orders/{orderId}/pay | ✅ | ★ | 예치금으로 주문 결제 |
| 예치금 충전 준비 | POST | /payment/deposit/charge/ready | ✅ | | PG 결제창 URL 발급 |
| 예치금 충전 승인 | POST | /payment/deposit/charge/confirm | ✅ | | PG 승인 + 예치금 충전 |
| 예치금 내역 조회 | GET | /payment/deposit/history | ✅ | | 충전/사용/환불 내역 |
| 환불 요청 | POST | /payment/{paymentId}/refund | ❌ | | 미구현 |
| PG 웹훅 수신 | POST | /payment/webhook/pg | ❌ | | 미구현 |

> 설계 변경: 상품 결제는 예치금 전용. PG는 예치금 충전에만 사용. `POST /payment/ready`, `/payment/confirm` 등 PG 기반 주문 결제 API는 제거됨.

---

## 📌 Settlement API (정산 — 별도 서비스)

> 상세: [#1-6_APIspec_Payment.md](#1-6_APIspec_Payment.md)

| 기능 | Method | Endpoint | 구현 | 필수 | 설명 |
|------|--------|----------|:---:|:---:|------|
| 내 정산 조회 | GET | /api/settlements/me | ✅ | ★ | 판매자 정산 내역 |
| 월 정산 수동 실행 | POST | /internal/settlements/execute | ✅ | ★ | 관리자 수동 트리거 |
| 정산 상세 조회 | GET | /payment/settlement/{settlementId} | ❌ | | 미구현 |
| 출금 신청 | POST | /payment/settlement/payout | ❌ | | 미구현 |
| 정산 요약 | GET | /payment/settlement/summary | ❌ | | 미구현 |
| 수수료 정책 등록 | POST | /payment/commission | ❌ | | 미구현 |
| 수수료 정책 조회 | GET | /payment/commission | ❌ | | 미구현 |
