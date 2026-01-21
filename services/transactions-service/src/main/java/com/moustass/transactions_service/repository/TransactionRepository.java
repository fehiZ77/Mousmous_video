package com.moustass.transactions_service.repository;

import com.moustass.transactions_service.entity.Transaction;
import com.moustass.transactions_service.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByOwnerId(Long ownerId);
    List<Transaction> findByRecipientId(Long recipientId);

    @Modifying
    @Query("""
        UPDATE Transaction t
        SET t.status = :status
        WHERE t.id = :transactionId
    """)
    void updateStatus(
            @Param("transactionId") Long transactionId,
            @Param("status") TransactionStatus status
    );
}
