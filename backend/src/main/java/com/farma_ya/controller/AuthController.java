package com.farma_ya.controller;

import com.farma_ya.dto.JwtResponseDTO;
import com.farma_ya.dto.LoginRequestDTO;
import com.farma_ya.dto.UserRegistrationDTO;
import com.farma_ya.model.User;
import com.farma_ya.security.JwtTokenProvider;
import com.farma_ya.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de usuarios")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

        @Autowired
        private UserService userService;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private JwtTokenProvider tokenProvider;

        @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario en el sistema")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", content = @Content(schema = @Schema(implementation = User.class))),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/register")
        public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
                User registeredUser = userService.registerUser(registrationDTO);
                return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        }

        @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve tokens JWT")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
        })
        @PostMapping("/login")
        public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
                // Buscar usuario por username o email
                User user = null;
                try {
                        user = userService.getUserByUsername(loginRequest.getUsername());
                } catch (Exception e) {
                        // Si no se encuentra por username, intentar por email
                        try {
                                user = userService.getUserByEmail(loginRequest.getUsername());
                        } catch (Exception ex) {
                                // Usuario no encontrado
                                throw new IllegalArgumentException("Usuario o email no encontrado");
                        }
                }

                // Autenticar con el username real del usuario encontrado
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(user.getUsername(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String accessToken = tokenProvider.generateToken(authentication);
                String refreshToken = tokenProvider.generateRefreshToken(authentication);

                JwtResponseDTO response = new JwtResponseDTO(
                                accessToken,
                                refreshToken,
                                user.getUsername(),
                                user.getRole().name(),
                                tokenProvider.getExpirationTime());

                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Obtener usuario actual", description = "Obtiene la información del usuario autenticado")
        @SecurityRequirement(name = "Bearer Authentication")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente", content = @Content(schema = @Schema(implementation = User.class))),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        @GetMapping("/me")
        public ResponseEntity<User> getCurrentUser() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = userService.getUserByUsername(username);
                return ResponseEntity.ok(user);
        }

        @GetMapping("/test")
        public ResponseEntity<String> test() {
                return ResponseEntity.ok("Backend funcionando correctamente");
        }
}