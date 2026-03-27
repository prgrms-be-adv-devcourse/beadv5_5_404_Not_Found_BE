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
        return new MemberException("MEMBER_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.");
    }

    public static MemberException notFound() {
        return new MemberException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다.");
    }

    public static MemberException invalidPassword() {
        return new MemberException("MEMBER_INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.");
    }

    public static MemberException invalidCredentials() {
        return new MemberException("MEMBER_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    public static MemberException inactiveAccount() {
        return new MemberException("MEMBER_INACTIVE_ACCOUNT", "비활성화된 계정입니다.");
    }

    public static MemberException invalidRefreshToken() {
        return new MemberException("MEMBER_INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다.");
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

    public static MemberException insufficientDeposit() {
        return new MemberException("MEMBER_INSUFFICIENT_DEPOSIT", "예치금 잔액이 부족합니다.");
    }

    public static MemberException invalidDepositAmount() {
        return new MemberException("MEMBER_INVALID_DEPOSIT_AMOUNT", "유효하지 않은 금액입니다.");
    }

    public static MemberException addressNotFound() {
        return new MemberException("ADDRESS_NOT_FOUND", "배송지를 찾을 수 없습니다.");
    }

    public static MemberException sellerAlreadyRegistered() {
        return new MemberException("SELLER_ALREADY_REGISTERED", "이미 판매자로 등록된 회원입니다.");
    }

    public static MemberException sellerNotPending() {
        return new MemberException("SELLER_NOT_PENDING", "대기 상태의 판매자만 승인할 수 있습니다.");
    }
}
