package online.sevika.tm.mapper;
import online.sevika.tm.dto.UserRequestDTO;
import online.sevika.tm.dto.UserResponseDTO;
import online.sevika.tm.dto.UserSummaryDTO;
import online.sevika.tm.dto.UserUpdateRequestDTO;

import online.sevika.tm.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity.
 * 
 * Demonstrates:
 * - MapStruct for object mapping
 * - Automatic mapping configuration
 * - Custom mapping methods
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to Response DTO
     */
    UserResponseDTO toResponse(User user);

    /**
     * Convert Request DTO to User entity
     */
    User toEntity(UserRequestDTO request);

    /**
     * Convert User entity to Summary DTO
     */
    UserSummaryDTO toSummary(User user);

    /**
     * Update entity from UpdateRequest DTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserUpdateRequestDTO updateRequest, @MappingTarget User user);
}
