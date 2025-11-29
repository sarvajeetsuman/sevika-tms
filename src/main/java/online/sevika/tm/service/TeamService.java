package online.sevika.tm.service;
import online.sevika.tm.dto.TeamMemberRequestDTO;
import online.sevika.tm.dto.TeamMemberResponseDTO;
import online.sevika.tm.dto.TeamRequestDTO;
import online.sevika.tm.dto.TeamResponseDTO;

import online.sevika.tm.entity.enums.TeamRole;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Team operations
 */
public interface TeamService {

    /**
     * Create a new team
     */
    TeamResponseDTO createTeam(TeamRequestDTO request, UUID ownerId);

    /**
     * Get team by ID
     */
    TeamResponseDTO getTeamById(UUID teamId);

    /**
     * Update team
     */
    TeamResponseDTO updateTeam(UUID teamId, TeamRequestDTO request, UUID userId);

    /**
     * Delete team
     */
    void deleteTeam(UUID teamId, UUID userId);

    /**
     * Get teams owned by user
     */
    List<TeamResponseDTO> getTeamsOwnedByUser(UUID userId);

    /**
     * Get teams where user is a member
     */
    List<TeamResponseDTO> getTeamsForUser(UUID userId);

    /**
     * Add member to team
     */
    TeamMemberResponseDTO addTeamMember(UUID teamId, TeamMemberRequestDTO request, UUID addedBy);

    /**
     * Remove member from team
     */
    void removeTeamMember(UUID teamId, UUID userId, UUID removedBy);

    /**
     * Update team member role
     */
    TeamMemberResponseDTO updateTeamMemberRole(UUID teamId, UUID userId, TeamRole role, UUID updatedBy);

    /**
     * Get team members
     */
    List<TeamMemberResponseDTO> getTeamMembers(UUID teamId);

    /**
     * Check if user is team owner
     */
    boolean isTeamOwner(UUID teamId, UUID userId);

    /**
     * Check if user is team admin or owner
     */
    boolean isTeamAdminOrOwner(UUID teamId, UUID userId);
}
