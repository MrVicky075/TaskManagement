package com.company.usermanagement.repository;

import com.company.usermanagement.entity.MailSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailSettingRepository extends JpaRepository<MailSetting, Long> {
}
