package com.company.usermanagement.repository;

import com.company.usermanagement.entity.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {

	Optional<MailTemplate> findByTemplateCodeAndIsActive(String templateCode, Boolean isActive);

}
