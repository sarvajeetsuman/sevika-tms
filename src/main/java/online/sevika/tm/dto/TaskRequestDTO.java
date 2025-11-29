package online.sevika.tm.dto;
import online.sevika.tm.dto.TaskRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Task;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TaskRequest", description = "Request to create a new task")
public class TaskRequestDTO {
    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID assignedToId;

    @Builder.Default
    private Task.TaskPriority priority = Task.TaskPriority.MEDIUM;

    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
}
