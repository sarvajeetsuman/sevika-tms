package online.sevika.tm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Task entity representing a task in the task management system.
 * 
 * Demonstrates:
 * - Many-to-One relationships
 * - Enumerations for status and priority
 * - Date/Time handling
 * - Indexing for query optimization
 */
@Entity
@Table(name = "tasks",
       indexes = {
           @Index(name = "idx_task_project", columnList = "project_id"),
           @Index(name = "idx_task_assigned", columnList = "assigned_to"),
           @Index(name = "idx_task_status", columnList = "status"),
           @Index(name = "idx_task_priority", columnList = "priority"),
           @Index(name = "idx_task_due_date", columnList = "due_date")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Task status enumeration
     */
    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        DONE
    }

    /**
     * Task priority enumeration
     */
    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH
    }
}
