package com.company.usermanagement.repository;

import com.company.usermanagement.entity.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntity, Long> {

    @Query("""
            SELECT a FROM AuditEntity a
            WHERE (:username IS NULL OR :username = '' OR LOWER(a.userName) LIKE LOWER(CONCAT('%', :username, '%')))
              AND (:action IS NULL OR :action = '' OR :action = 'all' OR a.action = :action)
              AND (:entityType IS NULL OR :entityType = '' OR :entityType = 'all' OR a.entityType = :entityType)
              AND (:description IS NULL OR :description = '' OR LOWER(a.description) LIKE LOWER(CONCAT('%', :description, '%')))
              AND (:ipAddress IS NULL OR :ipAddress = '' OR LOWER(a.ipAddress) LIKE LOWER(CONCAT('%', :ipAddress, '%')))
              AND (:dateFrom IS NULL OR a.createdAt >= :dateFrom)
              AND (:dateTo IS NULL OR a.createdAt <= :dateTo)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditEntity> searchAudits(
            @Param("username") String username,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("description") String description,
            @Param("ipAddress") String ipAddress,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    boolean existsByUserIdAndActionAndCreatedAtAfter(Long userId, String action, LocalDateTime createdAt);
}
