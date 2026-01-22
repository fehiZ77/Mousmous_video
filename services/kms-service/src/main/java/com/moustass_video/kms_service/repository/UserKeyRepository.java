package com.moustass_video.kms_service.repository;

import com.moustass_video.kms_service.entity.KeyStatus;
import com.moustass_video.kms_service.entity.UserKeys;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserKeyRepository extends JpaRepository<UserKeys, Long> {
    List<UserKeys> findByUserId(Long userId);

    Optional<UserKeys> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndKeyName(Long userId, String keyName);

    @Query("""
        SELECT uk FROM UserKeys uk
        WHERE uk.userId = :userId
          AND uk.status = :status
          AND (uk.expiredAt IS NULL OR uk.expiredAt > :now)
    """)
    List<UserKeys> findValidKeysByUserId(
            @Param("userId") Long userId,
            @Param("status") KeyStatus status,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE UserKeys uk
        SET uk.status = :newStatus
        WHERE uk.expiredAt IS NOT NULL
          AND uk.expiredAt <= :now
          AND uk.status <> :newStatus
    """)
    void expireOldKeys(
            @Param("newStatus") KeyStatus newStatus,
            @Param("now") LocalDateTime now
    );
}
