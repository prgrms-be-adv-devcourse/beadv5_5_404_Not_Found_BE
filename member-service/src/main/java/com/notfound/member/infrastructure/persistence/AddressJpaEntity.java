package com.notfound.member.infrastructure.persistence;

import com.notfound.member.domain.model.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "address")
public class AddressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberJpaEntity member;

    @Column(length = 50)
    private String label;

    @Column(length = 100)
    private String recipient;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String zipcode;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static AddressJpaEntity from(Address address, MemberJpaEntity memberEntity) {
        AddressJpaEntity entity = new AddressJpaEntity();
        entity.id = address.getId();
        entity.member = memberEntity;
        entity.label = address.getLabel();
        entity.recipient = address.getRecipient();
        entity.phone = address.getPhone();
        entity.zipcode = address.getZipcode();
        entity.address1 = address.getAddress1();
        entity.address2 = address.getAddress2();
        entity.isDefault = address.isDefault();
        entity.isDeleted = address.isDeleted();
        return entity;
    }

    public Address toDomain() {
        return Address.builder()
                .id(this.id)
                .memberId(this.member.getId())
                .label(this.label)
                .recipient(this.recipient)
                .phone(this.phone)
                .zipcode(this.zipcode)
                .address1(this.address1)
                .address2(this.address2)
                .isDefault(this.isDefault)
                .isDeleted(this.isDeleted)
                .build();
    }
}
