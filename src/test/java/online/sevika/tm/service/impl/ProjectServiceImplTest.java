package online.sevika.tm.service.impl;

import online.sevika.tm.dto.ProjectRequestDTO;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;

import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.User;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.ProjectMapper;
import online.sevika.tm.repository.ProjectRepository;
import online.sevika.tm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User owner;
    private Project project;
    private ProjectRequestDTO request;
    private ProjectResponseDTO response;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(UUID.randomUUID())
                .username("owner")
                .email("owner@example.com")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();

        project = Project.builder()
                .id(UUID.randomUUID())
                .name("Test Project")
                .description("Test Description")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();

        request = ProjectRequestDTO.builder()
                .name("Test Project")
                .description("Test Description")
                .build();

        response = ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .build();
    }

    @Test
    void createProject_Success() {
        // Arrange
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(projectMapper.toEntity(request)).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        ProjectResponseDTO result = projectService.createProject(request, owner.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Project");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(owner.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.createProject(request, owner.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getProjectById_Success() {
        // Arrange
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        ProjectResponseDTO result = projectService.getProjectById(project.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(project.getId());
    }

    @Test
    void getProjectById_NotFound_ThrowsException() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> projectService.getProjectById(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void getAllProjects_ReturnsProjects() {
        // Arrange
        List<Project> projects = Arrays.asList(project);
        when(projectRepository.findAll()).thenReturn(projects);
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        List<ProjectResponseDTO> results = projectService.getAllProjects();

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void getProjectsByOwner_ReturnsProjects() {
        // Arrange
        List<Project> projects = Arrays.asList(project);
        when(projectRepository.findByOwnerId(owner.getId())).thenReturn(projects);
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        List<ProjectResponseDTO> results = projectService.getProjectsByOwner(owner.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void updateProject_Success() {
        // Arrange
        ProjectUpdateRequestDTO updateRequest = ProjectUpdateRequestDTO.builder()
                .name("Updated Project")
                .description("Updated Description")
                .status(Project.ProjectStatus.COMPLETED)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        ProjectResponseDTO result = projectService.updateProject(project.getId(), updateRequest, owner.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(projectMapper).updateEntityFromDto(updateRequest, project);
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_NotOwner_ThrowsException() {
        // Arrange
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other")
                .role(User.Role.USER)
                .build();

        ProjectUpdateRequestDTO updateRequest = ProjectUpdateRequestDTO.builder()
                .name("Updated Project")
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThatThrownBy(() -> projectService.updateProject(project.getId(), updateRequest, otherUser.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("don't have permission");
    }

    @Test
    void updateProject_AdminCanUpdate() {
        // Arrange
        User admin = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .role(User.Role.ADMIN)
                .build();

        ProjectUpdateRequestDTO updateRequest = ProjectUpdateRequestDTO.builder()
                .name("Updated Project")
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        // Act
        ProjectResponseDTO result = projectService.updateProject(project.getId(), updateRequest, admin.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(projectRepository).save(project);
    }

    @Test
    void deleteProject_Success() {
        // Arrange
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        // Act
        projectService.deleteProject(project.getId(), owner.getId());

        // Assert
        verify(projectRepository).deleteById(project.getId());
    }

    @Test
    void deleteProject_NotOwner_ThrowsException() {
        // Arrange
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .username("other")
                .role(User.Role.USER)
                .build();

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThatThrownBy(() -> projectService.deleteProject(project.getId(), otherUser.getId()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("don't have permission");
        verify(projectRepository, never()).deleteById(any());
    }
}
