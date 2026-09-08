package com.company.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_user_id")
    private UserEntity assignedUser;

    @Column(name = "priority", length = 20)
    private String priority = "-";

    @Column(name = "paid", length = 20)
    private String paid = "Paid";

    @Column(name = "client_name", length = 255)
    private String clientName;

    @Column(name = "issue")
    private String issue="-";

    @Column(name = "redmine_id")
    private Long redmineId=0L;

    @Column(name = "days")
    private Integer days;

    @Column(name = "requirement", length = 5000)
    private String requirement;

    @Column(name = "task_details", length = 5000)
    private String taskDetails;

    @Column(name = "status", length = 20)
    private String status = "-";

    @Column(name = "fixedOn")
    private String fixedOn="-";

    @Column(name = "remarks", length = 2000)
    private String remarks;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive;

}
