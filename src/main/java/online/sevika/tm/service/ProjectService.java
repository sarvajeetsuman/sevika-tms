package online.sevika.tm.service;

import online.sevika.tm.dto.ProjectRequestDTO;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Project operations.
 */
public interface ProjectService {

    /**
     * Create a new project
     */
    ProjectResponseDTO createProject(ProjectRequestDTO request, UUID ownerId);

    /**
     * Get project by ID
     */
    ProjectResponseDTO getProjectById(UUID id);

    /**
     * Get all projects
     */
    List<ProjectResponseDTO> getAllProjects();

    /**
     * Get projects by owner
     */
    List<ProjectResponseDTO> getProjectsByOwner(UUID ownerId);

    /**
     * Update project
     */
    ProjectResponseDTO updateProject(UUID id, ProjectUpdateRequestDTO updateRequest, UUID userId);

    /**
     * Delete project
     */
    void deleteProject(UUID id, UUID userId);
}
