package com.company.usermanagement.audit;

import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.dto.UserResponseDTO;
import com.company.usermanagement.entity.TaskEntity;
import com.company.usermanagement.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AuditSnapshotUtil {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "oldpassword", "newpassword", "confirmpassword",
            "token", "secret", "credential", "authorization"
    );

    private AuditSnapshotUtil() {
    }

    public static Map<String, Object> userSnapshot(UserEntity entity) {
        if (entity == null) {
            return Map.of();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", entity.getUserId());
        map.put("userName", entity.getUserName());
        map.put("email", entity.getEmail());
        map.put("mobileNo", entity.getMobileNo());
        map.put("role", entity.getRole() != null ? entity.getRole().name() : null);
        map.put("isActive", entity.getIsActive());
        return sanitize(map);
    }

    public static Map<String, Object> userSnapshot(UserResponseDTO dto) {
        if (dto == null) {
            return Map.of();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", dto.getUserId());
        map.put("userName", dto.getUserName());
        map.put("email", dto.getEmail());
        map.put("mobileNo", dto.getMobileNo());
        map.put("role", dto.getRole() != null ? dto.getRole().name() : null);
        map.put("isActive", dto.getIsActive());
        return sanitize(map);
    }

    public static Map<String, Object> taskSnapshot(TaskEntity entity) {
        if (entity == null) {
            return Map.of();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskId", entity.getTaskId());
        map.put("clientName", entity.getClientName());
        map.put("requirement", entity.getRequirement());
        map.put("taskDetails", entity.getTaskDetails());
        map.put("remarks", entity.getRemarks());
        map.put("priority", entity.getPriority());
        map.put("paid", entity.getPaid());
        map.put("status", entity.getStatus());
        map.put("issue", entity.getIssue());
        map.put("fixedOn", entity.getFixedOn());
        map.put("days", entity.getDays());
        map.put("redmineId", entity.getRedmineId());
        map.put("isActive", entity.getIsActive());
        if (entity.getAssignedUser() != null) {
            map.put("assignUserId", entity.getAssignedUser().getUserId());
            map.put("assignUserName", entity.getAssignedUser().getUserName());
        } else {
            map.put("assignUserId", null);
            map.put("assignUserName", null);
        }
        return sanitize(map);
    }

    public static Map<String, Object> taskSnapshot(TaskDTO dto) {
        if (dto == null) {
            return Map.of();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskId", dto.getTaskId());
        map.put("clientName", dto.getClientName());
        map.put("requirement", dto.getRequirement());
        map.put("taskDetails", dto.getTaskDetails());
        map.put("remarks", dto.getRemarks());
        map.put("priority", dto.getPriority());
        map.put("paid", dto.getPaid());
        map.put("status", dto.getStatus());
        map.put("issue", dto.getIssue());
        map.put("fixedOn", dto.getFixedOn());
        map.put("days", dto.getDays());
        map.put("redmineId", dto.getRedmineId());
        map.put("isActive", dto.getIsActive());
        map.put("assignUserId", dto.getAssignUserId());
        map.put("assignUserName", dto.getAssignUserName());
        return sanitize(map);
    }

    public static Map<String, Object> diff(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> changes = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            String key = entry.getKey();
            if (isSensitiveKey(key)) {
                continue;
            }
            Object newVal = entry.getValue();
            Object oldVal = before.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                changes.put(key, changeEntry(oldVal, newVal));
            }
        }
        return changes;
    }

    public static Map<String, Object> changeEntry(Object oldVal, Object newVal) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("old", oldVal);
        entry.put("new", newVal);
        return entry;
    }

    public static Map<String, Object> singleValueMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    public static String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }

    private static Map<String, Object> sanitize(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (!isSensitiveKey(entry.getKey())) {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    private static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase());
    }
}
