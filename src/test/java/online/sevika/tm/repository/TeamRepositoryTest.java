package online.sevika.tm.repository;

import online.sevika.tm.entity.Team;
import online.sevika.tm.entity.TeamMember;
import online.sevika.tm.entity.User;
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
 * Tests for TeamRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TeamRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private User owner;
    private User member;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .username("teamowner")
                .email("owner@test.com")
                .password("password")
                .firstName("Team")
                .lastName("Owner")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(owner);

        member = User.builder()
                .username("teammember")
                .email("member@test.com")
                .password("password")
                .firstName("Team")
                .lastName("Member")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(member);

        entityManager.flush();
    }

    @Test
    void shouldSaveTeam() {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName("Development Team");
        team.setDescription("Backend development team");
        team.setOwnerId(owner.getId());
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());

        Team saved = teamRepository.save(team);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Development Team");
        assertThat(saved.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void shouldFindTeamByName() {
        Team team = createTeam("QA Team", owner.getId());
        entityManager.persist(team);
        entityManager.flush();

        Optional<Team> found = teamRepository.findByName("QA Team");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("QA Team");
    }

    @Test
    void shouldCheckIfTeamNameExists() {
        Team team = createTeam("Design Team", owner.getId());
        entityManager.persist(team);
        entityManager.flush();

        boolean exists = teamRepository.existsByName("Design Team");
        boolean notExists = teamRepository.existsByName("Nonexistent Team");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldFindTeamsByOwnerId() {
        Team team1 = createTeam("Team 1", owner.getId());
        Team team2 = createTeam("Team 2", owner.getId());
        entityManager.persist(team1);
        entityManager.persist(team2);
        entityManager.flush();

        List<Team> teams = teamRepository.findByOwnerId(owner.getId());

        assertThat(teams).hasSize(2);
        assertThat(teams).extracting(Team::getName).containsExactlyInAnyOrder("Team 1", "Team 2");
    }

    @Test
    void shouldCountTeamsByOwnerId() {
        Team team1 = createTeam("Team A", owner.getId());
        Team team2 = createTeam("Team B", owner.getId());
        entityManager.persist(team1);
        entityManager.persist(team2);
        entityManager.flush();

        long count = teamRepository.countByOwnerId(owner.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindTeamsByUserId() {
        Team team = createTeam("Project Team", owner.getId());
        entityManager.persist(team);

        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(team.getId());
        teamMember.setUserId(member.getId());
        teamMember.setRole(TeamRole.MEMBER);
        teamMember.setJoinedAt(LocalDateTime.now());
        entityManager.persist(teamMember);
        entityManager.flush();

        List<Team> teams = teamRepository.findTeamsByUserId(member.getId());

        assertThat(teams).hasSize(1);
        assertThat(teams.get(0).getName()).isEqualTo("Project Team");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTeams() {
        List<Team> teams = teamRepository.findTeamsByUserId(member.getId());

        assertThat(teams).isEmpty();
    }

    @Test
    void shouldDeleteTeam() {
        Team team = createTeam("Temporary Team", owner.getId());
        entityManager.persist(team);
        entityManager.flush();

        teamRepository.deleteById(team.getId());

        Optional<Team> found = teamRepository.findById(team.getId());
        assertThat(found).isEmpty();
    }

    private Team createTeam(String name, UUID ownerId) {
        Team team = new Team();
        team.setName(name);
        team.setDescription("Test team");
        team.setOwnerId(ownerId);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        return team;
    }
}
