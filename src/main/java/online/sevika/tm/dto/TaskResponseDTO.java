package online.sevika.tm.dto;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TaskResponse", description = "Task response data")
public class TaskResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private ProjectSummaryDTO project;
    private UserSummaryDTO assignedTo;
    private UserSummaryDTO createdBy;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
