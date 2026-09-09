package com.company.usermanagement.service;

import com.company.usermanagement.entity.MailSetting;
import com.company.usermanagement.repository.MailSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailSettingService {

	private final MailSettingRepository mailSettingRepository;

	@Transactional(readOnly = true)
	public boolean isCcEnabled() {
		return Boolean.TRUE.equals(getOrDefault().getCcEnabled());
	}

	@Transactional(readOnly = true)
	public boolean isMailSendEnabled() {
		return Boolean.TRUE.equals(getOrDefault().getMailSendEnabled());
	}

	@Transactional(readOnly = true)
	public boolean isAuditEntryEnabled() {
		return Boolean.TRUE.equals(getOrDefault().getAuditEntryEnabled());
	}

	@Transactional
	public boolean setCcEnabled(boolean enabled) {
		MailSetting setting = getOrCreate();
		setting.setCcEnabled(enabled);
		mailSettingRepository.save(setting);
		return enabled;
	}

	@Transactional
	public boolean setMailSendEnabled(boolean enabled) {
		MailSetting setting = getOrCreate();
		setting.setMailSendEnabled(enabled);
		mailSettingRepository.save(setting);
		return enabled;
	}

	@Transactional
	public boolean setAuditEntryEnabled(boolean enabled) {
		MailSetting setting = getOrCreate();
		setting.setAuditEntryEnabled(enabled);
		mailSettingRepository.save(setting);
		return enabled;
	}

	private MailSetting getOrDefault() {
		return mailSettingRepository.findById(MailSetting.SINGLETON_ID)
				.map(this::normalize)
				.orElse(defaultSetting());
	}

	private MailSetting getOrCreate() {
		return mailSettingRepository.findById(MailSetting.SINGLETON_ID)
				.map(this::normalize)
				.orElseGet(this::defaultSetting);
	}

	private MailSetting defaultSetting() {
		return MailSetting.builder()
				.id(MailSetting.SINGLETON_ID)
				.ccEnabled(true)
				.mailSendEnabled(true)
				.auditEntryEnabled(true)
				.build();
	}

	private MailSetting normalize(MailSetting setting) {
		if (setting.getCcEnabled() == null) {
			setting.setCcEnabled(true);
		}
		if (setting.getMailSendEnabled() == null) {
			setting.setMailSendEnabled(true);
		}
		if (setting.getAuditEntryEnabled() == null) {
			setting.setAuditEntryEnabled(true);
		}
		return setting;
	}
}
