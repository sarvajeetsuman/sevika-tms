package online.sevika.tm.service;
import online.sevika.tm.dto.UserRequestDTO;
import online.sevika.tm.dto.UserResponseDTO;
import online.sevika.tm.dto.UserUpdateRequestDTO;

import online.sevika.tm.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User operations.
 * 
 * Demonstrates:
 * - Service layer abstraction
 * - Interface Segregation Principle
 * - Dependency Inversion Principle
 */
public interface UserService {

    /**
     * Create a new user
     */
    UserResponseDTO createUser(UserRequestDTO request);

    /**
     * Get user by ID
     */
    UserResponseDTO getUserById(UUID id);

    /**
     * Get all users
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Update user
     */
    UserResponseDTO updateUser(UUID id, UserUpdateRequestDTO updateRequest);

    /**
     * Delete user
     */
    void deleteUser(UUID id);

    /**
     * Get user by username
     */
    User getUserByUsername(String username);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
