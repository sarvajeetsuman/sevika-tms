package online.sevika.tm.controller;
import online.sevika.tm.dto.PaymentResponseDTO;
import online.sevika.tm.dto.PaymentVerificationRequestDTO;
import online.sevika.tm.dto.RazorpayOrderResponseDTO;
import online.sevika.tm.dto.SubscriptionRequestDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for subscription management.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management and payment endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Create subscription", description = "Create a new subscription and initiate payment")
    public ResponseEntity<RazorpayOrderResponseDTO> createSubscription(
            @Valid @RequestBody SubscriptionRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        RazorpayOrderResponseDTO response = subscriptionService.createSubscription(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-payment")
    @Operation(summary = "Verify payment", description = "Verify Razorpay payment and activate subscription")
    public ResponseEntity<SubscriptionResponseDTO> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequestDTO request) {
        SubscriptionResponseDTO response = subscriptionService.verifyAndActivateSubscription(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active subscription", description = "Get user's active subscription")
    public ResponseEntity<SubscriptionResponseDTO> getActiveSubscription(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        SubscriptionResponseDTO response = subscriptionService.getActiveSubscription(userId);
        
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get user subscriptions", description = "Get all subscriptions for current user")
    public ResponseEntity<List<SubscriptionResponseDTO>> getUserSubscriptions(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<SubscriptionResponseDTO> response = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "Get subscription", description = "Get subscription by ID")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable UUID subscriptionId) {
        SubscriptionResponseDTO response = subscriptionService.getSubscriptionById(subscriptionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{subscriptionId}")
    @Operation(summary = "Cancel subscription", description = "Cancel user's subscription")
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable UUID subscriptionId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        subscriptionService.cancelSubscription(subscriptionId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payments")
    @Operation(summary = "Get payment history", description = "Get payment history for current user")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentHistory(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<PaymentResponseDTO> response = subscriptionService.getUserPayments(userId);
        return ResponseEntity.ok(response);
    }
}
