package online.sevika.tm.repository;

import online.sevika.tm.entity.SubscriptionPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SubscriptionPlanRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubscriptionPlanRepositoryTest {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByName_Success() {
        // Arrange
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name("Test Plan " + UUID.randomUUID())
                .description("Test Description")
                .price(BigDecimal.valueOf(999.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(10)
                .maxTasksPerProject(100)
                .maxTeamMembers(5)
                .fileAttachments(true)
                .advancedReporting(false)
                .prioritySupport(false)
                .apiAccess(false)
                .active(true)
                .build();
        entityManager.persist(plan);
        entityManager.flush();

        // Act
        Optional<SubscriptionPlan> result = subscriptionPlanRepository.findByName(plan.getName());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(plan.getName());
        assertThat(result.get().getPrice()).isEqualTo(BigDecimal.valueOf(999.00));
    }

    @Test
    void findByName_NotFound() {
        // Act
        Optional<SubscriptionPlan> result = subscriptionPlanRepository.findByName("NonExistentPlan");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByActiveTrue_ReturnsActivePlans() {
        // Arrange
        SubscriptionPlan activePlan = SubscriptionPlan.builder()
                .name("Active Plan " + UUID.randomUUID())
                .price(BigDecimal.valueOf(499.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(5)
                .maxTasksPerProject(50)
                .maxTeamMembers(3)
                .fileAttachments(false)
                .advancedReporting(false)
                .prioritySupport(false)
                .apiAccess(false)
                .active(true)
                .build();
        entityManager.persist(activePlan);

        SubscriptionPlan inactivePlan = SubscriptionPlan.builder()
                .name("Inactive Plan " + UUID.randomUUID())
                .price(BigDecimal.valueOf(299.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(3)
                .maxTasksPerProject(30)
                .maxTeamMembers(2)
                .fileAttachments(false)
                .advancedReporting(false)
                .prioritySupport(false)
                .apiAccess(false)
                .active(false)
                .build();
        entityManager.persist(inactivePlan);
        entityManager.flush();

        // Act
        List<SubscriptionPlan> activePlans = subscriptionPlanRepository.findByActiveTrue();

        // Assert
        assertThat(activePlans).hasSizeGreaterThanOrEqualTo(1);
        assertThat(activePlans).anyMatch(p -> p.getId().equals(activePlan.getId()));
        assertThat(activePlans).noneMatch(p -> p.getId().equals(inactivePlan.getId()));
    }

    @Test
    void existsByName_ReturnsTrue() {
        // Arrange
        String planName = "Unique Plan " + UUID.randomUUID();
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(planName)
                .price(BigDecimal.valueOf(999.00))
                .billingCycle(SubscriptionPlan.BillingCycle.YEARLY)
                .maxProjects(50)
                .maxTasksPerProject(500)
                .maxTeamMembers(20)
                .fileAttachments(true)
                .advancedReporting(true)
                .prioritySupport(true)
                .apiAccess(false)
                .active(true)
                .build();
        entityManager.persist(plan);
        entityManager.flush();

        // Act
        boolean exists = subscriptionPlanRepository.existsByName(planName);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ReturnsFalse() {
        // Act
        boolean exists = subscriptionPlanRepository.existsByName("NonExistentPlan");

        // Assert
        assertThat(exists).isFalse();
    }
}
