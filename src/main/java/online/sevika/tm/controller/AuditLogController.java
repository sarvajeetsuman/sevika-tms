package online.sevika.tm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.dto.AuditLogDTO;
import online.sevika.tm.dto.AuditLogFilterDTO;
import online.sevika.tm.entity.enums.EntityType;
import online.sevika.tm.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for audit log operations
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log and activity tracking APIs")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get audit logs with filters", description = "Retrieve audit logs with optional filters (Admin only)")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(@Valid @ModelAttribute AuditLogFilterDTO filter) {
        Page<AuditLogDTO> auditLogs = auditLogService.getAuditLogs(filter);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get entity activity timeline", description = "Retrieve activity timeline for a specific entity")
    public ResponseEntity<List<AuditLogDTO>> getEntityTimeline(
            @PathVariable EntityType entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        List<AuditLogDTO> timeline = auditLogService.getEntityTimeline(entityType, entityId, limit);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user activity history", description = "Retrieve activity history for a specific user")
    public ResponseEntity<Page<AuditLogDTO>> getUserActivity(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLogDTO> activity = auditLogService.getUserActivity(userId, page, size);
        return ResponseEntity.ok(activity);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get recent activity", description = "Retrieve recent activity across the system")
    public ResponseEntity<List<AuditLogDTO>> getRecentActivity(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "100") int limit
    ) {
        List<AuditLogDTO> recentActivity = auditLogService.getRecentActivity(hours, limit);
        return ResponseEntity.ok(recentActivity);
    }

    @GetMapping("/count/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get entity activity count", description = "Get total activity count for an entity")
    public ResponseEntity<Long> getEntityActivityCount(
            @PathVariable EntityType entityType,
            @PathVariable String entityId
    ) {
        long count = auditLogService.getActivityCount(entityType, entityId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user activity count", description = "Get total activity count for a user")
    public ResponseEntity<Long> getUserActivityCount(@PathVariable UUID userId) {
        long count = auditLogService.getUserActivityCount(userId);
        return ResponseEntity.ok(count);
    }
}
