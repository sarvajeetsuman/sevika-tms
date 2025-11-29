package online.sevika.tm.service.impl;

import online.sevika.tm.dto.SubscriptionPlanRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;
import online.sevika.tm.entity.SubscriptionPlan;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.mapper.SubscriptionPlanMapper;
import online.sevika.tm.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionPlanServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @InjectMocks
    private SubscriptionPlanServiceImpl subscriptionPlanService;

    private SubscriptionPlan plan;
    private SubscriptionPlanRequestDTO request;
    private SubscriptionPlanResponseDTO response;

    @BeforeEach
    void setUp() {
        plan = SubscriptionPlan.builder()
                .id(UUID.randomUUID())
                .name("Premium")
                .description("Premium plan")
                .price(BigDecimal.valueOf(999.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(50)
                .maxTasksPerProject(500)
                .maxTeamMembers(20)
                .fileAttachments(true)
                .advancedReporting(true)
                .prioritySupport(true)
                .apiAccess(false)
                .active(true)
                .build();

        request = SubscriptionPlanRequestDTO.builder()
                .name("Premium")
                .description("Premium plan")
                .price(BigDecimal.valueOf(999.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(50)
                .maxTasksPerProject(500)
                .maxTeamMembers(20)
                .fileAttachments(true)
                .advancedReporting(true)
                .prioritySupport(true)
                .apiAccess(false)
                .active(true)
                .build();

        response = SubscriptionPlanResponseDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .billingCycle(plan.getBillingCycle())
                .maxProjects(plan.getMaxProjects())
                .build();
    }

    @Test
    void createPlan_Success() {
        // Arrange
        when(subscriptionPlanRepository.existsByName(request.getName())).thenReturn(false);
        when(subscriptionPlanMapper.toEntity(request)).thenReturn(plan);
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(plan);
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        SubscriptionPlanResponseDTO result = subscriptionPlanService.createPlan(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Premium");
        verify(subscriptionPlanRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    void createPlan_DuplicateName_ThrowsException() {
        // Arrange
        when(subscriptionPlanRepository.existsByName(request.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> subscriptionPlanService.createPlan(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(subscriptionPlanRepository, never()).save(any());
    }

    @Test
    void getPlanById_Success() {
        // Arrange
        when(subscriptionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        SubscriptionPlanResponseDTO result = subscriptionPlanService.getPlanById(plan.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(plan.getId());
    }

    @Test
    void getPlanById_NotFound_ThrowsException() {
        // Arrange
        UUID planId = UUID.randomUUID();
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> subscriptionPlanService.getPlanById(planId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getActivePlans_ReturnsPlans() {
        // Arrange
        List<SubscriptionPlan> plans = Arrays.asList(plan);
        when(subscriptionPlanRepository.findByActiveTrue()).thenReturn(plans);
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        List<SubscriptionPlanResponseDTO> results = subscriptionPlanService.getActivePlans();

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Premium");
    }

    @Test
    void getAllPlans_ReturnsAllPlans() {
        // Arrange
        List<SubscriptionPlan> plans = Arrays.asList(plan);
        when(subscriptionPlanRepository.findAll()).thenReturn(plans);
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        List<SubscriptionPlanResponseDTO> results = subscriptionPlanService.getAllPlans();

        // Assert
        assertThat(results).hasSize(1);
    }

    @Test
    void updatePlan_Success() {
        // Arrange
        SubscriptionPlanRequestDTO updateRequest = SubscriptionPlanRequestDTO.builder()
                .name("Premium Updated")
                .price(BigDecimal.valueOf(1099.00))
                .build();

        when(subscriptionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(subscriptionPlanRepository.save(plan)).thenReturn(plan);
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        SubscriptionPlanResponseDTO result = subscriptionPlanService.updatePlan(plan.getId(), updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(subscriptionPlanMapper).updateEntityFromDto(updateRequest, plan);
        verify(subscriptionPlanRepository).save(plan);
    }

    @Test
    void deactivatePlan_Success() {
        // Arrange
        when(subscriptionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        // Act
        subscriptionPlanService.deactivatePlan(plan.getId());

        // Assert
        verify(subscriptionPlanRepository).save(plan);
    }
}
