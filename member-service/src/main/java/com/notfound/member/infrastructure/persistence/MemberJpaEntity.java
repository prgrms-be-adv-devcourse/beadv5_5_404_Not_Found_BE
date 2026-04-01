package com.notfound.member.infrastructure.persistence;

import com.notfound.member.domain.model.Member;
import com.notfound.member.domain.model.MemberRole;
import com.notfound.member.domain.model.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "member")
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "point_balance", nullable = false)
    private int pointBalance;

    @Column(name = "deposit_balance", nullable = false)
    private int depositBalance;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "member")
    private List<AddressJpaEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<RefreshTokenJpaEntity> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<SellerJpaEntity> sellers = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static MemberJpaEntity from(Member member) {
        MemberJpaEntity entity = new MemberJpaEntity();
        entity.id = member.getId();
        entity.email = member.getEmail();
        entity.passwordHash = member.getPasswordHash();
        entity.name = member.getName();
        entity.phone = member.getPhone();
        entity.role = member.getRole();
        entity.status = member.getStatus();
        entity.pointBalance = member.getPointBalance();
        entity.depositBalance = member.getDepositBalance();
        entity.emailVerified = member.isEmailVerified();
        entity.createdAt = member.getCreatedAt();
        entity.updatedAt = member.getUpdatedAt();
        entity.version = member.getVersion();
        return entity;
    }

    public Member toDomain() {
        return Member.builder()
                .id(this.id)
                .email(this.email)
                .passwordHash(this.passwordHash)
                .name(this.name)
                .phone(this.phone)
                .role(this.role)
                .status(this.status)
                .pointBalance(this.pointBalance)
                .depositBalance(this.depositBalance)
                .emailVerified(this.emailVerified)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .version(this.version)
                .build();
    }
}
