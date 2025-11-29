package online.sevika.tm.dto;
import online.sevika.tm.dto.UserSummaryDTO;

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
@Schema(name = "UserSummary", description = "User summary information")
public class UserSummaryDTO {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
}
