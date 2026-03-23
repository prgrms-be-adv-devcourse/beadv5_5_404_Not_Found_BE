# CLAUDE.md

이 파일은 Claude Code가 이 프로젝트에서 코드를 작성할 때 참고하는 컨벤션 가이드입니다.

---

## 프로젝트 개요

- **프로젝트명**: 도서 이커머스 플랫폼 (Book Commerce)
- **아키텍처**: MSA (Spring Cloud — Eureka + Gateway)
- **패키지 구조**: 헥사고날 아키텍처 (Ports & Adapters)
- **언어**: Java 21
- **프레임워크**: Spring Boot 4.0.4
- **Spring Cloud**: 2025.1.x

> ⚠️ Spring Boot 4.0.x는 Spring Cloud 2025.1.x와 함께 사용해야 한다.
> Spring Cloud 2025.0.x는 Spring Boot 4.0.1 이상과 호환되지 않는다.

---

## 브랜치 전략

```
main        ← 배포용, 직접 push 금지
develop     ← 개발 통합, 직접 push 금지
feature/*   ← 기능 개발 (예: feature/member-login)
fix/*       ← 버그 수정 (예: fix/token-refresh-error)
```

- 기능 개발은 반드시 `feature/*` 브랜치에서 진행
- 개발 완료 후 PR을 통해 `develop`으로 병합
- `main` 병합은 배포 시점에만 진행

---

## 커밋 메시지 규칙

형식: `<prefix>: <작업 내용>`

| prefix | 의미 | 예시 |
|--------|------|------|
| `feat` | 기능 추가 | `feat: 로그인 구현` |
| `fix` | 버그 수정 | `fix: 로그인 과정에서 발생한 토큰 관련 이슈 수정` |
| `refactor` | 리팩토링 | `refactor: 로그인 비즈니스 로직 가독성 개선` |
| `docs` | 문서 수정 | `docs: API 설계 문서 request-response 수정` |
| `test` | 테스트 코드 | `test: 로그인 기능 테스트 코드 작성` |
| `chore` | 설정, 빌드, 기타 | `chore: build.gradle 의존성 추가` |
| `style` | 포맷팅, 공백 등 | `style: 코드 들여쓰기 수정` |

---

## 코드 스타일

- **클래스명**: PascalCase (`MemberController`)
- **메서드/변수명**: camelCase (`findMemberById`)
- **상수명**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **들여쓰기**: 4 spaces
- **import 정렬**: IDE formatter 기준 유지

---

## 패키지 구조 (헥사고날 아키텍처)

각 서비스는 아래 구조를 따른다.

```
{service-name}/src/main/java/com/404notfound/{service}/
├── domain/                  # 순수 도메인 (JPA, Spring 의존 없음)
│   ├── model/               # 도메인 객체
│   └── exception/           # 도메인 예외
├── application/             # 포트 (인터페이스)
│   ├── port/
│   │   ├── in/              # 인바운드 포트 (UseCase 인터페이스)
│   │   └── out/             # 아웃바운드 포트 (Repository 인터페이스)
│   └── service/             # 유스케이스 구현체
├── infrastructure/          # 기술 세부사항 (어댑터)
│   ├── persistence/         # JPA Entity, Repository 구현체
│   └── client/              # HTTP 클라이언트 (현재 Feign, 추후 gRPC 전환 가능)
└── presentation/            # 외부 인터페이스
    ├── controller/          # REST Controller
    └── dto/                 # Request / Response DTO (컨트롤러마다 별도 정의)
```

### 핵심 원칙
- `domain`은 어떤 외부 라이브러리에도 의존하면 안 된다 (순수 Java)
- `presentation`의 DTO는 다른 서비스와 공유하지 않는다
- 서비스 간 통신은 `infrastructure/client`를 통해서만 한다

---

## API 응답 형식

### 성공 응답

```json
{
  "status": 201,
  "code": "MEMBER_REGISTER_SUCCESS",
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "memberId": 1,
    "email": "user@example.com",
    "name": "홍길동"
  }
}
```

### 실패 응답

```json
{
  "status": 400,
  "code": "INVALID_INPUT_VALUE",
  "message": "입력값이 올바르지 않습니다.",
  "data": {
    "email": "이메일 형식이 올바르지 않습니다.",
    "password": "비밀번호는 8자 이상이어야 합니다."
  }
}
```

### 규칙
- `status`: HTTP 상태 코드와 동일한 값
- `code`: 서비스별 prefix를 붙인 고유 코드 (예: `MEMBER_`, `ORDER_`, `PAYMENT_`)
- `data`: 성공 시 응답 데이터, 실패 시 필드별 검증 오류 메시지 (없으면 `null`)
- `GlobalExceptionHandler`는 `presentation` 레이어에 위치한다
- 공통 응답 래퍼 클래스(`ApiResponse<T>`)를 각 서비스마다 별도 정의한다 (공유 금지)

---

## @Transactional 경계

- `@Transactional`은 **`application/service` 레이어에서만** 선언한다
- `infrastructure/persistence` 레이어에서는 선언하지 않는다
- `presentation` 레이어(Controller)에서는 선언하지 않는다
- 조회 전용 메서드에는 `@Transactional(readOnly = true)`를 명시한다

```java
// 올바른 예 — application/service
@Transactional
public void register(RegisterMemberCommand command) { ... }

@Transactional(readOnly = true)
public MemberInfo findMember(Long memberId) { ... }

// 금지 — infrastructure/persistence 또는 Controller에서 선언하지 않음
```

---

## 레이어 간 매핑 전략

각 레이어 간 객체 변환은 **해당 레이어의 클래스 내부에 정의한 static 메서드**를 통해 수행한다.

### 도메인 ↔ JPA 엔티티 (`infrastructure/persistence`)

변환 메서드는 JPA 엔티티 클래스 내부에 static 메서드로 정의한다.

```java
// MemberJpaEntity.java
public class MemberJpaEntity {

    // 도메인 → 엔티티
    public static MemberJpaEntity from(Member member) {
        MemberJpaEntity entity = new MemberJpaEntity();
        entity.id = member.getId();
        entity.email = member.getEmail();
        entity.name = member.getName();
        return entity;
    }

    // 엔티티 → 도메인
    public Member toDomain() {
        return Member.builder()
                .id(this.id)
                .email(this.email)
                .name(this.name)
                .build();
    }
}
```

### 도메인 ↔ Response DTO (`presentation/dto`)

변환 메서드는 Response DTO 클래스 내부에 static 메서드로 정의한다.

```java
// MemberRegisterResponse.java
public class MemberRegisterResponse {

    // 도메인 → DTO
    public static MemberRegisterResponse from(Member member) {
        return new MemberRegisterResponse(
                member.getId(),
                member.getEmail(),
                member.getName()
        );
    }
}
```

### Request DTO → 커맨드/도메인 (`presentation/dto`)

변환 메서드는 Request DTO 클래스 내부에 정의한다.

```java
// MemberRegisterRequest.java
public class MemberRegisterRequest {

    // DTO → 커맨드
    public RegisterMemberCommand toCommand() {
        return new RegisterMemberCommand(this.email, this.password, this.name);
    }
}
```

### 규칙 요약
- 변환 책임은 **변환 대상을 더 잘 아는 쪽**(주로 외부 레이어)에 둔다
- 도메인 객체는 다른 레이어의 타입을 알지 못한다 (의존 방향 위반 금지)
- MapStruct 등 외부 매핑 라이브러리는 사용하지 않는다

---

## 서비스 간 통신

- **현재**: HTTP 기반 (Feign Client)
- **추후**: gRPC 전환 가능성 있음
- 통신 구현체는 반드시 `infrastructure/client` 패키지에 위치시켜 교체 가능하도록 한다
- 아웃바운드 포트(인터페이스)를 통해 호출하므로 구현체 교체 시 도메인/서비스 코드 수정 불필요

---

## Pull Request 규칙

### PR 제목 형식
```
Issue #xx - <prefix>: <작업 내용>
예) Issue #12 - feat: 회원가입 기능
```

### PR 본문 템플릿
```markdown
## 작업 내용
-

## 변경 이유
-

## 테스트
-

## 참고 사항
-

## 스크린샷 또는 API 예시
```

### PR 규칙
- 모든 기능 개발은 PR 기반으로 병합
- 최소 1명 리뷰 승인 후 merge
- 리뷰어는 작업 내용과 관련 있는 팀원으로 지정

---

## 서비스 구성 및 포트

| 서비스 | 포트 | 역할 |
|--------|------|------|
| eureka-server | 8761 | 서비스 레지스트리 |
| gateway-service | 8080 | 단일 진입점, 라우팅 |
| member-service | 8081 | 회원, 판매자 등록, 예치금 |
| product-service | 8082 | 상품, 재고 |
| order-service | 8083 | 주문, 장바구니 |
| payment-service | 8084 | 결제, 정산 |

---

## 금지 사항

- `domain` 패키지에 `@Entity`, `@Repository`, Spring 어노테이션 사용 금지
- DTO를 서비스 간 공유 금지 (각 서비스에서 별도 정의)
- `main`, `develop` 브랜치 직접 push 금지
- `.env` 파일 커밋 금지 (`.gitignore`에 반드시 포함)
- `infrastructure/client` 외부에서 타 서비스 직접 호출 금지
- 도메인 객체에서 JPA 엔티티 또는 DTO 타입 참조 금지
- `infrastructure/persistence` 또는 `presentation` 레이어에 `@Transactional` 선언 금지
