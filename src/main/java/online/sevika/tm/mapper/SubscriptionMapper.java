package online.sevika.tm.mapper;
import online.sevika.tm.dto.SubscriptionResponseDTO;

import online.sevika.tm.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Subscription entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class, SubscriptionPlanMapper.class})
public interface SubscriptionMapper {

    @Mapping(target = "isActive", expression = "java(subscription.isActive())")
    @Mapping(target = "isExpired", expression = "java(subscription.isExpired())")
    SubscriptionResponseDTO toResponse(Subscription subscription);
}
