package online.sevika.tm.mapper;
import online.sevika.tm.dto.TeamRequestDTO;
import online.sevika.tm.dto.TeamResponseDTO;
import online.sevika.tm.dto.TeamSummaryDTO;

import online.sevika.tm.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Team entity
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamMapper {

    Team toEntity(TeamRequestDTO request);

    TeamResponseDTO toResponse(Team team);

    TeamSummaryDTO toSummary(Team team);

    void updateEntityFromRequest(TeamRequestDTO request, @MappingTarget Team team);
}
