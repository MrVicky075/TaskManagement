package com.company.usermanagement.mapper;


import com.company.usermanagement.dto.TaskDTO;
import com.company.usermanagement.entity.TaskEntity;
import com.company.usermanagement.entity.UserEntity;
import com.company.usermanagement.exception.ResourceNotFoundException;
import com.company.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskMapper {

    @Autowired
    private UserRepository repository;
    //TaskDTO toDTO(TaskEntity taskEntity);
    public TaskDTO toDTO(TaskEntity entity){
        if (entity == null) { return null;}
        TaskDTO dto = new TaskDTO();
        dto.setTaskId(entity.getTaskId());
        dto.setPriority(entity.getPriority());
        dto.setPaid(entity.getPaid() != null ? entity.getPaid() : "Paid");
        dto.setClientName(entity.getClientName());
        dto.setIssue(entity.getIssue());
        dto.setRedmineId(entity.getRedmineId());
        dto.setDays(entity.getDays());
        dto.setRequirement(entity.getRequirement());
        dto.setTaskDetails(entity.getTaskDetails());
        dto.setStatus(entity.getStatus());
        dto.setFixedOn(entity.getFixedOn());
        dto.setRemarks(entity.getRemarks());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateAt(entity.getCreateAt());
        dto.setUpdateAt(entity.getUpdateAt());
        dto.setCreateBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        if (entity.getAssignedUser() != null) {
            dto.setAssignUserId(entity.getAssignedUser().getUserId());
            dto.setAssignUserName(entity.getAssignedUser().getUserName());
            dto.setAssignUserRole(entity.getAssignedUser().getRole());
        } else {
            dto.setAssignUserId(null);
            dto.setAssignUserName(null);
            dto.setAssignUserRole(null);
        }
        return dto;
    }

    //TaskEntity toEntity(TaskDTO taskDTO);
    public TaskEntity toEntity(TaskDTO dto) {
        if (dto == null) {return null;}
        TaskEntity entity = new TaskEntity();
        entity.setTaskId(dto.getTaskId());
        entity.setPriority(dto.getPriority());
        entity.setPaid(dto.getPaid() != null && !dto.getPaid().isBlank() ? dto.getPaid() : "Paid");
        entity.setClientName(dto.getClientName());
        entity.setIssue(dto.getIssue());
        entity.setRedmineId(dto.getRedmineId());
        entity.setDays(dto.getDays());
        entity.setRequirement(dto.getRequirement());
        entity.setTaskDetails(dto.getTaskDetails());
        entity.setStatus(dto.getStatus());
        entity.setFixedOn(dto.getFixedOn());
        entity.setRemarks(dto.getRemarks());
        entity.setIsActive(dto.getIsActive());
        entity.setCreateAt(dto.getCreateAt());
        entity.setUpdateAt(dto.getUpdateAt());
        entity.setCreatedBy(dto.getCreateBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        if (dto.getAssignUserId() != null) {
            UserEntity user = repository.findById(dto.getAssignUserId()).orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + dto.getAssignUserId()));
            entity.setAssignedUser(user);
        } else {
            entity.setAssignedUser(null);
        }
        return entity;
    }

    public void updateTaskEntity(TaskDTO dto, TaskEntity entity) {
        if (dto == null || entity == null) {return;}
        entity.setPriority(dto.getPriority());
        entity.setPaid(dto.getPaid() != null && !dto.getPaid().isBlank() ? dto.getPaid() : "Paid");
        entity.setClientName(dto.getClientName());
        entity.setIssue(dto.getIssue());
        entity.setRedmineId(dto.getRedmineId());
        entity.setDays(dto.getDays());
        entity.setRequirement(dto.getRequirement());
        entity.setTaskDetails(dto.getTaskDetails());
        entity.setStatus(dto.getStatus());
        entity.setFixedOn(dto.getFixedOn());
        entity.setRemarks(dto.getRemarks());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        if (dto.getAssignUserId() != null) {
            UserEntity user = repository.findById(dto.getAssignUserId()).orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + dto.getAssignUserId()));
            entity.setAssignedUser(user);
        } else {
            entity.setAssignedUser(null);
        }
    }

    public List<TaskDTO> toDTOList(List<TaskEntity> entities) {
        if (entities == null) {return null;}
        List<TaskDTO> dtoList = new ArrayList<>();
        for (TaskEntity entity : entities) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }


}
