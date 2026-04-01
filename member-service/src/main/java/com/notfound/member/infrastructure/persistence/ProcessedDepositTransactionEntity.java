package com.notfound.member.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_deposit_transaction")
public class ProcessedDepositTransactionEntity {

    @Id
    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false)
    private int amount;

    @Column(name = "remaining_balance", nullable = false)
    private int remainingBalance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType { DEDUCT, CHARGE }

    protected ProcessedDepositTransactionEntity() {}

    public ProcessedDepositTransactionEntity(String transactionId, UUID memberId,
                                              TransactionType type, int amount, int remainingBalance) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.remainingBalance = remainingBalance;
        this.createdAt = LocalDateTime.now();
    }

    public String getTransactionId() { return transactionId; }
    public UUID getMemberId() { return memberId; }
    public TransactionType getType() { return type; }
    public int getAmount() { return amount; }
    public int getRemainingBalance() { return remainingBalance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
