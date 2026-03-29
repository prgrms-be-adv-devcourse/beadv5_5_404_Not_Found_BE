package com.notfound.member.domain.model;

import java.util.UUID;

public class Address {

    private UUID id;
    private UUID memberId;
    private String label;
    private String recipient;
    private String phone;
    private String zipcode;
    private String address1;
    private String address2;
    private boolean isDefault;
    private boolean isDeleted;

    private Address() {
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

    public String getLabel() {
        return label;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getPhone() {
        return phone;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void update(String recipient, String phone, String zipcode,
                       String address1, String address2, Boolean isDefault) {
        if (recipient != null && !recipient.isBlank()) {
            this.recipient = recipient;
        }
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
        if (zipcode != null && !zipcode.isBlank()) {
            this.zipcode = zipcode;
        }
        if (address1 != null && !address1.isBlank()) {
            this.address1 = address1;
        }
        if (address2 != null) {
            this.address2 = address2;
        }
        if (isDefault != null) {
            this.isDefault = isDefault;
        }
    }

    public static class Builder {
        private final Address address = new Address();

        public Builder id(UUID id) {
            address.id = id;
            return this;
        }

        public Builder memberId(UUID memberId) {
            address.memberId = memberId;
            return this;
        }

        public Builder label(String label) {
            address.label = label;
            return this;
        }

        public Builder recipient(String recipient) {
            address.recipient = recipient;
            return this;
        }

        public Builder phone(String phone) {
            address.phone = phone;
            return this;
        }

        public Builder zipcode(String zipcode) {
            address.zipcode = zipcode;
            return this;
        }

        public Builder address1(String address1) {
            address.address1 = address1;
            return this;
        }

        public Builder address2(String address2) {
            address.address2 = address2;
            return this;
        }

        public Builder isDefault(boolean isDefault) {
            address.isDefault = isDefault;
            return this;
        }

        public Builder isDeleted(boolean isDeleted) {
            address.isDeleted = isDeleted;
            return this;
        }

        public Address build() {
            return address;
        }
    }
}
