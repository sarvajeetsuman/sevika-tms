package online.sevika.tm.dto;
import online.sevika.tm.dto.TeamMemberUpdateRoleDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.TeamRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TeamMemberUpdateRole", description = "Request to update team member role")
public class TeamMemberUpdateRoleDTO {
    @NotNull(message = "Role is required")
    private TeamRole role;
}
