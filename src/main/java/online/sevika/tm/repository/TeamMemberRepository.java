package online.sevika.tm.repository;

import online.sevika.tm.entity.TeamMember;
import online.sevika.tm.entity.enums.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TeamMember entity
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    /**
     * Find all members of a team
     */
    List<TeamMember> findByTeamId(UUID teamId);

    /**
     * Find all teams a user is member of
     */
    List<TeamMember> findByUserId(UUID userId);

    /**
     * Find specific team membership
     */
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    /**
     * Check if user is member of team
     */
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    /**
     * Find members by team and role
     */
    List<TeamMember> findByTeamIdAndRole(UUID teamId, TeamRole role);

    /**
     * Count members in a team
     */
    long countByTeamId(UUID teamId);

    /**
     * Delete team member
     */
    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);

    /**
     * Find team owners
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.teamId = :teamId AND tm.role = 'OWNER'")
    List<TeamMember> findOwnersByTeamId(@Param("teamId") UUID teamId);
}
