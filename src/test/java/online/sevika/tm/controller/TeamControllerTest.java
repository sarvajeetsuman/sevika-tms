package online.sevika.tm.controller;

import online.sevika.tm.dto.TeamMemberRequestDTO;
import online.sevika.tm.dto.TeamRequestDTO;
import online.sevika.tm.dto.TeamMemberResponseDTO;
import online.sevika.tm.dto.TeamResponseDTO;
import online.sevika.tm.dto.TeamMemberUpdateRoleDTO;
import online.sevika.tm.dto.UserSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.User;
import online.sevika.tm.entity.enums.TeamRole;
import online.sevika.tm.service.TeamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TeamController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeamService teamService;

    /**
     * Custom annotation for mocking authenticated user with entity User
     */
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    @interface WithMockCustomUser {
        String username() default "testuser";
        String role() default "USER";
        String userId() default "";
    }

    /**
     * Security context factory that provides entity User instead of Spring Security User
     */
    static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            
            UUID userId = customUser.userId().isEmpty() 
                ? UUID.randomUUID() 
                : UUID.fromString(customUser.userId());
            
            User user = new User();
            user.setId(userId);
            user.setUsername(customUser.username());
            user.setRole(User.Role.valueOf(customUser.role()));
            user.setEnabled(true);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + customUser.role()))
            );
            authentication.setDetails(user);
            context.setAuthentication(authentication);
            
            return context;
        }
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Create team - Authenticated - Success")
    void createTeam_Authenticated_Success() throws Exception {
        // Arrange
        TeamRequestDTO request = new TeamRequestDTO("Test Team", "Test Description");
        
        TeamResponseDTO response = TeamResponseDTO.builder().id(UUID.randomUUID()).name("Test Team").description("Test Description").owner(UserSummaryDTO.builder().id(UUID.randomUUID()).username("testuser").build()).ownerName("testuser").memberCount(1).createdAt(LocalDateTime.now()).updatedAt(null).build();
        
        when(teamService.createTeam(any(TeamRequestDTO.class), any(UUID.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Team"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    @DisplayName("Create team - Unauthenticated - Forbidden")
    void createTeam_Unauthenticated_Forbidden() throws Exception {
        // Arrange
        TeamRequestDTO request = new TeamRequestDTO("Test Team", "Test Description");

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Create team - Invalid request - Bad Request")
    void createTeam_InvalidRequest_BadRequest() throws Exception {
        // Arrange - name too short
        TeamRequestDTO request = new TeamRequestDTO("AB", "Test Description");

        // Act & Assert
        mockMvc.perform(post("/api/teams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Get team by ID - Success")
    void getTeam_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        TeamResponseDTO response = TeamResponseDTO.builder().id(teamId).name("Test Team").description("Test Description").owner(UserSummaryDTO.builder().id(UUID.randomUUID()).username("owner").build()).ownerName("owner").memberCount(5).createdAt(LocalDateTime.now()).updatedAt(null).build();
        
        when(teamService.getTeamById(teamId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/teams/{teamId}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Test Team"))
                .andExpect(jsonPath("$.memberCount").value(5));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Update team - Success")
    void updateTeam_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        TeamRequestDTO request = new TeamRequestDTO("Updated Team", "Updated Description");
        
        TeamResponseDTO response = TeamResponseDTO.builder()
                .id(teamId)
                .name("Updated Team")
                .description("Updated Description")
                .owner(UserSummaryDTO.builder().id(UUID.randomUUID()).username("owner").build())
                .ownerName("owner")
                .memberCount(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(teamService.updateTeam(eq(teamId), any(TeamRequestDTO.class), any(UUID.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/teams/{teamId}", teamId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Team"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Delete team - Success")
    void deleteTeam_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        doNothing().when(teamService).deleteTeam(eq(teamId), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/teams/{teamId}", teamId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Get my teams - Success")
    void getMyTeams_Success() throws Exception {
        // Arrange
        List<TeamResponseDTO> teams = Arrays.asList(
                TeamResponseDTO.builder().id(UUID.randomUUID()).name("Team 1").description("Description 1").owner(UserSummaryDTO.builder().id(UUID.randomUUID()).username("owner").build()).ownerName("owner").memberCount(3).createdAt(LocalDateTime.now()).updatedAt(null
                ).build(),
                TeamResponseDTO.builder().id(UUID.randomUUID()).name("Team 2").description("Description 2").owner(UserSummaryDTO.builder().id(UUID.randomUUID()).username("owner").build()).ownerName("owner").memberCount(5).createdAt(LocalDateTime.now()).updatedAt(null
                ).build()
        );
        
        when(teamService.getTeamsOwnedByUser(any(UUID.class))).thenReturn(teams);

        // Act & Assert
        mockMvc.perform(get("/api/teams/my-teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Team 1"))
                .andExpect(jsonPath("$[1].name").value("Team 2"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Get teams member of - Success")
    void getTeamsMemberOf_Success() throws Exception {
        // Arrange
        List<TeamResponseDTO> teams = Arrays.asList(
                TeamResponseDTO.builder().id(UUID.randomUUID()).name("Member Team 1").description("Description 1").owner(UserSummaryDTO.builder().id(UUID.randomUUID()).username("otheruser").build()).ownerName("otheruser").memberCount(8).createdAt(LocalDateTime.now()).updatedAt(null
                ).build()
        );
        
        when(teamService.getTeamsForUser(any(UUID.class))).thenReturn(teams);

        // Act & Assert
        mockMvc.perform(get("/api/teams/member-of"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Member Team 1"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Get team members - Success")
    void getTeamMembers_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        List<TeamMemberResponseDTO> members = Arrays.asList(
                TeamMemberResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .teamId(teamId)
                        .teamName("Test Team")
                        .userId(UUID.randomUUID())
                        .username("user1")
                        .email("user1@example.com")
                        .role(TeamRole.OWNER)
                        .joinedAt(LocalDateTime.now())
                        .build(),
                TeamMemberResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .teamId(teamId)
                        .teamName("Test Team")
                        .userId(UUID.randomUUID())
                        .username("user2")
                        .email("user2@example.com")
                        .role(TeamRole.MEMBER)
                        .joinedAt(LocalDateTime.now())
                        .build()
        );
        
        when(teamService.getTeamMembers(teamId)).thenReturn(members);

        // Act & Assert
        mockMvc.perform(get("/api/teams/{teamId}/members", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[1].role").value("MEMBER"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Add team member - Success")
    void addTeamMember_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TeamMemberRequestDTO request = new TeamMemberRequestDTO(userId, TeamRole.MEMBER);
        
        TeamMemberResponseDTO response = TeamMemberResponseDTO.builder()
                .id(UUID.randomUUID())
                .teamId(teamId)
                .teamName("Test Team")
                .userId(userId)
                .username("newmember")
                .email("newmember@example.com")
                .role(TeamRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        
        when(teamService.addTeamMember(eq(teamId), any(TeamMemberRequestDTO.class), any(UUID.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/teams/{teamId}/members", teamId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newmember"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Remove team member - Success")
    void removeTeamMember_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        doNothing().when(teamService).removeTeamMember(eq(teamId), eq(memberId), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/teams/{teamId}/members/{memberId}", teamId, memberId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Update team member role - Success")
    void updateTeamMemberRole_Success() throws Exception {
        // Arrange
        UUID teamId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        TeamMemberUpdateRoleDTO request = new TeamMemberUpdateRoleDTO(TeamRole.ADMIN);
        
        TeamMemberResponseDTO response = TeamMemberResponseDTO.builder()
                .id(UUID.randomUUID())
                .teamId(teamId)
                .teamName("Test Team")
                .userId(UUID.randomUUID())
                .username("member")
                .email("member@example.com")
                .role(TeamRole.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();
        
        when(teamService.updateTeamMemberRole(eq(teamId), eq(memberId), eq(TeamRole.ADMIN), any(UUID.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(patch("/api/teams/{teamId}/members/{memberId}/role", teamId, memberId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
