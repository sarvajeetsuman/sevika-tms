package online.sevika.tm.repository;

import online.sevika.tm.entity.ProjectPermission;
import online.sevika.tm.entity.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProjectPermission entity
 */
@Repository
public interface ProjectPermissionRepository extends JpaRepository<ProjectPermission, UUID> {

    /**
     * Find permissions for a project
     */
    List<ProjectPermission> findByProjectId(UUID projectId);

    /**
     * Find permissions for a team
     */
    List<ProjectPermission> findByTeamId(UUID teamId);

    /**
     * Find permissions for a user
     */
    List<ProjectPermission> findByUserId(UUID userId);

    /**
     * Find specific team permission for project
     */
    Optional<ProjectPermission> findByProjectIdAndTeamId(UUID projectId, UUID teamId);

    /**
     * Find specific user permission for project
     */
    Optional<ProjectPermission> findByProjectIdAndUserId(UUID projectId, UUID userId);

    /**
     * Check if user has permission on project (direct or through team)
     */
    @Query("SELECT CASE WHEN COUNT(pp) > 0 THEN true ELSE false END " +
           "FROM ProjectPermission pp " +
           "WHERE pp.projectId = :projectId " +
           "AND (pp.userId = :userId OR pp.teamId IN " +
           "(SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId))")
    boolean hasUserAccessToProject(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /**
     * Get user's effective permissions on project
     */
    @Query("SELECT pp FROM ProjectPermission pp " +
           "WHERE pp.projectId = :projectId " +
           "AND (pp.userId = :userId OR pp.teamId IN " +
           "(SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId))")
    List<ProjectPermission> findUserPermissionsOnProject(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /**
     * Delete permissions for project
     */
    void deleteByProjectId(UUID projectId);

    /**
     * Delete team permissions for project
     */
    void deleteByProjectIdAndTeamId(UUID projectId, UUID teamId);

    /**
     * Delete user permissions for project
     */
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}
