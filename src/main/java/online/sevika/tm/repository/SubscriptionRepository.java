package online.sevika.tm.repository;

import online.sevika.tm.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Subscription entity.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    /**
     * Find active subscription by user ID
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status IN ('ACTIVE', 'TRIAL') ORDER BY s.createdAt DESC")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") UUID userId);

    /**
     * Find all subscriptions by user ID
     */
    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find subscriptions by status
     */
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    /**
     * Find expiring subscriptions
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :startDate AND :endDate")
    List<Subscription> findExpiringSubscriptions(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find expired subscriptions that need status update
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :now")
    List<Subscription> findExpiredActiveSubscriptions(@Param("now") LocalDateTime now);
}
