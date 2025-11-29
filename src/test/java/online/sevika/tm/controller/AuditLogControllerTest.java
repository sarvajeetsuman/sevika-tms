package online.sevika.tm.controller;

import online.sevika.tm.dto.AuditLogDTO;
import online.sevika.tm.dto.AuditLogFilterDTO;
import online.sevika.tm.entity.User;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import online.sevika.tm.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    // Custom annotation for mock user
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.test.context.support.WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    public @interface WithMockCustomUser {
        String username() default "testuser";
        String role() default "USER";
    }

    // Security context factory for custom user
    public static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            User user = new User();
            user.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            user.setUsername(customUser.username());
            user.setRole(User.Role.valueOf(customUser.role()));

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_" + customUser.role()));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getId().toString(), null, authorities);
            context.setAuthentication(auth);

            return context;
        }
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getAuditLogs_Success() throws Exception {
        // Arrange
        UUID logId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuditLogDTO auditLog = AuditLogDTO.builder()
                .id(logId)
                .entityType(EntityType.PROJECT)
                .entityId("project-123")
                .action(AuditAction.CREATED)
                .userId(userId)
                .username("testuser")
                .timestamp(LocalDateTime.now())
                .description("Project created")
                .ipAddress("192.168.1.1")
                .build();

        Page<AuditLogDTO> page = new PageImpl<>(Arrays.asList(auditLog), PageRequest.of(0, 20), 1);
        when(auditLogService.getAuditLogs(any(AuditLogFilterDTO.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].entityType").value("PROJECT"))
                .andExpect(jsonPath("$.content[0].action").value("CREATED"))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void getAuditLogs_AccessDenied_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getEntityTimeline_Success() throws Exception {
        // Arrange
        UUID logId1 = UUID.randomUUID();
        UUID logId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<AuditLogDTO> timeline = Arrays.asList(
                AuditLogDTO.builder()
                        .id(logId1)
                        .entityType(EntityType.PROJECT)
                        .entityId("project-123")
                        .action(AuditAction.CREATED)
                        .userId(userId)
                        .username("testuser")
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .description("Project created")
                        .build(),
                AuditLogDTO.builder()
                        .id(logId2)
                        .entityType(EntityType.PROJECT)
                        .entityId("project-123")
                        .action(AuditAction.UPDATED)
                        .userId(userId)
                        .username("testuser")
                        .timestamp(LocalDateTime.now())
                        .description("Project updated")
                        .build()
        );

        when(auditLogService.getEntityTimeline(eq(EntityType.PROJECT), eq("project-123"), anyInt()))
                .thenReturn(timeline);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs/entity/PROJECT/project-123")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].action").value("CREATED"))
                .andExpect(jsonPath("$[1].action").value("UPDATED"));
    }

    @Test
    @WithMockCustomUser
    void getUserActivity_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();

        AuditLogDTO auditLog = AuditLogDTO.builder()
                .id(logId)
                .entityType(EntityType.TASK)
                .entityId("task-456")
                .action(AuditAction.CREATED)
                .userId(userId)
                .username("testuser")
                .timestamp(LocalDateTime.now())
                .description("Task created")
                .build();

        Page<AuditLogDTO> page = new PageImpl<>(Arrays.asList(auditLog), PageRequest.of(0, 20), 1);
        when(auditLogService.getUserActivity(eq(userId), anyInt(), anyInt())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].action").value("CREATED"))
                .andExpect(jsonPath("$.content[0].entityType").value("TASK"));
    }

    @Test
    @WithMockCustomUser
    void getRecentActivity_Success() throws Exception {
        // Arrange
        UUID logId1 = UUID.randomUUID();
        UUID logId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<AuditLogDTO> recentActivity = Arrays.asList(
                AuditLogDTO.builder()
                        .id(logId1)
                        .entityType(EntityType.PROJECT)
                        .entityId("project-123")
                        .action(AuditAction.CREATED)
                        .userId(userId)
                        .username("user1")
                        .timestamp(LocalDateTime.now().minusHours(1))
                        .build(),
                AuditLogDTO.builder()
                        .id(logId2)
                        .entityType(EntityType.TASK)
                        .entityId("task-456")
                        .action(AuditAction.UPDATED)
                        .userId(userId)
                        .username("user2")
                        .timestamp(LocalDateTime.now().minusMinutes(30))
                        .build()
        );

        when(auditLogService.getRecentActivity(anyInt(), anyInt())).thenReturn(recentActivity);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs/recent")
                        .param("hours", "24")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].action").value("CREATED"))
                .andExpect(jsonPath("$[1].action").value("UPDATED"));
    }

    @Test
    @WithMockCustomUser
    void getEntityActivityCount_Success() throws Exception {
        // Arrange
        when(auditLogService.getActivityCount(eq(EntityType.PROJECT), eq("project-123")))
                .thenReturn(42L);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs/count/entity/PROJECT/project-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @WithMockCustomUser
    void getUserActivityCount_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(auditLogService.getUserActivityCount(eq(userId))).thenReturn(150L);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs/count/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("150"));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getAuditLogs_WithFilters_Success() throws Exception {
        // Arrange
        UUID logId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AuditLogDTO auditLog = AuditLogDTO.builder()
                .id(logId)
                .entityType(EntityType.USER)
                .entityId(userId.toString())
                .action(AuditAction.LOGIN)
                .userId(userId)
                .username("testuser")
                .timestamp(LocalDateTime.now())
                .description("User logged in")
                .build();

        Page<AuditLogDTO> page = new PageImpl<>(Arrays.asList(auditLog), PageRequest.of(0, 20), 1);
        when(auditLogService.getAuditLogs(any(AuditLogFilterDTO.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs")
                        .param("entityType", "USER")
                        .param("action", "LOGIN")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].entityType").value("USER"))
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"));
    }

    @Test
    @WithMockCustomUser
    void getEntityTimeline_DefaultLimit_Success() throws Exception {
        // Arrange
        UUID logId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<AuditLogDTO> timeline = Arrays.asList(
                AuditLogDTO.builder()
                        .id(logId)
                        .entityType(EntityType.TASK)
                        .entityId("task-789")
                        .action(AuditAction.STATUS_CHANGED)
                        .userId(userId)
                        .username("testuser")
                        .timestamp(LocalDateTime.now())
                        .description("Status changed to DONE")
                        .build()
        );

        when(auditLogService.getEntityTimeline(eq(EntityType.TASK), eq("task-789"), eq(50)))
                .thenReturn(timeline);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs/entity/TASK/task-789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].action").value("STATUS_CHANGED"));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getAuditLogs_EmptyResult_Success() throws Exception {
        // Arrange
        Page<AuditLogDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(auditLogService.getAuditLogs(any(AuditLogFilterDTO.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
