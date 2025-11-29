package online.sevika.tm.dto;
import online.sevika.tm.dto.TaskUpdateRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "TaskUpdateRequest", description = "Request to update a task")
public class TaskUpdateRequestDTO {
    @Size(min = 3, max = 200, message = "Task title must be between 3 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Task.TaskStatus status;

    private Task.TaskPriority priority;

    private UUID assignedToId;

    private LocalDate dueDate;
}
