package com.company.usermanagement.service;

import com.company.usermanagement.audit.AuditAction;
import com.company.usermanagement.audit.AuditEntityType;
import com.company.usermanagement.audit.AuditSnapshotUtil;
import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.entity.TaskEntity;
import com.company.usermanagement.entity.UserEntity;
import com.company.usermanagement.exception.ResourceNotFoundException;
import com.company.usermanagement.mapper.TaskMapper;
import com.company.usermanagement.repository.TaskRepository;
import com.company.usermanagement.session.UserLoginSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskMapper mapper;
    private final UserLoginSession userLoginSession;
    private final AuditService auditService;
    private final MailService mailService;

    @Override
    @Transactional
    public TaskDTO saveTask(TaskDTO taskDTO) {
        Long currentUser = userLoginSession.getUserId();
        taskDTO.setCreateBy(currentUser);
        taskDTO.setUpdatedBy(currentUser);
        taskDTO.setCreateAt(LocalDateTime.now());
        taskDTO.setUpdateAt(LocalDateTime.now());
        taskDTO.setIsActive(true);
        TaskEntity saved = taskRepository.save(mapper.toEntity(taskDTO));
        // Ensure assigned user is available for mail
        if (saved.getAssignedUser() != null) {
            saved.getAssignedUser().getEmail();
        }
        log.info("Task creation completed for taskId={}", saved.getTaskId());

        auditService.log(
                AuditAction.CREATE,
                AuditEntityType.TASK,
                saved.getTaskId(),
                "Created task #" + saved.getTaskId(),
                null,
                AuditSnapshotUtil.taskSnapshot(saved)
        );

        try {
            mailService.sendTaskCreatedEmail(saved);
        } catch (Exception ex) {
            log.error("Task id={} created but notification email failed", saved.getTaskId(), ex);
        }

        return mapper.toDTO(saved);
    }

    @Transactional
    public List<TaskEntity> saveAll(List<TaskEntity> tasks) {
        List<TaskEntity> saved = taskRepository.saveAll(tasks);
        auditService.log(
                AuditAction.BULK_CREATE,
                AuditEntityType.TASK,
                null,
                "Bulk imported " + saved.size() + " tasks",
                null,
                AuditSnapshotUtil.singleValueMap("count", saved.size())
        );
        return saved;
    }

    @Override
    public TaskDTO getTaskById(Long taskId) {
        return mapper.toDTO(taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId)));
    }

    @Override
    public List<TaskDTO> getAllTasks() {
        return mapper.toDTOList(taskRepository.getAllTask());
    }

    @Override
    public List<TaskDTO> getMyAllTasks(List<Long> userIds) {
        return mapper.toDTOList(taskRepository.getMyAllTask(userIds));
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long taskId, TaskDTO taskDTO) {
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        // Touch lazy relation before snapshot/mail
        if (existingTask.getAssignedUser() != null) {
            existingTask.getAssignedUser().getEmail();
        }

        Map<String, Object> before = AuditSnapshotUtil.taskSnapshot(existingTask);

        taskDTO.setCreateBy(existingTask.getCreatedBy());
        taskDTO.setUpdatedBy(userLoginSession.getUserId());
        taskDTO.setCreateAt(existingTask.getCreateAt());
        taskDTO.setUpdateAt(LocalDateTime.now());
        if (taskDTO.getIsActive() == null) {
            taskDTO.setIsActive(existingTask.getIsActive());
        }
        mapper.updateTaskEntity(taskDTO, existingTask);
        TaskEntity saved = taskRepository.save(existingTask);
        if (saved.getAssignedUser() != null) {
            saved.getAssignedUser().getEmail();
        }
        Map<String, Object> after = AuditSnapshotUtil.taskSnapshot(saved);
        log.info("Task update completed for taskId={}", saved.getTaskId());

        logTaskUpdateAudit(taskId, before, after);

        try {
            mailService.sendTaskUpdatedEmail(before, saved);
        } catch (Exception ex) {
            log.error("Task id={} updated but notification email failed", saved.getTaskId(), ex);
        }

        return mapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Capture assigned user + details BEFORE soft-delete
        UserEntity assignedUser = existingTask.getAssignedUser();
        if (assignedUser != null) {
            assignedUser.getEmail();
            assignedUser.getUserName();
        }
        Map<String, Object> before = AuditSnapshotUtil.taskSnapshot(existingTask);

        taskRepository.deleteTaskById(taskId);
        log.info("Task deletion completed for taskId={}", taskId);

        auditService.log(
                AuditAction.DELETE,
                AuditEntityType.TASK,
                taskId,
                "Deleted task #" + taskId,
                before,
                AuditSnapshotUtil.singleValueMap("isActive", false)
        );

        try {
            mailService.sendTaskDeletedEmail(existingTask);
        } catch (Exception ex) {
            log.error("Task id={} deleted but notification email failed", taskId, ex);
        }
    }

    @Override
    public List<TaskDTO> getDeletedTasks() {
        return mapper.toDTOList(taskRepository.getDeletedTasks());
    }

    @Override
    @Transactional
    public void restoreTask(Long taskId) {
        TaskEntity existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (Boolean.TRUE.equals(existingTask.getIsActive())) {
            throw new ResourceNotFoundException("Task is already active: " + taskId);
        }

        Map<String, Object> before = AuditSnapshotUtil.singleValueMap("isActive", false);
        existingTask.setIsActive(true);
        existingTask.setUpdatedBy(userLoginSession.getUserId());
        taskRepository.save(existingTask);
        log.info("Task restore completed for taskId={}", taskId);

        auditService.log(
                AuditAction.UPDATE,
                AuditEntityType.TASK,
                taskId,
                "Restored deleted task #" + taskId,
                before,
                AuditSnapshotUtil.singleValueMap("isActive", true)
        );
    }

    @Override
    public List<TaskDTO> getFilteredTasks(String client, String assignedTo, String issueType, String priority,
                                          String status, String fixedOn, String dateFrom, String dateTo) {
        Long assignedUserId = null;
        if (!"all".equals(assignedTo) && assignedTo != null && !assignedTo.isEmpty()) {
            try {
                assignedUserId = Long.parseLong(assignedTo);
            } catch (NumberFormatException e) {
                // ignore - keep as null
            }
        }

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                fromDate = LocalDateTime.parse(dateFrom + "T00:00:00");
            } catch (Exception e) {
                // ignore
            }
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                toDate = LocalDateTime.parse(dateTo + "T23:59:59");
            } catch (Exception e) {
                // ignore
            }
        }

        List<TaskEntity> tasks = taskRepository.findTasksWithFilters(
                assignedUserId, issueType, priority, status, fixedOn, client, fromDate, toDate
        );

        return mapper.toDTOList(tasks);
    }

    @Override
    public List<String> getDistinctClientNames() {
        return taskRepository.findDistinctClientNames();
    }

    private void logTaskUpdateAudit(Long taskId, Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> changes = AuditSnapshotUtil.diff(before, after);
        if (changes.isEmpty()) {
            return;
        }

        String actor = userLoginSession.getUserName() != null ? userLoginSession.getUserName() : "System";
        Object oldStatus = before.get("status");
        Object newStatus = after.get("status");
        boolean statusChanged = !Objects.equals(oldStatus, newStatus);

        if (statusChanged && changes.size() == 1) {
            auditService.log(
                    AuditAction.STATUS_CHANGE,
                    AuditEntityType.TASK,
                    taskId,
                    actor + " changed task #" + taskId + " status from " + oldStatus + " to " + newStatus,
                    AuditSnapshotUtil.singleValueMap("status", oldStatus),
                    AuditSnapshotUtil.singleValueMap("status", newStatus)
            );
            return;
        }

        String description = statusChanged
                ? actor + " updated task #" + taskId + " (status: " + oldStatus + " -> " + newStatus + ")"
                : actor + " updated task #" + taskId;

        auditService.log(
                AuditAction.UPDATE,
                AuditEntityType.TASK,
                taskId,
                description,
                before,
                after
        );
    }
}
