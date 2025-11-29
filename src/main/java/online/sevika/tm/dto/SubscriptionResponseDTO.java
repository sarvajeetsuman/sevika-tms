package online.sevika.tm.dto;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;
import online.sevika.tm.dto.SubscriptionResponseDTO;
import online.sevika.tm.dto.UserSummaryDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SubscriptionResponse", description = "Subscription response data")
public class SubscriptionResponseDTO {
    private UUID id;
    private UserSummaryDTO user;
    private SubscriptionPlanResponseDTO plan;
    private Subscription.SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal amountPaid;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean autoRenew;
    private Boolean isActive;
    private Boolean isExpired;
}
