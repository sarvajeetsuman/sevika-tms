package online.sevika.tm.service;
import online.sevika.tm.dto.PermissionGrantRequestDTO;
import online.sevika.tm.dto.ProjectPermissionResponseDTO;
import online.sevika.tm.dto.TaskPermissionResponseDTO;

import online.sevika.tm.entity.enums.PermissionType;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Permission operations
 */
public interface PermissionService {

    /**
     * Grant project permission to team or user
     */
    ProjectPermissionResponseDTO grantProjectPermission(UUID projectId, PermissionGrantRequestDTO request, UUID grantedBy);

    /**
     * Revoke project permission
     */
    void revokeProjectPermission(UUID projectId, UUID teamId, UUID userId, UUID revokedBy);

    /**
     * Get project permissions
     */
    List<ProjectPermissionResponseDTO> getProjectPermissions(UUID projectId);

    /**
     * Check if user has project permission
     */
    boolean hasProjectPermission(UUID projectId, UUID userId, PermissionType requiredPermission);

    /**
     * Grant task permission to team or user
     */
    TaskPermissionResponseDTO grantTaskPermission(UUID taskId, PermissionGrantRequestDTO request, UUID grantedBy);

    /**
     * Revoke task permission
     */
    void revokeTaskPermission(UUID taskId, UUID teamId, UUID userId, UUID revokedBy);

    /**
     * Get task permissions
     */
    List<TaskPermissionResponseDTO> getTaskPermissions(UUID taskId);

    /**
     * Check if user has task permission
     */
    boolean hasTaskPermission(UUID taskId, UUID userId, PermissionType requiredPermission);
}
