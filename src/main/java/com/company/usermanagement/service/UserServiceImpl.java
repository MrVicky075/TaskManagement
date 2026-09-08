package com.company.usermanagement.service;

import com.company.usermanagement.audit.AuditAction;
import com.company.usermanagement.audit.AuditEntityType;
import com.company.usermanagement.audit.AuditSnapshotUtil;
import com.company.usermanagement.dto.ChangePasswordDTO;
import com.company.usermanagement.dto.UserRequestDTO;
import com.company.usermanagement.dto.UserResponseDTO;
import com.company.usermanagement.entity.UserEntity;
import com.company.usermanagement.exception.BusinessException;
import com.company.usermanagement.exception.ResourceNotFoundException;
import com.company.usermanagement.mapper.UserMapper;
import com.company.usermanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final MailService mailService;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        UserEntity entity = mapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setIsActive(true);
        entity.setCreatedOn(LocalDateTime.now());
        entity.setUpdatedOn(LocalDateTime.now());

        UserEntity saved = userRepository.save(entity);
        log.info("User registration completed for userId={}", saved.getUserId());

        try {
            mailService.sendUserRegistrationEmail(saved);
        } catch (Exception ex) {
            log.error("User id={} saved but registration email failed", saved.getUserId(), ex);
        }

        auditService.log(
                AuditAction.CREATE,
                AuditEntityType.USER,
                saved.getUserId(),
                "Created user " + saved.getUserName(),
                null,
                AuditSnapshotUtil.userSnapshot(saved)
        );
        return mapper.toResponse(saved);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, UserResponseDTO request) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Map<String, Object> before = AuditSnapshotUtil.userSnapshot(entity);

        if (!entity.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email already registered");
            }
        }
        mapper.updateEntity(entity, request);
        entity.setUpdatedOn(LocalDateTime.now());
        UserEntity saved = userRepository.save(entity);
        Map<String, Object> after = AuditSnapshotUtil.userSnapshot(saved);
        log.info("User update completed for userId={}", saved.getUserId());

        try {
            mailService.sendUserUpdatedEmail(before, saved);
        } catch (Exception ex) {
            log.error("User id={} updated but notification email failed", saved.getUserId(), ex);
        }

        auditService.log(
                AuditAction.UPDATE,
                AuditEntityType.USER,
                saved.getUserId(),
                "Updated user " + saved.getUserName(),
                before,
                after
        );
        return mapper.toResponse(saved);
    }

    @Override
    public void deleteUser(Long userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + userId));
        Map<String, Object> before = AuditSnapshotUtil.userSnapshot(entity);
        userRepository.deleteById(userId);
        auditService.log(
                AuditAction.DELETE,
                AuditEntityType.USER,
                userId,
                "Deleted user " + entity.getUserName(),
                before,
                null
        );
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + userId));
        return mapper.toResponse(user);
    }

    @Override
    public UserEntity findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAllActiveUsers()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void changeActiveStatus(Long userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + userId));
        Boolean oldStatus = entity.getIsActive();
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        entity.setUpdatedOn(LocalDateTime.now());
        userRepository.save(entity);

        auditService.log(
                AuditAction.STATUS_CHANGE,
                AuditEntityType.USER,
                userId,
                "Changed user " + entity.getUserName() + " status from " + oldStatus + " to " + entity.getIsActive(),
                AuditSnapshotUtil.singleValueMap("isActive", oldStatus),
                AuditSnapshotUtil.singleValueMap("isActive", entity.getIsActive())
        );
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO request) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found" + userId));
        if (request.getMode().equalsIgnoreCase("user")) {
            if (!passwordEncoder.matches(request.getOldPassword(), entity.getPassword())) {
                throw new BusinessException("Old password is incorrect");
            }
            if (!passwordEncoder.matches(request.getOldPassword(), request.getConfirmPassword())) {
                throw new BusinessException("New password and confirm password do not match");
            }
        }
        entity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        entity.setUpdatedOn(LocalDateTime.now());
        userRepository.save(entity);

        auditService.log(
                AuditAction.UPDATE,
                AuditEntityType.USER,
                userId,
                "Password changed for user " + entity.getUserName(),
                null,
                AuditSnapshotUtil.singleValueMap("passwordChanged", true)
        );
    }

    @Override
    public UserEntity getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found"));
    }

    public UserEntity findByUserName(String username) {
        return userRepository.findByUserName(username);
    }
}
