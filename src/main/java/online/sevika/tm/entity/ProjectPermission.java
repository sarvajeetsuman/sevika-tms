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
 * Entity representing project-level permissions for teams or users
 */
@Entity
@Table(name = "project_permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "team_id", "user_id"}),
       indexes = {
        @Index(name = "idx_project_permission_project", columnList = "project_id"),
        @Index(name = "idx_project_permission_team", columnList = "team_id"),
        @Index(name = "idx_project_permission_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

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
