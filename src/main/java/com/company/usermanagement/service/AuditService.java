package com.company.usermanagement.service;

import com.company.usermanagement.audit.AuditAction;
import com.company.usermanagement.audit.AuditEntityType;
import com.company.usermanagement.dto.AuditDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public interface AuditService {

    void log(AuditAction action,
             AuditEntityType entityType,
             Long entityId,
             String description,
             Object oldValue,
             Object newValue);

    void logLogin(Long userId, String userName);

    Page<AuditDTO> searchAudits(String username,
                                String action,
                                String entityType,
                                LocalDateTime dateFrom,
                                LocalDateTime dateTo,
                                int page,
                                int size);

    AuditDTO getAuditById(Long auditId);
}
