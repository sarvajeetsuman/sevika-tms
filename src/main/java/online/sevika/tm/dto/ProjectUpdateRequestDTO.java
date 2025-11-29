package online.sevika.tm.dto;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Project;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProjectUpdateRequest", description = "Request to update a project")
public class ProjectUpdateRequestDTO {
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Project.ProjectStatus status;
}
