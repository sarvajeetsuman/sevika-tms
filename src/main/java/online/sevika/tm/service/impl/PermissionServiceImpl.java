package online.sevika.tm.service.impl;
import online.sevika.tm.dto.PermissionGrantRequestDTO;
import online.sevika.tm.dto.ProjectPermissionResponseDTO;
import online.sevika.tm.dto.TaskPermissionResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.ProjectPermission;
import online.sevika.tm.entity.Task;
import online.sevika.tm.entity.TaskPermission;
import online.sevika.tm.entity.Team;
import online.sevika.tm.entity.User;
import online.sevika.tm.entity.enums.PermissionType;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.PermissionMapper;
import online.sevika.tm.repository.ProjectPermissionRepository;
import online.sevika.tm.repository.ProjectRepository;
import online.sevika.tm.repository.TaskPermissionRepository;
import online.sevika.tm.repository.TaskRepository;
import online.sevika.tm.repository.TeamRepository;
import online.sevika.tm.repository.UserRepository;
import online.sevika.tm.service.PermissionService;
import online.sevika.tm.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Permission operations
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final ProjectPermissionRepository projectPermissionRepository;
    private final TaskPermissionRepository taskPermissionRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PermissionMapper permissionMapper;
    private final TeamService teamService;

    @Override
    public ProjectPermissionResponseDTO grantProjectPermission(UUID projectId, PermissionGrantRequestDTO request, UUID grantedBy) {
        log.info("Granting project permission for project: {} by user: {}", projectId, grantedBy);

        // Verify project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Validate request
        if (request.getTeamId() == null && request.getUserId() == null) {
            throw new IllegalArgumentException("Either teamId or userId must be provided");
        }
        if (request.getTeamId() != null && request.getUserId() != null) {
            throw new IllegalArgumentException("Cannot provide both teamId and userId");
        }

        // Check if granter has admin permission or is project owner
        if (!hasProjectPermission(projectId, grantedBy, PermissionType.ADMIN) && 
            !project.getOwner().getId().equals(grantedBy)) {
            throw new UnauthorizedException("Only project admin can grant permissions");
        }

        // Verify team or user exists
        if (request.getTeamId() != null) {
            teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));
        } else {
            userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        }

        // Check if permission already exists
        if (request.getTeamId() != null) {
            projectPermissionRepository.findByProjectIdAndTeamId(projectId, request.getTeamId())
                    .ifPresent(p -> {
                        throw new IllegalArgumentException("Permission already exists for this team");
                    });
        } else {
            projectPermissionRepository.findByProjectIdAndUserId(projectId, request.getUserId())
                    .ifPresent(p -> {
                        throw new IllegalArgumentException("Permission already exists for this user");
                    });
        }

        // Create permission
        ProjectPermission permission = new ProjectPermission();
        permission.setId(UUID.randomUUID());
        permission.setProjectId(projectId);
        permission.setTeamId(request.getTeamId());
        permission.setUserId(request.getUserId());
        permission.setPermission(request.getPermission());
        permission.setGrantedAt(LocalDateTime.now());
        permission.setGrantedBy(grantedBy);
        permission = projectPermissionRepository.save(permission);

        return toProjectPermissionResponseWithDetails(permission);
    }

    @Override
    public void revokeProjectPermission(UUID projectId, UUID teamId, UUID userId, UUID revokedBy) {
        log.info("Revoking project permission for project: {} by user: {}", projectId, revokedBy);

        // Verify project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Check if revoker has admin permission or is project owner
        if (!hasProjectPermission(projectId, revokedBy, PermissionType.ADMIN) && 
            !project.getOwner().getId().equals(revokedBy)) {
            throw new UnauthorizedException("Only project admin can revoke permissions");
        }

        if (teamId != null) {
            projectPermissionRepository.deleteByProjectIdAndTeamId(projectId, teamId);
        } else if (userId != null) {
            projectPermissionRepository.deleteByProjectIdAndUserId(projectId, userId);
        } else {
            throw new IllegalArgumentException("Either teamId or userId must be provided");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectPermissionResponseDTO> getProjectPermissions(UUID projectId) {
        log.info("Fetching permissions for project: {}", projectId);

        // Verify project exists
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        return projectPermissionRepository.findByProjectId(projectId).stream()
                .map(this::toProjectPermissionResponseWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProjectPermission(UUID projectId, UUID userId, PermissionType requiredPermission) {
        // Check if user is project owner
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && project.getOwner().getId().equals(userId)) {
            return true;
        }

        // Get user's permissions (direct or through team)
        List<ProjectPermission> permissions = projectPermissionRepository.findUserPermissionsOnProject(projectId, userId);
        
        if (permissions.isEmpty()) {
            return false;
        }

        // Check permission hierarchy: ADMIN > DELETE > WRITE > READ
        return permissions.stream().anyMatch(p -> hasRequiredPermission(p.getPermission(), requiredPermission));
    }

    @Override
    public TaskPermissionResponseDTO grantTaskPermission(UUID taskId, PermissionGrantRequestDTO request, UUID grantedBy) {
        log.info("Granting task permission for task: {} by user: {}", taskId, grantedBy);

        // Verify task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Validate request
        if (request.getTeamId() == null && request.getUserId() == null) {
            throw new IllegalArgumentException("Either teamId or userId must be provided");
        }
        if (request.getTeamId() != null && request.getUserId() != null) {
            throw new IllegalArgumentException("Cannot provide both teamId and userId");
        }

        // Check if granter has admin permission on task or project
        if (!hasTaskPermission(taskId, grantedBy, PermissionType.ADMIN) && 
            !hasProjectPermission(task.getProject().getId(), grantedBy, PermissionType.ADMIN)) {
            throw new UnauthorizedException("Only task or project admin can grant permissions");
        }

        // Verify team or user exists
        if (request.getTeamId() != null) {
            teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + request.getTeamId()));
        } else {
            userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        }

        // Check if permission already exists
        if (request.getTeamId() != null) {
            taskPermissionRepository.findByTaskIdAndTeamId(taskId, request.getTeamId())
                    .ifPresent(p -> {
                        throw new IllegalArgumentException("Permission already exists for this team");
                    });
        } else {
            taskPermissionRepository.findByTaskIdAndUserId(taskId, request.getUserId())
                    .ifPresent(p -> {
                        throw new IllegalArgumentException("Permission already exists for this user");
                    });
        }

        // Create permission
        TaskPermission permission = new TaskPermission();
        permission.setId(UUID.randomUUID());
        permission.setTaskId(taskId);
        permission.setTeamId(request.getTeamId());
        permission.setUserId(request.getUserId());
        permission.setPermission(request.getPermission());
        permission.setGrantedAt(LocalDateTime.now());
        permission.setGrantedBy(grantedBy);
        permission = taskPermissionRepository.save(permission);

        return toTaskPermissionResponseWithDetails(permission);
    }

    @Override
    public void revokeTaskPermission(UUID taskId, UUID teamId, UUID userId, UUID revokedBy) {
        log.info("Revoking task permission for task: {} by user: {}", taskId, revokedBy);

        // Verify task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Check if revoker has admin permission
        if (!hasTaskPermission(taskId, revokedBy, PermissionType.ADMIN) && 
            !hasProjectPermission(task.getProject().getId(), revokedBy, PermissionType.ADMIN)) {
            throw new UnauthorizedException("Only task or project admin can revoke permissions");
        }

        if (teamId != null) {
            taskPermissionRepository.deleteByTaskIdAndTeamId(taskId, teamId);
        } else if (userId != null) {
            taskPermissionRepository.deleteByTaskIdAndUserId(taskId, userId);
        } else {
            throw new IllegalArgumentException("Either teamId or userId must be provided");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskPermissionResponseDTO> getTaskPermissions(UUID taskId) {
        log.info("Fetching permissions for task: {}", taskId);

        // Verify task exists
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        return taskPermissionRepository.findByTaskId(taskId).stream()
                .map(this::toTaskPermissionResponseWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasTaskPermission(UUID taskId, UUID userId, PermissionType requiredPermission) {
        // Get task
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return false;
        }

        // Check task-specific permissions first
        List<TaskPermission> taskPermissions = taskPermissionRepository.findUserPermissionsOnTask(taskId, userId);
        if (!taskPermissions.isEmpty()) {
            return taskPermissions.stream().anyMatch(p -> hasRequiredPermission(p.getPermission(), requiredPermission));
        }

        // Fall back to project permissions
        return hasProjectPermission(task.getProject().getId(), userId, requiredPermission);
    }

    private boolean hasRequiredPermission(PermissionType granted, PermissionType required) {
        // Permission hierarchy: ADMIN > DELETE > WRITE > READ
        int grantedLevel = getPermissionLevel(granted);
        int requiredLevel = getPermissionLevel(required);
        return grantedLevel >= requiredLevel;
    }

    private int getPermissionLevel(PermissionType permission) {
        return switch (permission) {
            case READ -> 1;
            case WRITE -> 2;
            case DELETE -> 3;
            case ADMIN -> 4;
        };
    }

    private ProjectPermissionResponseDTO toProjectPermissionResponseWithDetails(ProjectPermission permission) {
        ProjectPermissionResponseDTO response = permissionMapper.toProjectPermissionResponse(permission);
        
        // Get project name
        projectRepository.findById(permission.getProjectId()).ifPresent(project -> 
            response.setProjectName(project.getName())
        );
        
        // Get team name if applicable
        if (permission.getTeamId() != null) {
            teamRepository.findById(permission.getTeamId()).ifPresent(team -> 
                response.setTeamName(team.getName())
            );
        }
        
        // Get user name if applicable
        if (permission.getUserId() != null) {
            userRepository.findById(permission.getUserId()).ifPresent(user -> 
                response.setUsername(user.getUsername())
            );
        }
        
        // Get granter name
        userRepository.findById(permission.getGrantedBy()).ifPresent(user -> 
            response.setGrantedByUsername(user.getUsername())
        );
        
        return response;
    }

    private TaskPermissionResponseDTO toTaskPermissionResponseWithDetails(TaskPermission permission) {
        TaskPermissionResponseDTO response = permissionMapper.toTaskPermissionResponse(permission);
        
        // Get task title
        taskRepository.findById(permission.getTaskId()).ifPresent(task -> 
            response.setTaskTitle(task.getTitle())
        );
        
        // Get team name if applicable
        if (permission.getTeamId() != null) {
            teamRepository.findById(permission.getTeamId()).ifPresent(team -> 
                response.setTeamName(team.getName())
            );
        }
        
        // Get user name if applicable
        if (permission.getUserId() != null) {
            userRepository.findById(permission.getUserId()).ifPresent(user -> 
                response.setUsername(user.getUsername())
            );
        }
        
        // Get granter name
        userRepository.findById(permission.getGrantedBy()).ifPresent(user -> 
            response.setGrantedByUsername(user.getUsername())
        );
        
        return response;
    }
}
