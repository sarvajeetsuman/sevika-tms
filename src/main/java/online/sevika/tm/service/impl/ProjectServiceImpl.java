package online.sevika.tm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.dto.ProjectRequestDTO;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.User;
import online.sevika.tm.exception.ResourceNotFoundException;
import online.sevika.tm.exception.UnauthorizedException;
import online.sevika.tm.mapper.ProjectMapper;
import online.sevika.tm.repository.ProjectRepository;
import online.sevika.tm.repository.UserRepository;
import online.sevika.tm.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ProjectService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO request, UUID ownerId) {
        log.info("Creating new project: {} for owner ID: {}", request.getName(), ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + ownerId));

        Project project = projectMapper.toEntity(request);
        project.setOwner(owner);
        project.setStatus(Project.ProjectStatus.ACTIVE);

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return projectMapper.toResponse(savedProject);
    }

    @Override
    public ProjectResponseDTO getProjectById(UUID id) {
        log.debug("Fetching project by ID: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponseDTO> getAllProjects() {
        log.debug("Fetching all projects");
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectResponseDTO> getProjectsByOwner(UUID ownerId) {
        log.debug("Fetching projects for owner ID: {}", ownerId);
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(UUID id, ProjectUpdateRequestDTO updateRequest, UUID userId) {
        log.info("Updating project with ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!project.getOwner().getId().equals(userId) && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("You don't have permission to update this project");
        }

        projectMapper.updateEntityFromDto(updateRequest, project);

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully with ID: {}", updatedProject.getId());

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id, UUID userId) {
        log.info("Deleting project with ID: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!project.getOwner().getId().equals(userId) && user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("You don't have permission to delete this project");
        }

        projectRepository.deleteById(id);
        log.info("Project deleted successfully with ID: {}", id);
    }
}
