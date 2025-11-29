package online.sevika.tm.service.impl;
import online.sevika.tm.dto.TaskRequestDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.TaskUpdateRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.Task;
import online.sevika.tm.entity.User;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.TaskMapper;
import online.sevika.tm.repository.ProjectRepository;
import online.sevika.tm.repository.TaskRepository;
import online.sevika.tm.repository.UserRepository;
import online.sevika.tm.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of TaskService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request, UUID createdById) {
        log.info("Creating new task: {} for project ID: {}", request.getTitle(), request.getProjectId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + createdById));

        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setCreatedBy(createdBy);

        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getAssignedToId()));
            task.setAssignedTo(assignedTo);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponseDTO getTaskById(UUID id) {
        log.debug("Fetching task by ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponseDTO> getAllTasks(UUID projectId, UUID assignedToId, 
                                               Task.TaskStatus status, Task.TaskPriority priority) {
        log.debug("Fetching all tasks with filters");
        return taskRepository.findTasksWithFilters(projectId, assignedToId, status, priority).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> getTasksByProject(UUID projectId) {
        log.debug("Fetching tasks for project ID: {}", projectId);
        return taskRepository.findByProjectId(projectId).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> getTasksAssignedToUser(UUID userId) {
        log.debug("Fetching tasks assigned to user ID: {}", userId);
        return taskRepository.findByAssignedToId(userId).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(UUID id, TaskUpdateRequestDTO updateRequest, UUID userId) {
        log.info("Updating task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        validateTaskAccess(task, user);

        taskMapper.updateEntityFromDto(updateRequest, task);

        if (updateRequest.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(updateRequest.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + updateRequest.getAssignedToId()));
            task.setAssignedTo(assignedTo);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with ID: {}", updatedTask.getId());

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatus(UUID id, Task.TaskStatus status, UUID userId) {
        log.info("Updating task status for task ID: {} to {}", id, status);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        validateTaskAccess(task, user);

        task.setStatus(status);

        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated successfully");

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id, UUID userId) {
        log.info("Deleting task with ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        validateTaskAccess(task, user);

        taskRepository.deleteById(id);
        log.info("Task deleted successfully with ID: {}", id);
    }

    @Override
    public List<TaskResponseDTO> getOverdueTasks() {
        log.debug("Fetching overdue tasks");
        return taskRepository.findOverdueTasks(LocalDate.now()).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate if user has access to modify the task
     */
    private void validateTaskAccess(Task task, User user) {
        boolean isOwner = task.getProject().getOwner().getId().equals(user.getId());
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(user.getId());
        boolean isCreator = task.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAssignee && !isCreator && !isAdmin) {
            throw new UnauthorizedException("You don't have permission to modify this task");
        }
    }
}
