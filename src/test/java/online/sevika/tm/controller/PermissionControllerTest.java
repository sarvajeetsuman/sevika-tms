package online.sevika.tm.controller;

import online.sevika.tm.dto.PermissionGrantRequestDTO;
import online.sevika.tm.dto.TaskPermissionResponseDTO;
import online.sevika.tm.dto.ProjectPermissionResponseDTO;
import online.sevika.tm.dto.UserSummaryDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.dto.TaskSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.User;
import online.sevika.tm.entity.enums.PermissionType;
import online.sevika.tm.service.PermissionService;
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
 * Integration tests for PermissionController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionService permissionService;

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
    @DisplayName("Grant project permission - Success")
    void grantProjectPermission_Success() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PermissionGrantRequestDTO request = PermissionGrantRequestDTO.builder()
                .userId(userId)
                .permissionLevel(PermissionType.WRITE)
                .permission(PermissionType.WRITE)
                .build();
        
        ProjectPermissionResponseDTO response = ProjectPermissionResponseDTO.builder()
                .id(UUID.randomUUID())
                .user(UserSummaryDTO.builder().id(userId).username("testuser").build())
                .project(ProjectSummaryDTO.builder().id(projectId).name("Test Project").build())
                .projectName("Test Project")
                .username("testuser")
                .permissionLevel(PermissionType.WRITE)
                .grantedAt(LocalDateTime.now())
                .grantedByUsername("admin")
                .build();
        
        when(permissionService.grantProjectPermission(eq(projectId), any(PermissionGrantRequestDTO.class), any(UUID.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/permissions/projects/{projectId}", projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.project.id").value(projectId.toString()))
                .andExpect(jsonPath("$.permissionLevel").value("WRITE"));
    }

    @Test
    @DisplayName("Grant project permission - Unauthenticated - Forbidden")
    void grantProjectPermission_Unauthenticated_Forbidden() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PermissionGrantRequestDTO request = PermissionGrantRequestDTO.builder().userId(userId).permissionLevel(PermissionType.WRITE).permission(PermissionType.WRITE).build();

        // Act & Assert
        mockMvc.perform(post("/api/permissions/projects/{projectId}", projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Grant project permission - Invalid request - Bad Request")
    void grantProjectPermission_InvalidRequest_BadRequest() throws Exception {
        // Arrange - missing permission type
        UUID projectId = UUID.randomUUID();
        PermissionGrantRequestDTO request = PermissionGrantRequestDTO.builder().userId(UUID.randomUUID()).permission(null).build();

        // Act & Assert
        mockMvc.perform(post("/api/permissions/projects/{projectId}", projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Revoke project permission - Success")
    void revokeProjectPermission_Success() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        doNothing().when(permissionService).revokeProjectPermission(eq(projectId), eq(null), eq(userId), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/permissions/projects/{projectId}", projectId)
                        .with(csrf())
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Get project permissions - Success")
    void getProjectPermissions_Success() throws Exception {
        // Arrange
        UUID projectId = UUID.randomUUID();
        List<ProjectPermissionResponseDTO> permissions = Arrays.asList(
                ProjectPermissionResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .user(UserSummaryDTO.builder().id(UUID.randomUUID()).username("user1").build())
                        .project(ProjectSummaryDTO.builder().id(projectId).name("Test Project").build())
                        .projectName("Test Project")
                        .username("user1")
                        .permissionLevel(PermissionType.READ)
                        .grantedAt(LocalDateTime.now())
                        .grantedByUsername("admin")
                        .build(),
                ProjectPermissionResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .user(UserSummaryDTO.builder().id(UUID.randomUUID()).username("user2").build())
                        .project(ProjectSummaryDTO.builder().id(projectId).name("Test Project").build())
                        .projectName("Test Project")
                        .username("user2")
                        .permissionLevel(PermissionType.WRITE)
                        .grantedAt(LocalDateTime.now())
                        .grantedByUsername("admin")
                        .build()
        );
        
        when(permissionService.getProjectPermissions(projectId)).thenReturn(permissions);

        // Act & Assert
        mockMvc.perform(get("/api/permissions/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].permissionLevel").value("READ"))
                .andExpect(jsonPath("$[1].permissionLevel").value("WRITE"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Grant task permission - Success")
    void grantTaskPermission_Success() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        PermissionGrantRequestDTO request = PermissionGrantRequestDTO.builder()
                .userId(teamId)
                .permissionLevel(PermissionType.ADMIN)
                .permission(PermissionType.ADMIN)
                .build();
        
        TaskPermissionResponseDTO response = TaskPermissionResponseDTO.builder()
                .id(UUID.randomUUID())
                .user(UserSummaryDTO.builder().id(teamId).username("testuser").build())
                .task(TaskSummaryDTO.builder().id(taskId).title("Test Task").build())
                .taskTitle("Test Task")
                .teamName("Test Team")
                .username("testuser")
                .permissionLevel(PermissionType.ADMIN)
                .grantedAt(LocalDateTime.now())
                .grantedByUsername("admin")
                .build();
        
        when(permissionService.grantTaskPermission(eq(taskId), any(PermissionGrantRequestDTO.class), any(UUID.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/permissions/tasks/{taskId}", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.task.id").value(taskId.toString()))
                .andExpect(jsonPath("$.permissionLevel").value("ADMIN"));
    }

    @Test
    @DisplayName("Grant task permission - Unauthenticated - Forbidden")
    void grantTaskPermission_Unauthenticated_Forbidden() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        PermissionGrantRequestDTO request = PermissionGrantRequestDTO.builder().userId(UUID.randomUUID()).permissionLevel(PermissionType.READ).permission(PermissionType.READ).build();

        // Act & Assert
        mockMvc.perform(post("/api/permissions/tasks/{taskId}", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Revoke task permission - Success")
    void revokeTaskPermission_Success() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        doNothing().when(permissionService).revokeTaskPermission(eq(taskId), eq(teamId), eq(null), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/permissions/tasks/{taskId}", taskId)
                        .with(csrf())
                        .param("teamId", teamId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("Get task permissions - Success")
    void getTaskPermissions_Success() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        List<TaskPermissionResponseDTO> permissions = Arrays.asList(
                TaskPermissionResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .user(UserSummaryDTO.builder().id(UUID.randomUUID()).username("user1").build())
                        .task(TaskSummaryDTO.builder().id(taskId).title("Test Task").build())
                        .taskTitle("Test Task")
                        .teamName("Team A")
                        .username("user1")
                        .permissionLevel(PermissionType.READ)
                        .grantedAt(LocalDateTime.now())
                        .grantedByUsername("admin")
                        .build(),
                TaskPermissionResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .user(UserSummaryDTO.builder().id(UUID.randomUUID()).username("user2").build())
                        .task(TaskSummaryDTO.builder().id(taskId).title("Test Task").build())
                        .taskTitle("Test Task")
                        .username("user2")
                        .permissionLevel(PermissionType.DELETE)
                        .grantedAt(LocalDateTime.now())
                        .grantedByUsername("admin")
                        .build()
        );
        
        when(permissionService.getTaskPermissions(taskId)).thenReturn(permissions);

        // Act & Assert
        mockMvc.perform(get("/api/permissions/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].permissionLevel").value("READ"))
                .andExpect(jsonPath("$[1].permissionLevel").value("DELETE"));
    }
}
