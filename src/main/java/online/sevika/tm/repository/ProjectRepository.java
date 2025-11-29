package online.sevika.tm.repository;

import online.sevika.tm.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find all projects owned by a specific user
     */
    List<Project> findByOwnerId(UUID ownerId);

    /**
     * Find projects by status
     */
    List<Project> findByStatus(Project.ProjectStatus status);

    /**
     * Find projects by owner and status
     */
    List<Project> findByOwnerIdAndStatus(UUID ownerId, Project.ProjectStatus status);

    /**
     * Find projects with task count
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :projectId")
    Project findByIdWithTasks(@Param("projectId") UUID projectId);

    /**
     * Check if project exists by name and owner
     */
    boolean existsByNameAndOwnerId(String name, UUID ownerId);
}
