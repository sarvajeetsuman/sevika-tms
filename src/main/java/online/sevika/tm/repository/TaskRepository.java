package online.sevika.tm.repository;

import online.sevika.tm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Find all tasks in a project
     */
    List<Task> findByProjectId(UUID projectId);

    /**
     * Find tasks assigned to a specific user
     */
    List<Task> findByAssignedToId(UUID userId);

    /**
     * Find tasks created by a specific user
     */
    List<Task> findByCreatedById(UUID userId);

    /**
     * Find tasks by status
     */
    List<Task> findByStatus(Task.TaskStatus status);

    /**
     * Find tasks by priority
     */
    List<Task> findByPriority(Task.TaskPriority priority);

    /**
     * Find tasks by project and status
     */
    List<Task> findByProjectIdAndStatus(UUID projectId, Task.TaskStatus status);

    /**
     * Find overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);

    /**
     * Find tasks due soon (within specified days)
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status != 'DONE'")
    List<Task> findTasksDueSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find tasks with filters
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:#{#projectId} IS NULL OR t.project.id = :#{#projectId}) AND " +
           "(:#{#assignedToId} IS NULL OR t.assignedTo.id = :#{#assignedToId}) AND " +
           "(:#{#status} IS NULL OR t.status = :#{#status}) AND " +
           "(:#{#priority} IS NULL OR t.priority = :#{#priority})")
    List<Task> findTasksWithFilters(
            @Param("projectId") UUID projectId,
            @Param("assignedToId") UUID assignedToId,
            @Param("status") Task.TaskStatus status,
            @Param("priority") Task.TaskPriority priority
    );

    /**
     * Count tasks by project
     */
    long countByProjectId(UUID projectId);

    /**
     * Count tasks by status for a project
     */
    long countByProjectIdAndStatus(UUID projectId, Task.TaskStatus status);
}
