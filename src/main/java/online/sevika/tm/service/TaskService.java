package online.sevika.tm.service;
import online.sevika.tm.dto.TaskRequestDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.TaskUpdateRequestDTO;

import online.sevika.tm.entity.Task;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Task operations.
 */
public interface TaskService {

    /**
     * Create a new task
     */
    TaskResponseDTO createTask(TaskRequestDTO request, UUID createdById);

    /**
     * Get task by ID
     */
    TaskResponseDTO getTaskById(UUID id);

    /**
     * Get all tasks with optional filters
     */
    List<TaskResponseDTO> getAllTasks(UUID projectId, UUID assignedToId, 
                                       Task.TaskStatus status, Task.TaskPriority priority);

    /**
     * Get tasks by project
     */
    List<TaskResponseDTO> getTasksByProject(UUID projectId);

    /**
     * Get tasks assigned to user
     */
    List<TaskResponseDTO> getTasksAssignedToUser(UUID userId);

    /**
     * Update task
     */
    TaskResponseDTO updateTask(UUID id, TaskUpdateRequestDTO updateRequest, UUID userId);

    /**
     * Update task status
     */
    TaskResponseDTO updateTaskStatus(UUID id, Task.TaskStatus status, UUID userId);

    /**
     * Delete task
     */
    void deleteTask(UUID id, UUID userId);

    /**
     * Get overdue tasks
     */
    List<TaskResponseDTO> getOverdueTasks();
}
