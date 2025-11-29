package online.sevika.tm.service;
import online.sevika.tm.dto.PaymentResponseDTO;
import online.sevika.tm.dto.PaymentVerificationRequestDTO;
import online.sevika.tm.dto.RazorpayOrderResponseDTO;
import online.sevika.tm.dto.SubscriptionRequestDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Subscription operations.
 */
public interface SubscriptionService {

    /**
     * Create a new subscription (initiate payment)
     */
    RazorpayOrderResponseDTO createSubscription(SubscriptionRequestDTO request, UUID userId);

    /**
     * Verify payment and activate subscription
     */
    SubscriptionResponseDTO verifyAndActivateSubscription(PaymentVerificationRequestDTO request);

    /**
     * Get user's active subscription
     */
    SubscriptionResponseDTO getActiveSubscription(UUID userId);

    /**
     * Get all user's subscriptions
     */
    List<SubscriptionResponseDTO> getUserSubscriptions(UUID userId);

    /**
     * Cancel subscription
     */
    void cancelSubscription(UUID subscriptionId, UUID userId);

    /**
     * Get subscription by ID
     */
    SubscriptionResponseDTO getSubscriptionById(UUID subscriptionId);

    /**
     * Get user's payment history
     */
    List<PaymentResponseDTO> getUserPayments(UUID userId);

    /**
     * Update expired subscriptions (scheduled task)
     */
    void updateExpiredSubscriptions();
}
