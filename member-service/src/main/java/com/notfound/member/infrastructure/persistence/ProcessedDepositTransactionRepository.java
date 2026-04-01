package com.notfound.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedDepositTransactionRepository extends JpaRepository<ProcessedDepositTransactionEntity, String> {
    Optional<ProcessedDepositTransactionEntity> findByTransactionId(String transactionId);
}
