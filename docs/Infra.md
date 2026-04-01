# 인프라 설정 문서

## 개요

7개 MSA 서비스를 Docker 이미지로 빌드하고, GitHub Actions를 통해 GHCR에 푸시 후 AWS EC2에 자동 배포하는 파이프라인 구성.

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

포함 서비스: 위 인프라 + 7개 Spring Boot 서비스 (GHCR 이미지)

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

### CD (`.github/workflows/cd.yml`)

- 트리거: push → `main`
- 동작:
  1. JAR 빌드 (테스트 제외)
  2. GHCR 로그인 (`GITHUB_TOKEN` 자동 제공)
  3. 이미지 빌드 & 푸시 (matrix 전략, 7개 병렬)
     - `ghcr.io/{owner}/{service}:latest`
     - `ghcr.io/{owner}/{service}:{sha}`
  4. EC2 SSH 접속 → `scripts/deploy.sh` 실행

### 배포 스크립트 (`scripts/deploy.sh`)

```bash
docker compose -f docker/docker-compose.prod.yml pull
docker compose -f docker/docker-compose.prod.yml up -d --remove-orphans
docker image prune -f
```

---

## GitHub Actions Secrets

| Secret | 설명 |
|--------|------|
| `EC2_HOST` | EC2 Elastic IP |
| `EC2_USERNAME` | `ubuntu` |
| `EC2_SSH_KEY` | PEM 키 |
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

```bash
# Docker 설치
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu

# 재접속 후
cd /home/ubuntu
git clone {repo-url} app

# 환경변수 파일 작성
vim /home/ubuntu/app/.env
# GHCR_OWNER={github-org} 추가 필요
```

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
| CI 워크플로우 (GitHub Actions) | ⬜ PR 생성 후 확인 예정 |
| CD 워크플로우 (EC2 배포) | ⬜ EC2 설정 후 확인 예정 |

---

## 향후 작업

- [ ] 나머지 5개 Dockerfile 빌드 확인
- [ ] GitHub Secrets 등록
- [ ] EC2 인스턴스 생성 및 초기 설정
- [ ] PR → CI 통과 확인
- [ ] main 머지 → CD 배포 확인
- [ ] 도메인 확보 후 Nginx + HTTPS 추가
