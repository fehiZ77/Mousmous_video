package com.moustass.notification_service.repository;

import com.moustass.notification_service.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceivedId(Long userId);

    List<Notification> findByReceivedIdAndDateSeenAtIsNull(Long receivedId);

    @Modifying
    @Transactional
    @Query("""
                UPDATE Notification n
                SET n.dateSeenAt = CURRENT_TIMESTAMP
                WHERE n.receivedId = :userId
                  AND n.dateSeenAt IS NULL
            """)
    int markAllAsSeen(@Param("userId") Long userId);

    List<Notification> findByReceivedIdOrderByIdDesc(Long userId);
}

