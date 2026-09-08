package com.company.usermanagement.repository;

import com.company.usermanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
    UserEntity findByUserName(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.userId NOT IN (1)")
    List<UserEntity> findAllActiveUsers();


}
