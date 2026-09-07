package com.company.usermanagement.mapper;

import com.company.usermanagement.dto.AuditDTO;
import com.company.usermanagement.entity.AuditEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditDTO toDTO(AuditEntity entity) {
        if (entity == null) {
            return null;
        }
        return AuditDTO.builder()
                .auditId(entity.getAuditId())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .description(entity.getDescription())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
