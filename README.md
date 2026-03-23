# beadv5_5_404_Not_Found_BE
백엔드 단기심화 데브코스 5기 5팀 404 Not Found 리포지토리입니다.

---

## 서비스 구성

| 서비스 | 포트 | 역할 |
|--------|------|------|
| eureka-server | 8761 | 서비스 레지스트리 |
| gateway-service | 8080 | 단일 진입점, 라우팅 |
| member-service | 8081 | 회원, 판매자 등록, 예치금 |
| product-service | 8082 | 상품, 재고 |
| order-service | 8083 | 주문, 장바구니 |
| payment-service | 8084 | 결제, 정산 |

---

## 로컬 실행 방법

### 사전 요구사항

- Java 21
- Docker Desktop

### 1. 환경변수 설정

터미널에 아래 환경변수를 등록하거나 `~/.zshrc`에 추가한다.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/bookcommerce
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

`~/.zshrc`에 추가하면 터미널을 열 때마다 자동 적용된다.

```bash
echo 'export DB_URL=jdbc:postgresql://localhost:5432/bookcommerce
export DB_USERNAME=postgres
export DB_PASSWORD=postgres' >> ~/.zshrc
source ~/.zshrc
```

### 2. PostgreSQL 실행

```bash
cd docker
echo "DB_USERNAME=postgres
DB_PASSWORD=postgres" > .env   # 최초 1회만
docker compose up -d postgres
```

### 3. 서비스 실행

모든 명령어는 **프로젝트 루트**에서 실행한다. 서비스마다 별도 터미널을 사용한다.

> eureka-server → gateway-service → 나머지 순서로 실행한다.

```bash
# eureka-server (먼저 실행)
./gradlew :eureka-server:bootRun

# gateway-service
./gradlew :gateway-service:bootRun

# member-service
./gradlew :member-service:bootRun

# product-service
./gradlew :product-service:bootRun

# order-service
./gradlew :order-service:bootRun

# payment-service
./gradlew :payment-service:bootRun
```

### 4. Eureka 대시보드 확인

서비스 기동 후 약 30초~1분 뒤 브라우저에서 확인한다.

```
http://localhost:8761
```

`Instances currently registered with Eureka` 목록에 각 서비스가 `UP` 상태로 표시되면 정상이다.
