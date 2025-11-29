package online.sevika.tm.mapper;
import online.sevika.tm.dto.ProjectPermissionResponseDTO;
import online.sevika.tm.dto.TaskPermissionResponseDTO;

import online.sevika.tm.entity.ProjectPermission;
import online.sevika.tm.entity.TaskPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Permission entities
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    ProjectPermissionResponseDTO toProjectPermissionResponse(ProjectPermission permission);

    TaskPermissionResponseDTO toTaskPermissionResponse(TaskPermission permission);
}
