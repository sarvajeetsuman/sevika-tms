package online.sevika.tm.dto;
import online.sevika.tm.dto.SubscriptionPlanRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.sevika.tm.entity.SubscriptionPlan;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SubscriptionPlanRequest", description = "Request to create a subscription plan")
public class SubscriptionPlanRequestDTO {
    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationInDays;

    @NotNull(message = "Max projects is required")
    @Min(value = 1, message = "Max projects must be at least 1")
    private Integer maxProjects;

    @NotNull(message = "Max tasks per project is required")
    @Min(value = 1, message = "Max tasks per project must be at least 1")
    private Integer maxTasksPerProject;

    @NotNull(message = "Max team members is required")
    @Min(value = 1, message = "Max team members must be at least 1")
    private Integer maxTeamMembers;

    private SubscriptionPlan.BillingCycle billingCycle;

    @Builder.Default
    private Boolean fileAttachments = false;

    @Builder.Default
    private Boolean advancedReporting = false;

    @Builder.Default
    private Boolean prioritySupport = false;

    @Builder.Default
    private Boolean apiAccess = false;

    @Builder.Default
    private Boolean active = true;
}
