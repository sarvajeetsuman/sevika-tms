package online.sevika.tm.controller;
import online.sevika.tm.dto.TeamMemberRequestDTO;
import online.sevika.tm.dto.TeamMemberResponseDTO;
import online.sevika.tm.dto.TeamMemberUpdateRoleDTO;
import online.sevika.tm.dto.TeamRequestDTO;
import online.sevika.tm.dto.TeamResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.sevika.tm.entity.enums.TeamRole;
import online.sevika.tm.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Team operations
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "APIs for managing teams and team members")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new team")
    public ResponseEntity<TeamResponseDTO> createTeam(
            @Valid @RequestBody TeamRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        TeamResponseDTO response = teamService.createTeam(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{teamId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get team by ID")
    public ResponseEntity<TeamResponseDTO> getTeam(@PathVariable UUID teamId) {
        TeamResponseDTO response = teamService.getTeamById(teamId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{teamId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update team")
    public ResponseEntity<TeamResponseDTO> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        TeamResponseDTO response = teamService.updateTeam(teamId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{teamId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete team")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable UUID teamId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        teamService.deleteTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-teams")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get teams owned by current user")
    public ResponseEntity<List<TeamResponseDTO>> getMyTeams(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<TeamResponseDTO> response = teamService.getTeamsOwnedByUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member-of")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get teams where current user is a member")
    public ResponseEntity<List<TeamResponseDTO>> getTeamsMemberOf(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<TeamResponseDTO> response = teamService.getTeamsForUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get team members")
    public ResponseEntity<List<TeamMemberResponseDTO>> getTeamMembers(@PathVariable UUID teamId) {
        List<TeamMemberResponseDTO> response = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{teamId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add member to team")
    public ResponseEntity<TeamMemberResponseDTO> addTeamMember(
            @PathVariable UUID teamId,
            @Valid @RequestBody TeamMemberRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        TeamMemberResponseDTO response = teamService.addTeamMember(teamId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove member from team")
    public ResponseEntity<Void> removeTeamMember(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        teamService.removeTeamMember(teamId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{teamId}/members/{memberId}/role")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update team member role")
    public ResponseEntity<TeamMemberResponseDTO> updateTeamMemberRole(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @Valid @RequestBody TeamMemberUpdateRoleDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        TeamMemberResponseDTO response = teamService.updateTeamMemberRole(teamId, memberId, request.getRole(), userId);
        return ResponseEntity.ok(response);
    }
}
