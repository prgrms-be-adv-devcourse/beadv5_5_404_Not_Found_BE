package com.notfound.member.domain.exception;

public class MemberException extends RuntimeException {

    private final String code;

    public MemberException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static MemberException duplicateEmail() {
        return new MemberException("EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다.");
    }

    public static MemberException notFound() {
        return new MemberException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다.");
    }

    public static MemberException invalidPassword() {
        return new MemberException("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.");
    }

    public static MemberException invalidCredentials() {
        return new MemberException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    public static MemberException inactiveAccount() {
        return new MemberException("MEMBER_INACTIVE", "비활성화된 계정입니다.");
    }

    public static MemberException invalidRefreshToken() {
        return new MemberException("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다.");
    }

    public static MemberException tokenHijacked() {
        return new MemberException("MEMBER_TOKEN_HIJACKED", "토큰 탈취가 감지되었습니다. 다시 로그인해주세요.");
    }

    public static MemberException emailNotVerified() {
        return new MemberException("MEMBER_EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다.");
    }

    public static MemberException accessDenied() {
        return new MemberException("MEMBER_ACCESS_DENIED", "접근 권한이 없습니다.");
    }

    public static MemberException sellerNotApproved() {
        return new MemberException("MEMBER_SELLER_NOT_APPROVED", "승인된 판매자만 접근할 수 있습니다.");
    }

    public static MemberException alreadyWithdrawn() {
        return new MemberException("MEMBER_ALREADY_WITHDRAWN", "이미 탈퇴한 회원입니다.");
    }

    public static MemberException invalidAccessToken() {
        return new MemberException("MEMBER_INVALID_ACCESS_TOKEN", "유효하지 않은 액세스 토큰입니다.");
    }

    public static MemberException sellerNotFound() {
        return new MemberException("SELLER_NOT_FOUND", "판매자 정보를 찾을 수 없습니다.");
    }

    public static MemberException sellerApplicationNotFound() {
        return new MemberException("SELLER_APPLICATION_NOT_FOUND", "판매자 신청 정보를 찾을 수 없습니다.");
    }

    public static MemberException insufficientDeposit() {
        return new MemberException("MEMBER_INSUFFICIENT_DEPOSIT", "예치금 잔액이 부족합니다.");
    }

    public static MemberException invalidDepositAmount() {
        return new MemberException("MEMBER_INVALID_DEPOSIT_AMOUNT", "유효하지 않은 금액입니다.");
    }

    public static MemberException addressLimitExceeded() {
        return new MemberException("ADDRESS_LIMIT_EXCEEDED", "배송지는 최대 10개까지 등록할 수 있습니다.");
    }

    public static MemberException addressNotFound() {
        return new MemberException("ADDRESS_NOT_FOUND", "배송지를 찾을 수 없습니다.");
    }

    public static MemberException sellerAlreadyRegistered() {
        return new MemberException("SELLER_APPLICATION_ALREADY_EXISTS", "이미 판매자 신청이 존재합니다.");
    }

    public static MemberException sellerNotPending() {
        return new MemberException("INVALID_SELLER_STATUS", "유효하지 않은 판매자 상태값입니다. (허용값: APPROVED, SUSPENDED)");
    }
}
