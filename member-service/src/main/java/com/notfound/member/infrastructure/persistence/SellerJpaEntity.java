package com.notfound.member.infrastructure.persistence;

import com.notfound.member.domain.model.Seller;
import com.notfound.member.domain.model.SellerStatus;
import com.notfound.member.infrastructure.encryption.AesEncryptionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seller", indexes = {
        @Index(name = "idx_seller_member_id", columnList = "member_id")
})
public class SellerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private MemberJpaEntity member;

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(name = "shop_name", nullable = false, length = 100)
    private String shopName;

    @Convert(converter = AesEncryptionConverter.class)
    @Column(name = "bank_code", length = 255)
    private String bankCode;

    @Convert(converter = AesEncryptionConverter.class)
    @Column(name = "bank_account", length = 255)
    private String bankAccount;

    @Convert(converter = AesEncryptionConverter.class)
    @Column(name = "account_holder", length = 255)
    private String accountHolder;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public static SellerJpaEntity from(Seller seller, MemberJpaEntity memberEntity) {
        SellerJpaEntity entity = new SellerJpaEntity();
        entity.id = seller.getId();
        entity.member = memberEntity;
        entity.businessNumber = seller.getBusinessNumber();
        entity.shopName = seller.getShopName();
        entity.bankCode = seller.getBankCode();
        entity.bankAccount = seller.getBankAccount();
        entity.accountHolder = seller.getAccountHolder();
        entity.commissionRate = seller.getCommissionRate();
        entity.status = seller.getStatus();
        entity.approvedAt = seller.getApprovedAt();
        return entity;
    }

    public Seller toDomain() {
        return Seller.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .businessNumber(this.businessNumber)
                .shopName(this.shopName)
                .bankCode(this.bankCode)
                .bankAccount(this.bankAccount)
                .accountHolder(this.accountHolder)
                .commissionRate(this.commissionRate)
                .status(this.status)
                .approvedAt(this.approvedAt)
                .build();
    }
}
