package online.sevika.tm.mapper;
import online.sevika.tm.dto.PaymentResponseDTO;

import online.sevika.tm.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Payment entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class})
public interface PaymentMapper {

    @Mapping(target = "subscriptionId", source = "subscription.id")
    PaymentResponseDTO toResponse(Payment payment);
}
