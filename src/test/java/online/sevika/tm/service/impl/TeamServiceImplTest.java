package online.sevika.tm.service.impl;

import online.sevika.tm.dto.TeamMemberRequestDTO;
import online.sevika.tm.dto.TeamRequestDTO;
import online.sevika.tm.dto.TeamMemberResponseDTO;
import online.sevika.tm.dto.TeamResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private TeamMemberMapper teamMemberMapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    private UUID teamId;
    private UUID ownerId;
    private UUID memberId;
    private Team team;
    private User owner;
    private User member;
    private TeamMember teamMember;
    private TeamRequestDTO teamRequest;
    private TeamResponseDTO teamResponse;
    private TeamMemberRequestDTO memberRequest;
    private TeamMemberResponseDTO memberResponse;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        owner = User.builder()
                .id(ownerId)
                .username("owner")
                .email("owner@example.com")
                .build();

        member = User.builder()
                .id(memberId)
                .username("member")
                .email("member@example.com")
                .build();

        team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .description("Test Description")
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .build();

        teamMember = TeamMember.builder()
                .id(UUID.randomUUID())
                .teamId(teamId)
                .userId(memberId)
                .role(TeamRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();

        teamRequest = new TeamRequestDTO();
        teamRequest.setName("Test Team");
        teamRequest.setDescription("Test Description");

        teamResponse = new TeamResponseDTO();
        teamResponse.setId(teamId);
        teamResponse.setName("Test Team");
        teamResponse.setDescription("Test Description");
        teamResponse.setOwnerName("owner");
        teamResponse.setMemberCount(1);

        memberRequest = new TeamMemberRequestDTO();
        memberRequest.setUserId(memberId);
        memberRequest.setRole(TeamRole.MEMBER);

        memberResponse = new TeamMemberResponseDTO();
        memberResponse.setId(teamMember.getId());
        memberResponse.setUsername("member");
        memberResponse.setRole(TeamRole.MEMBER);
    }

    @Test
    void createTeam_Success() {
        // Arrange
        when(teamRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(teamMapper.toEntity(any(TeamRequestDTO.class))).thenReturn(team);
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        when(teamMapper.toResponse(any(Team.class))).thenReturn(teamResponse);
        when(teamMemberRepository.countByTeamId(any(UUID.class))).thenReturn(1L);

        // Act
        TeamResponseDTO response = teamService.createTeam(teamRequest, ownerId);

        // Assert
        assertNotNull(response);
        assertEquals("Test Team", response.getName());
        verify(teamRepository).existsByName("Test Team");
        verify(userRepository, times(2)).findById(ownerId);
        verify(teamRepository).save(any(Team.class));
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void createTeam_DuplicateName_ThrowsException() {
        // Arrange
        when(teamRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                teamService.createTeam(teamRequest, ownerId));

        verify(teamRepository, never()).save(any());
    }

    @Test
    void createTeam_OwnerNotFound_ThrowsException() {
        // Arrange
        when(teamRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                teamService.createTeam(teamRequest, ownerId));
    }

    @Test
    void getTeamById_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMapper.toResponse(any(Team.class))).thenReturn(teamResponse);
        when(teamMemberRepository.countByTeamId(teamId)).thenReturn(1L);

        // Act
        TeamResponseDTO response = teamService.getTeamById(teamId);

        // Assert
        assertNotNull(response);
        assertEquals(teamId, response.getId());
        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_NotFound_ThrowsException() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                teamService.getTeamById(teamId));
    }

    @Test
    void updateTeam_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamMapper.toResponse(any(Team.class))).thenReturn(teamResponse);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(teamMemberRepository.countByTeamId(any(UUID.class))).thenReturn(1L);

        // Act
        TeamResponseDTO response = teamService.updateTeam(teamId, teamRequest, ownerId);

        // Assert
        assertNotNull(response);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void updateTeam_NotAuthorized_ThrowsException() {
        // Arrange
        UUID unauthorizedUserId = UUID.randomUUID();
        Team teamWithDifferentOwner = Team.builder()
                .id(teamId)
                .name("Test Team")
                .ownerId(ownerId)
                .build();
        
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamWithDifferentOwner));
        when(teamMemberRepository.findByTeamIdAndUserId(teamId, unauthorizedUserId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                teamService.updateTeam(teamId, teamRequest, unauthorizedUserId));
    }

    @Test
    void deleteTeam_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act
        teamService.deleteTeam(teamId, ownerId);

        // Assert
        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_NotOwner_ThrowsException() {
        // Arrange
        UUID notOwnerId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                teamService.deleteTeam(teamId, notOwnerId));

        verify(teamRepository, never()).delete(any());
    }

    @Test
    void getTeamsOwnedByUser_Success() {
        // Arrange
        when(teamRepository.findByOwnerId(ownerId))
                .thenReturn(Collections.singletonList(team));
        when(teamMapper.toResponse(any(Team.class))).thenReturn(teamResponse);
        when(teamMemberRepository.countByTeamId(teamId)).thenReturn(1L);

        // Act
        List<TeamResponseDTO> teams = teamService.getTeamsOwnedByUser(ownerId);

        // Assert
        assertNotNull(teams);
        assertEquals(1, teams.size());
        verify(teamRepository).findByOwnerId(ownerId);
    }

    @Test
    void getTeamsForUser_Success() {
        // Arrange
        when(teamRepository.findTeamsByUserId(memberId))
                .thenReturn(Collections.singletonList(team));
        when(teamMapper.toResponse(any(Team.class))).thenReturn(teamResponse);
        when(teamMemberRepository.countByTeamId(teamId)).thenReturn(1L);

        // Act
        List<TeamResponseDTO> teams = teamService.getTeamsForUser(memberId);

        // Assert
        assertNotNull(teams);
        assertEquals(1, teams.size());
        verify(teamRepository).findTeamsByUserId(memberId);
    }

    @Test
    void addTeamMember_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).thenReturn(false);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        when(teamMemberMapper.toResponse(any(TeamMember.class))).thenReturn(memberResponse);

        // Act
        TeamMemberResponseDTO response = teamService.addTeamMember(teamId, memberRequest, ownerId);

        // Assert
        assertNotNull(response);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void addTeamMember_AlreadyExists_ThrowsException() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(teamId, memberId)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                teamService.addTeamMember(teamId, memberRequest, ownerId));
    }

    @Test
    void removeTeamMember_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId))
                .thenReturn(Optional.of(teamMember));

        // Act
        teamService.removeTeamMember(teamId, memberId, ownerId);

        // Assert
        verify(teamMemberRepository).delete(teamMember);
    }

    @Test
    void removeTeamMember_CannotRemoveOwner_ThrowsException() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                teamService.removeTeamMember(teamId, ownerId, ownerId));

        verify(teamMemberRepository, never()).delete(any());
    }

    @Test
    void updateTeamMemberRole_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId))
                .thenReturn(Optional.of(teamMember));
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);
        when(teamMemberMapper.toResponse(any(TeamMember.class))).thenReturn(memberResponse);

        // Act
        TeamMemberResponseDTO response = teamService.updateTeamMemberRole(
                teamId, memberId, TeamRole.ADMIN, ownerId);

        // Assert
        assertNotNull(response);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void updateTeamMemberRole_CannotChangeOwnerRole_ThrowsException() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                teamService.updateTeamMemberRole(teamId, ownerId, TeamRole.MEMBER, ownerId));
    }

    @Test
    void getTeamMembers_Success() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamId(teamId))
                .thenReturn(Collections.singletonList(teamMember));
        when(teamMemberMapper.toResponse(any(TeamMember.class))).thenReturn(memberResponse);

        // Act
        List<TeamMemberResponseDTO> members = teamService.getTeamMembers(teamId);

        // Assert
        assertNotNull(members);
        assertEquals(1, members.size());
        verify(teamMemberRepository).findByTeamId(teamId);
    }

    @Test
    void isTeamOwner_ReturnsTrue() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act
        boolean isOwner = teamService.isTeamOwner(teamId, ownerId);

        // Assert
        assertTrue(isOwner);
    }

    @Test
    void isTeamOwner_ReturnsFalse() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act
        boolean isOwner = teamService.isTeamOwner(teamId, memberId);

        // Assert
        assertFalse(isOwner);
    }

    @Test
    void isTeamAdminOrOwner_OwnerReturnsTrue() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Act
        boolean isAdminOrOwner = teamService.isTeamAdminOrOwner(teamId, ownerId);

        // Assert
        assertTrue(isAdminOrOwner);
    }

    @Test
    void isTeamAdminOrOwner_AdminReturnsTrue() {
        // Arrange
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        TeamMember adminMember = TeamMember.builder()
                .role(TeamRole.ADMIN)
                .build();
        when(teamMemberRepository.findByTeamIdAndUserId(teamId, memberId))
                .thenReturn(Optional.of(adminMember));

        // Act
        boolean isAdminOrOwner = teamService.isTeamAdminOrOwner(teamId, memberId);

        // Assert
        assertTrue(isAdminOrOwner);
    }
}
