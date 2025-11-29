package online.sevika.tm.controller;

import online.sevika.tm.dto.ProjectRequestDTO;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.ProjectService;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    @interface WithMockCustomUser {
        String username() default "testuser";
        String role() default "USER";
    }

    static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername(customUser.username());
            user.setEmail(customUser.username() + "@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(User.Role.valueOf(customUser.role()));
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + customUser.role()))
            );
            context.setAuthentication(authentication);
            return context;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    @WithMockCustomUser
    void createProject_Success() throws Exception {
        ProjectRequestDTO request = ProjectRequestDTO.builder()
                .name("New Project")
                .description("Project description")
                .build();

        ProjectResponseDTO response = ProjectResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("New Project")
                .description("Project description")
                .status(Project.ProjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectService.createProject(any(ProjectRequestDTO.class), any(UUID.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description").value("Project description"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createProject_Unauthenticated_Unauthorized() throws Exception {
        ProjectRequestDTO request = ProjectRequestDTO.builder()
                .name("New Project")
                .description("Project description")
                .build();

        mockMvc.perform(post("/api/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getAllProjects_Success() throws Exception {
        List<ProjectResponseDTO> projects = Arrays.asList(
                ProjectResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .name("Project 1")
                        .status(Project.ProjectStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                ProjectResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .name("Project 2")
                        .status(Project.ProjectStatus.COMPLETED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        when(projectService.getAllProjects()).thenReturn(projects);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Project 1"))
                .andExpect(jsonPath("$[1].name").value("Project 2"));
    }

    @Test
    @WithMockCustomUser
    void getProjectById_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectResponseDTO response = ProjectResponseDTO.builder()
                .id(projectId)
                .name("Test Project")
                .status(Project.ProjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectService.getProjectById(projectId)).thenReturn(response);

        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @WithMockCustomUser
    void updateProject_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectUpdateRequestDTO request = ProjectUpdateRequestDTO.builder()
                .name("Updated Project")
                .description("Updated Description")
                .status(Project.ProjectStatus.COMPLETED)
                .build();

        ProjectResponseDTO response = ProjectResponseDTO.builder()
                .id(projectId)
                .name("Updated Project")
                .description("Updated Description")
                .status(Project.ProjectStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(projectService.updateProject(eq(projectId), any(ProjectUpdateRequestDTO.class), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/projects/{id}", projectId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockCustomUser
    void deleteProject_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        doNothing().when(projectService).deleteProject(eq(projectId), any(UUID.class));

        mockMvc.perform(delete("/api/projects/{id}", projectId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
