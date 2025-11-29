package online.sevika.tm.dto;
import online.sevika.tm.dto.TaskSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "TaskSummary", description = "Task summary information")
public class TaskSummaryDTO {
    private UUID id;
    private String title;
    private Task.TaskStatus status;
    private Task.TaskPriority priority;
    private LocalDate dueDate;
}
