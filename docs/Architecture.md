## 서비스 아키텍처 다이어그램

> MSA (Microservices Architecture) + 헥사고날 아키텍처 기반
> 도메인 단위로 서버 분리, 동기 통신은 REST API, 비동기 통신은 Kafka
> 서비스 디스커버리: Eureka Server / API 라우팅 및 토큰 검증: Spring Cloud Gateway

---

### 전체 시스템 구성

```mermaid
flowchart TB
    user[User / Client]

    user --> dns[Route53]
    dns --> alb[Application Load Balancer]
    alb --> gateway[Spring Cloud Gateway\n- 라우팅\n- JWT 토큰 검증]

    subgraph discovery[Service Discovery]
        eureka[Eureka Server]
    end

    subgraph member-service[Member Service]
        member[Spring Boot\n- 회원 CRUD\n- 로그인 / 회원가입\n- 토큰 발급 / 재발급 / 로그아웃]
    end

    subgraph product-service[Product Service]
        product[Spring Boot]
    end

    subgraph review-service[Review Service]
        review[Spring Boot]
    end

    subgraph order-service[Order Service]
        order[Spring Boot]
    end

    subgraph payment-service[Payment Service]
        pgClient[PgClient Interface]
        tossImpl[TossPayClient]
        futureImpl[Future PG Client\n확장 포인트]
        depositService[DepositService\n충전·차감·환불 실행]
        payment[Spring Boot]
    end

    subgraph messaging[Message Broker]
        kafka[Apache Kafka]
    end

    subgraph infra[Infrastructure]
        memberDB[(Member DB\nMySQL)]
        productDB[(Product DB\nMySQL)]
        reviewDB[(Review DB\nMySQL)]
        orderDB[(Order DB\nMySQL)]
        paymentDB[(Payment DB\nMySQL)]
    end

    subgraph ext[External]
        toss[Toss Payments API]
        mail[Email Service]
        ship[Delivery API]
    end

    %% Gateway → 각 서비스 라우팅
    gateway --> member
    gateway --> product
    gateway --> review
    gateway --> order
    gateway --> payment

    %% Eureka 등록
    gateway -. "register" .-> eureka
    member -. "register" .-> eureka
    product -. "register" .-> eureka
    review -. "register" .-> eureka
    order -. "register" .-> eureka
    payment -. "register" .-> eureka

    %% DB 연결
    member --> memberDB
    product --> productDB
    review --> reviewDB
    order --> orderDB
    payment --> paymentDB

    %% 동기 통신 (REST API)
    order -- "REST" --> product
    order -- "REST" --> member
    payment -- "REST" --> order
    payment -- "REST" --> member
    product -- "REST" --> member
    review -- "REST" --> order

    %% 비동기 통신 (Kafka)
    order -. "Kafka" .-> kafka
    payment -. "Kafka" .-> kafka
    review -. "Kafka" .-> kafka
    member -. "Kafka" .-> kafka
    kafka -. "consume" .-> order
    kafka -. "consume" .-> product
    kafka -. "consume" .-> payment
    kafka -. "consume" .-> review

    %% Payment 내부
    payment --> pgClient
    payment --> depositService
    pgClient --> tossImpl
    pgClient -.-> futureImpl
    tossImpl --> toss

    payment --> mail
    order --> ship
```

---

### 인프라 구성 요소

| 구성 요소 | 기술 | 역할 |
|----------|------|------|
| API Gateway | Spring Cloud Gateway | 요청 라우팅, JWT 토큰 검증 (서명 확인 + 만료 확인 + 블랙리스트 조회) |
| Service Discovery | Eureka Server | 서비스 등록/탐색, 동적 라우팅 지원 |
| Message Broker | Apache Kafka | 서비스 간 비동기 이벤트 통신 |

### Gateway 역할 (토큰 검증만 수행)

Gateway는 토큰 **발급은 하지 않으며**, 발급된 토큰의 **검증만** 수행합니다.

| 단계 | Gateway에서 수행 | 설명 |
|------|:---:|------|
| JWT 서명 검증 | O | Secret Key로 토큰 위변조 확인 |
| 토큰 만료 확인 | O | `exp` 클레임 검증 |
| 블랙리스트 조회 | O | `jti`로 TOKEN_BLACKLIST 확인 |
| 공개 API 판별 | O | 인증 불필요 경로 바이패스 |
| role/email_verified 인가 | X | 각 서비스 내부에서 처리 |
| 비즈니스 로직 | X | 각 서비스 내부에서 처리 |

> 토큰 발급(로그인, 회원가입, 토큰 재발급)은 **Member Service**가 전담합니다.

---

### 서비스 간 통신 방식

| 구분 | 방식 | 사용 구간 | 설명 |
|------|------|----------|------|
| 동기 | REST API | 서비스 → 서비스 | 즉시 응답이 필요한 조회/검증 호출 (Eureka 기반 서비스 디스커버리) |
| 비동기 | Kafka Event | 서비스 → Kafka → 서비스 | 상태 변경 알림, 결과적 일관성(Eventual Consistency) 보장 |

**동기 통신 (REST API)**

| 호출 방향 | 목적 |
|----------|------|
| Order → Product | 재고 및 가격 검증 (`GET /products?ids=` 배치 조회로 대체), 재고 차감/복원은 Kafka 이벤트로 처리 |
| Order → Member | 배송지 조회, 회원 상태 확인 |
| Payment → Order | 주문 금액 검증 |
| Payment → Member | 예치금 잔액 확인, 판매자 계좌 정보 조회 |
| Product → Member | 판매자 권한 및 상태 확인 |
| Review → Order | 구매 이력 확인 |

**비동기 통신 (Kafka Event)**

| 이벤트 | Producer | Consumer | 목적 |
|--------|----------|----------|------|
| PaymentApprovedEvent | Payment | Order, Product | 주문 확정, 재고 차감 |
| PaymentFailedEvent | Payment | Order | 결제 실패 시 주문 상태 정리 |
| OrderCancelRequestedEvent | Order | Payment | 환불 처리 시작 |
| RefundCompletedEvent | Payment | Order, Product | 환불 완료, 재고 복원 |
| ReviewCreatedEvent | Review | Product | 평균 평점 업데이트 |
| MemberRegisteredEvent | Member | (이메일 발송) | 인증 메일 발송 |
| SellerApprovedEvent | Member | Product | 판매자 승인 반영 |

---

### 헥사고날 아키텍처 (서비스별 내부 구조)

```
service-name/
├── adapter/
│   ├── in/
│   │   ├── web/           ← Controller (REST API 수신)
│   │   └── kafka/         ← Kafka Consumer (이벤트 수신)
│   └── out/
│       ├── persistence/   ← JPA Repository, Entity
│       ├── kafka/         ← Kafka Producer (이벤트 발행)
│       └── rest/          ← 다른 서비스 REST 호출 (FeignClient 등)
├── application/
│   ├── port/
│   │   ├── in/            ← UseCase 인터페이스
│   │   └── out/           ← Port 인터페이스 (DB, 외부 서비스)
│   └── service/           ← UseCase 구현체
└── domain/
    └── model/             ← 도메인 엔티티, VO, Enum
```

각 서비스는 동일한 헥사고날 구조를 따르며, Port/Adapter 패턴을 통해 비즈니스 로직과 인프라를 분리합니다. 서비스 간 호출 시 out adapter의 rest 또는 kafka adapter를 사용합니다.

---

### DB 분리 전략

각 서비스는 독립된 MySQL 데이터베이스를 사용합니다 (Database per Service 패턴).

| 서비스 | DB | 주요 테이블 |
|--------|-----|------------|
| Member | member_db | MEMBER, ADDRESS, SELLER, REFRESH_TOKEN, TOKEN_BLACKLIST |
| Product | product_db | PRODUCT, CATEGORY |
| Review | review_db | REVIEW |
| Order | order_db | ORDER, ORDER_ITEM, CART, CART_ITEM, SHIPMENT |
| Payment | payment_db | PAYMENT, REFUND, DEPOSIT, SETTLEMENT |
