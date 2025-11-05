package com.farma_ya.config;

import com.farma_ya.model.User;
import com.farma_ya.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inicializador de datos para crear el usuario administrador por defecto
 * Solo se ejecuta en perfiles que NO sean "test"
 */
@Configuration
@Profile("!test & !acceptance")
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificar si ya existe el usuario admin
            if (!userRepository.existsByUsername("Admin123")) {
                User admin = new User();
                admin.setUsername("Admin123");
                admin.setEmail("admin@farmaya.com");
                admin.setPassword(passwordEncoder.encode("Admin123"));
                admin.setRolId(1);
                admin.setTelefono("000000000");

                userRepository.save(admin);
                logger.info(" Usuario administrador creado exitosamente:");
                logger.info("   Username: Admin123");
                logger.info("   Password: Admin123");
                logger.info("   Email: admin@farmaya.com");
                logger.info("   Rol: ADMIN (rol_id: 1)");
            } else {
                logger.info("ℹ El usuario administrador 'Admin123' ya existe en la base de datos");
            }
        };
    }
}
