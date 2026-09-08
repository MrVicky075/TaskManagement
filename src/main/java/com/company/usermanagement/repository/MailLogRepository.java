package com.company.usermanagement.repository;

import com.company.usermanagement.constraint.MailLogStatus;
import com.company.usermanagement.entity.MailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MailLogRepository extends JpaRepository<MailLog, Long> {

	@Query("""
			SELECT m FROM MailLog m
			WHERE (:templateCode IS NULL OR :templateCode = '' OR LOWER(m.templateCode) LIKE LOWER(CONCAT('%', :templateCode, '%')))
			  AND (:toEmail IS NULL OR :toEmail = '' OR LOWER(m.toEmail) LIKE LOWER(CONCAT('%', :toEmail, '%')))
			  AND (:status IS NULL OR m.status = :status)
			  AND (:dateFrom IS NULL OR m.createdAt >= :dateFrom)
			  AND (:dateTo IS NULL OR m.createdAt <= :dateTo)
			ORDER BY m.createdAt DESC
			""")
	Page<MailLog> searchMailLogs(
			@Param("templateCode") String templateCode,
			@Param("toEmail") String toEmail,
			@Param("status") MailLogStatus status,
			@Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo,
			Pageable pageable
	);
}
