package online.sevika.tm.dto;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Project;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProjectResponse", description = "Project response")
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private UserSummaryDTO owner;
    private Project.ProjectStatus status;
    private Integer taskCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
