# CLAUDE.md

이 파일은 Claude Code가 이 프로젝트에서 코드를 작성할 때 참고하는 컨벤션 가이드입니다.

---

## 기본 원칙

- 도서 이커머스 플랫폼, Java Spring Boot 구현
- 결과를 맨위에 노출 후 밑에 세세한 내용
- 보수적으로 사실에 근거하여 답변, 애매모호한 것은 미리 알리기

## 코드 작성 전 필수 참조 (docs/ 하위)

- 코드 작성 전 반드시 아래 설계 문서를 읽고 그 기준에 맞춰 구현할 것
- `#1_APIspec.md` → API 엔드포인트 경로, Method, 역할 구분
- `#2_ERD.md` → 테이블 구조, 컬럼 타입, 관계
- `#3_AuthDesign.md` → 인증/인가 흐름, 토큰 구조, Gateway/서비스 역할 분리
- `#4_ConventionSpec.md` → 네이밍, 커밋, 브랜치, PR 규칙, 패키지 구조
- `FeatureSpec.md` → 기능 상세 및 서비스 간 통신 방식
- `EventDesign.md` → Kafka 이벤트 Topic, Producer/Consumer
- `Architecture.md` → MSA 구조, 헥사고날 내부 구조, DB 분리 전략
- `EnumSpec.md` → Enum/상수값 정의, 상태 전이, 서비스 정책 상수
- `UserStory.md` → 유저 스토리 (기능 요구사항)

---

## 프로젝트 개요

- **프로젝트명**: 도서 이커머스 플랫폼 (Book Commerce)
- **아키텍처**: MSA (Spring Cloud — Eureka + Gateway)
- **패키지 구조**: 헥사고날 아키텍처 (Ports & Adapters)
- **언어**: Java 21
- **프레임워크**: Spring Boot 4.0.4
- **Spring Cloud**: 2025.1.x
- **MSA 5개 서비스**: Member / Product / Review / Order / Payment
- **동기 통신**: REST API (FeignClient) / **비동기 통신**: Kafka
- **Gateway**: Spring Cloud Gateway (토큰 검증만) / **Discovery**: Eureka Server
- **인증**: Member Service가 토큰 발급 담당, API 경로는 `/auth/*`로 분리

> ⚠️ Spring Boot 4.0.x는 Spring Cloud 2025.1.x와 함께 사용해야 한다.
> Spring Cloud 2025.0.x는 Spring Boot 4.0.1 이상과 호환되지 않는다.

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
- `GlobalExceptionHandler`는 `adapter/in/web` 레이어에 위치한다
- 공통 응답 래퍼 클래스(`ApiResponse<T>`)를 각 서비스마다 별도 정의한다 (공유 금지)

---

## @Transactional 경계

- `@Transactional`은 **`application/service` 레이어에서만** 선언한다
- `adapter/out/persistence` 레이어에서는 선언하지 않는다
- `adapter/in/web` 레이어(Controller)에서는 선언하지 않는다
- 조회 전용 메서드에는 `@Transactional(readOnly = true)`를 명시한다

```java
// 올바른 예 — application/service
@Transactional
public void register(RegisterMemberCommand command) { ... }

@Transactional(readOnly = true)
public MemberInfo findMember(Long memberId) { ... }

// 금지 — adapter/out/persistence 또는 Controller에서 선언하지 않음
```

---

## 레이어 간 매핑 전략

각 레이어 간 객체 변환은 **해당 레이어의 클래스 내부에 정의한 static 메서드**를 통해 수행한다.

### 도메인 ↔ JPA 엔티티 (`adapter/out/persistence`)

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

### 도메인 ↔ Response DTO (`adapter/in/web/dto`)

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

### Request DTO → 커맨드/도메인 (`adapter/in/web/dto`)

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

## 금지 사항

- `domain` 패키지에 `@Entity`, `@Repository`, Spring 어노테이션 사용 금지
- DTO를 서비스 간 공유 금지 (각 서비스에서 별도 정의)
- `main`, `develop` 브랜치 직접 push 금지
- `.env` 파일 커밋 금지 (`.gitignore`에 반드시 포함)
- `adapter/out/rest` 외부에서 타 서비스 직접 호출 금지
- 도메인 객체에서 JPA 엔티티 또는 DTO 타입 참조 금지
- `adapter/out/persistence` 또는 `adapter/in/web` 레이어에 `@Transactional` 선언 금지