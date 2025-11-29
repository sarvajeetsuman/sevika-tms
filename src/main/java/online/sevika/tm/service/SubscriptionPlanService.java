package online.sevika.tm.service;
import online.sevika.tm.dto.SubscriptionPlanRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for SubscriptionPlan operations.
 */
public interface SubscriptionPlanService {

    /**
     * Create a new subscription plan
     */
    SubscriptionPlanResponseDTO createPlan(SubscriptionPlanRequestDTO request);

    /**
     * Get subscription plan by ID
     */
    SubscriptionPlanResponseDTO getPlanById(UUID planId);

    /**
     * Get all active subscription plans
     */
    List<SubscriptionPlanResponseDTO> getActivePlans();

    /**
     * Get all subscription plans (including inactive)
     */
    List<SubscriptionPlanResponseDTO> getAllPlans();

    /**
     * Update subscription plan
     */
    SubscriptionPlanResponseDTO updatePlan(UUID planId, SubscriptionPlanRequestDTO request);

    /**
     * Deactivate subscription plan
     */
    void deactivatePlan(UUID planId);
}
