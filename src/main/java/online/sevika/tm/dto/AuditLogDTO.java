package online.sevika.tm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for AuditLog entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {

    private UUID id;
    private EntityType entityType;
    private String entityId;
    private AuditAction action;
    private UUID userId;
    private String username;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private String description;
}
