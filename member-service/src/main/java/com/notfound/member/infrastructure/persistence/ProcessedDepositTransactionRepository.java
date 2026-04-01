package com.notfound.member.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedDepositTransactionRepository extends JpaRepository<ProcessedDepositTransactionEntity, String> {
}
