package online.sevika.tm.repository;

import online.sevika.tm.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Team entity
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    /**
     * Find teams by owner ID
     */
    List<Team> findByOwnerId(UUID ownerId);

    /**
     * Find team by name
     */
    Optional<Team> findByName(String name);

    /**
     * Check if team name exists
     */
    boolean existsByName(String name);

    /**
     * Find teams where user is a member
     */
    @Query("SELECT t FROM Team t JOIN TeamMember tm ON t.id = tm.teamId WHERE tm.userId = :userId")
    List<Team> findTeamsByUserId(@Param("userId") UUID userId);

    /**
     * Count teams owned by user
     */
    long countByOwnerId(UUID ownerId);
}
