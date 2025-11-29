package online.sevika.tm.dto;
import online.sevika.tm.dto.TaskStatusUpdateRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Task;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TaskStatusUpdateRequest", description = "Request to update task status")
public class TaskStatusUpdateRequestDTO {
    @NotNull(message = "Status is required")
    private Task.TaskStatus status;
}
