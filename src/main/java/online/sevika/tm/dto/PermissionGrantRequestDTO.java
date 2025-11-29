package online.sevika.tm.dto;
import online.sevika.tm.dto.PermissionGrantRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.PermissionType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PermissionGrantRequest", description = "Request to grant a permission")
public class PermissionGrantRequestDTO {
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private UUID teamId;

    @NotNull(message = "Permission level is required")
    private PermissionType permissionLevel;
    
    @NotNull(message = "Permission is required")
    private PermissionType permission;
}
