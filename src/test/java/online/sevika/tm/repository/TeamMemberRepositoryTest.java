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
 * Tests for TeamMemberRepository
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TeamMemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Team team;
    private User user1;
    private User user2;

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

        user1 = User.builder()
                .username("user1")
                .email("user1@test.com")
                .password("password")
                .firstName("User")
                .lastName("One")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(user1);

        user2 = User.builder()
                .username("user2")
                .email("user2@test.com")
                .password("password")
                .firstName("User")
                .lastName("Two")
                .role(User.Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entityManager.persist(user2);

        team = new Team();
        team.setName("Test Team");
        team.setDescription("Test team");
        team.setOwnerId(owner.getId());
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(team);

        entityManager.flush();
    }

    @Test
    void shouldSaveTeamMember() {
        TeamMember member = createTeamMember(team.getId(), user1.getId(), TeamRole.MEMBER);

        TeamMember saved = teamMemberRepository.save(member);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTeamId()).isEqualTo(team.getId());
        assertThat(saved.getUserId()).isEqualTo(user1.getId());
        assertThat(saved.getRole()).isEqualTo(TeamRole.MEMBER);
    }

    @Test
    void shouldFindTeamMembersByTeamId() {
        TeamMember member1 = createTeamMember(team.getId(), user1.getId(), TeamRole.ADMIN);
        TeamMember member2 = createTeamMember(team.getId(), user2.getId(), TeamRole.MEMBER);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());

        assertThat(members).hasSize(2);
        assertThat(members).extracting(TeamMember::getUserId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void shouldFindTeamMembersByUserId() {
        Team team2 = new Team();
        team2.setName("Team 2");
        team2.setOwnerId(user1.getId());
        team2.setCreatedAt(LocalDateTime.now());
        team2.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(team2);

        TeamMember member1 = createTeamMember(team.getId(), user1.getId(), TeamRole.MEMBER);
        TeamMember member2 = createTeamMember(team2.getId(), user1.getId(), TeamRole.OWNER);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        List<TeamMember> memberships = teamMemberRepository.findByUserId(user1.getId());

        assertThat(memberships).hasSize(2);
    }

    @Test
    void shouldFindTeamMemberByTeamIdAndUserId() {
        TeamMember member = createTeamMember(team.getId(), user1.getId(), TeamRole.MEMBER);
        entityManager.persist(member);
        entityManager.flush();

        Optional<TeamMember> found = teamMemberRepository.findByTeamIdAndUserId(team.getId(), user1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(TeamRole.MEMBER);
    }

    @Test
    void shouldCheckIfUserIsMemberOfTeam() {
        TeamMember member = createTeamMember(team.getId(), user1.getId(), TeamRole.MEMBER);
        entityManager.persist(member);
        entityManager.flush();

        boolean exists = teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user1.getId());
        boolean notExists = teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user2.getId());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldFindMembersByRole() {
        TeamMember admin = createTeamMember(team.getId(), user1.getId(), TeamRole.ADMIN);
        TeamMember member = createTeamMember(team.getId(), user2.getId(), TeamRole.MEMBER);
        entityManager.persist(admin);
        entityManager.persist(member);
        entityManager.flush();

        List<TeamMember> admins = teamMemberRepository.findByTeamIdAndRole(team.getId(), TeamRole.ADMIN);
        List<TeamMember> members = teamMemberRepository.findByTeamIdAndRole(team.getId(), TeamRole.MEMBER);

        assertThat(admins).hasSize(1);
        assertThat(admins.get(0).getUserId()).isEqualTo(user1.getId());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo(user2.getId());
    }

    @Test
    void shouldCountTeamMembers() {
        TeamMember member1 = createTeamMember(team.getId(), user1.getId(), TeamRole.ADMIN);
        TeamMember member2 = createTeamMember(team.getId(), user2.getId(), TeamRole.MEMBER);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        long count = teamMemberRepository.countByTeamId(team.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldDeleteTeamMember() {
        TeamMember member = createTeamMember(team.getId(), user1.getId(), TeamRole.MEMBER);
        entityManager.persist(member);
        entityManager.flush();

        teamMemberRepository.delete(member);

        Optional<TeamMember> found = teamMemberRepository.findByTeamIdAndUserId(team.getId(), user1.getId());
        assertThat(found).isEmpty();
    }

    private TeamMember createTeamMember(UUID teamId, UUID userId, TeamRole role) {
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        return member;
    }
}
