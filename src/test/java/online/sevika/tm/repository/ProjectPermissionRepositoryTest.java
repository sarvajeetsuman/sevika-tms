package online.sevika.tm.repository;

import online.sevika.tm.entity.*;
import online.sevika.tm.entity.enums.PermissionType;
import online.sevika.tm.entity.enums.TeamRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ProjectPermissionRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectPermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectPermissionRepository projectPermissionRepository;

    private Project project;
    private Team team;
    private User user;
    private User granter;

    @BeforeEach
    void setUp() {
        User owner = User.builder()
                .username("owner")
                .email("owner@test.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(owner);

        user = User.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(user);

        granter = User.builder()
                .username("granter")
                .email("granter@test.com")
                .password("password")
                .firstName("Granter")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(granter);

        project = Project.builder()
                .name("Test Project")
                .description("Test project")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(project);

        team = new Team();
        team.setName("Test Team");
        team.setOwnerId(owner.getId());
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(team);

        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(user.getId());
        teamMember.setRole(TeamRole.MEMBER);
        teamMember.setJoinedAt(LocalDateTime.now());
        entityManager.persist(teamMember);

        entityManager.flush();
    }

    @Test
    void shouldSaveProjectPermission() {
        ProjectPermission permission = createUserPermission(project.getId(), user.getId(), PermissionType.WRITE);

        ProjectPermission saved = projectPermissionRepository.save(permission);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProjectId()).isEqualTo(project.getId());
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getPermission()).isEqualTo(PermissionType.WRITE);
    }

    @Test
    void shouldFindPermissionsByProjectId() {
        ProjectPermission perm1 = createUserPermission(project.getId(), user.getId(), PermissionType.READ);
        ProjectPermission perm2 = createTeamPermission(project.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(perm1);
        entityManager.persist(perm2);
        entityManager.flush();

        List<ProjectPermission> permissions = projectPermissionRepository.findByProjectId(project.getId());

        assertThat(permissions).hasSize(2);
    }

    @Test
    void shouldFindPermissionsByTeamId() {
        ProjectPermission permission = createTeamPermission(project.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(permission);
        entityManager.flush();

        List<ProjectPermission> permissions = projectPermissionRepository.findByTeamId(team.getId());

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getTeamId()).isEqualTo(team.getId());
    }

    @Test
    void shouldFindPermissionsByUserId() {
        ProjectPermission permission = createUserPermission(project.getId(), user.getId(), PermissionType.READ);
        entityManager.persist(permission);
        entityManager.flush();

        List<ProjectPermission> permissions = projectPermissionRepository.findByUserId(user.getId());

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getUserId()).isEqualTo(user.getId());
    }

    @Test
    void shouldFindTeamPermissionForProject() {
        ProjectPermission permission = createTeamPermission(project.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(permission);
        entityManager.flush();

        Optional<ProjectPermission> found = projectPermissionRepository.findByProjectIdAndTeamId(
                project.getId(), team.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPermission()).isEqualTo(PermissionType.WRITE);
    }

    @Test
    void shouldFindUserPermissionForProject() {
        ProjectPermission permission = createUserPermission(project.getId(), user.getId(), PermissionType.DELETE);
        entityManager.persist(permission);
        entityManager.flush();

        Optional<ProjectPermission> found = projectPermissionRepository.findByProjectIdAndUserId(
                project.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPermission()).isEqualTo(PermissionType.DELETE);
    }

    @Test
    void shouldCheckIfUserHasAccessToProject() {
        ProjectPermission permission = createUserPermission(project.getId(), user.getId(), PermissionType.READ);
        entityManager.persist(permission);
        entityManager.flush();

        boolean hasAccess = projectPermissionRepository.hasUserAccessToProject(project.getId(), user.getId());

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldCheckIfUserHasAccessThroughTeam() {
        ProjectPermission permission = createTeamPermission(project.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(permission);
        entityManager.flush();

        boolean hasAccess = projectPermissionRepository.hasUserAccessToProject(project.getId(), user.getId());

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldFindUserPermissionsOnProject() {
        ProjectPermission directPerm = createUserPermission(project.getId(), user.getId(), PermissionType.READ);
        ProjectPermission teamPerm = createTeamPermission(project.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(directPerm);
        entityManager.persist(teamPerm);
        entityManager.flush();

        List<ProjectPermission> permissions = projectPermissionRepository.findUserPermissionsOnProject(
                project.getId(), user.getId());

        assertThat(permissions).hasSize(2);
    }

    private ProjectPermission createUserPermission(UUID projectId, UUID userId, PermissionType permission) {
        ProjectPermission perm = new ProjectPermission();
        perm.setProjectId(projectId);
        perm.setUserId(userId);
        perm.setPermission(permission);
        perm.setGrantedAt(LocalDateTime.now());
        perm.setGrantedBy(granter.getId());
        return perm;
    }

    private ProjectPermission createTeamPermission(UUID projectId, UUID teamId, PermissionType permission) {
        ProjectPermission perm = new ProjectPermission();
        perm.setProjectId(projectId);
        perm.setTeamId(teamId);
        perm.setPermission(permission);
        perm.setGrantedAt(LocalDateTime.now());
        perm.setGrantedBy(granter.getId());
        return perm;
    }
}
