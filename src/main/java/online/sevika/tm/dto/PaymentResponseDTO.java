package online.sevika.tm.dto;
import online.sevika.tm.dto.PaymentResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PaymentResponse", description = "Payment response data")
public class PaymentResponseDTO {
    private UUID id;
    private UUID subscriptionId;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentStatus status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
