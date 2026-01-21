package com.daniax.auth_service.repository;

import com.daniax.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);

    @Query("""
        SELECT u FROM User u
        WHERE u.id <> :userId
          AND u.role <> 'ADMIN'
    """)
    List<User> findOtherNonAdminUsers(@Param("userId") Long userId);
}
