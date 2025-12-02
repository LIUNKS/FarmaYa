package com.farma_ya.unit.service;

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
    private User deliveryUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "testuser", "test@example.com", "password", Role.USER);
        testUser.setRolId(2); // Explicitly set rolId for USER

        adminUser = new User(2, "admin", "admin@example.com", "adminpass", Role.ADMIN);
        adminUser.setRolId(1); // Explicitly set rolId for ADMIN

        deliveryUser = new User(3, "delivery", "delivery@example.com", "delpass", Role.DELIVERY);
        deliveryUser.setRolId(3); // Explicitly set rolId for DELIVERY
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void getUserByUsername_ExistingUser_ReturnsUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_NonExistingUser_ThrowsException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void getUsersByRole_AdminRole_ReturnsAdminUsers() {
        // Given
        List<User> adminUsers = Arrays.asList(adminUser);
        when(userRepository.findByRole(1)).thenReturn(adminUsers); // 1 = ADMIN

        // When
        List<User> result = userService.getUsersByRole(Role.ADMIN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).findByRole(1);
    }

    @Test
    void getUsersByRole_DeliveryRole_ReturnsDeliveryUsers() {
        // Given
        List<User> deliveryUsers = Arrays.asList(deliveryUser);
        when(userRepository.findByRole(35)).thenReturn(deliveryUsers); // 35 = DELIVERY

        // When
        List<User> result = userService.getUsersByRole(Role.DELIVERY);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.DELIVERY);
        verify(userRepository).findByRole(35);
    }

    @Test
    void countUsersByRole_ReturnsCorrectCount() {
        // Given
        when(userRepository.countByRole(2)).thenReturn(10L); // 2 = USER

        // When
        long count = userService.countUsersByRole(Role.USER);

        // Then
        assertThat(count).isEqualTo(10);
        verify(userRepository).countByRole(2);
    }

    @Test
    void registerUser_ValidData_CreatesSuccessfully() {
        // Given
        com.farma_ya.dto.UserRegistrationDTO registrationDTO = new com.farma_ya.dto.UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password");
        registrationDTO.setTelefono("912345678");

        User savedUser = new User();
        savedUser.setId(4);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.USER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.registerUser(registrationDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ExistingUsername_ThrowsException() {
        // Given
        com.farma_ya.dto.UserRegistrationDTO registrationDTO = new com.farma_ya.dto.UserRegistrationDTO();
        registrationDTO.setUsername("existinguser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registrationDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El usuario ya existe");
    }

    @Test
    void registerUser_ExistingEmail_ThrowsException() {
        // Given
        com.farma_ya.dto.UserRegistrationDTO registrationDTO = new com.farma_ya.dto.UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("existing@example.com");
        registrationDTO.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(registrationDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email ya está registrado");
    }

    @Test
    void updateUserRole_ValidUpdate_UpdatesSuccessfully() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUserRole(1, Role.ADMIN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(testUser);
    }

    @Test
    void encodePassword_EncodesPassword() {
        // Given
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        // When
        String result = userService.encodePassword("rawPassword");

        // Then
        assertThat(result).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("rawPassword");
    }

    @Test
    void passwordMatches_ValidPassword_ReturnsTrue() {
        // Given
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // When
        boolean result = userService.passwordMatches("rawPassword", "encodedPassword");

        // Then
        assertThat(result).isTrue();
        verify(passwordEncoder).matches("rawPassword", "encodedPassword");
    }

    @Test
    void passwordMatches_InvalidPassword_ReturnsFalse() {
        // Given
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When
        boolean result = userService.passwordMatches("wrongPassword", "encodedPassword");

        // Then
        assertThat(result).isFalse();
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    void getAllUsers_ReturnsAllUsers() {
        // Given
        List<User> allUsers = Arrays.asList(testUser, adminUser, deliveryUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        verify(userRepository).findAll();
    }
}