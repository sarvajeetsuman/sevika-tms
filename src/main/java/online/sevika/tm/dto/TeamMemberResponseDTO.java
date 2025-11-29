package online.sevika.tm.dto;
import online.sevika.tm.dto.TeamMemberResponseDTO;
import online.sevika.tm.dto.TeamSummaryDTO;
import online.sevika.tm.dto.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.TeamRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TeamMemberResponse", description = "Team member response data")
public class TeamMemberResponseDTO {
    private UUID id;
    private UserSummaryDTO user;
    private TeamSummaryDTO team;
    private UUID userId;
    private UUID teamId;
    private String teamName;
    private String username;
    private String email;
    private TeamRole role;
    private LocalDateTime joinedAt;
}
