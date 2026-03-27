package com.notfound.member.domain.model;

import com.notfound.member.domain.exception.MemberException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Member {

    private UUID id;
    private String email;
    private String passwordHash;
    private String name;
    private String phone;
    private MemberRole role;
    private MemberStatus status;
    private int pointBalance;
    private int depositBalance;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Member() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public MemberRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public int getPointBalance() {
        return pointBalance;
    }

    public int getDepositBalance() {
        return depositBalance;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void deductDeposit(int amount) {
        if (amount <= 0) {
            throw MemberException.invalidDepositAmount();
        }
        if (this.depositBalance < amount) {
            throw MemberException.insufficientDeposit();
        }
        this.depositBalance -= amount;
    }

    public void chargeDeposit(int amount) {
        if (amount <= 0) {
            throw MemberException.invalidDepositAmount();
        }
        this.depositBalance += amount;
    }

    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw MemberException.alreadyWithdrawn();
        }
        this.status = MemberStatus.WITHDRAWN;
    }

    public static class Builder {
        private final Member member = new Member();

        public Builder id(UUID id) {
            member.id = id;
            return this;
        }

        public Builder email(String email) {
            member.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            member.passwordHash = passwordHash;
            return this;
        }

        public Builder name(String name) {
            member.name = name;
            return this;
        }

        public Builder phone(String phone) {
            member.phone = phone;
            return this;
        }

        public Builder role(MemberRole role) {
            member.role = role;
            return this;
        }

        public Builder status(MemberStatus status) {
            member.status = status;
            return this;
        }

        public Builder pointBalance(int pointBalance) {
            member.pointBalance = pointBalance;
            return this;
        }

        public Builder depositBalance(int depositBalance) {
            member.depositBalance = depositBalance;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            member.emailVerified = emailVerified;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            member.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            member.updatedAt = updatedAt;
            return this;
        }

        public Member build() {
            return member;
        }
    }
}
