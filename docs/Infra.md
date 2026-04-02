# 인프라 설정 문서

## 개요

7개 MSA 서비스를 GitHub Actions에서 빌드하여 Docker Hub에 푸시, EC2는 이미지를 pull하여 실행. GitHub Actions → Docker Hub push → EC2 pull 방식으로 동작.

---

## 아키텍처

```
인터넷
  ↓
EC2 (t3.large, ap-northeast-2)
  ↓
gateway-service (:80)
  ↓
각 MSA 서비스 (Docker 내부 네트워크)
  ↓
PostgreSQL / Kafka
```

> HTTPS(Nginx + Let's Encrypt)는 도메인 확보 후 추가 예정

---

## 생성 파일 목록

| 파일 | 설명 |
|------|------|
| `.dockerignore` | 빌드 컨텍스트 제외 설정 |
| `{service}/Dockerfile` x7 | 멀티스테이지 빌드 |
| `docker/docker-compose.yml` | 로컬 개발용 인프라 |
| `docker/docker-compose.prod.yml` | EC2 프로덕션 배포용 |
| `.github/workflows/ci.yml` | PR 빌드 & 테스트 |
| `.github/workflows/cd.yml` | main 머지 시 자동 배포 |
| `scripts/deploy.sh` | EC2 배포 스크립트 |

---

## Dockerfile

### 구조 (멀티스테이지 빌드)

```
Stage 1 (build): gradle:9.0.0-jdk21-alpine
  → ./gradlew :{service}:bootJar -x test

Stage 2 (run): eclipse-temurin:21-jre-alpine
  → JAR만 복사하여 실행
```

### JVM 옵션

```
-Xms128m -Xmx256m
```

t3.large(8GB)에서 7개 서비스 동시 운영 기준 메모리 계획:

| 컴포넌트 | 예상 사용량 |
|---------|-----------|
| Spring Boot x7 | ~2.8GB |
| PostgreSQL | ~512MB |
| Kafka + Zookeeper | ~1GB |
| OS + 여유 | ~1GB |
| **총합** | **~5.3GB** |

### 서비스별 포트

| 서비스 | 포트 |
|--------|------|
| eureka-server | 8761 |
| gateway-service | 8080 (외부 80) |
| member-service | 8081 |
| product-service | 8082 |
| order-service | 8083 |
| payment-service | 8084 |
| settlement-service | 8085 |

### 빌드 명령어 (루트 기준)

```bash
docker build -f {service}/Dockerfile -t {service}:test .
```

---

## Docker Compose

### 로컬 개발용 (`docker/docker-compose.yml`)

포함 서비스: PostgreSQL, Zookeeper, Kafka, Eureka

```bash
docker compose -f docker/docker-compose.yml --env-file .env up
```

> `--env-file .env` 필수: `-f`로 compose 파일 경로 지정 시 루트의 `.env`를 자동으로 못 찾음

### 프로덕션용 (`docker/docker-compose.prod.yml`)

포함 서비스: 위 인프라 + 7개 Spring Boot 서비스 (EC2 직접 빌드)

Docker 네트워크 내부 URL:

| 항목 | URL |
|------|-----|
| DB | `jdbc:postgresql://postgres:5432/bookcommerce` |
| Kafka | `kafka:9092` |
| Eureka | `http://eureka-server:8761/eureka/` |

---

## CI/CD

### CI (`.github/workflows/ci.yml`)

- 트리거: PR → `develop`, `main`
- 동작: Gradle 빌드 + 테스트, 실패 시 merge 차단
- 비고: 전체 서비스 테스트 포함 (issue #56, #58 해결로 임시 제외 옵션 제거)

### CD (`.github/workflows/cd.yml`)

- 트리거: push → `main`, `workflow_dispatch` (수동 실행 가능)
- 동작:
  1. `detect-changes`: `dorny/paths-filter`로 변경된 서비스 감지 → JSON 배열 출력
  2. `build-and-push`: 변경된 서비스만 병렬 빌드 + Docker Hub 푸시 (dynamic matrix)
  3. `deploy`: EC2 SSH 접속 → `scripts/deploy.sh` 실행 (변경된 서비스 목록 전달)
- 비고: `workflow_dispatch` 수동 실행 시 7개 전부 배포

### 배포 스크립트 (`scripts/deploy.sh`)

```bash
# JSON 배열 인자로 변경된 서비스 목록 수신 (예: ["member-service","order-service"])
SERVICES=$(echo "$1" | tr -d '[]"' | tr ',' ' ')
docker compose -f docker/docker-compose.prod.yml --env-file .env pull $SERVICES
docker compose -f docker/docker-compose.prod.yml --env-file .env up -d --no-deps $SERVICES
docker image prune -f
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

---

## GitHub Actions Secrets

| Secret | 설명 |
|--------|------|
| `EC2_HOST` | EC2 Elastic IP |
| `EC2_USERNAME` | `ec2-user` (Amazon Linux) |
| `EC2_SSH_KEY` | PEM 키 |
| `DOCKERHUB_USERNAME` | Docker Hub 계정명 |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token |
| `DB_USERNAME` | DB 계정 |
| `DB_PASSWORD` | DB 비밀번호 |
| `JWT_SECRET_KEY` | JWT 서명 키 (32자 이상) |
| `INTERNAL_SECRET_KEY` | 내부 서비스 인증 키 |
| `PAYMENT_ENCRYPTION_KEY` | 결제 암호화 키 |
| `TOSS_CLIENT_KEY` | 토스페이먼츠 클라이언트 키 |
| `TOSS_SECRET_KEY` | 토스페이먼츠 시크릿 키 |
| `TOSS_SUCCESS_URL` | 결제 성공 콜백 URL |
| `TOSS_FAIL_URL` | 결제 실패 콜백 URL |
| `ADMIN_EMAIL` | 관리자 이메일 |
| `ADMIN_PASSWORD` | 관리자 비밀번호 |

---

## EC2 초기 설정 (1회)

> Amazon Linux 2023 기준 (Ubuntu가 아닌 경우)

```bash
# Docker 설치
sudo yum update -y
sudo yum install -y docker git
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
newgrp docker

# docker-compose-plugin 설치
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
docker compose version

# 레포 클론
git clone {repo-url} /home/ec2-user/app
cd /home/ec2-user/app

# 환경변수 파일 작성 (운영 값으로 채울 것)
cat > .env << EOF
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET_KEY=...
INTERNAL_SECRET_KEY=...
PAYMENT_ENCRYPTION_KEY=...
TOSS_CLIENT_KEY=...
TOSS_SECRET_KEY=...
TOSS_SUCCESS_URL=...
TOSS_FAIL_URL=...
ADMIN_EMAIL=...
ADMIN_PASSWORD=...
MEMBER_SERVICE_URL=http://member-service:8081
PRODUCT_SERVICE_URL=http://product-service:8082
EOF
```

### 보안 그룹 인바운드 규칙

| 유형 | 프로토콜 | 포트 | 소스 |
|------|----------|------|------|
| SSH | TCP | 22 | 내 IP |
| HTTP | TCP | 80 | 0.0.0.0/0 |

### GitHub Actions Secrets `EC2_USERNAME`

| AMI 종류 | username |
|----------|----------|
| Amazon Linux | `ec2-user` |
| Ubuntu | `ubuntu` |

---

## 로컬 테스트 결과

| 항목 | 결과 |
|------|------|
| eureka-server Dockerfile 빌드 | ✅ 성공 (127MB) |
| gateway-service Dockerfile 빌드 | ✅ 성공 (130MB) |
| member-service Dockerfile 빌드 | ✅ 성공 (156MB) |
| product-service Dockerfile 빌드 | ✅ 성공 (151MB) |
| order-service Dockerfile 빌드 | ✅ 성공 (173MB) |
| payment-service Dockerfile 빌드 | ✅ 성공 (152MB) |
| settlement-service Dockerfile 빌드 | ✅ 성공 (169MB) |
| docker-compose 인프라 기동 (postgres, kafka, zookeeper, eureka) | ✅ 성공 |
| CI 워크플로우 (GitHub Actions) | ✅ 성공 |
| CD 워크플로우 (EC2 배포) | ⬜ EC2 설정 후 확인 예정 |

---

## CI 트러블슈팅

### 문제 1: DB 없이 통합 테스트 실패
- **증상**: `IllegalStateException` — Spring Context 로딩 실패
- **원인**: CI 환경에 PostgreSQL 없음
- **해결**: `ci.yml`에 PostgreSQL 서비스 컨테이너 추가
```yaml
services:
  postgres:
    image: postgres:16
    env:
      POSTGRES_DB: bookcommerce
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    options: --health-cmd "pg_isready -U postgres" ...
```

### 문제 2: INTERNAL_SECRET_KEY 불일치
- **증상**: `AssertionError` — 내부 API 인증 실패 (15개 테스트 실패)
- **원인**: CI 환경변수 값이 테스트 코드 하드코딩 값과 다름
  - 잘못된 CI 설정값: `test-internal-secret-key-for-ci`
  - 테스트 코드: `test-internal-secret`
- **해결**: CI 환경변수를 `test-internal-secret`으로 수정 → **현재 ci.yml 반영 완료**

### 문제 3: EC2 디스크 부족으로 빌드 실패
- **증상**: `No space left on device` — Gradle wrapper unzip 중 실패
- **원인**: 반복 배포로 Docker 이미지/캐시 누적
- **해결**: `deploy.sh`에 빌드 전 `docker system prune -f` 추가

### 문제 4: Docker Compose --env-file 누락
- **증상**: `DB_USERNAME`, `DB_PASSWORD` 변수 미인식
- **원인**: `-f docker/docker-compose.yml` 옵션 사용 시 루트 `.env` 자동 로딩 안 됨
- **해결**: `--env-file .env` 명시적 지정 필요
```bash
docker compose -f docker/docker-compose.yml --env-file .env up
```

### 문제 5: EC2 재시작 후 컨테이너 미기동
- **증상**: EC2 인스턴스 재시작 후 모든 서비스 `Exited` 상태
- **원인**: `docker-compose.prod.yml`에 `restart` 정책 미설정
- **해결**: 모든 서비스에 `restart: unless-stopped` 추가
- **추가 조치**: EC2 재기동 후 수동 복구 절차 (아래 참고)

### 문제 6: EC2 재시작 후 Kafka NodeExists 오류
- **증상**: `KeeperErrorCode = NodeExists` — Kafka 브로커 등록 실패
- **원인**: EC2 재시작 시 Zookeeper에 이전 세션의 `/brokers/ids/1` ephemeral 노드가 잔존
- **해결**: Zookeeper에서 stale 노드 삭제 후 Kafka 재시작
```bash
docker exec bookcommerce-zookeeper zookeeper-shell zookeeper:2181 delete /brokers/ids/1
docker compose -f docker-compose.prod.yml restart kafka
```

### 문제 7: EC2 재시작 후 .env 파일 유실
- **증상**: `DB_USERNAME`, `DOCKERHUB_USERNAME` 변수 미인식
- **원인**: `.env` 파일이 git에 포함되지 않아 EC2 재시작(또는 재클론) 시 유실
- **해결**: EC2에서 수동으로 두 위치에 `.env` 재생성 필요
```bash
# docker compose 변수 치환용 (DOCKERHUB_USERNAME 등)
cat > ~/app/docker/.env << 'EOF'
DB_USERNAME=postgres
DB_PASSWORD=postgres
DOCKERHUB_USERNAME=hoya517
EOF

# 컨테이너 런타임 환경변수용 (env_file: ../.env 참조)
cat > ~/app/.env << 'EOF'
DB_USERNAME=postgres
DB_PASSWORD=postgres
DOCKERHUB_USERNAME=hoya517
# 나머지 운영 시크릿 값 추가
EOF
```

---

## 향후 작업

- [x] Dockerfile x7 빌드 확인
- [x] docker-compose 인프라 로컬 기동 확인
- [x] GitHub Secrets 등록
- [x] EC2 인스턴스 생성 (Amazon Linux 2023, t3.large)
- [x] Elastic IP 할당 및 연결 (not-found)
- [x] 보안 그룹 설정 (포트 22, 80)
- [x] EC2 Docker + git 설치 및 레포 클론
- [x] main 머지 → CD 배포 확인
- [x] EC2 재시작 후 자동 기동 설정 (`restart: unless-stopped`)
- [ ] 도메인 확보 후 Nginx + HTTPS 추가
