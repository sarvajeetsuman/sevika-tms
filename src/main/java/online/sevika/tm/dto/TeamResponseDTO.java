package online.sevika.tm.dto;
import online.sevika.tm.dto.TeamResponseDTO;
import online.sevika.tm.dto.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TeamResponse", description = "Team response data")
public class TeamResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private UserSummaryDTO owner;
    private UUID ownerId;
    private String ownerName;
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
