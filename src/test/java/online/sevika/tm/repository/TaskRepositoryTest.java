package online.sevika.tm.repository;

import online.sevika.tm.entity.Project;
import online.sevika.tm.entity.Task;
import online.sevika.tm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskRepository using Testcontainers.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User owner;
    private User assignee;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .username("owner" + UUID.randomUUID())
                .email("owner" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Owner")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(owner);

        assignee = User.builder()
                .username("assignee" + UUID.randomUUID())
                .email("assignee" + UUID.randomUUID() + "@example.com")
                .password("password")
                .firstName("Assignee")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        entityManager.persist(assignee);

        project = Project.builder()
                .name("Test Project")
                .owner(owner)
                .status(Project.ProjectStatus.ACTIVE)
                .build();
        entityManager.persist(project);
        entityManager.flush();
    }

    @Test
    void findByProjectId_ReturnsTasks() {
        // Arrange
        Task task1 = Task.builder()
                .title("Task 1")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Task 2")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(Task.TaskPriority.HIGH)
                .build();
        entityManager.persist(task2);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findByProjectId(project.getId());

        // Assert
        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void findByAssignedToId_ReturnsTasks() {
        // Arrange
        Task task = Task.builder()
                .title("Assigned Task")
                .project(project)
                .createdBy(owner)
                .assignedTo(assignee)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(task);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findByAssignedToId(assignee.getId());

        // Assert
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Assigned Task");
    }

    @Test
    void findByCreatedById_ReturnsTasks() {
        // Arrange
        Task task = Task.builder()
                .title("Created Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(task);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findByCreatedById(owner.getId());

        // Assert
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Created Task");
    }

    @Test
    void findByStatus_ReturnsTasks() {
        // Arrange
        Task todoTask = Task.builder()
                .title("Todo Task " + UUID.randomUUID())
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(todoTask);

        Task doneTask = Task.builder()
                .title("Done Task " + UUID.randomUUID())
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.DONE)
                .priority(Task.TaskPriority.LOW)
                .build();
        entityManager.persist(doneTask);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findByStatus(Task.TaskStatus.TODO);

        // Assert
        assertThat(tasks).hasSizeGreaterThanOrEqualTo(1);
        assertThat(tasks).anyMatch(t -> t.getId().equals(todoTask.getId()));
    }

    @Test
    void findByPriority_ReturnsTasks() {
        // Arrange
        Task highPriorityTask = Task.builder()
                .title("High Priority Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.HIGH)
                .build();
        entityManager.persist(highPriorityTask);

        Task lowPriorityTask = Task.builder()
                .title("Low Priority Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.LOW)
                .build();
        entityManager.persist(lowPriorityTask);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findByPriority(Task.TaskPriority.HIGH);

        // Assert
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("High Priority Task");
    }

    @Test
    void findByProjectIdAndStatus_ReturnsTasks() {
        // Arrange
        Task task = Task.builder()
                .title("Specific Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(task);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findByProjectIdAndStatus(project.getId(), Task.TaskStatus.IN_PROGRESS);

        // Assert
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Specific Task");
    }

    @Test
    void findOverdueTasks_ReturnsTasks() {
        // Arrange
        Task overdueTask = Task.builder()
                .title("Overdue Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(1))
                .build();
        entityManager.persist(overdueTask);

        Task futureTask = Task.builder()
                .title("Future Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .build();
        entityManager.persist(futureTask);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findOverdueTasks(LocalDate.now());

        // Assert - Check that our overdue task is in the results
        assertThat(tasks).isNotEmpty();
        assertThat(tasks).anyMatch(task -> task.getTitle().equals("Overdue Task"));
        assertThat(tasks).noneMatch(task -> task.getTitle().equals("Future Task"));
    }

    @Test
    void findTasksDueSoon_ReturnsTasks() {
        // Arrange
        LocalDate specificDueDate = LocalDate.now().plusDays(15);
        Task dueSoonTask = Task.builder()
                .title("Due Soon Task " + UUID.randomUUID())
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.HIGH)
                .dueDate(specificDueDate)
                .build();
        entityManager.persist(dueSoonTask);

        Task farFutureTask = Task.builder()
                .title("Far Future Task " + UUID.randomUUID())
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(30))
                .build();
        entityManager.persist(farFutureTask);
        entityManager.flush();

        // Act
        LocalDate startDate = specificDueDate.minusDays(1);
        LocalDate endDate = specificDueDate.plusDays(1);
        List<Task> tasks = taskRepository.findTasksDueSoon(startDate, endDate);

        // Assert
        assertThat(tasks).hasSizeGreaterThanOrEqualTo(1);
        assertThat(tasks).anyMatch(t -> t.getId().equals(dueSoonTask.getId()));
    }

    @Test
    void findTasksWithFilters_AllFilters_ReturnsTasks() {
        // Arrange
        Task matchingTask = Task.builder()
                .title("Matching Task")
                .project(project)
                .createdBy(owner)
                .assignedTo(assignee)
                .status(Task.TaskStatus.IN_PROGRESS)
                .priority(Task.TaskPriority.HIGH)
                .build();
        entityManager.persist(matchingTask);

        Task nonMatchingTask = Task.builder()
                .title("Non Matching Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.LOW)
                .build();
        entityManager.persist(nonMatchingTask);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findTasksWithFilters(
                project.getId(), assignee.getId(), Task.TaskStatus.IN_PROGRESS, Task.TaskPriority.HIGH);

        // Assert
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Matching Task");
    }

    @Test
    void findTasksWithFilters_NullFilters_ReturnsAllTasks() {
        // Arrange
        Task task1 = Task.builder()
                .title("Task 1")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Task 2")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.DONE)
                .priority(Task.TaskPriority.HIGH)
                .build();
        entityManager.persist(task2);
        entityManager.flush();

        // Act
        List<Task> tasks = taskRepository.findTasksWithFilters(null, null, null, null);

        // Assert
        assertThat(tasks).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void countByProjectId_ReturnsCount() {
        // Arrange
        Task task1 = Task.builder()
                .title("Task 1")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(task1);

        Task task2 = Task.builder()
                .title("Task 2")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.HIGH)
                .build();
        entityManager.persist(task2);
        entityManager.flush();

        // Act
        Long count = taskRepository.countByProjectId(project.getId());

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByProjectIdAndStatus_ReturnsCount() {
        // Arrange
        Task todoTask = Task.builder()
                .title("Todo Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.TODO)
                .priority(Task.TaskPriority.MEDIUM)
                .build();
        entityManager.persist(todoTask);

        Task doneTask = Task.builder()
                .title("Done Task")
                .project(project)
                .createdBy(owner)
                .status(Task.TaskStatus.DONE)
                .priority(Task.TaskPriority.HIGH)
                .build();
        entityManager.persist(doneTask);
        entityManager.flush();

        // Act
        Long count = taskRepository.countByProjectIdAndStatus(project.getId(), Task.TaskStatus.TODO);

        // Assert
        assertThat(count).isEqualTo(1);
    }
}
