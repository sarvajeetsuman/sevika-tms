package online.sevika.tm.mapper;

import online.sevika.tm.dto.ProjectRequestDTO;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.entity.Project;
import org.mapstruct.*;

/**
 * MapStruct mapper for Project entity.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

    /**
     * Convert Project entity to Response DTO
     */
    @Mapping(target = "taskCount", expression = "java(project.getTasks() != null ? project.getTasks().size() : 0)")
    ProjectResponseDTO toResponse(Project project);

    /**
     * Convert Request DTO to Project entity
     */
    Project toEntity(ProjectRequestDTO request);

    /**
     * Convert Project entity to Summary DTO
     */
    ProjectSummaryDTO toSummary(Project project);

    /**
     * Update entity from UpdateRequest DTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProjectUpdateRequestDTO updateRequest, @MappingTarget Project project);
}
