package online.sevika.tm.controller;
import online.sevika.tm.dto.SubscriptionPlanRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.service.SubscriptionPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for subscription plan management.
 */
@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Subscription plan management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create subscription plan", description = "Create a new subscription plan (Admin only)")
    public ResponseEntity<SubscriptionPlanResponseDTO> createPlan(@Valid @RequestBody SubscriptionPlanRequestDTO request) {
        SubscriptionPlanResponseDTO response = subscriptionPlanService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get subscription plan", description = "Get subscription plan by ID")
    public ResponseEntity<SubscriptionPlanResponseDTO> getPlanById(@PathVariable UUID planId) {
        SubscriptionPlanResponseDTO response = subscriptionPlanService.getPlanById(planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all plans", description = "Get all subscription plans")
    public ResponseEntity<List<SubscriptionPlanResponseDTO>> getAllPlans() {
        List<SubscriptionPlanResponseDTO> response = subscriptionPlanService.getAllPlans();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active plans", description = "Get all active subscription plans")
    public ResponseEntity<List<SubscriptionPlanResponseDTO>> getActivePlans() {
        List<SubscriptionPlanResponseDTO> response = subscriptionPlanService.getActivePlans();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update subscription plan", description = "Update subscription plan (Admin only)")
    public ResponseEntity<SubscriptionPlanResponseDTO> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SubscriptionPlanRequestDTO request) {
        SubscriptionPlanResponseDTO response = subscriptionPlanService.updatePlan(planId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate subscription plan", description = "Deactivate subscription plan (Admin only)")
    public ResponseEntity<Void> deactivatePlan(@PathVariable UUID planId) {
        subscriptionPlanService.deactivatePlan(planId);
        return ResponseEntity.noContent().build();
    }
}
