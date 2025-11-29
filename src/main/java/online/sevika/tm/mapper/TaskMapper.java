package online.sevika.tm.mapper;
import online.sevika.tm.dto.TaskRequestDTO;
import online.sevika.tm.dto.TaskResponseDTO;
import online.sevika.tm.dto.TaskSummaryDTO;
import online.sevika.tm.dto.TaskUpdateRequestDTO;

import online.sevika.tm.entity.Task;
import org.mapstruct.*;

/**
 * MapStruct mapper for Task entity.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    /**
     * Convert Task entity to Response DTO
     */
    TaskResponseDTO toResponse(Task task);

    /**
     * Convert Request DTO to Task entity
     */
    @Mapping(target = "status", constant = "TODO")
    Task toEntity(TaskRequestDTO request);

    /**
     * Convert Task entity to Summary DTO
     */
    TaskSummaryDTO toSummary(Task task);

    /**
     * Update entity from UpdateRequest DTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(TaskUpdateRequestDTO updateRequest, @MappingTarget Task task);
}
