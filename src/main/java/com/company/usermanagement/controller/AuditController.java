package com.company.usermanagement.controller;

import com.company.usermanagement.audit.AuditAction;
import com.company.usermanagement.audit.AuditEntityType;
import com.company.usermanagement.dto.AuditDTO;
import com.company.usermanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/report")
    public String auditReport(
            @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @RequestParam(value = "action", required = false, defaultValue = "all") String action,
            @RequestParam(value = "entityType", required = false, defaultValue = "all") String entityType,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "ipAddress", required = false, defaultValue = "") String ipAddress,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model) {

        LocalDateTime fromDateTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime toDateTime = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;

        Page<AuditDTO> auditPage = auditService.searchAudits(
                username, action, entityType, description, ipAddress, fromDateTime, toDateTime, page, size);

        model.addAttribute("currentPage", "auditReport");
        model.addAttribute("auditPage", auditPage);
        model.addAttribute("audits", auditPage.getContent());
        model.addAttribute("selectedUsername", username);
        model.addAttribute("selectedAction", action);
        model.addAttribute("selectedEntityType", entityType);
        model.addAttribute("selectedDescription", description);
        model.addAttribute("selectedIpAddress", ipAddress);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("pageNumber", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("actions", auditActionOptions());
        model.addAttribute("entityTypes", auditEntityTypeOptions());
        return "audit/audit-report";
    }

    @GetMapping("/detail/{id}")
    public String auditDetail(@PathVariable Long id, Model model) {
        model.addAttribute("currentPage", "auditReport");
        model.addAttribute("audit", auditService.getAuditById(id));
        return "audit/audit-detail";
    }

    private List<String> auditActionOptions() {
        return Arrays.stream(AuditAction.values()).map(AuditAction::name).toList();
    }

    private List<String> auditEntityTypeOptions() {
        return Arrays.stream(AuditEntityType.values()).map(AuditEntityType::name).toList();
    }
}
