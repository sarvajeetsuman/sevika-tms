package online.sevika.tm.controller;

import online.sevika.tm.dto.SubscriptionPlanRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.SubscriptionPlan;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.SubscriptionPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionPlanService subscriptionPlanService;

    // Custom annotation for mock user
    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.test.context.support.WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    public @interface WithMockCustomUser {
        String username() default "testuser";
        String role() default "USER";
    }

    // Security context factory for custom user
    public static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            User user = new User();
            user.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            user.setUsername(customUser.username());
            user.setRole(User.Role.valueOf(customUser.role()));

            Collection<? extends GrantedAuthority> authorities =
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_" + customUser.role()));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getId().toString(), null, authorities);
            context.setAuthentication(auth);

            return context;
        }
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void createPlan_Success() throws Exception {
        // Arrange
        SubscriptionPlanRequestDTO request = SubscriptionPlanRequestDTO.builder()
                .name("Premium Plan")
                .description("Premium subscription plan")
                .price(BigDecimal.valueOf(999.00))
                .durationInDays(30)
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(50)
                .maxTasksPerProject(100)
                .maxTeamMembers(20)
                .fileAttachments(true)
                .advancedReporting(true)
                .prioritySupport(true)
                .apiAccess(true)
                .active(true)
                .build();

        UUID planId = UUID.randomUUID();
        SubscriptionPlanResponseDTO response = SubscriptionPlanResponseDTO.builder()
                .id(planId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .billingCycle(request.getBillingCycle())
                .maxProjects(request.getMaxProjects())
                .maxTasksPerProject(request.getMaxTasksPerProject())
                .maxTeamMembers(request.getMaxTeamMembers())
                .fileAttachments(request.getFileAttachments())
                .advancedReporting(request.getAdvancedReporting())
                .prioritySupport(request.getPrioritySupport())
                .apiAccess(request.getApiAccess())
                .active(request.getActive())
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionPlanService.createPlan(any(SubscriptionPlanRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/subscription-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.name").value("Premium Plan"))
                .andExpect(jsonPath("$.price").value(999.00))
                .andExpect(jsonPath("$.billingCycle").value("MONTHLY"))
                .andExpect(jsonPath("$.maxProjects").value(50));
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void createPlan_AccessDenied_Forbidden() throws Exception {
        // Arrange
        SubscriptionPlanRequestDTO request = SubscriptionPlanRequestDTO.builder()
                .name("Premium Plan")
                .price(BigDecimal.valueOf(999.00))
                .durationInDays(30)
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(50)
                .maxTasksPerProject(100)
                .maxTeamMembers(20)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/subscription-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void createPlan_InvalidRequest_BadRequest() throws Exception {
        // Arrange - missing required field
        SubscriptionPlanRequestDTO request = SubscriptionPlanRequestDTO.builder()
                .name("") // Invalid - blank name
                .price(BigDecimal.valueOf(999.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(50)
                .maxTasksPerProject(100)
                .maxTeamMembers(20)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/subscription-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    void getPlanById_Success() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();
        SubscriptionPlanResponseDTO response = SubscriptionPlanResponseDTO.builder()
                .id(planId)
                .name("Premium Plan")
                .description("Premium subscription plan")
                .price(BigDecimal.valueOf(999.00))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(50)
                .maxTasksPerProject(100)
                .maxTeamMembers(20)
                .fileAttachments(true)
                .advancedReporting(true)
                .prioritySupport(true)
                .apiAccess(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionPlanService.getPlanById(planId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/subscription-plans/{planId}", planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.name").value("Premium Plan"))
                .andExpect(jsonPath("$.price").value(999.00));
    }

    @Test
    @WithMockCustomUser
    void getAllPlans_Success() throws Exception {
        // Arrange
        UUID plan1Id = UUID.randomUUID();
        UUID plan2Id = UUID.randomUUID();

        List<SubscriptionPlanResponseDTO> plans = Arrays.asList(
                SubscriptionPlanResponseDTO.builder()
                        .id(plan1Id)
                        .name("Basic Plan")
                        .price(BigDecimal.valueOf(499.00))
                        .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                        .maxProjects(10)
                        .maxTasksPerProject(50)
                        .maxTeamMembers(5)
                        .active(true)
                        .build(),
                SubscriptionPlanResponseDTO.builder()
                        .id(plan2Id)
                        .name("Premium Plan")
                        .price(BigDecimal.valueOf(999.00))
                        .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                        .maxProjects(50)
                        .maxTasksPerProject(100)
                        .maxTeamMembers(20)
                        .active(true)
                        .build()
        );

        when(subscriptionPlanService.getAllPlans()).thenReturn(plans);

        // Act & Assert
        mockMvc.perform(get("/api/subscription-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Basic Plan"))
                .andExpect(jsonPath("$[0].price").value(499.00))
                .andExpect(jsonPath("$[1].name").value("Premium Plan"))
                .andExpect(jsonPath("$[1].price").value(999.00));
    }

    @Test
    @WithMockCustomUser
    void getActivePlans_Success() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();

        List<SubscriptionPlanResponseDTO> plans = Arrays.asList(
                SubscriptionPlanResponseDTO.builder()
                        .id(planId)
                        .name("Premium Plan")
                        .price(BigDecimal.valueOf(999.00))
                        .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                        .maxProjects(50)
                        .maxTasksPerProject(100)
                        .maxTeamMembers(20)
                        .active(true)
                        .build()
        );

        when(subscriptionPlanService.getActivePlans()).thenReturn(plans);

        // Act & Assert
        mockMvc.perform(get("/api/subscription-plans/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Premium Plan"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void updatePlan_Success() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();
        SubscriptionPlanRequestDTO request = SubscriptionPlanRequestDTO.builder()
                .name("Premium Plan Updated")
                .description("Updated premium plan")
                .price(BigDecimal.valueOf(1099.00))
                .durationInDays(30)
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(60)
                .maxTasksPerProject(120)
                .maxTeamMembers(25)
                .fileAttachments(true)
                .advancedReporting(true)
                .prioritySupport(true)
                .apiAccess(true)
                .active(true)
                .build();

        SubscriptionPlanResponseDTO response = SubscriptionPlanResponseDTO.builder()
                .id(planId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .billingCycle(request.getBillingCycle())
                .maxProjects(request.getMaxProjects())
                .maxTasksPerProject(request.getMaxTasksPerProject())
                .maxTeamMembers(request.getMaxTeamMembers())
                .fileAttachments(request.getFileAttachments())
                .advancedReporting(request.getAdvancedReporting())
                .prioritySupport(request.getPrioritySupport())
                .apiAccess(request.getApiAccess())
                .active(request.getActive())
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        when(subscriptionPlanService.updatePlan(eq(planId), any(SubscriptionPlanRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/subscription-plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId.toString()))
                .andExpect(jsonPath("$.name").value("Premium Plan Updated"))
                .andExpect(jsonPath("$.price").value(1099.00))
                .andExpect(jsonPath("$.maxProjects").value(60));
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void updatePlan_AccessDenied_Forbidden() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();
        SubscriptionPlanRequestDTO request = SubscriptionPlanRequestDTO.builder()
                .name("Premium Plan Updated")
                .price(BigDecimal.valueOf(1099.00))
                .durationInDays(30)
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .maxProjects(60)
                .maxTasksPerProject(120)
                .maxTeamMembers(25)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/subscription-plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void deactivatePlan_Success() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();
        doNothing().when(subscriptionPlanService).deactivatePlan(planId);

        // Act & Assert
        mockMvc.perform(delete("/api/subscription-plans/{planId}", planId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void deactivatePlan_AccessDenied_Forbidden() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/subscription-plans/{planId}", planId))
                .andExpect(status().isForbidden());
    }
}
