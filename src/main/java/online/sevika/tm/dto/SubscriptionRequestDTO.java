package online.sevika.tm.dto;
import online.sevika.tm.dto.SubscriptionRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SubscriptionRequest", description = "Request to create a subscription")
public class SubscriptionRequestDTO {
    @NotNull(message = "Plan ID is required")
    private UUID planId;
    
    @Builder.Default
    private Boolean autoRenew = false;
}
