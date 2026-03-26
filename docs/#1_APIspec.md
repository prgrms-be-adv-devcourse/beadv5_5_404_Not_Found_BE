# API Specification

> 도서 이커머스 플랫폼 API 명세서
> MSA 5개 서비스: Member / Product / Review / Order / Payment
> 상세 Request / Response는 추후 추가 예정

---

## 📌 Auth API

> Member Service 내부에서 처리하되, API 경로는 `/auth/*`로 독립 구분합니다.

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 회원가입 | POST | /auth/register | 신규 회원 생성 + 토큰 발급 |
| 로그인 | POST | /auth/login | JWT 발급 |
| 토큰 재발급 | POST | /auth/refresh | Access Token 재발급 |
| 로그아웃 | POST | /auth/logout | 로그아웃 처리 |

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

---

## 📌 Product API

| 기능 | Method | Endpoint | 설명 | 개발 여부 |
|------|--------|----------|------|-----------|
| 상품 목록 조회 | GET | /products | 상품 리스트 조회, `?ids=` 파라미터로 배치 조회 가능 (비회원 접근 가능) | ✅ |
| 상품 상세 조회 | GET | /products/{productId} | 상품 상세 (비회원 접근 가능) | ✅ |
| 상품 등록 | POST | /products | 상품 등록 | ✅ |
| 상품 수정 | PATCH | /products/{productId} | 상품 정보 + 재고 수량 수정 (판매자) | ✅ |
| 상품 상태 변경 | PATCH | /products/{productId}/status | 판매 상태 변경 (판매자 + 관리자) | ✅ |
| 재고 조회 | GET | /products/{productId}/stock | 재고 확인 | ✅ |
| 재고 차감 | POST | /products/{productId}/stock/deduct | 재고 차감 (내부 서비스 호출) | ✅ |
| 재고 복구 | POST | /products/{productId}/stock/restore | 재고 복구 (내부 서비스 호출) | ✅ |
| 카테고리 조회 | GET | /products/categories | 카테고리 조회 | ✅ |
| 카테고리 추가 | POST | /products/categories | 카테고리 등록 | ✅ |

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
| 배송 정보 수정 | PATCH | /order/{orderId}/shipment | 송장/배송 수정 |

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

---
