package com.company.usermanagement.dto;

import com.company.usermanagement.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private Long userId;

    @NotBlank(message = "User name is required")
    @Size(max = 100, message = "User name cannot exceed 100 characters")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    private String mobileNo;

    private Boolean isActive;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    @NotNull(message = "Role is required")
    private UserEntity.UserRole role;
}
