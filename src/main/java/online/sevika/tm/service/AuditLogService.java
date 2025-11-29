package online.sevika.tm.service;

import online.sevika.tm.dto.AuditLogDTO;
import online.sevika.tm.dto.AuditLogFilterDTO;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for audit log operations
 */
public interface AuditLogService {

    /**
     * Log an audit entry
     */
    void logActivity(
            EntityType entityType,
            String entityId,
            AuditAction action,
            UUID userId,
            String username,
            String oldValue,
            String newValue,
            String description,
            String ipAddress,
            String userAgent
    );

    /**
     * Get audit logs with filtering
     */
    Page<AuditLogDTO> getAuditLogs(AuditLogFilterDTO filter);

    /**
     * Get activity timeline for a specific entity
     */
    List<AuditLogDTO> getEntityTimeline(EntityType entityType, String entityId, int limit);

    /**
     * Get user activity history
     */
    Page<AuditLogDTO> getUserActivity(UUID userId, int page, int size);

    /**
     * Get recent activity across the system
     */
    List<AuditLogDTO> getRecentActivity(int hours, int limit);

    /**
     * Get audit log statistics
     */
    long getActivityCount(EntityType entityType, String entityId);

    /**
     * Get user activity count
     */
    long getUserActivityCount(UUID userId);
}
