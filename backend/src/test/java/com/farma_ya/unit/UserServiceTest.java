package com.farma_ya.unit;

import com.farma_ya.model.Role;
import com.farma_ya.model.User;
import com.farma_ya.repository.UserRepository;
import com.farma_ya.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "testuser", "test@example.com", "encodedPassword", Role.USER);
        adminUser = new User(2, "admin", "admin@example.com", "encodedPassword", Role.ADMIN);
    }

    @Test
    void registerUser_ValidData_ShouldRegisterSuccessfully() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(createRegistrationDTO("newuser", "new@example.com", "password123"));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerUser_UsernameExists_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(
                () -> userService.registerUser(createRegistrationDTO("existinguser", "new@example.com", "password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario ya existe");
    }

    @Test
    void registerUser_EmailExists_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(
                () -> userService.registerUser(createRegistrationDTO("newuser", "existing@example.com", "password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El email ya está registrado");
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1);

        // Then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUserById_NonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado con ID: 999");
    }

    @Test
    void getUserByUsername_ExistingUser_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUserByEmail_ExistingUser_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(testUser, adminUser);
    }

    @Test
    void getUsersByRole_ShouldReturnUsersByRole() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByRole(2)).thenReturn(users); // USER role = 2

        // When
        List<User> result = userService.getUsersByRole(Role.USER);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUser);
    }

    @Test
    void updateUserRole_ShouldUpdateRoleSuccessfully() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUserRole(1, Role.ADMIN);

        // Then
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(testUser);
    }

    @Test
    void passwordMatches_ValidPassword_ShouldReturnTrue() {
        // Given
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // When
        boolean result = userService.passwordMatches("rawPassword", "encodedPassword");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void encodePassword_ShouldEncodePassword() {
        // Given
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        // When
        String result = userService.encodePassword("rawPassword");

        // Then
        assertThat(result).isEqualTo("encodedPassword");
    }

    @Test
    void saveUser_ShouldSaveUser() {
        // Given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.saveUser(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void countUsersByRole_ShouldReturnCount() {
        // Given
        when(userRepository.countByRole(1)).thenReturn(5L); // ADMIN role = 1

        // When
        long result = userService.countUsersByRole(Role.ADMIN);

        // Then
        assertThat(result).isEqualTo(5);
    }

    private com.farma_ya.dto.UserRegistrationDTO createRegistrationDTO(String username, String email, String password) {
        com.farma_ya.dto.UserRegistrationDTO dto = new com.farma_ya.dto.UserRegistrationDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setTelefono("999888777");
        return dto;
    }
}