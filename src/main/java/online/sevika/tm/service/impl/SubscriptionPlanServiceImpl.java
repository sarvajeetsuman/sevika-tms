package online.sevika.tm.service.impl;
import online.sevika.tm.dto.SubscriptionPlanRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.entity.SubscriptionPlan;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.mapper.SubscriptionPlanMapper;
import online.sevika.tm.repository.SubscriptionPlanRepository;
import online.sevika.tm.service.SubscriptionPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of SubscriptionPlanService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    @Transactional
    public SubscriptionPlanResponseDTO createPlan(SubscriptionPlanRequestDTO request) {
        log.info("Creating new subscription plan: {}", request.getName());

        if (subscriptionPlanRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Subscription plan with name '" + request.getName() + "' already exists");
        }

        SubscriptionPlan plan = subscriptionPlanMapper.toEntity(request);
        plan = subscriptionPlanRepository.save(plan);

        log.info("Subscription plan created successfully with ID: {}", plan.getId());
        return subscriptionPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponseDTO getPlanById(UUID planId) {
        log.info("Fetching subscription plan by ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));

        return subscriptionPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> getActivePlans() {
        log.info("Fetching all active subscription plans");

        return subscriptionPlanRepository.findByActiveTrue().stream()
                .map(subscriptionPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponseDTO> getAllPlans() {
        log.info("Fetching all subscription plans");

        return subscriptionPlanRepository.findAll().stream()
                .map(subscriptionPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionPlanResponseDTO updatePlan(UUID planId, SubscriptionPlanRequestDTO request) {
        log.info("Updating subscription plan with ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));

        subscriptionPlanMapper.updateEntityFromDto(request, plan);
        plan = subscriptionPlanRepository.save(plan);

        log.info("Subscription plan updated successfully with ID: {}", planId);
        return subscriptionPlanMapper.toResponse(plan);
    }

    @Override
    @Transactional
    public void deactivatePlan(UUID planId) {
        log.info("Deactivating subscription plan with ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with ID: " + planId));

        plan.setActive(false);
        subscriptionPlanRepository.save(plan);

        log.info("Subscription plan deactivated successfully with ID: {}", planId);
    }
}
