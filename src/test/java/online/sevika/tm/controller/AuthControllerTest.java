package online.sevika.tm.controller;

import online.sevika.tm.dto.LoginRequestDTO;
import online.sevika.tm.dto.UserResponseDTO;
import online.sevika.tm.dto.RegisterRequestDTO;
import online.sevika.tm.dto.AuthResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.sevika.tm.entity.User;
import online.sevika.tm.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AuthController.
 * 
 * Demonstrates:
 * - Controller testing with MockMvc
 * - Testing REST endpoints
 * - Security testing
 * - JSON serialization testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_Success() throws Exception {
        // Arrange
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Test@123")
                .firstName("Test")
                .lastName("User")
                .build();

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .token("jwt-token")
                .type("Bearer")
                .user(userResponse)
                .build();

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void register_InvalidRequest_BadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("a") // Too short
                .email("invalid-email")
                .password("123") // Too short
                .firstName("")
                .lastName("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("testuser")
                .password("Test@123")
                .build();

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .token("jwt-token")
                .type("Bearer")
                .user(userResponse)
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }
}
