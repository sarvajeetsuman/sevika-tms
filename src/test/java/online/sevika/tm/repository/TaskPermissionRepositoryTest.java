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
 * Tests for TaskPermissionRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskPermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskPermissionRepository taskPermissionRepository;

    private Task task;
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

        Project project = Project.builder()
                .name("Test Project")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(project);

        task = Task.builder()
                .title("Test Task")
                .description("Test task")
                .project(project)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(task);

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
    void shouldSaveTaskPermission() {
        TaskPermission permission = createUserPermission(task.getId(), user.getId(), PermissionType.WRITE);

        TaskPermission saved = taskPermissionRepository.save(permission);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTaskId()).isEqualTo(task.getId());
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getPermission()).isEqualTo(PermissionType.WRITE);
    }

    @Test
    void shouldFindPermissionsByTaskId() {
        TaskPermission perm1 = createUserPermission(task.getId(), user.getId(), PermissionType.READ);
        TaskPermission perm2 = createTeamPermission(task.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(perm1);
        entityManager.persist(perm2);
        entityManager.flush();

        List<TaskPermission> permissions = taskPermissionRepository.findByTaskId(task.getId());

        assertThat(permissions).hasSize(2);
    }

    @Test
    void shouldFindPermissionsByTeamId() {
        TaskPermission permission = createTeamPermission(task.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(permission);
        entityManager.flush();

        List<TaskPermission> permissions = taskPermissionRepository.findByTeamId(team.getId());

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getTeamId()).isEqualTo(team.getId());
    }

    @Test
    void shouldFindPermissionsByUserId() {
        TaskPermission permission = createUserPermission(task.getId(), user.getId(), PermissionType.READ);
        entityManager.persist(permission);
        entityManager.flush();

        List<TaskPermission> permissions = taskPermissionRepository.findByUserId(user.getId());

        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getUserId()).isEqualTo(user.getId());
    }

    @Test
    void shouldFindTeamPermissionForTask() {
        TaskPermission permission = createTeamPermission(task.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(permission);
        entityManager.flush();

        Optional<TaskPermission> found = taskPermissionRepository.findByTaskIdAndTeamId(task.getId(), team.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPermission()).isEqualTo(PermissionType.WRITE);
    }

    @Test
    void shouldFindUserPermissionForTask() {
        TaskPermission permission = createUserPermission(task.getId(), user.getId(), PermissionType.DELETE);
        entityManager.persist(permission);
        entityManager.flush();

        Optional<TaskPermission> found = taskPermissionRepository.findByTaskIdAndUserId(task.getId(), user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPermission()).isEqualTo(PermissionType.DELETE);
    }

    @Test
    void shouldCheckIfUserHasAccessToTask() {
        TaskPermission permission = createUserPermission(task.getId(), user.getId(), PermissionType.READ);
        entityManager.persist(permission);
        entityManager.flush();

        boolean hasAccess = taskPermissionRepository.hasUserAccessToTask(task.getId(), user.getId());

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldCheckIfUserHasAccessThroughTeam() {
        TaskPermission permission = createTeamPermission(task.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(permission);
        entityManager.flush();

        boolean hasAccess = taskPermissionRepository.hasUserAccessToTask(task.getId(), user.getId());

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldFindUserPermissionsOnTask() {
        TaskPermission directPerm = createUserPermission(task.getId(), user.getId(), PermissionType.READ);
        TaskPermission teamPerm = createTeamPermission(task.getId(), team.getId(), PermissionType.WRITE);
        entityManager.persist(directPerm);
        entityManager.persist(teamPerm);
        entityManager.flush();

        List<TaskPermission> permissions = taskPermissionRepository.findUserPermissionsOnTask(task.getId(), user.getId());

        assertThat(permissions).hasSize(2);
    }

    private TaskPermission createUserPermission(UUID taskId, UUID userId, PermissionType permission) {
        TaskPermission perm = new TaskPermission();
        perm.setTaskId(taskId);
        perm.setUserId(userId);
        perm.setPermission(permission);
        perm.setGrantedAt(LocalDateTime.now());
        perm.setGrantedBy(granter.getId());
        return perm;
    }

    private TaskPermission createTeamPermission(UUID taskId, UUID teamId, PermissionType permission) {
        TaskPermission perm = new TaskPermission();
        perm.setTaskId(taskId);
        perm.setTeamId(teamId);
        perm.setPermission(permission);
        perm.setGrantedAt(LocalDateTime.now());
        perm.setGrantedBy(granter.getId());
        return perm;
    }
}
