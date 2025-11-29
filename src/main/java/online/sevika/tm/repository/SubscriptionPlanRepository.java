package online.sevika.tm.repository;

import online.sevika.tm.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SubscriptionPlan entity.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    /**
     * Find subscription plan by name
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Find all active subscription plans
     */
    List<SubscriptionPlan> findByActiveTrue();

    /**
     * Check if a plan exists by name
     */
    boolean existsByName(String name);
}
