package online.sevika.tm.controller;
import online.sevika.tm.dto.TaskRequestDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.TaskStatusUpdateRequestDTO;
import online.sevika.tm.dto.TaskUpdateRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.entity.Task;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for task operations.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Create a new task")
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskResponseDTO response = taskService.createTask(request, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieve all tasks with optional filters")
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID assignedToId,
            @RequestParam(required = false) Task.TaskStatus status,
            @RequestParam(required = false) Task.TaskPriority priority) {
        List<TaskResponseDTO> tasks = taskService.getAllTasks(projectId, assignedToId, status, priority);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieve a task by its ID")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable UUID id) {
        TaskResponseDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project", description = "Retrieve all tasks for a specific project")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByProject(@PathVariable UUID projectId) {
        List<TaskResponseDTO> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assigned/{userId}")
    @Operation(summary = "Get tasks assigned to user", description = "Retrieve all tasks assigned to a specific user")
    public ResponseEntity<List<TaskResponseDTO>> getTasksAssignedToUser(@PathVariable UUID userId) {
        List<TaskResponseDTO> tasks = taskService.getTasksAssignedToUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    @Operation(summary = "Get my tasks", description = "Retrieve tasks assigned to the current user")
    public ResponseEntity<List<TaskResponseDTO>> getMyTasks(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<TaskResponseDTO> tasks = taskService.getTasksAssignedToUser(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks", description = "Retrieve all overdue tasks")
    public ResponseEntity<List<TaskResponseDTO>> getOverdueTasks() {
        List<TaskResponseDTO> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Update task information")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequestDTO request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskResponseDTO response = taskService.updateTask(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Update the status of a task")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TaskStatusUpdateRequestDTO request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskResponseDTO response = taskService.updateTaskStatus(id, request.getStatus(), user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        taskService.deleteTask(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
