package com.company.usermanagement.audit;

import com.company.usermanagement.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditSnapshotUtilTest {

    @Test
    void userSnapshot_excludesPassword() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUserName("Vikas");
        user.setEmail("vikas@test.com");
        user.setPassword("secret-hash");
        user.setIsActive(true);
        user.setRole(UserEntity.UserRole.ADMIN);

        Map<String, Object> snapshot = AuditSnapshotUtil.userSnapshot(user);

        assertEquals("Vikas", snapshot.get("userName"));
        assertFalse(snapshot.containsKey("password"));
    }

    @Test
    void diff_detectsChangedFields() {
        Map<String, Object> before = Map.of("status", "Pending", "priority", "P1");
        Map<String, Object> after = Map.of("status", "Complete", "priority", "P1");

        Map<String, Object> changes = AuditSnapshotUtil.diff(before, after);

        assertEquals(1, changes.size());
        assertTrue(changes.containsKey("status"));
    }

    @Test
    void diff_ignoresSensitiveKeys() {
        Map<String, Object> before = Map.of("password", "old");
        Map<String, Object> after = Map.of("password", "new");

        Map<String, Object> changes = AuditSnapshotUtil.diff(before, after);

        assertTrue(changes.isEmpty());
    }

    @Test
    void diff_allowsNullValues() {
        Map<String, Object> before = Map.of("status", "Pending");
        Map<String, Object> after = new java.util.LinkedHashMap<>();
        after.put("status", null);

        Map<String, Object> changes = AuditSnapshotUtil.diff(before, after);

        assertEquals(1, changes.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) changes.get("status");
        assertEquals("Pending", entry.get("old"));
        assertNull(entry.get("new"));
    }
}
