package online.sevika.tm.controller;

import online.sevika.tm.dto.SubscriptionRequestDTO;
import online.sevika.tm.dto.UserSummaryDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;
import online.sevika.tm.dto.RazorpayOrderResponseDTO;
import online.sevika.tm.dto.PaymentVerificationRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;
import online.sevika.tm.dto.PaymentResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.Payment;
import online.sevika.tm.entity.Subscription;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import java.util.Collections;
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
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    @Retention(RetentionPolicy.RUNTIME)
    @org.springframework.security.test.context.support.WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    public @interface WithMockCustomUser {
        String userId() default "123e4567-e89b-12d3-a456-426614174000";
        String role() default "USER";
    }

    public static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            UUID userId = customUser.userId().isEmpty()
                    ? UUID.randomUUID()
                    : UUID.fromString(customUser.userId());

            User user = new User();
            user.setId(userId);
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setRole(User.Role.valueOf(customUser.role()));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + customUser.role()))
            );
            context.setAuthentication(auth);
            return context;
        }
    }

    @Test
    @WithMockCustomUser
    void createSubscription_Success() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();
        SubscriptionRequestDTO request = SubscriptionRequestDTO.builder()
                .planId(planId)
                .autoRenew(true)
                .build();

        UUID subscriptionId = UUID.randomUUID();
        RazorpayOrderResponseDTO razorpayResponse = RazorpayOrderResponseDTO.builder()
                .orderId("order_123456")
                .amount("99900")
                .currency("INR")
                .key("rzp_test_key")
                .name("Sevika Task Management")
                .description("Premium Plan Subscription")
                .prefillName("Test User")
                .prefillEmail("test@example.com")
                .subscriptionId(subscriptionId)
                .build();

        when(subscriptionService.createSubscription(any(SubscriptionRequestDTO.class), any(UUID.class)))
                .thenReturn(razorpayResponse);

        // Act & Assert
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("order_123456"))
                .andExpect(jsonPath("$.amount").value("99900"))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.key").value("rzp_test_key"))
                .andExpect(jsonPath("$.subscriptionId").value(subscriptionId.toString()));
    }

    @Test
    void createSubscription_Unauthenticated_Forbidden() throws Exception {
        // Arrange
        UUID planId = UUID.randomUUID();
        SubscriptionRequestDTO request = SubscriptionRequestDTO.builder()
                .planId(planId)
                .autoRenew(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void createSubscription_InvalidRequest_BadRequest() throws Exception {
        // Arrange - Request with null planId
        SubscriptionRequestDTO request = SubscriptionRequestDTO.builder()
                .planId(null)
                .autoRenew(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    void verifyPayment_Success() throws Exception {
        // Arrange
        UUID subscriptionId = UUID.randomUUID();
        PaymentVerificationRequestDTO request = PaymentVerificationRequestDTO.builder()
                .subscriptionId(subscriptionId)
                .razorpayPaymentId("pay_123456")
                .razorpayOrderId("order_123456")
                .razorpaySignature("signature_abc123")
                .build();

        UUID planId = UUID.randomUUID();
        SubscriptionResponseDTO subscriptionResponse = SubscriptionResponseDTO.builder()
                .id(subscriptionId)
                .user(UserSummaryDTO.builder()
                        .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                        .username("testuser")
                        .firstName("Test")
                        .lastName("User")
                        .build())
                .plan(SubscriptionPlanResponseDTO.builder()
                        .id(planId)
                        .name("Premium Plan")
                        .price(BigDecimal.valueOf(999.00))
                        .build())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .isActive(true)
                .isExpired(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionService.verifyAndActivateSubscription(any(PaymentVerificationRequestDTO.class)))
                .thenReturn(subscriptionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/subscriptions/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockCustomUser
    void verifyPayment_InvalidRequest_BadRequest() throws Exception {
        // Arrange - Request with null payment ID
        UUID subscriptionId = UUID.randomUUID();
        PaymentVerificationRequestDTO request = PaymentVerificationRequestDTO.builder()
                .subscriptionId(subscriptionId)
                .razorpayPaymentId(null)
                .razorpayOrderId("order_123456")
                .razorpaySignature("signature_abc123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/subscriptions/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    void getActiveSubscription_Success() throws Exception {
        // Arrange
        UUID subscriptionId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        SubscriptionResponseDTO subscriptionResponse = SubscriptionResponseDTO.builder()
                .id(subscriptionId)
                .user(UserSummaryDTO.builder()
                        .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                        .username("testuser")
                        .firstName("Test")
                        .lastName("User")
                        .build())
                .plan(SubscriptionPlanResponseDTO.builder()
                        .id(planId)
                        .name("Premium Plan")
                        .price(BigDecimal.valueOf(999.00))
                        .build())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .isActive(true)
                .isExpired(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionService.getActiveSubscription(any(UUID.class)))
                .thenReturn(subscriptionResponse);

        // Act & Assert
        mockMvc.perform(get("/api/subscriptions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.plan.name").value("Premium Plan"));
    }

    @Test
    @WithMockCustomUser
    void getActiveSubscription_NoActiveSubscription_NoContent() throws Exception {
        // Arrange
        when(subscriptionService.getActiveSubscription(any(UUID.class)))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/subscriptions/active"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser
    void getUserSubscriptions_Success() throws Exception {
        // Arrange
        UUID subscription1Id = UUID.randomUUID();
        UUID subscription2Id = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        List<SubscriptionResponseDTO> subscriptions = Arrays.asList(
                SubscriptionResponseDTO.builder()
                        .id(subscription1Id)
                        .user(UserSummaryDTO.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                                .username("testuser")
                                .firstName("Test")
                                .lastName("User")
                                .build())
                        .plan(SubscriptionPlanResponseDTO.builder()
                                .id(planId)
                                .name("Premium Plan")
                                .price(BigDecimal.valueOf(999.00))
                                .build())
                        .status(Subscription.SubscriptionStatus.ACTIVE)
                        .isActive(true)
                        .isExpired(false)
                        .build(),
                SubscriptionResponseDTO.builder()
                        .id(subscription2Id)
                        .user(UserSummaryDTO.builder()
                                .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                                .username("testuser")
                                .firstName("Test")
                                .lastName("User")
                                .build())
                        .plan(SubscriptionPlanResponseDTO.builder()
                                .id(planId)
                                .name("Basic Plan")
                                .price(BigDecimal.valueOf(499.00))
                                .build())
                        .status(Subscription.SubscriptionStatus.EXPIRED)
                        .isActive(false)
                        .isExpired(true)
                        .build()
        );

        when(subscriptionService.getUserSubscriptions(any(UUID.class)))
                .thenReturn(subscriptions);

        // Act & Assert
        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(subscription1Id.toString()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(subscription2Id.toString()))
                .andExpect(jsonPath("$[1].status").value("EXPIRED"));
    }

    @Test
    @WithMockCustomUser
    void getSubscriptionById_Success() throws Exception {
        // Arrange
        UUID subscriptionId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        SubscriptionResponseDTO subscriptionResponse = SubscriptionResponseDTO.builder()
                .id(subscriptionId)
                .user(UserSummaryDTO.builder()
                        .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                        .username("testuser")
                        .firstName("Test")
                        .lastName("User")
                        .build())
                .plan(SubscriptionPlanResponseDTO.builder()
                        .id(planId)
                        .name("Premium Plan")
                        .price(BigDecimal.valueOf(999.00))
                        .build())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .isActive(true)
                .isExpired(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(subscriptionService.getSubscriptionById(eq(subscriptionId)))
                .thenReturn(subscriptionResponse);

        // Act & Assert
        mockMvc.perform(get("/api/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.plan.name").value("Premium Plan"));
    }

    @Test
    @WithMockCustomUser
    void cancelSubscription_Success() throws Exception {
        // Arrange
        UUID subscriptionId = UUID.randomUUID();
        doNothing().when(subscriptionService).cancelSubscription(eq(subscriptionId), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelSubscription_Unauthenticated_Forbidden() throws Exception {
        // Arrange
        UUID subscriptionId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getPaymentHistory_Success() throws Exception {
        // Arrange
        UUID payment1Id = UUID.randomUUID();
        UUID payment2Id = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        List<PaymentResponseDTO> payments = Arrays.asList(
                PaymentResponseDTO.builder()
                        .id(payment1Id)
                        .subscriptionId(subscriptionId)
                        .amount(BigDecimal.valueOf(999.00))
                        .currency("INR")
                        .status(Payment.PaymentStatus.SUCCESS)
                        .paymentMethod("razorpay")
                        .razorpayPaymentId("pay_123456")
                        .razorpayOrderId("order_123456")
                        .createdAt(LocalDateTime.now().minusMonths(1))
                        .build(),
                PaymentResponseDTO.builder()
                        .id(payment2Id)
                        .subscriptionId(subscriptionId)
                        .amount(BigDecimal.valueOf(999.00))
                        .currency("INR")
                        .status(Payment.PaymentStatus.SUCCESS)
                        .paymentMethod("razorpay")
                        .razorpayPaymentId("pay_789012")
                        .razorpayOrderId("order_789012")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(subscriptionService.getUserPayments(any(UUID.class)))
                .thenReturn(payments);

        // Act & Assert
        mockMvc.perform(get("/api/subscriptions/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(payment1Id.toString()))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].amount").value(999.00))
                .andExpect(jsonPath("$[1].id").value(payment2Id.toString()))
                .andExpect(jsonPath("$[1].status").value("SUCCESS"));
    }

    @Test
    void getPaymentHistory_Unauthenticated_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/subscriptions/payments"))
                .andExpect(status().isForbidden());
    }
}
