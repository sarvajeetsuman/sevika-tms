package online.sevika.tm.service;
import online.sevika.tm.dto.AuthResponseDTO;
import online.sevika.tm.dto.LoginRequestDTO;
import online.sevika.tm.dto.RegisterRequestDTO;

/**
 * Service interface for Authentication operations.
 */
public interface AuthService {

    /**
     * Register a new user
     */
    AuthResponseDTO register(RegisterRequestDTO request);

    /**
     * Authenticate user and generate JWT token
     */
    AuthResponseDTO login(LoginRequestDTO request);
}
