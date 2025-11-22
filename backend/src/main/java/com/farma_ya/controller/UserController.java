package com.farma_ya.controller;

import com.farma_ya.model.User;
import com.farma_ya.model.Role;
import com.farma_ya.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Retorna lista de todos los usuarios (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Obtener usuarios por rol", description = "Retorna lista de usuarios filtrados por rol (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Retorna detalles de un usuario específico (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Actualizar rol de usuario", description = "Actualiza el rol de un usuario (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Integer id,
            @RequestParam Role role) {
        User updatedUser = userService.updateUserRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Contar usuarios por rol", description = "Retorna el conteo de usuarios por rol (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count/role/{role}")
    public ResponseEntity<Map<String, Long>> countUsersByRole(@PathVariable Role role) {
        long count = userService.countUsersByRole(role);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Obtener perfil del usuario actual", description = "Retorna la información del usuario autenticado")
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(currentUser);
    }

    @Operation(summary = "Actualizar contraseña", description = "Cambia la contraseña del usuario autenticado")
    @PutMapping("/profile/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> passwordData) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        // Verificar contraseña actual
        if (!userService.passwordMatches(currentPassword, currentUser.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Contraseña actual incorrecta"));
        }

        // Validar nueva contraseña
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres"));
        }

        // Actualizar contraseña
        currentUser.setPassword(userService.encodePassword(newPassword));
        userService.saveUser(currentUser);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

    @Operation(summary = "Actualizar información personal", description = "Actualiza nombre y teléfono del usuario autenticado")
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> profileData) {
        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        String newName = profileData.get("name");
        String newPhone = profileData.get("phone");

        if (newName != null && !newName.trim().isEmpty()) {
            currentUser.setUsername(newName.trim());
        }

        if (newPhone != null) {
            currentUser.setTelefono(newPhone.trim());
        }

        User updatedUser = userService.saveUser(currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Limpiar usuarios de prueba", description = "Elimina todos los usuarios de prueba del sistema (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cleanup-test-users")
    public ResponseEntity<Map<String, Object>> cleanupTestUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Lista de usuarios de prueba a eliminar
            String[] testUsers = {
                    "testuser",
                    "delivery"
            };

            int deletedCount = 0;
            List<String> deletedUsers = new ArrayList<>();
            List<String> notFoundUsers = new ArrayList<>();

            for (String username : testUsers) {
                if (userService.existsByUsername(username)) {
                    userService.deleteByUsername(username);
                    deletedUsers.add(username);
                    deletedCount++;
                } else {
                    notFoundUsers.add(username);
                }
            }

            response.put("status", "SUCCESS");
            response.put("message", "Limpieza de usuarios de prueba completada");
            response.put("deletedCount", deletedCount);
            response.put("deletedUsers", deletedUsers);
            response.put("notFoundUsers", notFoundUsers);

            // Mostrar usuarios restantes
            List<User> remainingUsers = userService.getAllUsers();
            response.put("remainingUsersCount", remainingUsers.size());
            response.put("remainingUsers", remainingUsers.stream()
                    .map(user -> Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "role", user.getRolId()))
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error durante la limpieza: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}