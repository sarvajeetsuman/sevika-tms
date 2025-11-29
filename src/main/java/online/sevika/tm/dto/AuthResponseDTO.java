package online.sevika.tm.dto;
import online.sevika.tm.dto.AuthResponseDTO;
import online.sevika.tm.dto.UserResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AuthResponse", description = "Authentication response with token")
public class AuthResponseDTO {
    private String token;
    private String type = "Bearer";
    private UserResponseDTO user;
}
