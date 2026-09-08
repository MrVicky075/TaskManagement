package com.company.usermanagement.entity;


import com.company.usermanagement.constraint.MailLogStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_mail_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "template_code", nullable = false, length = 100)
	private String templateCode;

	@Column(name = "to_email", nullable = false, length = 255)
	private String toEmail;

	@Column(name = "cc_email", length = 500)
	private String ccEmail;

	@Column(name = "from_email", nullable = false, length = 255)
	private String fromEmail;

	@Column(nullable = false, length = 500)
	private String subject;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MailLogStatus status;

	@Column(name = "error_message", length = 2000)
	private String errorMessage;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

}
