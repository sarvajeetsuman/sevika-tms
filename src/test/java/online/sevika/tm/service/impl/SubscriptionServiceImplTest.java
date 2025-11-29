package online.sevika.tm.service.impl;

import online.sevika.tm.dto.SubscriptionRequestDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;
import online.sevika.tm.dto.RazorpayOrderResponseDTO;
import online.sevika.tm.dto.PaymentVerificationRequestDTO;
import online.sevika.tm.dto.PaymentResponseDTO;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
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
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private UUID userId;
    private UUID planId;
    private UUID subscriptionId;
    private User user;
    private SubscriptionPlan plan;
    private Subscription subscription;
    private Payment payment;
    private SubscriptionRequestDTO subscriptionRequest;
    private SubscriptionResponseDTO subscriptionResponse;
    private Order razorpayOrder;

    @BeforeEach
    void setUp() throws RazorpayException {
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        plan = SubscriptionPlan.builder()
                .id(planId)
                .name("Premium")
                .price(new BigDecimal("999.00"))
                .billingCycle(SubscriptionPlan.BillingCycle.MONTHLY)
                .active(true)
                .build();

        subscription = Subscription.builder()
                .id(subscriptionId)
                .user(user)
                .plan(plan)
                .status(Subscription.SubscriptionStatus.TRIAL)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .autoRenew(true)
                .build();

        payment = Payment.builder()
                .id(UUID.randomUUID())
                .user(user)
                .subscription(subscription)
                .amount(new BigDecimal("999.00"))
                .currency("INR")
                .status(Payment.PaymentStatus.PENDING)
                .razorpayOrderId("order_123")
                .build();

        subscriptionRequest = SubscriptionRequestDTO.builder()
                .planId(planId)
                .autoRenew(true)
                .build();

        subscriptionResponse = SubscriptionResponseDTO.builder()
                .id(subscriptionId)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        // Mock Razorpay Order
        JSONObject orderJson = new JSONObject();
        orderJson.put("id", "order_123");
        orderJson.put("amount", 99900);
        orderJson.put("currency", "INR");
        razorpayOrder = new Order(orderJson);
    }

    @Test
    void createSubscription_Success() throws RazorpayException {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(razorpayService.createOrder(any(BigDecimal.class), anyString(), anyString()))
                .thenReturn(razorpayOrder);
        when(razorpayService.getKeyId()).thenReturn("rzp_test_key");
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        RazorpayOrderResponseDTO response = subscriptionService.createSubscription(
                subscriptionRequest, userId);

        // Assert
        assertNotNull(response);
        assertEquals("order_123", response.getOrderId());
        assertEquals("rzp_test_key", response.getKey());
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createSubscription_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.createSubscription(subscriptionRequest, userId));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_PlanNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.createSubscription(subscriptionRequest, userId));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_InactivePlan_ThrowsException() {
        // Arrange
        plan.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                subscriptionService.createSubscription(subscriptionRequest, userId));
    }

    @Test
    void createSubscription_RazorpayError_ThrowsException() throws RazorpayException {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(razorpayService.createOrder(any(BigDecimal.class), anyString(), anyString()))
                .thenThrow(new RazorpayException("Payment gateway error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                subscriptionService.createSubscription(subscriptionRequest, userId));
    }

    @Test
    void verifyAndActivateSubscription_Success() {
        // Arrange
        PaymentVerificationRequestDTO verificationRequest = 
                PaymentVerificationRequestDTO.builder()
                        .subscriptionId(subscriptionId)
                        .razorpayOrderId("order_123")
                        .razorpayPaymentId("pay_123")
                        .razorpaySignature("signature_123")
                        .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findByRazorpayOrderId("order_123")).thenReturn(Optional.of(payment));
        when(razorpayService.verifyPaymentSignature(anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(subscriptionResponse);

        // Act
        SubscriptionResponseDTO response = subscriptionService.verifyAndActivateSubscription(verificationRequest);

        // Assert
        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void verifyAndActivateSubscription_InvalidSignature_ThrowsException() {
        // Arrange
        PaymentVerificationRequestDTO verificationRequest = 
                PaymentVerificationRequestDTO.builder()
                        .subscriptionId(subscriptionId)
                        .razorpayOrderId("order_123")
                        .razorpayPaymentId("pay_123")
                        .razorpaySignature("invalid_signature")
                        .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(paymentRepository.findByRazorpayOrderId("order_123")).thenReturn(Optional.of(payment));
        when(razorpayService.verifyPaymentSignature(anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                subscriptionService.verifyAndActivateSubscription(verificationRequest));

        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    void getActiveSubscription_Success() {
        // Arrange
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
                .thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(subscriptionResponse);

        // Act
        SubscriptionResponseDTO response = subscriptionService.getActiveSubscription(userId);

        // Assert
        assertNotNull(response);
        assertEquals(subscriptionId, response.getId());
        verify(subscriptionRepository).findActiveSubscriptionByUserId(userId);
    }

    @Test
    void getActiveSubscription_NoActiveSubscription_ReturnsNull() {
        // Arrange
        when(subscriptionRepository.findActiveSubscriptionByUserId(userId))
                .thenReturn(Optional.empty());

        // Act
        SubscriptionResponseDTO response = subscriptionService.getActiveSubscription(userId);

        // Assert
        assertNull(response);
    }

    @Test
    void getUserSubscriptions_Success() {
        // Arrange
        when(subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.singletonList(subscription));
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(subscriptionResponse);

        // Act
        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getUserSubscriptions(userId);

        // Assert
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        verify(subscriptionRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void cancelSubscription_Success() {
        // Arrange
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        // Act
        subscriptionService.cancelSubscription(subscriptionId, userId);

        // Assert
        verify(subscriptionRepository).save(argThat(sub ->
                sub.getStatus() == Subscription.SubscriptionStatus.CANCELLED &&
                !sub.getAutoRenew()
        ));
    }

    @Test
    void cancelSubscription_NotFound_ThrowsException() {
        // Arrange
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.cancelSubscription(subscriptionId, userId));
    }

    @Test
    void cancelSubscription_Unauthorized_ThrowsException() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                subscriptionService.cancelSubscription(subscriptionId, otherUserId));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getSubscriptionById_Success() {
        // Arrange
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(subscriptionResponse);

        // Act
        SubscriptionResponseDTO response = subscriptionService.getSubscriptionById(subscriptionId);

        // Assert
        assertNotNull(response);
        assertEquals(subscriptionId, response.getId());
        verify(subscriptionRepository).findById(subscriptionId);
    }

    @Test
    void getUserPayments_Success() {
        // Arrange
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Collections.singletonList(payment));
        when(paymentMapper.toResponse(any(Payment.class)))
                .thenReturn(new PaymentResponseDTO());

        // Act
        List<PaymentResponseDTO> payments = subscriptionService.getUserPayments(userId);

        // Assert
        assertNotNull(payments);
        assertEquals(1, payments.size());
        verify(paymentRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void updateExpiredSubscriptions_Success() {
        // Arrange
        Subscription expiredSub1 = Subscription.builder()
                .id(UUID.randomUUID())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .endDate(LocalDateTime.now().minusDays(1))
                .build();
        
        Subscription expiredSub2 = Subscription.builder()
                .id(UUID.randomUUID())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .endDate(LocalDateTime.now().minusDays(5))
                .build();

        when(subscriptionRepository.findExpiredActiveSubscriptions(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(expiredSub1, expiredSub2));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        subscriptionService.updateExpiredSubscriptions();

        // Assert
        verify(subscriptionRepository).findExpiredActiveSubscriptions(any(LocalDateTime.class));
        verify(subscriptionRepository, times(2)).save(argThat(sub ->
                sub.getStatus() == Subscription.SubscriptionStatus.EXPIRED
        ));
    }
}
