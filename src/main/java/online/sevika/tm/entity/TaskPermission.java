package online.sevika.tm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.PermissionType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing task-level permissions for teams or users
 */
@Entity
@Table(name = "task_permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "team_id", "user_id"}),
       indexes = {
        @Index(name = "idx_task_permission_task", columnList = "task_id"),
        @Index(name = "idx_task_permission_team", columnList = "team_id"),
        @Index(name = "idx_task_permission_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionType permission;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "granted_by", nullable = false)
    private UUID grantedBy;

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }
}
