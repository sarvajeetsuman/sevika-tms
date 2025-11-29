package online.sevika.tm.repository;

import online.sevika.tm.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payments by user ID
     */
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find payments by subscription ID
     */
    List<Payment> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId);

    /**
     * Find payment by Razorpay order ID
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
