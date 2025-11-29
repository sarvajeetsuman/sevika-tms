package online.sevika.tm.dto;
import online.sevika.tm.dto.ProjectSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Project;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProjectSummary", description = "Project summary information")
public class ProjectSummaryDTO {
    private UUID id;
    private String name;
    private Project.ProjectStatus status;
}
