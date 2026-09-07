package com.company.usermanagement.service;

import com.company.usermanagement.audit.AuditAction;
import com.company.usermanagement.audit.AuditEntityType;
import com.company.usermanagement.audit.AuditSnapshotUtil;
import com.company.usermanagement.dto.AuditDTO;
import com.company.usermanagement.entity.AuditEntity;
import com.company.usermanagement.exception.ResourceNotFoundException;
import com.company.usermanagement.mapper.AuditMapper;
import com.company.usermanagement.repository.AuditRepository;
import com.company.usermanagement.session.UserLoginSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;
    private final UserLoginSession userLoginSession;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action,
                    AuditEntityType entityType,
                    Long entityId,
                    String description,
                    Object oldValue,
                    Object newValue) {
        try {
            AuditEntity audit = AuditEntity.builder()
                    .userId(userLoginSession.getUserId())
                    .userName(userLoginSession.getUserName())
                    .action(action.name())
                    .entityType(entityType.name())
                    .entityId(entityId)
                    .description(description)
                    .oldValue(toJson(oldValue))
                    .newValue(toJson(newValue))
                    .ipAddress(AuditSnapshotUtil.getClientIpAddress())
                    .build();
            auditRepository.save(audit);
        } catch (Exception ex) {
            log.error("Failed to write audit log: action={}, entityType={}, entityId={}",
                    action, entityType, entityId, ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogin(Long userId, String userName) {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusSeconds(5);
            if (auditRepository.existsByUserIdAndActionAndCreatedAtAfter(
                    userId, AuditAction.LOGIN.name(), cutoff)) {
                return;
            }

            AuditEntity audit = AuditEntity.builder()
                    .userId(userId)
                    .userName(userName)
                    .action(AuditAction.LOGIN.name())
                    .entityType(AuditEntityType.USER.name())
                    .entityId(userId)
                    .description(userName + " logged in")
                    .ipAddress(AuditSnapshotUtil.getClientIpAddress())
                    .build();
            auditRepository.save(audit);
        } catch (Exception ex) {
            log.error("Failed to write login audit for userId={}", userId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditDTO> searchAudits(String username,
                                       String action,
                                       String entityType,
                                       LocalDateTime dateFrom,
                                       LocalDateTime dateTo,
                                       int page,
                                       int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return auditRepository.searchAudits(username, action, entityType, dateFrom, dateTo, pageable)
                .map(auditMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditDTO getAuditById(Long auditId) {
        return auditRepository.findById(auditId)
                .map(auditMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Audit record not found: " + auditId));
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }
        try {
            if (value instanceof Map<?, ?> map && map.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialize audit value", ex);
            return String.valueOf(value);
        }
    }
}
