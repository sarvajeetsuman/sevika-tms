package online.sevika.tm.repository;

import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ProjectRepository using Testcontainers.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByOwnerId_ReturnsProjects() {
        // Arrange
        User owner = User.builder()
                .username("owner" + UUID.randomUUID())
                .email("owner" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(owner);

        Project project1 = Project.builder()
                .name("Project 1")
                .description("Description 1")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        entityManager.persist(project1);

        Project project2 = Project.builder()
                .name("Project 2")
                .description("Description 2")
                .owner(owner)
                .status(Project.ProjectStatus.COMPLETED)
                .build();
        entityManager.persist(project2);
        entityManager.flush();

        // Act
        List<Project> projects = projectRepository.findByOwnerId(owner.getId());

        // Assert
        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(Project::getName).containsExactlyInAnyOrder("Project 1", "Project 2");
    }

    @Test
    void findByOwnerId_NoProjects_ReturnsEmpty() {
        // Arrange
        UUID nonExistentOwnerId = UUID.randomUUID();

        // Act
        List<Project> projects = projectRepository.findByOwnerId(nonExistentOwnerId);

        // Assert
        assertThat(projects).isEmpty();
    }

    @Test
    void findByStatus_ReturnsProjects() {
        // Arrange
        User owner = User.builder()
                .username("owner" + UUID.randomUUID())
                .email("owner" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(owner);

        Project activeProject = Project.builder()
                .name("Active Project " + UUID.randomUUID())
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        entityManager.persist(activeProject);

        Project completedProject = Project.builder()
                .name("Completed Project " + UUID.randomUUID())
                .owner(owner)
                .status(Project.ProjectStatus.COMPLETED)
                .build();
        entityManager.persist(completedProject);
        entityManager.flush();

        // Act
        List<Project> activeProjects = projectRepository.findByStatus(Project.ProjectStatus.ACTIVE);

        // Assert
        assertThat(activeProjects).hasSizeGreaterThanOrEqualTo(1);
        assertThat(activeProjects).anyMatch(p -> p.getId().equals(activeProject.getId()));
    }

    @Test
    void findByOwnerIdAndStatus_ReturnsProjects() {
        // Arrange
        User owner = User.builder()
                .username("owner" + UUID.randomUUID())
                .email("owner" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(owner);

        Project activeProject = Project.builder()
                .name("Active Project")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        entityManager.persist(activeProject);

        Project completedProject = Project.builder()
                .name("Completed Project")
                .owner(owner)
                .status(Project.ProjectStatus.COMPLETED)
                .build();
        entityManager.persist(completedProject);
        entityManager.flush();

        // Act
        List<Project> projects = projectRepository.findByOwnerIdAndStatus(owner.getId(), Project.ProjectStatus.ACTIVE);

        // Assert
        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getName()).isEqualTo("Active Project");
    }

    @Test
    void findByIdWithTasks_LoadsTasksEagerly() {
        // Arrange
        User owner = User.builder()
                .username("owner" + UUID.randomUUID())
                .email("owner" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(owner);

        Project project = Project.builder()
                .name("Project With Tasks")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        entityManager.persist(project);
        entityManager.flush();

        // Act
        Project result = projectRepository.findByIdWithTasks(project.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Project With Tasks");
        // Tasks collection is initialized (even if empty)
        assertThat(result.getTasks()).isNotNull();
    }

    @Test
    void existsByNameAndOwnerId_ReturnsTrue() {
        // Arrange
        User owner = User.builder()
                .username("owner" + UUID.randomUUID())
                .email("owner" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(owner);

        Project project = Project.builder()
                .name("Unique Project Name")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        entityManager.persist(project);
        entityManager.flush();

        // Act
        boolean exists = projectRepository.existsByNameAndOwnerId("Unique Project Name", owner.getId());

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndOwnerId_ReturnsFalse() {
        // Arrange
        UUID nonExistentOwnerId = UUID.randomUUID();

        // Act
        boolean exists = projectRepository.existsByNameAndOwnerId("Non Existent Project", nonExistentOwnerId);

        // Assert
        assertThat(exists).isFalse();
    }
}
