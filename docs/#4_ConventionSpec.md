# 컨벤션 정의

> 프로젝트의 협업 효율성과 코드 일관성을 위해 Git, 브랜치, 코드 스타일, PR 규칙에 대한 공통 컨벤션을 정의한다.

---

## Git 및 브랜치 전략

형상 관리는 GitHub를 사용하며, 브랜치 전략은 `main`, `develop`, `feature/*`, `fix/*` 형태로 운영한다.

기능 개발은 feature 브랜치에서 진행하고, 개발 완료 후 Pull Request를 통해 develop 브랜치로 병합한다.

---

## 멀티 모듈 프로젝트 구조 (MSA)

각 도메인 서비스는 독립된 Spring Boot 프로젝트로 운영하며, 헥사고날 아키텍처를 따른다.

```
bookstore/
├── eureka-server/         ← Eureka Service Discovery
├── gateway-service/       ← Spring Cloud Gateway (토큰 검증 + 라우팅)
├── member-service/        ← 회원 관리 + 인증(로그인/회원가입/토큰 발급)
├── product-service/
├── review-service/
├── order-service/
└── payment-service/
```

각 서비스 내부 패키지 구조:

```
{service-name}/
├── adapter/
│   ├── in/
│   │   ├── web/           ← Controller (REST API)
│   │   └── kafka/         ← Kafka Consumer
│   └── out/
│       ├── persistence/   ← JPA Repository, Entity
│       ├── kafka/         ← Kafka Producer
│       └── rest/          ← 외부 서비스 호출 (FeignClient 등)
├── application/
│   ├── port/
│   │   ├── in/            ← UseCase 인터페이스
│   │   └── out/           ← Port 인터페이스
│   └── service/           ← UseCase 구현체
└── domain/
    └── model/             ← 도메인 엔티티, VO, Enum
```

---

## 커밋 메시지 규칙

커밋 메시지는 작업 목적이 명확히 드러나도록 prefix 기반으로 작성한다.

| prefix | 의미 | 예시 |
|--------|------|------|
| `feat` | 기능 추가 | feat: 로그인 구현 |
| `fix` | 버그 수정 | fix: 로그인 과정에서 발생한 토큰 관련 이슈 수정 |
| `refactor` | 리팩토링 | refactor: 로그인 비즈니스 로직 가독성 개선 |
| `docs` | 문서 수정 | docs: API 설계 문서 request - response 수정 |
| `test` | 테스트 코드 | test: 로그인 기능 테스트 코드 작성 |
| `chore` | 설정, 빌드, 기타 업무 수정 | chore: build.gradle 의존성 추가 |
| `style` | 포맷팅, 세미콜론, 공백 등 수정 | style: 코드 들여쓰기 수정 |

---

## 코드 스타일 규칙

코드 스타일은 Java/Spring 표준 네이밍 규칙을 따르며, 클래스명은 PascalCase, 메서드와 변수명은 camelCase, 상수명은 UPPER_SNAKE_CASE를 사용한다.

들여쓰기, import 정렬, 포맷팅 규칙은 IDE formatter 또는 lint 도구를 통해 일관되게 유지한다.

---

## Pull Request 규칙

모든 기능 개발은 Pull Request 기반으로 병합하며, PR에는 작업 내용, 변경 이유, 테스트 결과를 포함한다.

리뷰 후 승인된 코드만 병합하는 것을 원칙으로 한다.

### PR 템플릿

PR에 포함할 항목: 작업 내용, 변경 이유, 테스트 결과, 참고 사항, 스크린샷 또는 API 예시

```
Issue 8xx - feat: 회원가입 기능

## 작업 내용
- 회원가입 API 구현
- 이메일 중복 체크 로직 추가

## 변경 이유
- 회원가입 기능 1차 구현

## 테스트
- 단위 테스트 작성
- Postman 수동 테스트 완료

## 참고 사항
- 해당 서비스: member-service
```
