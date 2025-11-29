package online.sevika.tm.dto;
import online.sevika.tm.dto.RazorpayOrderResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RazorpayOrderResponse", description = "Razorpay order details")
public class RazorpayOrderResponseDTO {
    private String orderId;
    private String currency;
    private String amount;
    private String key;
    private String name;
    private String description;
    private String prefillName;
    private String prefillEmail;
    private UUID subscriptionId;
}
