package com.company.usermanagement.service;

import com.company.usermanagement.audit.AuditSnapshotUtil;
import com.company.usermanagement.config.MailConfig;
import com.company.usermanagement.constraint.MailLogStatus;
import com.company.usermanagement.constraint.MailTemplateCode;
import com.company.usermanagement.entity.MailLog;
import com.company.usermanagement.entity.MailTemplate;
import com.company.usermanagement.entity.TaskEntity;
import com.company.usermanagement.entity.UserEntity;
import com.company.usermanagement.exception.MailSendingException;
import com.company.usermanagement.exception.MailTemplateNotFoundException;
import com.company.usermanagement.repository.MailLogRepository;
import com.company.usermanagement.utility.ChangeDetectionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

	private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

	private final MailTemplateService mailTemplateService;
	private final MailSenderService mailSenderService;
	private final MailLogRepository mailLogRepository;
	private final MailConfig mailConfig;
	private final ChangeDetectionService changeDetectionService;
	private final MailSettingService mailSettingService;

	@Override
	public void sendUserRegistrationEmail(UserEntity user) {
		log.info("Mail notification started: USER_REGISTERED for userId={}", user.getUserId());
		Map<String, Object> variables = new HashMap<>();
		variables.put("userName", nullSafe(user.getUserName()));
		variables.put("email", nullSafe(user.getEmail()));
		variables.put("userId", user.getUserId());

		sendTemplateEmail(MailTemplateCode.USER_REGISTERED, user.getEmail(), variables);
	}

	@Override
	public void sendUserUpdatedEmail(Map<String, Object> oldSnapshot, UserEntity updatedUser) {
		log.info("Mail notification started: USER_UPDATED for userId={}", updatedUser.getUserId());
		Map<String, Object> newSnapshot = AuditSnapshotUtil.userSnapshot(updatedUser);
		String changesHtml = changeDetectionService.buildChangesHtml(oldSnapshot, newSnapshot);

		Map<String, Object> variables = new HashMap<>();
		variables.put("userName", nullSafe(updatedUser.getUserName()));
		variables.put("email", nullSafe(updatedUser.getEmail()));
		variables.put("userId", updatedUser.getUserId());
		variables.put("changes", changesHtml);

		sendTemplateEmail(MailTemplateCode.USER_UPDATED, updatedUser.getEmail(), variables);
	}

	@Override
	public void sendTaskCreatedEmail(TaskEntity task) {
		log.info("Mail notification started: TASK_CREATED for taskId={}", task.getTaskId());
		UserEntity assignedUser = task.getAssignedUser();
		if (assignedUser == null || !StringUtils.hasText(assignedUser.getEmail())) {
			log.warn("Skipping TASK_CREATED email for taskId={} - no assigned user email", task.getTaskId());
			return;
		}

		Map<String, Object> variables = buildTaskVariables(task, assignedUser);
		sendTemplateEmail(MailTemplateCode.TASK_CREATED, assignedUser.getEmail(), variables);
	}

	@Override
	public void sendTaskUpdatedEmail(Map<String, Object> oldSnapshot, TaskEntity updatedTask) {
		log.info("Mail notification started: TASK_UPDATED for taskId={}", updatedTask.getTaskId());
		UserEntity assignedUser = updatedTask.getAssignedUser();
		if (assignedUser == null || !StringUtils.hasText(assignedUser.getEmail())) {
			log.warn("Skipping TASK_UPDATED email for taskId={} - no assigned user email", updatedTask.getTaskId());
			return;
		}

		Map<String, Object> newSnapshot = AuditSnapshotUtil.taskSnapshot(updatedTask);
		if (!changeDetectionService.hasChanges(oldSnapshot, newSnapshot)) {
			log.info("Skipping TASK_UPDATED email for taskId={} - no field changes", updatedTask.getTaskId());
			return;
		}

		Map<String, Object> variables = buildTaskVariables(updatedTask, assignedUser);
		variables.put("changes", changeDetectionService.buildChangesHtml(oldSnapshot, newSnapshot));
		sendTemplateEmail(MailTemplateCode.TASK_UPDATED, assignedUser.getEmail(), variables);
	}

	@Override
	public void sendTaskDeletedEmail(TaskEntity task) {
		log.info("Mail notification started: TASK_DELETED for taskId={}", task.getTaskId());
		UserEntity assignedUser = task.getAssignedUser();
		if (assignedUser == null || !StringUtils.hasText(assignedUser.getEmail())) {
			log.warn("Skipping TASK_DELETED email for taskId={} - no assigned user email", task.getTaskId());
			return;
		}

		Map<String, Object> variables = buildTaskVariables(task, assignedUser);
		sendTemplateEmail(MailTemplateCode.TASK_DELETED, assignedUser.getEmail(), variables);
	}

	private Map<String, Object> buildTaskVariables(TaskEntity task, UserEntity assignedUser) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("taskId", task.getTaskId());
		variables.put("taskName", nullSafe(task.getRequirement()));
		variables.put("requirement", nullSafe(task.getRequirement()));
		variables.put("taskDetails", nullSafe(task.getTaskDetails()));
		variables.put("remarks", nullSafe(task.getRemarks()));
		variables.put("priority", nullSafe(task.getPriority()));
		variables.put("status", nullSafe(task.getStatus()));
		variables.put("clientName", nullSafe(task.getClientName()));
		variables.put("issue", nullSafe(task.getIssue()));
		variables.put("fixedOn", nullSafe(task.getFixedOn()));
		variables.put("days", task.getDays() != null ? task.getDays() : 0);
		variables.put("redmineId", task.getRedmineId() != null ? task.getRedmineId() : 0);
		variables.put("assignedUser", assignedUser != null ? nullSafe(assignedUser.getUserName()) : "-");
		variables.put("assignedUserEmail", assignedUser != null ? nullSafe(assignedUser.getEmail()) : "");
		return variables;
	}

	private void sendTemplateEmail(String templateCode, String toEmail, Map<String, Object> variables) {
		if (!mailSettingService.isMailSendEnabled()) {
			log.info("Mail Send is OFF - skipping email for template {} to {}", templateCode, toEmail);
			return;
		}

		if (!StringUtils.hasText(toEmail)) {
			log.warn("Skipping email for template {} - recipient email is empty", templateCode);
			return;
		}

		String ccEmail = mailSenderService.resolveConfiguredCc();
		String subject = null;
		MailLog mailLog = MailLog.builder()
				.templateCode(templateCode)
				.toEmail(toEmail)
				.ccEmail(ccEmail)
				.fromEmail(mailConfig.getFrom())
				.subject("")
				.status(MailLogStatus.FAILED)
				.build();

		try {
			log.info("Loading mail template {}", templateCode);
			MailTemplate template = mailTemplateService.getActiveTemplate(templateCode);
			subject = mailTemplateService.resolveSubject(template, variables);
			String body = mailTemplateService.resolveBody(template, variables);
			mailLog.setSubject(subject);

			log.info("Recipient resolved for {}: to={}, cc={}", templateCode, toEmail, ccEmail);
			mailSenderService.sendHtmlEmail(toEmail, ccEmail, subject, body);

			mailLog.setStatus(MailLogStatus.SENT);
			mailLog.setSentAt(LocalDateTime.now());
			log.info("Mail sent successfully using template {}", templateCode);
		} catch (MailTemplateNotFoundException ex) {
			log.error("Mail template not found: {}", templateCode, ex);
			mailLog.setErrorMessage(ex.getMessage());
			mailLog.setSubject(subject != null ? subject : templateCode);
			mailLogRepository.save(mailLog);
			throw ex;
		} catch (MailSendingException ex) {
			log.error("Mail sending failed for template {} to {}", templateCode, toEmail, ex);
			mailLog.setErrorMessage(ex.getMessage());
			mailLog.setSubject(subject != null ? subject : templateCode);
			mailLogRepository.save(mailLog);
			throw ex;
		} catch (Exception ex) {
			log.error("Unexpected mail failure for template {} to {}", templateCode, toEmail, ex);
			mailLog.setErrorMessage(ex.getMessage());
			mailLog.setSubject(subject != null ? subject : templateCode);
			mailLogRepository.save(mailLog);
			throw new MailSendingException("Email notification failed", ex);
		}

		mailLogRepository.save(mailLog);
	}

	private static String nullSafe(String value) {
		return value != null ? value : "-";
	}

}
