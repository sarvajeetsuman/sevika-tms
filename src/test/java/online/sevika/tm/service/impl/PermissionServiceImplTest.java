package online.sevika.tm.service.impl;

import online.sevika.tm.dto.PermissionGrantRequestDTO;
import online.sevika.tm.dto.TaskPermissionResponseDTO;
import online.sevika.tm.dto.ProjectPermissionResponseDTO;
import online.sevika.tm.entity.*;
import online.sevika.tm.entity.enums.PermissionType;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.PermissionMapper;
import online.sevika.tm.repository.*;
import online.sevika.tm.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private ProjectPermissionRepository projectPermissionRepository;

    @Mock
    private TaskPermissionRepository taskPermissionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private UUID projectId;
    private UUID taskId;
    private UUID userId;
    private UUID teamId;
    private UUID ownerId;
    private Project project;
    private Task task;
    private User user;
    private User owner;
    private Team team;
    private ProjectPermission projectPermission;
    private TaskPermission taskPermission;
    private PermissionGrantRequestDTO grantRequest;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        owner = User.builder()
                .id(ownerId)
                .username("owner")
                .email("owner@example.com")
                .build();

        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        team = Team.builder()
                .id(teamId)
                .name("Test Team")
                .build();

        project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .owner(owner)
                .build();

        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .project(project)
                .build();

        projectPermission = new ProjectPermission();
        projectPermission.setId(UUID.randomUUID());
        projectPermission.setProjectId(projectId);
        projectPermission.setUserId(userId);
        projectPermission.setPermission(PermissionType.WRITE);
        projectPermission.setGrantedAt(LocalDateTime.now());
        projectPermission.setGrantedBy(ownerId);

        taskPermission = new TaskPermission();
        taskPermission.setId(UUID.randomUUID());
        taskPermission.setTaskId(taskId);
        taskPermission.setUserId(userId);
        taskPermission.setPermission(PermissionType.WRITE);
        taskPermission.setGrantedAt(LocalDateTime.now());
        taskPermission.setGrantedBy(ownerId);

        grantRequest = new PermissionGrantRequestDTO();
        grantRequest.setUserId(userId);
        grantRequest.setPermission(PermissionType.WRITE);
    }

    @Test
    void grantProjectPermission_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectPermissionRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.empty());
        when(projectPermissionRepository.save(any(ProjectPermission.class))).thenReturn(projectPermission);
        when(permissionMapper.toProjectPermissionResponse(any(ProjectPermission.class)))
                .thenReturn(new ProjectPermissionResponseDTO());

        // Act
        ProjectPermissionResponseDTO response = permissionService.grantProjectPermission(
                projectId, grantRequest, ownerId);

        // Assert
        assertNotNull(response);
        verify(projectRepository, times(3)).findById(projectId);
        verify(userRepository, times(2)).findById(userId);
        verify(projectPermissionRepository).save(any(ProjectPermission.class));
    }

    @Test
    void grantProjectPermission_ProjectNotFound_ThrowsException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                permissionService.grantProjectPermission(projectId, grantRequest, ownerId));

        verify(projectPermissionRepository, never()).save(any());
    }

    @Test
    void grantProjectPermission_NoTeamOrUser_ThrowsException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        PermissionGrantRequestDTO invalidRequest = new PermissionGrantRequestDTO();
        invalidRequest.setPermission(PermissionType.WRITE);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                permissionService.grantProjectPermission(projectId, invalidRequest, ownerId));
    }

    @Test
    void grantProjectPermission_BothTeamAndUser_ThrowsException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        PermissionGrantRequestDTO invalidRequest = new PermissionGrantRequestDTO();
        invalidRequest.setUserId(userId);
        invalidRequest.setTeamId(teamId);
        invalidRequest.setPermission(PermissionType.WRITE);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                permissionService.grantProjectPermission(projectId, invalidRequest, ownerId));
    }

    @Test
    void grantProjectPermission_NotAuthorized_ThrowsException() {
        // Arrange
        UUID unauthorizedUserId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectPermissionRepository.findUserPermissionsOnProject(projectId, unauthorizedUserId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(UnauthorizedException.class, () ->
                permissionService.grantProjectPermission(projectId, grantRequest, unauthorizedUserId));
    }

    @Test
    void grantProjectPermission_DuplicatePermission_ThrowsException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectPermissionRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(projectPermission));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                permissionService.grantProjectPermission(projectId, grantRequest, ownerId));
    }

    @Test
    void revokeProjectPermission_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        permissionService.revokeProjectPermission(projectId, null, userId, ownerId);

        // Assert
        verify(projectPermissionRepository).deleteByProjectIdAndUserId(projectId, userId);
    }

    @Test
    void revokeProjectPermission_ProjectNotFound_ThrowsException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                permissionService.revokeProjectPermission(projectId, null, userId, ownerId));
    }

    @Test
    void revokeProjectPermission_NoTeamOrUser_ThrowsException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                permissionService.revokeProjectPermission(projectId, null, null, ownerId));
    }

    @Test
    void getProjectPermissions_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectPermissionRepository.findByProjectId(projectId))
                .thenReturn(Collections.singletonList(projectPermission));
        when(permissionMapper.toProjectPermissionResponse(any(ProjectPermission.class)))
                .thenReturn(new ProjectPermissionResponseDTO());

        // Act
        List<ProjectPermissionResponseDTO> permissions = 
                permissionService.getProjectPermissions(projectId);

        // Assert
        assertNotNull(permissions);
        assertEquals(1, permissions.size());
        verify(projectRepository, times(2)).findById(projectId);
        verify(projectPermissionRepository).findByProjectId(projectId);
    }

    @Test
    void hasProjectPermission_OwnerAlwaysHasPermission() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        boolean hasPermission = permissionService.hasProjectPermission(projectId, ownerId, PermissionType.ADMIN);

        // Assert
        assertTrue(hasPermission);
    }

    @Test
    void hasProjectPermission_UserWithAdminPermission() {
        // Arrange
        ProjectPermission adminPermission = new ProjectPermission();
        adminPermission.setPermission(PermissionType.ADMIN);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectPermissionRepository.findUserPermissionsOnProject(projectId, userId))
                .thenReturn(Collections.singletonList(adminPermission));

        // Act
        boolean hasPermission = permissionService.hasProjectPermission(projectId, userId, PermissionType.WRITE);

        // Assert
        assertTrue(hasPermission);
    }

    @Test
    void hasProjectPermission_UserWithoutPermission() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectPermissionRepository.findUserPermissionsOnProject(projectId, userId))
                .thenReturn(Collections.emptyList());

        // Act
        boolean hasPermission = permissionService.hasProjectPermission(projectId, userId, PermissionType.WRITE);

        // Assert
        assertFalse(hasPermission);
    }

    @Test
    void grantTaskPermission_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskPermissionRepository.findByTaskIdAndUserId(taskId, userId))
                .thenReturn(Optional.empty());
        when(taskPermissionRepository.save(any(TaskPermission.class))).thenReturn(taskPermission);
        when(permissionMapper.toTaskPermissionResponse(any(TaskPermission.class)))
                .thenReturn(new TaskPermissionResponseDTO());

        // Act
        TaskPermissionResponseDTO response = permissionService.grantTaskPermission(
                taskId, grantRequest, ownerId);

        // Assert
        assertNotNull(response);
        verify(taskRepository, times(3)).findById(taskId);
        verify(userRepository, times(2)).findById(userId);
        verify(taskPermissionRepository).save(any(TaskPermission.class));
    }

    @Test
    void grantTaskPermission_TaskNotFound_ThrowsException() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                permissionService.grantTaskPermission(taskId, grantRequest, ownerId));
    }

    @Test
    void revokeTaskPermission_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act
        permissionService.revokeTaskPermission(taskId, null, userId, ownerId);

        // Assert
        verify(taskPermissionRepository).deleteByTaskIdAndUserId(taskId, userId);
    }

    @Test
    void getTaskPermissions_Success() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskPermissionRepository.findByTaskId(taskId))
                .thenReturn(Collections.singletonList(taskPermission));
        when(permissionMapper.toTaskPermissionResponse(any(TaskPermission.class)))
                .thenReturn(new TaskPermissionResponseDTO());

        // Act
        List<TaskPermissionResponseDTO> permissions = 
                permissionService.getTaskPermissions(taskId);

        // Assert
        assertNotNull(permissions);
        assertEquals(1, permissions.size());
        verify(taskRepository, times(2)).findById(taskId);
        verify(taskPermissionRepository).findByTaskId(taskId);
    }

    @Test
    void hasTaskPermission_WithTaskSpecificPermission() {
        // Arrange
        TaskPermission taskPerm = new TaskPermission();
        taskPerm.setPermission(PermissionType.WRITE);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskPermissionRepository.findUserPermissionsOnTask(taskId, userId))
                .thenReturn(Collections.singletonList(taskPerm));

        // Act
        boolean hasPermission = permissionService.hasTaskPermission(taskId, userId, PermissionType.READ);

        // Assert
        assertTrue(hasPermission);
    }

    @Test
    void hasTaskPermission_FallbackToProjectPermission() {
        // Arrange
        ProjectPermission projPerm = new ProjectPermission();
        projPerm.setPermission(PermissionType.ADMIN);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskPermissionRepository.findUserPermissionsOnTask(taskId, userId))
                .thenReturn(Collections.emptyList());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectPermissionRepository.findUserPermissionsOnProject(projectId, userId))
                .thenReturn(Collections.singletonList(projPerm));

        // Act
        boolean hasPermission = permissionService.hasTaskPermission(taskId, userId, PermissionType.WRITE);

        // Assert
        assertTrue(hasPermission);
    }

    @Test
    void hasTaskPermission_TaskNotFound_ReturnsFalse() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act
        boolean hasPermission = permissionService.hasTaskPermission(taskId, userId, PermissionType.READ);

        // Assert
        assertFalse(hasPermission);
    }
}
