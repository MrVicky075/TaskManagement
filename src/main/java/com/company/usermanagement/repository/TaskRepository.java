package com.company.usermanagement.repository;

import com.company.usermanagement.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity,Long> {

    @Query("SELECT t FROM TaskEntity t WHERE t.isActive = true order by t.taskId desc")
    List<TaskEntity> getAllTask();

    @Query("SELECT t FROM TaskEntity t WHERE t.isActive = false order by t.taskId desc")
    List<TaskEntity> getDeletedTasks();

    @Query("SELECT t FROM TaskEntity t WHERE t.assignedUser.userId IN :userIds and t.isActive = true order by t.taskId desc")
    List<TaskEntity> getMyAllTask(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("UPDATE TaskEntity t SET t.isActive = false WHERE t.taskId = :taskId")
    void deleteTaskById(@Param("taskId") Long userId);


    @Query("SELECT DISTINCT t.clientName FROM TaskEntity t WHERE t.isActive = true AND t.clientName IS NOT NULL AND t.clientName != '-' ORDER BY t.clientName")
    List<String> findDistinctClientNames();

    // Get filtered tasks with all filters - CORRECTED
    @Query("SELECT t FROM TaskEntity t WHERE t.isActive = true " +
            "AND (:assignedTo IS NULL OR :assignedTo = 0 OR t.assignedUser.userId = :assignedTo) " +
            "AND (:issueType IS NULL OR :issueType = 'all' OR t.issue = :issueType) " +
            "AND (:priority IS NULL OR :priority = 'all' OR t.priority = :priority) " +
            "AND (:status IS NULL OR :status = 'all' OR t.status = :status) " +
            "AND (:fixedOn IS NULL OR :fixedOn = 'all' OR t.fixedOn = :fixedOn) " +
            "AND (:client IS NULL OR :client = 'all' OR t.clientName = :client) " +
            "AND (:dateFrom IS NULL OR t.createAt >= :dateFrom) " +
            "AND (:dateTo IS NULL OR t.createAt <= :dateTo) " +
            "ORDER BY t.taskId DESC")
    List<TaskEntity> findTasksWithFilters(
            @Param("assignedTo") Long assignedTo,
            @Param("issueType") String issueType,
            @Param("priority") String priority,
            @Param("status") String status,
            @Param("fixedOn") String fixedOn,
            @Param("client") String client,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );
}
