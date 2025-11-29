package online.sevika.tm.repository;

import online.sevika.tm.entity.AuditLog;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditLog entity
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Find audit logs by entity type and entity ID
     */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
            EntityType entityType,
            String entityId,
            Pageable pageable
    );

    /**
     * Find audit logs by user ID
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(EntityType entityType, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByActionOrderByTimestampDesc(AuditAction action, Pageable pageable);

    /**
     * Find audit logs within a date range
     */
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find audit logs by user ID and date range
     */
    Page<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find audit logs by entity type, entity ID, and date range
     */
    Page<AuditLog> findByEntityTypeAndEntityIdAndTimestampBetweenOrderByTimestampDesc(
            EntityType entityType,
            String entityId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Count audit logs by entity type and entity ID
     */
    long countByEntityTypeAndEntityId(EntityType entityType, String entityId);

    /**
     * Count audit logs by user ID
     */
    long countByUserId(UUID userId);

    /**
     * Get recent activity across all entities
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentActivity(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Find audit logs with complex filtering
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:#{#entityType == null} = true OR a.entityType = :entityType) AND " +
            "(:#{#entityId == null} = true OR a.entityId = :entityId) AND " +
            "(:#{#userId == null} = true OR a.userId = :userId) AND " +
            "(:#{#action == null} = true OR a.action = :action) AND " +
            "(:#{#startDate == null} = true OR a.timestamp >= :startDate) AND " +
            "(:#{#endDate == null} = true OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> findWithFilters(
            @Param("entityType") EntityType entityType,
            @Param("entityId") String entityId,
            @Param("userId") UUID userId,
            @Param("action") AuditAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
