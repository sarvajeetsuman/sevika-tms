package online.sevika.tm.dto;
import online.sevika.tm.dto.TeamMemberRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.TeamRole;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TeamMemberRequest", description = "Request to add a team member")
public class TeamMemberRequestDTO {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Builder.Default
    private TeamRole role = TeamRole.MEMBER;
}
