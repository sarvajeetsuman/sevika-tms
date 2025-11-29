package online.sevika.tm.service.impl;
import online.sevika.tm.dto.PaymentResponseDTO;
import online.sevika.tm.dto.PaymentVerificationRequestDTO;
import online.sevika.tm.dto.RazorpayOrderResponseDTO;
import online.sevika.tm.dto.SubscriptionRequestDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.entity.Payment;
import online.sevika.tm.entity.Subscription;
import online.sevika.tm.entity.SubscriptionPlan;
import online.sevika.tm.entity.User;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.PaymentMapper;
import online.sevika.tm.mapper.SubscriptionMapper;
import online.sevika.tm.repository.PaymentRepository;
import online.sevika.tm.repository.SubscriptionPlanRepository;
import online.sevika.tm.repository.SubscriptionRepository;
import online.sevika.tm.repository.UserRepository;
import online.sevika.tm.service.RazorpayService;
import online.sevika.tm.service.SubscriptionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SubscriptionService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PaymentMapper paymentMapper;
    private final RazorpayService razorpayService;

    @Override
    @Transactional
    public RazorpayOrderResponseDTO createSubscription(SubscriptionRequestDTO request, UUID userId) {
        log.info("Creating subscription for user: {} with plan: {}", userId, request.getPlanId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + request.getPlanId()));

        if (!plan.getActive()) {
            throw new IllegalArgumentException("This subscription plan is no longer available");
        }

        // Calculate subscription period
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, plan.getBillingCycle());

        // Create subscription in pending state
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.TRIAL)
                .startDate(startDate)
                .endDate(endDate)
                .autoRenew(request.getAutoRenew())
                .build();
        subscription = subscriptionRepository.save(subscription);

        try {
            // Create Razorpay order
            String receipt = "SUB_" + subscription.getId().toString();
            Order razorpayOrder = razorpayService.createOrder(plan.getPrice(), "INR", receipt);

            // Create payment record
            Payment payment = Payment.builder()
                    .user(user)
                    .subscription(subscription)
                    .amount(plan.getPrice())
                    .currency("INR")
                    .status(Payment.PaymentStatus.PENDING)
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .build();
            paymentRepository.save(payment);

            log.info("Razorpay order created for subscription: {}", subscription.getId());

            return RazorpayOrderResponseDTO.builder()
                    .orderId(razorpayOrder.get("id"))
                    .amount(razorpayOrder.get("amount").toString())
                    .currency(razorpayOrder.get("currency"))
                    .key(razorpayService.getKeyId())
                    .name("Sevika Task Management")
                    .description(plan.getName() + " - " + plan.getBillingCycle())
                    .prefillName(user.getFirstName() + " " + user.getLastName())
                    .prefillEmail(user.getEmail())
                    .subscriptionId(subscription.getId())
                    .build();

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public SubscriptionResponseDTO verifyAndActivateSubscription(PaymentVerificationRequestDTO request) {
        log.info("Verifying payment for subscription: {}", request.getSubscriptionId());

        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + request.getSubscriptionId()));

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + request.getRazorpayOrderId()));

        // Verify signature
        boolean isValidSignature = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValidSignature) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Invalid payment signature");
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Payment verification failed: Invalid signature");
        }

        // Update payment status
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Activate subscription
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription activated successfully: {}", subscription.getId());
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDTO getActiveSubscription(UUID userId) {
        log.info("Fetching active subscription for user: {}", userId);

        Subscription subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .orElse(null);

        if (subscription == null) {
            return null;
        }

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getUserSubscriptions(UUID userId) {
        log.info("Fetching all subscriptions for user: {}", userId);

        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(subscriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelSubscription(UUID subscriptionId, UUID userId) {
        log.info("Cancelling subscription: {} for user: {}", subscriptionId, userId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to cancel this subscription");
        }

        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled successfully: {}", subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponseDTO getSubscriptionById(UUID subscriptionId) {
        log.info("Fetching subscription by ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subscriptionId));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getUserPayments(UUID userId) {
        log.info("Fetching payment history for user: {}", userId);

        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void updateExpiredSubscriptions() {
        log.info("Running scheduled task to update expired subscriptions");

        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredActiveSubscriptions(LocalDateTime.now());
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Subscription expired: {}", subscription.getId());
        }

        log.info("Updated {} expired subscriptions", expiredSubscriptions.size());
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, SubscriptionPlan.BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case YEARLY -> startDate.plusYears(1);
        };
    }
}
