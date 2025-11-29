package online.sevika.tm.mapper;
import online.sevika.tm.dto.TeamMemberResponseDTO;

import online.sevika.tm.entity.TeamMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for TeamMember entity
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamMemberMapper {

    TeamMemberResponseDTO toResponse(TeamMember teamMember);
}
