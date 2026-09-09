package com.company.usermanagement.service;

import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.entity.TaskEntity;

import java.util.List;

public interface TaskService {

        TaskDTO saveTask(TaskDTO taskDTO);

        public List<TaskEntity> saveAll(List<TaskEntity> tasks);

        TaskDTO getTaskById(Long taskId);

        List<TaskDTO> getAllTasks();
        public List<TaskDTO> getMyAllTasks(List<Long> userIds);
        TaskDTO updateTask(Long taskId, TaskDTO taskDTO);

        void deleteTask(Long taskId);

        List<TaskDTO> getDeletedTasks();

        void restoreTask(Long taskId);

        List<TaskDTO> getFilteredTasks(String client, String assignedTo, String issueType, String priority, String status, String fixedOn, String dateFrom, String dateTo);

        List<String> getDistinctClientNames();
}
