package online.sevika.tm.controller;

import online.sevika.tm.dto.UserRequestDTO;
import online.sevika.tm.dto.UserResponseDTO;
import online.sevika.tm.dto.UserUpdateRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for UserController.
 * 
 * Tests HTTP layer, security, validation, and request/response mapping.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    @interface WithMockCustomUser {
        String username() default "testuser";
        String role() default "USER";
        String userId() default ""; // Empty string means generate random UUID
    }

    static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            
            User user = new User();
            // Use provided userId if not empty, otherwise generate random
            UUID userId = customUser.userId().isEmpty() ? UUID.randomUUID() : UUID.fromString(customUser.userId());
            user.setId(userId);
            user.setUsername(customUser.username());
            user.setEmail(customUser.username() + "@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole(User.Role.valueOf(customUser.role()));
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + customUser.role()))
            );
            context.setAuthentication(authentication);
            return context;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void createUser_AsAdmin_Success() throws Exception {
        // Arrange
        UserRequestDTO request = UserRequestDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .build();

        UserResponseDTO response = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .firstName("New")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void createUser_AsUser_Forbidden() throws Exception {
        // Arrange
        UserRequestDTO request = UserRequestDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .build();

        // Mock the service (even though it shouldn't be called due to security)
        UserResponseDTO response = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .firstName("New")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();
        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_Unauthenticated_Unauthorized() throws Exception {
        // Arrange
        UserRequestDTO request = UserRequestDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123!")
                .firstName("New")
                .lastName("User")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_InvalidRequest_BadRequest() throws Exception {
        // Arrange - invalid email and short password
        UserRequestDTO request = UserRequestDTO.builder()
                .username("a")
                .email("invalid-email")
                .password("123")
                .firstName("")
                .lastName("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getAllUsers_AsAdmin_Success() throws Exception {
        // Arrange
        List<UserResponseDTO> users = Arrays.asList(
                UserResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .username("user1")
                        .email("user1@example.com")
                        .firstName("User")
                        .lastName("One")
                        .role(User.Role.USER)
                        .enabled(true)
                        .build(),
                UserResponseDTO.builder()
                        .id(UUID.randomUUID())
                        .username("user2")
                        .email("user2@example.com")
                        .firstName("User")
                        .lastName("Two")
                        .role(User.Role.ADMIN)
                        .enabled(true)
                        .build()
        );

        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void getAllUsers_AsUser_Forbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getUserById_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponseDTO response = UserResponseDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        when(userService.getUserById(userId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockCustomUser
    void getCurrentUser_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserResponseDTO response = UserResponseDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        when(userService.getUserById(any(UUID.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockCustomUser(userId = "550e8400-e29b-41d4-a716-446655440000")
    void updateUser_OwnProfile_Success() throws Exception {
        // Arrange
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UserUpdateRequestDTO request = UserUpdateRequestDTO.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        UserResponseDTO response = UserResponseDTO.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Updated")
                .lastName("Name")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        when(userService.updateUser(eq(userId), any(UserUpdateRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void deleteUser_AsAdmin_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockCustomUser(role = "USER")
    void deleteUser_AsUser_Forbidden() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
