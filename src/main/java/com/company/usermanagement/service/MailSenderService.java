package com.company.usermanagement.service;

import com.company.usermanagement.config.MailConfig;
import com.company.usermanagement.exception.MailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MailSenderService {

	private static final Logger log = LoggerFactory.getLogger(MailSenderService.class);

	private final JavaMailSender mailSender;
	private final MailConfig mailConfig;

	public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
		sendHtmlEmail(toEmail, resolveConfiguredCc(), subject, htmlBody);
	}

	public void sendHtmlEmail(String toEmail, String ccEmail, String subject, String htmlBody) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(mailConfig.getFrom());
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);

			String[] ccList = parseCcEmails(ccEmail, toEmail);
			if (ccList.length > 0) {
				helper.setCc(ccList);
			}

			mailSender.send(message);
			log.info("Email sent successfully to {} (cc={})", toEmail,
					ccList.length > 0 ? String.join(", ", ccList) : "none");
		} catch (MessagingException ex) {
			throw new MailSendingException("Failed to create email message", ex);
		} catch (Exception ex) {
			throw new MailSendingException("SMTP connection failed", ex);
		}
	}

	/**
	 * Returns configured CC as a single comma-separated string (for logging / mail_log).
	 */
	public String resolveConfiguredCc() {
		if (mailConfig.getNotification() == null) {
			return null;
		}
		String cc = mailConfig.getNotification().getCc();
		return StringUtils.hasText(cc) ? cc.trim() : null;
	}

	/**
	 * Splits comma-separated CC emails, trims blanks, de-duplicates,
	 * and skips any address that matches the To recipient.
	 */
	private String[] parseCcEmails(String ccEmail, String toEmail) {
		if (!StringUtils.hasText(ccEmail)) {
			return new String[0];
		}
		Set<String> unique = new LinkedHashSet<>();
		Arrays.stream(ccEmail.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.filter(email -> toEmail == null || !email.equalsIgnoreCase(toEmail.trim()))
				.forEach(unique::add);
		return unique.toArray(new String[0]);
	}

}
