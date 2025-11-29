package online.sevika.tm.mapper;
import online.sevika.tm.dto.SubscriptionPlanRequestDTO;
import online.sevika.tm.dto.SubscriptionPlanResponseDTO;

import online.sevika.tm.entity.SubscriptionPlan;
import org.mapstruct.*;

/**
 * Mapper for SubscriptionPlan entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionPlanMapper {

    SubscriptionPlan toEntity(SubscriptionPlanRequestDTO request);

    SubscriptionPlanResponseDTO toResponse(SubscriptionPlan subscriptionPlan);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SubscriptionPlanRequestDTO request, @MappingTarget SubscriptionPlan subscriptionPlan);
}
