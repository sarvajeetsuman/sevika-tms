package online.sevika.tm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Project entity representing a project in the task management system.
 * 
 * Demonstrates:
 * - One-to-Many relationship with Tasks
 * - Many-to-One relationship with User (owner)
 * - Cascading operations
 * - Entity lifecycle management
 */
@Entity
@Table(name = "projects",
       indexes = {
           @Index(name = "idx_project_owner", columnList = "owner_id"),
           @Index(name = "idx_project_status", columnList = "status")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Project status enumeration
     */
    public enum ProjectStatus {
        ACTIVE,
        COMPLETED,
        ARCHIVED
    }

    /**
     * Helper method to add a task to the project
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }

    /**
     * Helper method to remove a task from the project
     */
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
}
