package online.sevika.tm.dto;
import online.sevika.tm.dto.TeamSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TeamSummary", description = "Team summary information")
public class TeamSummaryDTO {
    private UUID id;
    private String name;
}
