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
 * DTO for filtering audit logs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogFilterDTO {

    private EntityType entityType;
    private String entityId;
    private UUID userId;
    private AuditAction action;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer page;
    private Integer size;
}
