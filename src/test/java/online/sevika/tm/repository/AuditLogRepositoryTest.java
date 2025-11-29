package online.sevika.tm.repository;

import online.sevika.tm.entity.AuditLog;
import online.sevika.tm.entity.enums.AuditAction;
import online.sevika.tm.entity.enums.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for AuditLogRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private UUID testUserId;
    private String testEntityId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID().toString();

        // Create test audit logs
        AuditLog log1 = AuditLog.builder()
                .entityType(EntityType.PROJECT)
                .entityId(testEntityId)
                .action(AuditAction.CREATED)
                .userId(testUserId)
                .username("testuser")
                .description("Project created")
                .timestamp(LocalDateTime.now().minusHours(2))
                .build();

        AuditLog log2 = AuditLog.builder()
                .entityType(EntityType.PROJECT)
                .entityId(testEntityId)
                .action(AuditAction.UPDATED)
                .userId(testUserId)
                .username("testuser")
                .description("Project updated")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build();

        AuditLog log3 = AuditLog.builder()
                .entityType(EntityType.TASK)
                .entityId(UUID.randomUUID().toString())
                .action(AuditAction.CREATED)
                .userId(testUserId)
                .username("testuser")
                .description("Task created")
                .timestamp(LocalDateTime.now().minusMinutes(30))
                .build();

        auditLogRepository.saveAll(List.of(log1, log2, log3));
    }

    @Test
    void testFindByEntityTypeAndEntityId() {
        Page<AuditLog> result = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
                EntityType.PROJECT,
                testEntityId,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getAction()).isEqualTo(AuditAction.UPDATED);
    }

    @Test
    void testFindByUserId() {
        Page<AuditLog> result = auditLogRepository.findByUserIdOrderByTimestampDesc(
                testUserId,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testFindByEntityType() {
        Page<AuditLog> result = auditLogRepository.findByEntityTypeOrderByTimestampDesc(
                EntityType.PROJECT,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void testFindByAction() {
        Page<AuditLog> result = auditLogRepository.findByActionOrderByTimestampDesc(
                AuditAction.CREATED,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void testFindByTimestampBetween() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();

        Page<AuditLog> result = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
                start,
                end,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    void testCountByEntityTypeAndEntityId() {
        long count = auditLogRepository.countByEntityTypeAndEntityId(EntityType.PROJECT, testEntityId);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByUserId() {
        long count = auditLogRepository.countByUserId(testUserId);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testFindRecentActivity() {
        LocalDateTime since = LocalDateTime.now().minusHours(3);
        List<AuditLog> result = auditLogRepository.findRecentActivity(since, PageRequest.of(0, 10));

        assertThat(result).hasSize(3);
    }

    @Test
    void testFindWithFilters() {
        Page<AuditLog> result = auditLogRepository.findWithFilters(
                EntityType.PROJECT,
                null,
                testUserId,
                null,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
    }
}
