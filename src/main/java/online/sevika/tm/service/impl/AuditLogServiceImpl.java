package online.sevika.tm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.dto.AuditLogDTO;
import online.sevika.tm.dto.AuditLogFilterDTO;
import online.sevika.tm.entity.AuditLog;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import online.sevika.tm.mapper.AuditLogMapper;
import online.sevika.tm.repository.AuditLogRepository;
import online.sevika.tm.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuditLogService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void logActivity(
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
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .userId(userId)
                    .username(username)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} by user {}", action, entityType, username);
        } catch (Exception e) {
            log.error("Failed to log activity: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogs(AuditLogFilterDTO filter) {
        Pageable pageable = PageRequest.of(
                filter.getPage() != null ? filter.getPage() : 0,
                filter.getSize() != null ? filter.getSize() : 20
        );

        Page<AuditLog> auditLogs = auditLogRepository.findWithFilters(
                filter.getEntityType(),
                filter.getEntityId(),
                filter.getUserId(),
                filter.getAction(),
                filter.getStartDate(),
                filter.getEndDate(),
                pageable
        );

        return auditLogs.map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getEntityTimeline(EntityType entityType, String entityId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                entityType,
                entityId,
                pageable
        );

        return auditLogs.getContent().stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getUserActivity(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        return auditLogs.map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getRecentActivity(int hours, int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        Pageable pageable = PageRequest.of(0, limit);
        List<AuditLog> auditLogs = auditLogRepository.findRecentActivity(since, pageable);

        return auditLogs.stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getActivityCount(EntityType entityType, String entityId) {
        return auditLogRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserActivityCount(UUID userId) {
        return auditLogRepository.countByUserId(userId);
    }
}
