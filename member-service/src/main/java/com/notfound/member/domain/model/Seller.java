package com.notfound.member.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Seller {

    private UUID id;
    private UUID memberId;
    private String businessNumber;
    private String shopName;
    private String bankCode;
    private String bankAccount;
    private String accountHolder;
    private BigDecimal commissionRate;
    private SellerStatus status;
    private LocalDateTime approvedAt;

    private Seller() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public String getShopName() {
        return shopName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void approve() {
        this.status = SellerStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = SellerStatus.SUSPENDED;
    }

    public static class Builder {
        private final Seller seller = new Seller();

        public Builder id(UUID id) {
            seller.id = id;
            return this;
        }

        public Builder memberId(UUID memberId) {
            seller.memberId = memberId;
            return this;
        }

        public Builder businessNumber(String businessNumber) {
            seller.businessNumber = businessNumber;
            return this;
        }

        public Builder shopName(String shopName) {
            seller.shopName = shopName;
            return this;
        }

        public Builder bankCode(String bankCode) {
            seller.bankCode = bankCode;
            return this;
        }

        public Builder bankAccount(String bankAccount) {
            seller.bankAccount = bankAccount;
            return this;
        }

        public Builder accountHolder(String accountHolder) {
            seller.accountHolder = accountHolder;
            return this;
        }

        public Builder commissionRate(BigDecimal commissionRate) {
            seller.commissionRate = commissionRate;
            return this;
        }

        public Builder status(SellerStatus status) {
            seller.status = status;
            return this;
        }

        public Builder approvedAt(LocalDateTime approvedAt) {
            seller.approvedAt = approvedAt;
            return this;
        }

        public Seller build() {
            return seller;
        }
    }
}
