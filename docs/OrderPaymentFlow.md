# 주문→결제 플로우 설계서

> 예치금 기반 결제 · 주문 생성과 결제 실행 분리 · 재고 차감은 REST 동기 호출 사용
> 5개 서비스: member-service / product-service / order-service / payment-service / settlement-service

---

## 핵심 원칙

- PG는 예치금 충전에만 사용한다 (Toss)
- 상품 결제는 무조건 예치금으로만 가능하다 (예치금 부족 시 결제 불가)
- 주문 생성(`POST /order`)과 결제 실행(`POST /payment/orders/{orderId}/pay`)은 분리된다
- 재고 차감은 결제 실행 시점에 payment-service가 product-service에 REST 동기 호출로 처리한다
- 재고 차감 실패 시 예치금 환급(보상 트랜잭션) 후 예외 반환
- 장바구니는 order-service 소속, 재고 제한 없이 담기 가능, 조회 시 품절/가격변동 표시
- 총 금액은 서버에서 계산한다 (클라이언트 금액 신뢰하지 않음)
- PENDING 주문은 30분 경과 시 자동 취소 (PendingOrderCleanupScheduler)

---

## 진입 경로 2가지

| 경로 | 흐름 |
|------|------|
| A. 장바구니 경유 | 장바구니 담기 → 장바구니 조회 → 주문하기 버튼 → 결제 페이지 → 결제하기 |
| B. 바로 구매 | 상품 상세에서 구매하기 버튼 → 결제 페이지 → 결제하기 |

---

## 시퀀스 다이어그램

```mermaid
sequenceDiagram
    actor Client
    participant Order as order-service
    participant Payment as payment-service
    participant Product as product-service
    participant Member as member-service
    participant Settlement as settlement-service

    rect rgb(235, 245, 255)
        Note over Client,Order: 1단계 — 장바구니 담기 (order-service)
        Client->>Order: POST /order/cart/item
        Order->>Order: CartItem 저장
        Order-->>Client: 장바구니 추가 완료
    end

    rect rgb(243, 243, 243)
        Note over Client,Product: 2단계 — 장바구니 조회 (order-service → product-service)
        Client->>Order: GET /order/cart
        Order->>Product: GET /internal/products?ids= (가격/재고 조회)
        Product-->>Order: price, stock, status
        Note right of Order: 품절 여부 표시<br/>재고 부족 표시<br/>가격 변동 표시
        Order-->>Client: 장바구니 목록 반환
    end

    rect rgb(255, 248, 235)
        Note over Client,Member: 3단계 — 결제 페이지 진입 (order-service → member-service)
        alt 장바구니 경로
            Client->>Order: GET /order/checkout?cartItemIds=
        else 바로 결제 경로
            Client->>Order: GET /order/checkout?productId=&quantity=
        end
        Order->>Member: GET /internal/member/{id}/address (배송지 조회)
        Member-->>Order: 배송지 정보
        Order->>Member: GET /internal/member/{id}/deposit (예치금 조회)
        Member-->>Order: 잔액 반환
        Order-->>Client: 결제 페이지 렌더링 (상품, 배송지, 잔액)
    end

    rect rgb(255, 240, 240)
        Note over Client,Order: 4단계 — 주문 생성 PENDING (order-service)
        Client->>Order: POST /order (items, addressId, idempotencyKey)
        Note over Order: 서버에서 총 금액 계산 (product-service 조회)
        Order->>Order: 주문 생성 (status: PENDING) + OrderItem 저장
        Order-->>Client: { orderId }
    end

    rect rgb(235, 255, 240)
        Note over Client,Settlement: 5단계 — 결제 실행 (payment-service 주도)
        Client->>Payment: POST /payment/orders/{orderId}/pay
        Payment->>Order: GET /internal/order/{orderId} (주문 조회)
        Order-->>Payment: orderId, status, totalAmount, items
        Payment->>Member: POST /internal/member/{id}/deposit/deduct (예치금 차감)
        alt 예치금 부족
            Member-->>Payment: 잔액 부족
            Payment-->>Client: 결제 실패 (예치금 부족)
        else 예치금 충분
            Member-->>Payment: 차감 완료
            Payment->>Product: POST /internal/products/stock/deduct (재고 차감)
            alt 재고 부족
                Product-->>Payment: 재고 부족
                Payment->>Member: POST /internal/member/{id}/deposit/charge (예치금 환급 — 보상 트랜잭션)
                Payment-->>Client: 결제 실패 (재고 부족)
            else 재고 충분
                Product-->>Payment: 차감 완료
                Payment->>Order: POST /internal/order/{orderId}/status (PAID)
                Note over Order: PENDING→PAID→SHIPPING→DELIVERED→PURCHASE_CONFIRMED 자동 전이
                Order->>Order: PurchaseConfirmedEvent 발행 (Spring Event → AFTER_COMMIT → Kafka)
                Order-->>Payment: 상태 변경 완료
                Payment-->>Client: 결제 완료 ({ orderId, depositUsed, balanceAfter })
                Note over Settlement: Kafka Consumer: order.purchase-confirmed 수신
                Settlement->>Settlement: settlement_target 생성
            end
        end
    end
```

---

## 플로우차트

```mermaid
flowchart TD
    Start([사용자]) --> ProductPage[상품 상세 페이지<br/>GET /products/{id}]

    ProductPage --> BuyNow[구매하기 버튼]
    ProductPage --> AddCart[장바구니 담기 버튼<br/>POST /order/cart/item]

    AddCart --> SaveCart[CartItem 저장<br/>재고 제한 없음, 품절도 담기 가능]
    SaveCart --> CartPage[장바구니 페이지<br/>GET /order/cart]
    CartPage --> CartQuery[최신 가격/재고 조회<br/>품절·재고부족·가격변동 표시]
    CartQuery --> OrderBtn[주문하기 버튼]

    BuyNow --> CheckoutPage
    OrderBtn --> CheckoutPage

    CheckoutPage[결제 페이지<br/>GET /order/checkout<br/>상품 목록 · 총 금액 · 배송지 · 예치금 잔액 노출]

    CheckoutPage --> CreateOrderBtn[주문하기 버튼 클릭<br/>POST /order]

    CreateOrderBtn --> CalcTotal[서버에서 총 금액 계산]
    CalcTotal --> CreateOrder[주문 생성 - PENDING<br/>응답: orderId]
    CreateOrder --> PayBtn[결제하기 버튼 클릭<br/>POST /payment/orders/{orderId}/pay]

    PayBtn --> DepositCheck{예치금 차감<br/>POST /internal/member/{id}/deposit/deduct}

    DepositCheck -->|잔액 부족| DepositFail[결제 실패<br/>예치금 부족 안내]
    DepositFail --> CheckoutPage

    DepositCheck -->|잔액 충분| StockCheck{재고 차감 요청<br/>POST /internal/products/stock/deduct}

    StockCheck -->|재고 부족| StockFail[결제 실패<br/>재고 부족 — 예치금 환급 보상 트랜잭션]
    StockFail --> CheckoutPage

    StockCheck -->|차감 성공| UpdateOrderStatus[주문 상태 PAID→PURCHASE_CONFIRMED 자동 전이<br/>POST /internal/order/{orderId}/status]
    UpdateOrderStatus --> KafkaPublish[PurchaseConfirmedEvent Kafka 발행<br/>→ settlement-service 정산 대상 생성]
    KafkaPublish --> Complete([결제 완료])
```

---

## 단계별 상세

### 1단계 — 장바구니 담기 (order-service)

- API: `POST /order/cart/item`
- 사용자가 상품을 장바구니에 담는다
- CartItem 저장 (productId, quantity)
- 재고 확인 안 함, 품절이어도 담기 가능

### 2단계 — 장바구니 조회 (order-service → product-service)

- API: `GET /order/cart`
- 내부 호출: product-service에 최신 가격/재고 조회
- 품절 여부, 재고 부족, 가격 변동을 표시한다
- 표시만 할 뿐 담기/제거를 강제하지 않는다

### 3단계 — 결제 페이지 진입 (order-service → member-service)

- API: `GET /order/checkout?cartItemIds=` 또는 `GET /order/checkout?productId=&quantity=`
- 내부 호출: `GET /internal/member/{id}/address` (배송지 조회)
- 내부 호출: `GET /internal/member/{id}/deposit` (예치금 잔액 조회)
- 이 시점에서는 아무것도 잠기지 않는다 (재고 차감 없음, 예치금 차감 없음)

### 4단계 — 주문 생성 PENDING (order-service)

- API: `POST /order`
1. **서버에서 총 금액 계산** (product-service 조회, 클라이언트 금액 신뢰하지 않음)
2. **주문 생성** (상태: PENDING) + **OrderItem 저장**
3. **배송지 스냅샷 저장** (주문 시점 배송지 정보 고정)
4. **응답**: `{ orderId }`

> 이 시점에서는 예치금 차감·재고 차감을 하지 않는다. PENDING 상태 30분 경과 시 자동 취소.

### 5단계 — 결제 실행 (payment-service 주도)

- API: `POST /payment/orders/{orderId}/pay`

아래 순서로 처리한다:

1. **주문 조회** → `GET /internal/order/{orderId}` (order-service)
2. **예치금 차감** → `POST /internal/member/{id}/deposit/deduct` (member-service)
   - 실패 시 즉시 예외 반환 (재고 차감 이전이므로 롤백 불필요)
3. **재고 차감** → `POST /internal/products/stock/deduct` (product-service)
   - 실패 시 예치금 환급 (payment-service 보상 트랜잭션)
4. **주문 상태 PAID 변경** → `POST /internal/order/{orderId}/status` (order-service)
   - order-service 내부에서 PAID→SHIPPING→DELIVERED→PURCHASE_CONFIRMED 자동 전이
   - PURCHASE_CONFIRMED 전환 시 `PurchaseConfirmedEvent` Kafka 발행 (AFTER_COMMIT)
5. **settlement-service**: Kafka Consumer가 `order.purchase-confirmed` 토픽 수신 → `settlement_target` 생성

---

## 실패 케이스

| 시점 | 실패 사유 | 처리 |
|------|-----------|------|
| 결제하기 버튼 | 예치금 부족 | 잔액/부족 금액 안내, 결제 실패 (재고 차감 없음) |
| 결제하기 버튼 | 재고 부족 | 품절 상품 안내, 예치금 환급(보상 트랜잭션) 후 결제 실패 |

예치금 차감 → 재고 차감 순서로 처리하므로, 예치금 부족 시 재고 롤백이 불필요하다.

---

## 환불(취소) 플로우

```mermaid
sequenceDiagram
    actor Client
    participant Order as order-service
    participant Payment as payment-service
    participant Product as product-service

    Client->>Order: POST /order/{orderId}/cancel
    Order->>Order: 주문 상태 검증 (PENDING, PAID, CONFIRMED만 취소 가능)

    alt PENDING 상태
        Order->>Order: 상태 변경 → CANCELLED (예치금/재고 처리 없음)
        Order-->>Client: 취소 완료
    else PAID/CONFIRMED 상태
        Order->>Order: 상태 변경 → CANCELLED
        Order->>Member: POST /internal/member/{id}/deposit/charge (예치금 환급)
        Member-->>Order: 환급 완료
        Order->>Product: restoreStock (STUB — 미구현, log.warn만 출력)
        Order-->>Client: 취소 완료
    end
```

> 🔴 **주의**: 재고 복원(`POST /internal/products/stock/restore`)은 product-service에 엔드포인트 미노출 상태. order-service `ProductServiceClient.restoreStock()`은 STUB으로 log.warn만 출력. PAID/CONFIRMED 취소 시 재고 복원 불가.

---

## 주문 상태

| 상태 | 설명 |
|------|------|
| `PENDING` | 결제 대기 (주문 생성 직후, 30분 경과 시 자동 취소) |
| `PAID` | 결제 완료 (payment-service → order-service 상태 변경). 🟡 현재 PAID → DELIVERED → PURCHASE_CONFIRMED 자동 전이 운영 중 (배송 모듈 미분리) |
| `CONFIRMED` | 주문 확정 (🟡 미사용 — 판매자 주문 승인 플로우 미구현) |
| `SHIPPING` | 배송 중 |
| `DELIVERED` | 배송 완료 |
| `PURCHASE_CONFIRMED` | 구매확정 → confirmed_at 기록, PurchaseConfirmedEvent Kafka 발행 → settlement-service 정산 대상 생성 |
| `CANCELLED` | 취소/환불 완료 |

---

## 이벤트 구분

| 구분 | 이벤트명 | 방식 | 발행 서비스 | 수신 서비스 | API/경로 |
|------|---------|------|-----------|-----------|---------|
| 구매확정 | PurchaseConfirmedEvent | Spring Event → Kafka | order-service | settlement-service | 토픽: `order.purchase-confirmed` |
| 예치금 충전 | DepositChargedEvent | Spring Event (AFTER_COMMIT) | payment-service | → member-service REST | `POST /internal/member/{id}/deposit/charge` |
| 예치금 차감 | DepositDeductedEvent | Spring Event (AFTER_COMMIT) | payment-service | → member-service REST | `POST /internal/member/{id}/deposit/deduct` |
| 예치금 환급 | DepositRefundedEvent | Spring Event (AFTER_COMMIT) | payment-service | → member-service REST | `POST /internal/member/{id}/deposit/charge` |

> 재고 차감/복원은 이벤트 없이 REST 동기 호출로 처리.

---

## API 엔드포인트 (모듈별)

### order-service
```
POST   /order/cart/item                    장바구니 상품 추가
GET    /order/cart                          장바구니 조회 (품절/가격변동 표시)
PATCH  /order/cart/item/{id}               장바구니 수량 수정
DELETE /order/cart/item/{id}               장바구니 상품 삭제
DELETE /order/cart                          장바구니 비우기
GET    /order/checkout                     결제 페이지 정보 조회 (상품, 배송지, 잔액)
POST   /order                              주문 생성 (PENDING)
GET    /order                              주문 목록 조회
GET    /order/{orderId}                    주문 상세 조회
POST   /order/{orderId}/cancel             주문 취소
POST   /order/{orderId}/confirm            구매확정
POST   /order/{orderId}/return             반품 신청
PATCH  /order/{orderId}/shipment           송장 등록 / 배송 상태 수정
```

### payment-service
```
POST   /payment/orders/{orderId}/pay       결제 실행 (예치금 차감 → 재고 차감 → 주문 PAID)
POST   /payment/deposit/charge/ready       예치금 충전 준비 (Toss PG)
POST   /payment/deposit/charge/confirm     예치금 충전 확정
GET    /payment/deposit/history            예치금 거래 내역 조회
```

### Internal API
```
GET    /internal/order/{orderId}           [order] 주문 조회 → payment
POST   /internal/order/{orderId}/status    [order] 주문 상태 변경 → payment
DELETE /internal/order/cart/{memberId}     [order] 장바구니 삭제 → payment
POST   /internal/products/stock/deduct     [product] 재고 차감 → payment
POST   /internal/products/stock/restore    [product] 재고 복원 → order (STUB 미구현)
POST   /internal/deposit/deduct            [payment] 예치금 차감 (payment 내부 처리, 외부 호출자 없음)
POST   /internal/deposit/refund            [payment] 예치금 환급 (❌ 환불 미구현)
POST   /internal/member/{id}/deposit/deduct  [member] 예치금 차감 → payment
POST   /internal/member/{id}/deposit/charge  [member] 예치금 충전 → payment
GET    /internal/member/{id}/address       [member] 배송지 조회 → order
GET    /internal/member/{id}/deposit       [member] 예치금 조회 → order, payment
GET    /internal/member/{id}/active        [member] 회원 활성 확인 → order, payment
```
