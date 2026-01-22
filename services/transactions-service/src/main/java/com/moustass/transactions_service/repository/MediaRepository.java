package com.moustass.transactions_service.repository;

import com.moustass.transactions_service.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByTransactionId(Long transactionId);
}
