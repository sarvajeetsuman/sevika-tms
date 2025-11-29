package online.sevika.tm.service.impl;

import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.TaskRequestDTO;
import online.sevika.tm.dto.TaskUpdateRequestDTO;
import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.Task;
import online.sevika.tm.entity.User;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.TaskMapper;
import online.sevika.tm.repository.ProjectRepository;
import online.sevika.tm.repository.TaskRepository;
import online.sevika.tm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User owner;
    private User assignee;
    private Project project;
    private Task task;
    private TaskRequestDTO request;
    private TaskResponseDTO response;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(UUID.randomUUID())
                .username("owner")
                .email("owner@example.com")
                .role(User.Role.USER)
                .build();

        assignee = User.builder()
                .id(UUID.randomUUID())
                .username("assignee")
                .email("assignee@example.com")
                .role(User.Role.USER)
                .build();

        project = Project.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();

        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Test Task")
                .description("Test Description")
                .project(project)
                .createdBy(owner)
                .assignedTo(assignee)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        request = TaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .projectId(project.getId())
                .assignedToId(assignee.getId())
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        response = TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .build();
    }

    @Test
    void createTask_Success() {
        // Arrange
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponseDTO result = taskService.createTask(request, owner.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ProjectNotFound_ThrowsException() {
        // Arrange
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(request, owner.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void createTask_WithoutAssignee_Success() {
        // Arrange
        TaskRequestDTO requestWithoutAssignee = TaskRequestDTO.builder()
                .title("Test Task")
                .projectId(project.getId())
                .priority(Task.TaskPriority.MEDIUM)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(taskMapper.toEntity(requestWithoutAssignee)).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponseDTO result = taskService.createTask(requestWithoutAssignee, owner.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTaskById_Success() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponseDTO result = taskService.getTaskById(task.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(task.getId());
    }

    @Test
    void getTaskById_NotFound_ThrowsException() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTaskById(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void getAllTasks_WithFilters_ReturnsTasks() {
        // Arrange
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findTasksWithFilters(project.getId(), assignee.getId(), 
                Task.TaskStatus.TODO, Task.TaskPriority.MEDIUM)).thenReturn(tasks);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        List<TaskResponseDTO> results = taskService.getAllTasks(
                project.getId(), assignee.getId(), Task.TaskStatus.TODO, Task.TaskPriority.MEDIUM);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Test Task");
    }

    @Test
    void getTasksByProject_ReturnsTasks() {
        // Arrange
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByProjectId(project.getId())).thenReturn(tasks);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        List<TaskResponseDTO> results = taskService.getTasksByProject(project.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Test Task");
    }

    @Test
    void getTasksAssignedToUser_ReturnsTasks() {
        // Arrange
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByAssignedToId(assignee.getId())).thenReturn(tasks);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        List<TaskResponseDTO> results = taskService.getTasksAssignedToUser(assignee.getId());

        // Assert
        assertThat(results).hasSize(1);
    }

    @Test
    void updateTask_AsOwner_Success() {
        // Arrange
        TaskUpdateRequestDTO updateRequest = TaskUpdateRequestDTO.builder()
                .title("Updated Task")
                .status(Task.TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponseDTO result = taskService.updateTask(task.getId(), updateRequest, owner.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(taskMapper).updateEntityFromDto(updateRequest, task);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_AsAssignee_Success() {
        // Arrange
        TaskUpdateRequestDTO updateRequest = TaskUpdateRequestDTO.builder()
                .status(Task.TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponseDTO result = taskService.updateTask(task.getId(), updateRequest, assignee.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_Unauthorized_ThrowsException() {
        // Arrange
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other")
                .role(User.Role.USER)
                .build();

        TaskUpdateRequestDTO updateRequest = TaskUpdateRequestDTO.builder()
                .title("Updated Task")
                .build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThatThrownBy(() -> taskService.updateTask(task.getId(), updateRequest, otherUser.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("don't have permission");
    }

    @Test
    void updateTaskStatus_Success() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        // Act
        TaskResponseDTO result = taskService.updateTaskStatus(task.getId(), Task.TaskStatus.DONE, owner.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        // Act
        taskService.deleteTask(task.getId(), owner.getId());

        // Assert
        verify(taskRepository).deleteById(task.getId());
    }

    @Test
    void deleteTask_Unauthorized_ThrowsException() {
        // Arrange
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other")
                .role(User.Role.USER)
                .build();

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(task.getId(), otherUser.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("don't have permission");
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void getOverdueTasks_ReturnsTasks() {
        // Arrange
        Task overdueTask = Task.builder()
                .id(UUID.randomUUID())
                .title("Overdue Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        List<Task> tasks = Arrays.asList(overdueTask);
        when(taskRepository.findOverdueTasks(any(LocalDate.class))).thenReturn(tasks);
        when(taskMapper.toResponse(overdueTask)).thenReturn(response);

        // Act
        List<TaskResponseDTO> results = taskService.getOverdueTasks();

        // Assert
        assertThat(results).hasSize(1);
    }
}
