package online.sevika.tm.service.impl;
import online.sevika.tm.dto.TeamMemberRequestDTO;
import online.sevika.tm.dto.TeamMemberResponseDTO;
import online.sevika.tm.dto.TeamRequestDTO;
import online.sevika.tm.dto.TeamResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.entity.Team;
import online.sevika.tm.entity.TeamMember;
import online.sevika.tm.entity.User;
import online.sevika.tm.entity.enums.TeamRole;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.TeamMapper;
import online.sevika.tm.mapper.TeamMemberMapper;
import online.sevika.tm.repository.TeamMemberRepository;
import online.sevika.tm.repository.TeamRepository;
import online.sevika.tm.repository.UserRepository;
import online.sevika.tm.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Team operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    public TeamResponseDTO createTeam(TeamRequestDTO request, UUID ownerId) {
        log.info("Creating team with name: {} for owner: {}", request.getName(), ownerId);

        // Check if team name already exists
        if (teamRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Team name already exists: " + request.getName());
        }

        // Verify owner exists
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));

        // Create team
        Team team = teamMapper.toEntity(request);
        team.setId(UUID.randomUUID());
        team.setOwnerId(ownerId);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        team = teamRepository.save(team);

        // Add owner as team member with OWNER role
        TeamMember ownerMember = new TeamMember();
        ownerMember.setId(UUID.randomUUID());
        ownerMember.setTeamId(team.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(TeamRole.OWNER);
        ownerMember.setJoinedAt(LocalDateTime.now());
        teamMemberRepository.save(ownerMember);

        return toResponseWithDetails(team);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamById(UUID teamId) {
        log.info("Fetching team with id: {}", teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        return toResponseWithDetails(team);
    }

    @Override
    public TeamResponseDTO updateTeam(UUID teamId, TeamRequestDTO request, UUID userId) {
        log.info("Updating team: {} by user: {}", teamId, userId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user is team owner or admin
        if (!isTeamAdminOrOwner(teamId, userId)) {
            throw new UnauthorizedException("Only team owner or admin can update the team");
        }

        // Check if new name conflicts with existing team
        if (!team.getName().equals(request.getName()) && teamRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Team name already exists: " + request.getName());
        }

        teamMapper.updateEntityFromRequest(request, team);
        team.setUpdatedAt(LocalDateTime.now());
        team = teamRepository.save(team);

        return toResponseWithDetails(team);
    }

    @Override
    public void deleteTeam(UUID teamId, UUID userId) {
        log.info("Deleting team: {} by user: {}", teamId, userId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Only team owner can delete
        if (!isTeamOwner(teamId, userId)) {
            throw new UnauthorizedException("Only team owner can delete the team");
        }

        teamRepository.delete(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsOwnedByUser(UUID userId) {
        log.info("Fetching teams owned by user: {}", userId);
        return teamRepository.findByOwnerId(userId).stream()
                .map(this::toResponseWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsForUser(UUID userId) {
        log.info("Fetching teams for user: {}", userId);
        return teamRepository.findTeamsByUserId(userId).stream()
                .map(this::toResponseWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    public TeamMemberResponseDTO addTeamMember(UUID teamId, TeamMemberRequestDTO request, UUID addedBy) {
        log.info("Adding member to team: {} by user: {}", teamId, addedBy);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user is team admin or owner
        if (!isTeamAdminOrOwner(teamId, addedBy)) {
            throw new UnauthorizedException("Only team owner or admin can add members");
        }

        // Check if member already exists
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this team");
        }

        // Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Create team member
        TeamMember teamMember = new TeamMember();
        teamMember.setId(UUID.randomUUID());
        teamMember.setTeamId(teamId);
        teamMember.setUserId(request.getUserId());
        teamMember.setRole(request.getRole());
        teamMember.setJoinedAt(LocalDateTime.now());
        teamMember = teamMemberRepository.save(teamMember);

        return toMemberResponseWithDetails(teamMember);
    }

    @Override
    public void removeTeamMember(UUID teamId, UUID userId, UUID removedBy) {
        log.info("Removing member from team: {} by user: {}", teamId, removedBy);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user is team admin or owner
        if (!isTeamAdminOrOwner(teamId, removedBy)) {
            throw new UnauthorizedException("Only team owner or admin can remove members");
        }

        // Cannot remove team owner
        if (isTeamOwner(teamId, userId)) {
            throw new IllegalArgumentException("Cannot remove team owner");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        teamMemberRepository.delete(teamMember);
    }

    @Override
    public TeamMemberResponseDTO updateTeamMemberRole(UUID teamId, UUID userId, TeamRole role, UUID updatedBy) {
        log.info("Updating member role in team: {} by user: {}", teamId, updatedBy);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user is team owner or admin
        if (!isTeamAdminOrOwner(teamId, updatedBy)) {
            throw new UnauthorizedException("Only team owner or admin can update member roles");
        }

        // Cannot change team owner role
        if (isTeamOwner(teamId, userId)) {
            throw new IllegalArgumentException("Cannot change team owner role");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

        teamMember.setRole(role);
        teamMember = teamMemberRepository.save(teamMember);

        return toMemberResponseWithDetails(teamMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponseDTO> getTeamMembers(UUID teamId) {
        log.info("Fetching members for team: {}", teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(this::toMemberResponseWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeamOwner(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        return team != null && team.getOwnerId().equals(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeamAdminOrOwner(UUID teamId, UUID userId) {
        if (isTeamOwner(teamId, userId)) {
            return true;
        }

        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .map(member -> member.getRole() == TeamRole.ADMIN || member.getRole() == TeamRole.OWNER)
                .orElse(false);
    }

    private TeamResponseDTO toResponseWithDetails(Team team) {
        TeamResponseDTO response = teamMapper.toResponse(team);
        
        // Get owner name
        userRepository.findById(team.getOwnerId()).ifPresent(owner -> 
            response.setOwnerName(owner.getUsername())
        );
        
        // Get member count
        response.setMemberCount((int) teamMemberRepository.countByTeamId(team.getId()));
        
        return response;
    }

    private TeamMemberResponseDTO toMemberResponseWithDetails(TeamMember teamMember) {
        TeamMemberResponseDTO response = teamMemberMapper.toResponse(teamMember);
        
        // Get team name
        teamRepository.findById(teamMember.getTeamId()).ifPresent(team -> 
            response.setTeamName(team.getName())
        );
        
        // Get user details
        userRepository.findById(teamMember.getUserId()).ifPresent(user -> {
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
        });
        
        return response;
    }
}
