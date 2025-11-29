package online.sevika.tm.dto;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SubscriptionPlanResponse", description = "Subscription plan response data")
public class SubscriptionPlanResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationInDays;
    private Integer maxProjects;
    private Integer maxTasksPerProject;
    private Integer maxTeamMembers;
    private SubscriptionPlan.BillingCycle billingCycle;
    private Boolean fileAttachments;
    private Boolean advancedReporting;
    private Boolean prioritySupport;
    private Boolean apiAccess;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
