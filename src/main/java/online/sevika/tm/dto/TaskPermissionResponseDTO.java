package online.sevika.tm.dto;
import online.sevika.tm.dto.TaskPermissionResponseDTO;
import online.sevika.tm.dto.TaskSummaryDTO;
import online.sevika.tm.dto.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.PermissionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TaskPermissionResponse", description = "Task permission response")
public class TaskPermissionResponseDTO {
    private UUID id;
    private UserSummaryDTO user;
    private TaskSummaryDTO task;
    private String taskTitle;
    private String teamName;
    private String username;
    private PermissionType permissionLevel;
    private LocalDateTime grantedAt;
    private String grantedByUsername;
}
