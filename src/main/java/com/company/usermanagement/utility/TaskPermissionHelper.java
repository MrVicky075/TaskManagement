package com.company.usermanagement.utility;

import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.entity.UserEntity.UserRole;
import com.company.usermanagement.session.UserLoginSession;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Edit permission for /dashboard and /tasks.
 *
 * Admin / Sr. Developer / Support → Edit on every task
 * Developer → Edit only when assignUserId == loginUserId OR assignee is "-" / unassigned
 */
@Component("taskPermissionHelper")
public class TaskPermissionHelper {

    public boolean canEdit(UserLoginSession session, TaskDTO task) {
        if (session == null || session.getRole() == null || task == null) {
            return false;
        }
        return canEdit(session.getRole(), session.getUserId(), task.getAssignUserId(), task.getAssignUserName());
    }

    public boolean canEdit(UserRole loggedInRole,
                           Long loggedInUserId,
                           Long assignUserId,
                           String assignUserName) {
        if (loggedInRole == null) {
            return false;
        }

        if (loggedInRole == UserRole.ADMIN
                || loggedInRole == UserRole.SR_DEVELOPER
                || loggedInRole == UserRole.SUPPORT) {
            return true;
        }

        if (loggedInRole == UserRole.DEVELOPER) {
            if (isUnassigned(assignUserId, assignUserName)) {
                return true;
            }
            return loggedInUserId != null && loggedInUserId.equals(assignUserId);
        }

        return false;
    }

    public void applyCanEdit(List<TaskDTO> tasks, UserLoginSession session) {
        if (tasks == null) {
            return;
        }
        for (TaskDTO task : tasks) {
            task.setCanEdit(canEdit(session, task));
        }
    }

    private boolean isUnassigned(Long assignUserId, String assignUserName) {
        if (assignUserId == null) {
            return true;
        }
        if (assignUserName == null) {
            return true;
        }
        String name = assignUserName.trim();
        return name.isEmpty() || "-".equals(name);
    }
}
