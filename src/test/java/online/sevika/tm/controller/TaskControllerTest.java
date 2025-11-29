package online.sevika.tm.controller;

import online.sevika.tm.dto.TaskStatusUpdateRequestDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.TaskRequestDTO;
import online.sevika.tm.dto.TaskUpdateRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.Task;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.TaskService;
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
import java.time.LocalDate;
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
class TaskControllerTest {

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
    private TaskService taskService;

    @Test
    @WithMockCustomUser
    void createTask_Success() throws Exception {
        TaskRequestDTO request = TaskRequestDTO.builder()
                .title("New Task")
                .description("Task description")
                .projectId(UUID.randomUUID())
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(UUID.randomUUID())
                .title("New Task")
                .description("Task description")
                .priority(Task.TaskPriority.HIGH)
                .status(Task.TaskStatus.TODO)
                .dueDate(request.getDueDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskService.createTask(any(TaskRequestDTO.class), any(UUID.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_Unauthenticated_Unauthorized() throws Exception {
        TaskRequestDTO request = TaskRequestDTO.builder()
                .title("New Task")
                .description("Task description")
                .projectId(UUID.randomUUID())
                .build();

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getAllTasks_Success() throws Exception {
        List<TaskResponseDTO> tasks = Arrays.asList(
                TaskResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .title("Task 1")
                        .priority(Task.TaskPriority.HIGH)
                        .status(Task.TaskStatus.TODO)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                TaskResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .title("Task 2")
                        .priority(Task.TaskPriority.MEDIUM)
                        .status(Task.TaskStatus.IN_PROGRESS)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        when(taskService.getAllTasks(any(), any(), any(), any())).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    @WithMockCustomUser
    void getTaskById_Success() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(taskId)
                .title("Test Task")
                .priority(Task.TaskPriority.MEDIUM)
                .status(Task.TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskService.getTaskById(taskId)).thenReturn(response);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @WithMockCustomUser
    void updateTask_Success() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskUpdateRequestDTO request = TaskUpdateRequestDTO.builder()
                .title("Updated Task")
                .description("Updated Description")
                .priority(Task.TaskPriority.MEDIUM)
                .status(Task.TaskStatus.IN_PROGRESS)
                .build();

        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(taskId)
                .title("Updated Task")
                .description("Updated Description")
                .priority(Task.TaskPriority.MEDIUM)
                .status(Task.TaskStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskService.updateTask(eq(taskId), any(TaskUpdateRequestDTO.class), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockCustomUser
    void updateTaskStatus_Success() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskStatusUpdateRequestDTO request = TaskStatusUpdateRequestDTO.builder()
                .status(Task.TaskStatus.DONE)
                .build();

        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(taskId)
                .title("Task")
                .priority(Task.TaskPriority.MEDIUM)
                .status(Task.TaskStatus.DONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskService.updateTaskStatus(eq(taskId), any(Task.TaskStatus.class), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockCustomUser
    void deleteTask_Success() throws Exception {
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskService).deleteTask(eq(taskId), any(UUID.class));

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
