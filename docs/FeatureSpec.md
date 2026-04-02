# 기능 명세서

## 1. 회원 모듈

### 1-1. 회원가입
- 이메일, 비밀번호, 이름, 전화번호로 회원가입
- 이메일 중복 검증
- BCrypt를 이용한 비밀번호 해싱
- 가입 시 기본값
  - role: USER
  - status: ACTIVE
  - email_verified: true (🟡 이메일 인증 미구현 — 현재 가입 시 true 자동 설정으로 임시 운영 중. 추후 이메일 인증 기능 구현 시 false로 변경 필요)
  - deposit_balance: 0
- 가입 완료 후 MemberRegisteredEvent 발행 (Spring Event)

### 1-2. 로그인 / 인증
- 이메일 + 비밀번호 기반 로그인
- JWT Access Token / Refresh Token 발급 및 갱신
- 로그인 시 회원 상태 검증
  - ACTIVE 상태만 로그인 가능
  - WITHDRAWN, SUSPENDED 상태는 로그인 불가
- 이메일 인증 정책 적용

### 1-3. 회원 정보 관리
- 내 프로필 조회
- 이름, 전화번호, 비밀번호 변경
  - 비밀번호 변경 시 현재 비밀번호 확인 필수
- 회원 탈퇴
  - status: WITHDRAWN
- 계정 정지
  - status: SUSPENDED
  - 관리자 전용 기능

### 1-4. 배송지 관리
- 배송지 CRUD
  - label, recipient, phone, zipcode, address1, address2
- 기본 배송지 설정
- 배송지 삭제 시 소프트 삭제 (is_deleted)
- 주문 생성 시 주문 모듈에 배송지 정보 제공
- 구매 시점의 배송지 스냅샷을 주문에 저장

### 1-5. 예치금 관리
- 예치금 잔액 조회 API 제공
- 주문/결제 화면에 예치금 잔액 노출
- 실제 충전/차감/환불은 결제 모듈에서 처리
- 회원 모듈은 소유 정보 및 조회 인터페이스만 제공

### 1-6. 판매자 등록
- 회원이 판매자로 신청
  - 사업자등록번호
  - 상호명
  - 은행 계좌 정보
  - 예금주명
- 사업자등록번호 중복 검증
- 판매자 상태 관리
  - PENDING
  - APPROVED
  - SUSPENDED
- 승인 후 상품 판매 권한 부여
- 은행 계좌 정보 암호화 저장
- 회원과 판매자 역할 공존 가능
  - 판매자 정보는 별도 판매자 프로필로 관리
  - 실제 판매 권한은 `SELLER.status = APPROVED`로 판단

### 1-7. 결제 수단 관리
- 토스페이먼츠가 기본 결제 수단
- 결제 시 토스에서 제공하는 결제 수단 선택
  - 신용/체크카드
  - 계좌이체
  - 간편결제 (카카오페이, 네이버페이 등)
- 결제 완료 후 결제 결과를 주문에 연결하여 저장
  - 결제 수단 유형
  - 결제 승인 정보
  - 거래 식별자

> ❗ 범위 외: 회원별 결제 수단 등록, 기본 결제 수단 설정, 삭제 기능.
> 사전 등록된 카드/간편결제 정보는 저장하지 않음.

### 1-8. 다른 모듈에 제공하는 인터페이스

| 소비 모듈 | 인터페이스 |
|---|---|
| 주문 | 회원 존재 및 상태 확인, 배송지 조회, 배송지 스냅샷 데이터 |
| 결제 | 회원 상태 확인, 예치금 잔액 확인, 판매자 계좌 정보 조회 (정산용) |
| 상품 | 판매자 권한 및 상태 확인 |
| 리뷰 | 회원 존재 확인, 구매 이력 확인 (주문 모듈 경유) |

---

## 2. 상품 모듈

### 2-1. 상품 등록
- 판매자가 도서 정보 등록
  - ISBN, 제목, 저자, 출판사, 가격, 재고 수량, 도서 유형, 카테고리
- 등록 전 회원 모듈을 통해 판매자 자격 및 상태 확인
- 등록 시 상태: `PENDING_REVIEW`
- 심사 완료 전까지 일반 사용자에게 노출되지 않음

> 💡 미결 사항:
> - ISBN 중복 정책 확인 필요 (동일 ISBN을 여러 판매자가 등록 가능한지?)
> - 공유 상품 마스터 사용 여부 미정

### 2-2. 상품 심사 및 관리
- 관리자 심사 후 판매 가능 상태로 전환
- 상품 상태
  - ACTIVE: 승인 완료, 판매 가능
  - INACTIVE: 판매 중단 또는 정책 위반
  - SOLD_OUT: 재고 소진
- 계층형 카테고리로 상품 분류

### 2-3. 재고 관리
- 상품별 재고 수량 및 버전 관리
- 주문 생성 시 재고 및 가격 검증 (주문 모듈 경유)
- 결제 완료 후 재고 차감 확정
- 주문 취소 / 환불 완료 시 재고 복원
- 재고 0 도달 시 상품 상태 `SOLD_OUT` 전환
- 동시성 제어를 위한 낙관적 잠금 (version)

---

## 3. 리뷰 모듈

> 🔴 **review-service 전체 미구현** — product-service에 `avgRating`, `reviewCount` 필드만 존재 (기본값 0, 읽기 전용)
> 기존 상품 모듈에서 분리된 독립 모듈입니다.

### 3-1. 리뷰(별점) 등록 🔴
- 구매 확정된 회원만 리뷰 작성 가능
  - 주문 모듈을 통해 구매 이력 확인
- 별점(1~5)만 지원
- 동일 상품에 대해 주문 건당 1개의 리뷰만 작성 가능

### 3-2. 리뷰 조회 🔴
- 상품별 별점 목록 조회
- 상품별 평균 평점 및 리뷰 수 집계
- 리뷰 조회는 로그인한 회원만 가능 (비회원 접근 불가)

### 3-3. 리뷰 수정/삭제 🔴
- 본인 작성 리뷰만 수정/삭제 가능
- 삭제 시 평균 평점 재계산

### 3-4. 다른 모듈에 제공하는 인터페이스 🔴

| 소비 모듈 | 인터페이스 |
|---|---|
| 상품 | 상품별 평균 평점 및 리뷰 수 조회 |

---

## 4. 주문 모듈

### 4-1. 장바구니

> 🟡 카트 모듈 미분리 — 현재 order-service 내 포함, 추후 독립 모듈로 분리 필요

- 회원당 장바구니 1개
- 상품 추가, 수량 변경, 상품 삭제
- 재고 제한 없이 담기 가능 - 품절 상품도 장바구니에 담을 수 있음
- 장바구니 데이터는 서버 측에 저장, 회원별 관리
- 장바구니 조회 시 최신 재고 및 가격을 응답하며, 품절 상품 여부를 표시

### 4-2. 주문 생성
- 두 가지 진입 경로 지원
  - 경로 1: 장바구니 → 결제 페이지
  - 경로 2: 바로구매 → 결제 페이지
- 결제 페이지 진입 시
  - 회원 모듈에서 배송지 조회 (기본 배송지 표시)
  - 배송지 스냅샷을 저장할 준비
  - 회원의 현재 예치금 잔액 노출
- 결제하기 버튼 = 단일 트랜잭션 처리
  - 서버 금액 재계산 (클라이언트 금액은 신뢰하지 않음)
  - 상품별 재고 재검증 (결제 시점의 재고 상태 확인)
  - 재고 부족 시 결제 실패 응답
  - 예치금에서 주문 금액 차감
  - 재고 차감 (payment-service → product-service REST 동기 호출)
  - 주문 생성 (상태: PAID)
- 중복 주문 방지를 위한 `idempotency_key` 적용
- 주문 생성 시점에 배송지 스냅샷 저장
- 예치금 사용 금액 반영: `deposit_used`

### 4-3. 주문 상태 관리
- 주문 상태 흐름: `PENDING → PAID → CONFIRMED → SHIPPING → DELIVERED → PURCHASE_CONFIRMED`
- PENDING: 결제 대기 (주문 생성 직후, 결제 실행 전. 30분 경과 시 PendingOrderCleanupScheduler가 자동 취소)
- PAID: 결제 완료 (payment-service 결제 완료 후). 🟡 **현재 PAID → DELIVERED → PURCHASE_CONFIRMED 자동 전이 설정** — 배송 모듈 미분리 상태이므로 결제 완료 시 배송 완료·구매확정까지 자동 처리됨
- CONFIRMED: 주문 확정 (🟡 **미사용** — 판매자 주문 승인 플로우 미구현, 추후 판매자 기능 확장 시 재검토)
- SHIPPING: 배송 중
- DELIVERED: 배송 완료
- PURCHASE_CONFIRMED: 구매 확정 (수동 확정 또는 배송 완료 후 7일 경과 시 자동 전환)
- CANCELLED: 주문 취소 (PENDING, PAID, CONFIRMED 상태에서만 가능)
- 주문 생성(`POST /order`)과 결제 실행(`POST /payment/orders/{orderId}/pay`)은 분리됨
- 결제 실행 시 payment-service가 재고 차감(REST) + 예치금 차감 + 주문 상태 PAID 전환을 처리

**재고 차감 실패 시나리오 (동시 주문)**

주문 생성 시점에 재고를 검증하고, 결제 실행 시 payment-service가 REST 동기 호출로 재고를 차감한다.
동시 주문으로 재고가 소진되면 차감 실패가 발생할 수 있다.

| 단계 | 처리 |
|------|------|
| 차감 실패 발생 | product-service REST 호출 시 즉시 예외 반환 |
| 결제 실패 | 예치금 차감 전이므로 롤백 불필요, 결제 실패 응답 |

### 4-4. 주문 취소
- 취소 가능 상태: PENDING, PAID, CONFIRMED
- PENDING 취소: 예치금 환급 없음, 재고 복원 없음 (아직 차감 전이므로 단순 상태 변경) 🟢
- PAID/CONFIRMED 취소: 예치금 환급 🟢 + 재고 복원 🔴 (STUB — `POST /internal/products/stock/restore` 엔드포인트 미노출, log.warn만 출력)
- 주문 상태: `CANCELLED`

### 4-5. 배송 관리

> 🟡 **배송 모듈 미분리** — 현재 order-service 내 shipment 엔드포인트로 처리 중, 추후 독립 모듈 분리 필요. 현재는 결제 완료 시 PAID → DELIVERED → PURCHASE_CONFIRMED 자동 전이로 운영 중.

- 택배사 및 송장번호 등록
- 배송 상태: `PREPARING → SHIPPED → IN_TRANSIT → DELIVERED → RETURNED`
- 배송 상태 변경에 따른 주문 상태 연동

### 4-6. 구매확정

> 🟡 **현재 자동 전이 운영 중** — 배송 모듈 미분리 상태이므로 결제 완료(PAID) 시 내부적으로 DELIVERED → PURCHASE_CONFIRMED까지 자동 전이. 수동 구매확정 API(`POST /order/{id}/confirm`)와 자동 확정 스케줄러(`AutoConfirmScheduler`)는 구현 완료.

- `DELIVERED` 상태에서만 전환 가능
- 구매확정 이후 환불 불가 — 환불 가능 기간은 `DELIVERED` 상태 구간으로 제한
- 구매확정 전환 조건

| 방식 | 조건 |
|------|------|
| 자동 확정 | 배송 완료(`DELIVERED`) 후 7일 경과 시 스케줄러가 자동 전환 |
| 수동 확정 | 구매자가 직접 구매확정 API 호출 |

- `PURCHASE_CONFIRMED` 전환 시 `confirmed_at` 시각 기록
- `PurchaseConfirmedEvent` 발행 시 `confirmedAt` 포함 → Payment 서비스가 정산 대상 생성

---

## 5. 결제 모듈

### 5-1. 결제 처리
- Payment Service가 결제 실행을 담당 (재고 차감 → 예치금 차감 → 주문 상태 PAID 변경)
- Payment Service는 예치금 충전(PG 연동), 예치금 결제 실행, 예치금 차감/환급을 담당
- 정산은 Settlement Service가 별도 담당
- PG(토스페이먼츠)는 **예치금 충전 목적**으로만 사용
- PG 클라이언트 인터페이스로 향후 PG사 확장 가능하도록 설계
- 중복 결제 방지를 위해 `idempotency_key`로 결제 요청 식별
- 승인 후 결제 결과 저장
  - 결제 금액
  - PG 거래 식별자
  - 결제 수단 유형
  - 결제 상태
  - 결제 승인 시각
- 주문 결제는 Payment Service의 `POST /payment/orders/{orderId}/pay`에서 처리

### 5-2. 환불 처리
- 주문 취소 시 예치금 복원으로 처리
- PG를 통한 환불은 예치금 충전 건에 대해서만 필요
- 환결과 저장
  - 환불 금액
  - 환불 사유
  - PG 환불 식별자 (해당하는 경우)
  - 환불 상태
  - 환불 완료 시각
- 환불 상태: `PENDING / COMPLETED / FAILED`

### 5-3. 정산 처리 (Settlement Service — 별도 서비스로 분리)
- **정산 트리거**: 구매확정(`PURCHASE_CONFIRMED`) 시 Order Service가 `PurchaseConfirmedEvent` Kafka 발행 → Settlement Service가 consume → `settlement_target` 레코드 생성
- 스케줄러 기반 배치 작업으로 정산 실행 (매월 25일, ShedLock 적용)
- 집계 기간: 전월 1일 ~ 전월 말일 구매확정 건
- 매출 금액에서 서비스 수수료(3%)를 차감하여 정산 금액 산출
- 회원 모듈에서 판매자 계좌 정보 조회 (REST — Settlement → Member)
- 판매자별 정산 결과 저장 (`settlement` 레코드)
  - 총 매출 금액
  - 수수료 금액
  - 순 정산 금액
  - 정산 일자
  - 집계 기간 (period_start, period_end)
  - 정산 상태
- 정산 상태: `PENDING → COMPLETED / FAILED`
- 정책값은 `application.yml`로 관리하여 코드 수정 없이 변경 가능

| 정책 항목 | 값 |
|------|-----|
| 자동 구매확정 | 배송 완료 후 7일 |
| 정산 실행일 | 매월 25일 |
| 수수료율 | 3% |

---

## 서비스 간 통신

> 서비스 간 REST 통신 구조 및 이벤트 설계 상세는 아래 문서 참조
> - 동기(REST) / 아키텍처 구성: [Architecture.md](Architecture.md)
> - 비동기(Kafka / Spring Event): [EventDesign.md](EventDesign.md)
