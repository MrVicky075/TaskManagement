package com.company.usermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_mail_setting")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailSetting {

	public static final Long SINGLETON_ID = 1L;

	@Id
	private Long id;

	@Column(name = "cc_enabled")
	private Boolean ccEnabled;

	@Column(name = "mail_send_enabled")
	private Boolean mailSendEnabled;

	@Column(name = "audit_entry_enabled")
	private Boolean auditEntryEnabled;
}
