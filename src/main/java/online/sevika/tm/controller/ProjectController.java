package online.sevika.tm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.dto.ProjectRequestDTO;
import online.sevika.tm.dto.ProjectResponseDTO;
import online.sevika.tm.dto.ProjectUpdateRequestDTO;
import online.sevika.tm.dto.ProjectSummaryDTO;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for project operations.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Create a new project")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProjectResponseDTO response = projectService.createProject(request, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all projects", description = "Retrieve all projects")
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieve a project by its ID")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/my-projects")
    @Operation(summary = "Get my projects", description = "Retrieve projects owned by the current user")
    public ResponseEntity<List<ProjectResponseDTO>> getMyProjects(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ProjectResponseDTO> projects = projectService.getProjectsByOwner(user.getId());
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Update project information")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectUpdateRequestDTO request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProjectResponseDTO response = projectService.updateProject(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Delete a project")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        projectService.deleteProject(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
