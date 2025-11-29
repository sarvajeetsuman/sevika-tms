package online.sevika.tm.mapper;

import online.sevika.tm.dto.AuditLogDTO;
import online.sevika.tm.entity.AuditLog;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for AuditLog entity and DTO conversion
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogDTO toDTO(AuditLog auditLog);

    AuditLog toEntity(AuditLogDTO auditLogDTO);
}
