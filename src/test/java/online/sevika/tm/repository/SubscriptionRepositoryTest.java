package online.sevika.tm.repository;

import online.sevika.tm.entity.Subscription;
import online.sevika.tm.entity.SubscriptionPlan;
import online.sevika.tm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SubscriptionRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("subscriber" + UUID.randomUUID())
                .email("subscriber" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(user);

        plan = SubscriptionPlan.builder()
                .name("Test Plan " + UUID.randomUUID())
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
    }

    @Test
    void findActiveSubscriptionByUserId_ReturnsActiveSubscription() {
        // Arrange
        Subscription activeSubscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .build();
        entityManager.persist(activeSubscription);
        entityManager.flush();

        // Act
        Optional<Subscription> result = subscriptionRepository.findActiveSubscriptionByUserId(user.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(activeSubscription.getId());
        assertThat(result.get().getStatus()).isEqualTo(Subscription.SubscriptionStatus.ACTIVE);
    }

    @Test
    void findActiveSubscriptionByUserId_ReturnsTrialSubscription() {
        // Arrange
        Subscription trialSubscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.TRIAL)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(14))
                .autoRenew(true)
                .build();
        entityManager.persist(trialSubscription);
        entityManager.flush();

        // Act
        Optional<Subscription> result = subscriptionRepository.findActiveSubscriptionByUserId(user.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(Subscription.SubscriptionStatus.TRIAL);
    }

    @Test
    void findActiveSubscriptionByUserId_NotFound() {
        // Act
        Optional<Subscription> result = subscriptionRepository.findActiveSubscriptionByUserId(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ReturnsSubscriptions() {
        // Arrange
        Subscription sub1 = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.EXPIRED)
                .startDate(LocalDateTime.now().minusMonths(2))
                .endDate(LocalDateTime.now().minusMonths(1))
                .autoRenew(false)
                .build();
        entityManager.persist(sub1);

        Subscription sub2 = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .build();
        entityManager.persist(sub2);
        entityManager.flush();

        // Act
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // Assert
        assertThat(subscriptions).hasSizeGreaterThanOrEqualTo(2);
        assertThat(subscriptions).anyMatch(s -> s.getId().equals(sub1.getId()));
        assertThat(subscriptions).anyMatch(s -> s.getId().equals(sub2.getId()));
    }

    @Test
    void findByStatus_ReturnsSubscriptionsByStatus() {
        // Arrange
        Subscription activeSubscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .build();
        entityManager.persist(activeSubscription);
        entityManager.flush();

        // Act
        List<Subscription> subscriptions = subscriptionRepository.findByStatus(Subscription.SubscriptionStatus.ACTIVE);

        // Assert
        assertThat(subscriptions).hasSizeGreaterThanOrEqualTo(1);
        assertThat(subscriptions).anyMatch(s -> s.getId().equals(activeSubscription.getId()));
    }

    @Test
    void findExpiringSubscriptions_ReturnsSubscriptionsInDateRange() {
        // Arrange
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Subscription expiringSoon = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusMonths(1))
                .endDate(tomorrow)
                .autoRenew(true)
                .build();
        entityManager.persist(expiringSoon);
        entityManager.flush();

        // Act
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        List<Subscription> expiringSubscriptions = subscriptionRepository.findExpiringSubscriptions(startDate, endDate);

        // Assert
        assertThat(expiringSubscriptions).hasSizeGreaterThanOrEqualTo(1);
        assertThat(expiringSubscriptions).anyMatch(s -> s.getId().equals(expiringSoon.getId()));
    }

    @Test
    void findExpiredActiveSubscriptions_ReturnsExpiredSubscriptions() {
        // Arrange
        Subscription expiredSubscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusMonths(2))
                .endDate(LocalDateTime.now().minusDays(1))
                .autoRenew(false)
                .build();
        entityManager.persist(expiredSubscription);
        entityManager.flush();

        // Act
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredActiveSubscriptions(LocalDateTime.now());

        // Assert
        assertThat(expiredSubscriptions).hasSizeGreaterThanOrEqualTo(1);
        assertThat(expiredSubscriptions).anyMatch(s -> s.getId().equals(expiredSubscription.getId()));
    }
}
