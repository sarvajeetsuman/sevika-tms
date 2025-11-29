package online.sevika.tm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.TeamRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a team member (user-team relationship with role)
 */
@Entity
@Table(name = "team_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "user_id"}),
       indexes = {
        @Index(name = "idx_team_member_team", columnList = "team_id"),
        @Index(name = "idx_team_member_user", columnList = "user_id"),
        @Index(name = "idx_team_member_role", columnList = "role")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}
