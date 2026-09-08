package com.company.usermanagement.service;

import com.company.usermanagement.entity.TaskEntity;
import com.company.usermanagement.entity.UserEntity;

import java.util.Map;

public interface MailService {

	void sendUserRegistrationEmail(UserEntity user);

	void sendUserUpdatedEmail(Map<String, Object> oldSnapshot, UserEntity updatedUser);

	void sendTaskCreatedEmail(TaskEntity task);

	void sendTaskUpdatedEmail(Map<String, Object> oldSnapshot, TaskEntity updatedTask);

	void sendTaskDeletedEmail(TaskEntity taskSnapshot);

}
