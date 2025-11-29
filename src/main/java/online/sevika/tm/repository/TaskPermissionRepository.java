package online.sevika.tm.repository;

import online.sevika.tm.entity.TaskPermission;
import online.sevika.tm.entity.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TaskPermission entity
 */
@Repository
public interface TaskPermissionRepository extends JpaRepository<TaskPermission, UUID> {

    /**
     * Find permissions for a task
     */
    List<TaskPermission> findByTaskId(UUID taskId);

    /**
     * Find permissions for a team
     */
    List<TaskPermission> findByTeamId(UUID teamId);

    /**
     * Find permissions for a user
     */
    List<TaskPermission> findByUserId(UUID userId);

    /**
     * Find specific team permission for task
     */
    Optional<TaskPermission> findByTaskIdAndTeamId(UUID taskId, UUID teamId);

    /**
     * Find specific user permission for task
     */
    Optional<TaskPermission> findByTaskIdAndUserId(UUID taskId, UUID userId);

    /**
     * Check if user has permission on task (direct or through team)
     */
    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN true ELSE false END " +
           "FROM TaskPermission tp " +
           "WHERE tp.taskId = :taskId " +
           "AND (tp.userId = :userId OR tp.teamId IN " +
           "(SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId))")
    boolean hasUserAccessToTask(@Param("taskId") UUID taskId, @Param("userId") UUID userId);

    /**
     * Get user's effective permissions on task
     */
    @Query("SELECT tp FROM TaskPermission tp " +
           "WHERE tp.taskId = :taskId " +
           "AND (tp.userId = :userId OR tp.teamId IN " +
           "(SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId))")
    List<TaskPermission> findUserPermissionsOnTask(@Param("taskId") UUID taskId, @Param("userId") UUID userId);

    /**
     * Delete permissions for task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Delete team permissions for task
     */
    void deleteByTaskIdAndTeamId(UUID taskId, UUID teamId);

    /**
     * Delete user permissions for task
     */
    void deleteByTaskIdAndUserId(UUID taskId, UUID userId);
}
