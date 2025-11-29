package online.sevika.tm.controller;
import online.sevika.tm.dto.PermissionGrantRequestDTO;
import online.sevika.tm.dto.ProjectPermissionResponseDTO;
import online.sevika.tm.dto.TaskPermissionResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Permission operations
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "APIs for managing project and task permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Grant project permission")
    public ResponseEntity<ProjectPermissionResponseDTO> grantProjectPermission(
            @PathVariable UUID projectId,
            @Valid @RequestBody PermissionGrantRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        ProjectPermissionResponseDTO response = permissionService.grantProjectPermission(projectId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Revoke project permission")
    public ResponseEntity<Void> revokeProjectPermission(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID userId,
            Authentication authentication) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        permissionService.revokeProjectPermission(projectId, teamId, userId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project permissions")
    public ResponseEntity<List<ProjectPermissionResponseDTO>> getProjectPermissions(
            @PathVariable UUID projectId) {
        List<ProjectPermissionResponseDTO> response = permissionService.getProjectPermissions(projectId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Grant task permission")
    public ResponseEntity<TaskPermissionResponseDTO> grantTaskPermission(
            @PathVariable UUID taskId,
            @Valid @RequestBody PermissionGrantRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        TaskPermissionResponseDTO response = permissionService.grantTaskPermission(taskId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Revoke task permission")
    public ResponseEntity<Void> revokeTaskPermission(
            @PathVariable UUID taskId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID userId,
            Authentication authentication) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        permissionService.revokeTaskPermission(taskId, teamId, userId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get task permissions")
    public ResponseEntity<List<TaskPermissionResponseDTO>> getTaskPermissions(
            @PathVariable UUID taskId) {
        List<TaskPermissionResponseDTO> response = permissionService.getTaskPermissions(taskId);
        return ResponseEntity.ok(response);
    }
}
