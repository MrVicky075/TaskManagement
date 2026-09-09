package com.company.usermanagement.security;

import com.company.usermanagement.audit.AuditAction;
import com.company.usermanagement.audit.AuditEntityType;
import com.company.usermanagement.audit.AuditSnapshotUtil;
import com.company.usermanagement.entity.AuditEntity;
import com.company.usermanagement.repository.AuditRepository;
import com.company.usermanagement.service.MailSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogoutHandler implements LogoutHandler {

    private final AuditRepository auditRepository;
    private final MailSettingService mailSettingService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return;
        }
        if (!mailSettingService.isAuditEntryEnabled()) {
            log.info("Audit Entry is OFF - skipping logout audit");
            return;
        }
        try {
            AuditEntity audit = AuditEntity.builder()
                    .userId(userDetails.getUser().getUserId())
                    .userName(userDetails.getUser().getUserName())
                    .action(AuditAction.LOGOUT.name())
                    .entityType(AuditEntityType.USER.name())
                    .entityId(userDetails.getUser().getUserId())
                    .description(userDetails.getUser().getUserName() + " logged out")
                    .ipAddress(AuditSnapshotUtil.getClientIpAddress())
                    .build();
            auditRepository.save(audit);
        } catch (Exception ex) {
            log.error("Failed to write logout audit", ex);
        }
    }
}
