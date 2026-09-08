package com.company.usermanagement.repository;


import com.company.usermanagement.entity.MailLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailLogRepository extends JpaRepository<MailLog, Long> {
}
