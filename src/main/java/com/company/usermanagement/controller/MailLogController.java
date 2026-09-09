package com.company.usermanagement.controller;

import com.company.usermanagement.constraint.MailLogStatus;
import com.company.usermanagement.entity.MailLog;
import com.company.usermanagement.exception.ResourceNotFoundException;
import com.company.usermanagement.repository.MailLogRepository;
import com.company.usermanagement.service.MailSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailLogController {

	private final MailLogRepository mailLogRepository;
	private final MailSettingService mailSettingService;

	@GetMapping("/report")
	public String mailReport(
			@RequestParam(value = "templateCode", required = false, defaultValue = "") String templateCode,
			@RequestParam(value = "toEmail", required = false, defaultValue = "") String toEmail,
			@RequestParam(value = "status", required = false, defaultValue = "all") String status,
			@RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			Model model) {

		LocalDateTime fromDateTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
		LocalDateTime toDateTime = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
		MailLogStatus statusFilter = parseStatus(status);

		Page<MailLog> mailPage = mailLogRepository.searchMailLogs(
				templateCode,
				toEmail,
				statusFilter,
				fromDateTime,
				toDateTime,
				PageRequest.of(Math.max(page, 0), Math.max(size, 1))
		);

		model.addAttribute("currentPage", "mailAuditReport");
		model.addAttribute("mailPage", mailPage);
		model.addAttribute("mailLogs", mailPage.getContent());
		model.addAttribute("selectedTemplateCode", templateCode);
		model.addAttribute("selectedToEmail", toEmail);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("dateFrom", dateFrom);
		model.addAttribute("dateTo", dateTo);
		model.addAttribute("pageNumber", page);
		model.addAttribute("pageSize", size);
		model.addAttribute("statusOptions", statusOptions());
		model.addAttribute("ccEnabled", mailSettingService.isCcEnabled());
		model.addAttribute("mailSendEnabled", mailSettingService.isMailSendEnabled());
		model.addAttribute("auditEntryEnabled", mailSettingService.isAuditEntryEnabled());
		return "mail/mail-report";
	}

	@PostMapping("/cc-toggle")
	public String toggleCc(
			@RequestParam("enabled") boolean enabled,
			RedirectAttributes redirectAttributes) {
		boolean ccEnabled = mailSettingService.setCcEnabled(enabled);
		redirectAttributes.addFlashAttribute("toggleMessage",
				ccEnabled ? "Mail CC is ON. Notifications will include CC recipients."
						: "Mail CC is OFF. Notifications will be sent To-only (no CC).");
		return "redirect:/mail/report";
	}

	@PostMapping("/send-toggle")
	public String toggleMailSend(
			@RequestParam("enabled") boolean enabled,
			RedirectAttributes redirectAttributes) {
		boolean mailSendEnabled = mailSettingService.setMailSendEnabled(enabled);
		redirectAttributes.addFlashAttribute("toggleMessage",
				mailSendEnabled ? "Mail Send is ON. Notification emails will be sent."
						: "Mail Send is OFF. Notification emails will not be sent.");
		return "redirect:/mail/report";
	}

	@PostMapping("/audit-toggle")
	public String toggleAuditEntry(
			@RequestParam("enabled") boolean enabled,
			RedirectAttributes redirectAttributes) {
		boolean auditEntryEnabled = mailSettingService.setAuditEntryEnabled(enabled);
		redirectAttributes.addFlashAttribute("toggleMessage",
				auditEntryEnabled ? "Audit Entry is ON. Audit records will be inserted."
						: "Audit Entry is OFF. Audit records will not be inserted.");
		return "redirect:/mail/report";
	}

	@GetMapping("/detail/{id}")
	public String mailDetail(@PathVariable Long id, Model model) {
		MailLog mailLog = mailLogRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Mail log not found: " + id));
		model.addAttribute("currentPage", "mailAuditReport");
		model.addAttribute("mailLog", mailLog);
		return "mail/mail-detail";
	}

	private MailLogStatus parseStatus(String status) {
		if (!StringUtils.hasText(status) || "all".equalsIgnoreCase(status)) {
			return null;
		}
		try {
			return MailLogStatus.valueOf(status.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private List<String> statusOptions() {
		return Arrays.stream(MailLogStatus.values()).map(Enum::name).toList();
	}
}
