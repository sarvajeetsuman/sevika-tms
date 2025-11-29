package online.sevika.tm.service.impl;

import online.sevika.tm.dto.AuditLogDTO;
import online.sevika.tm.dto.AuditLogFilterDTO;
import online.sevika.tm.entity.AuditLog;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import online.sevika.tm.mapper.AuditLogMapper;
import online.sevika.tm.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for AuditLogServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog testAuditLog;
    private AuditLogDTO testAuditLogDTO;
    private UUID testUserId;
    private String testEntityId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID().toString();

        testAuditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityType(EntityType.PROJECT)
                .entityId(testEntityId)
                .action(AuditAction.CREATED)
                .userId(testUserId)
                .username("testuser")
                .description("Project created")
                .timestamp(LocalDateTime.now())
                .build();

        testAuditLogDTO = AuditLogDTO.builder()
                .id(testAuditLog.getId())
                .entityType(EntityType.PROJECT)
                .entityId(testEntityId)
                .action(AuditAction.CREATED)
                .userId(testUserId)
                .username("testuser")
                .description("Project created")
                .timestamp(testAuditLog.getTimestamp())
                .build();
    }

    @Test
    void testLogActivity() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditLogService.logActivity(
                EntityType.PROJECT,
                testEntityId,
                AuditAction.CREATED,
                testUserId,
                "testuser",
                null,
                "newValue",
                "Project created",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetAuditLogs() {
        AuditLogFilterDTO filter = AuditLogFilterDTO.builder()
                .entityType(EntityType.PROJECT)
                .page(0)
                .size(20)
                .build();

        Page<AuditLog> auditLogPage = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(auditLogPage);
        when(auditLogMapper.toDTO(testAuditLog)).thenReturn(testAuditLogDTO);

        Page<AuditLogDTO> result = auditLogService.getAuditLogs(filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEntityType()).isEqualTo(EntityType.PROJECT);
        verify(auditLogRepository, times(1)).findWithFilters(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)
        );
    }

    @Test
    void testGetEntityTimeline() {
        Page<AuditLog> auditLogPage = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                eq(EntityType.PROJECT),
                eq(testEntityId),
                any(Pageable.class)
        )).thenReturn(auditLogPage);
        when(auditLogMapper.toDTO(testAuditLog)).thenReturn(testAuditLogDTO);

        List<AuditLogDTO> result = auditLogService.getEntityTimeline(EntityType.PROJECT, testEntityId, 50);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityId()).isEqualTo(testEntityId);
        verify(auditLogRepository, times(1)).findByEntityTypeAndEntityIdOrderByTimestampDesc(
                eq(EntityType.PROJECT),
                eq(testEntityId),
                any(Pageable.class)
        );
    }

    @Test
    void testGetUserActivity() {
        Page<AuditLog> auditLogPage = new PageImpl<>(Arrays.asList(testAuditLog));
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(
                eq(testUserId),
                any(Pageable.class)
        )).thenReturn(auditLogPage);
        when(auditLogMapper.toDTO(testAuditLog)).thenReturn(testAuditLogDTO);

        Page<AuditLogDTO> result = auditLogService.getUserActivity(testUserId, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(testUserId);
        verify(auditLogRepository, times(1)).findByUserIdOrderByTimestampDesc(
                eq(testUserId),
                any(Pageable.class)
        );
    }

    @Test
    void testGetRecentActivity() {
        List<AuditLog> auditLogs = Arrays.asList(testAuditLog);
        when(auditLogRepository.findRecentActivity(
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(auditLogs);
        when(auditLogMapper.toDTO(testAuditLog)).thenReturn(testAuditLogDTO);

        List<AuditLogDTO> result = auditLogService.getRecentActivity(24, 100);

        assertThat(result).hasSize(1);
        verify(auditLogRepository, times(1)).findRecentActivity(
                any(LocalDateTime.class),
                any(Pageable.class)
        );
    }

    @Test
    void testGetActivityCount() {
        when(auditLogRepository.countByEntityTypeAndEntityId(
                eq(EntityType.PROJECT),
                eq(testEntityId)
        )).thenReturn(5L);

        long count = auditLogService.getActivityCount(EntityType.PROJECT, testEntityId);

        assertThat(count).isEqualTo(5L);
        verify(auditLogRepository, times(1)).countByEntityTypeAndEntityId(
                eq(EntityType.PROJECT),
                eq(testEntityId)
        );
    }

    @Test
    void testGetUserActivityCount() {
        when(auditLogRepository.countByUserId(eq(testUserId))).thenReturn(10L);

        long count = auditLogService.getUserActivityCount(testUserId);

        assertThat(count).isEqualTo(10L);
        verify(auditLogRepository, times(1)).countByUserId(eq(testUserId));
    }
}
