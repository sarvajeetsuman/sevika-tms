package online.sevika.tm.dto;
import online.sevika.tm.dto.UserResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UserResponse", description = "User response data")
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
