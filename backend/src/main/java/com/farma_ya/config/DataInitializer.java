package com.farma_ya.config;

import com.farma_ya.model.User;
import com.farma_ya.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
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
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        return args -> {
            // Crear roles si no existen
            createRoleIfNotExists(jdbcTemplate, "ADMIN");
            createRoleIfNotExists(jdbcTemplate, "CLIENTE");
            createRoleIfNotExists(jdbcTemplate, "DELIVERY");

            // Obtener los IDs de los roles
            Integer adminRoleId = getRoleId(jdbcTemplate, "ADMIN");
            Integer deliveryRoleId = getRoleId(jdbcTemplate, "DELIVERY");

            // Verificar si ya existe el usuario admin
            if (!userRepository.existsByUsername("Admin123")) {
                User admin = new User();
                admin.setUsername("Admin123");
                admin.setEmail("admin@farmaya.com");
                admin.setPassword(passwordEncoder.encode("Admin123"));
                admin.setRolId(adminRoleId);
                admin.setTelefono("000000000");

                userRepository.save(admin);
                logger.info(" Usuario administrador creado exitosamente:");
                logger.info("   Username: Admin123");
                logger.info("   Password: Admin123");
                logger.info("   Email: admin@farmaya.com");
                logger.info("   Rol: ADMIN (rol_id: {})", adminRoleId);
            } else {
                logger.info("ℹ El usuario administrador 'Admin123' ya existe en la base de datos");
            }

            // Crear usuarios repartidores de ejemplo
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "PedroRepartidor",
                    "pedro@farmaya.com", "987654321");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "JuanDelivery",
                    "juan@farmaya.com", "987654322");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "MariaTransport",
                    "maria@farmaya.com", "987654323");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "CarlosExpress",
                    "carlos@farmaya.com", "987654324");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "AnaRapida",
                    "ana@farmaya.com", "987654325");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "LuisMoto",
                    "luis@farmaya.com", "987654326");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "SofiaDelivery",
                    "sofia@farmaya.com", "987654327");
            createDeliveryUserIfNotExists(userRepository, passwordEncoder, deliveryRoleId, "MiguelBike",
                    "miguel@farmaya.com", "987654328");
        };
    }

    private void createDeliveryUserIfNotExists(UserRepository userRepository, PasswordEncoder passwordEncoder,
            Integer rolId, String username, String email, String telefono) {
        if (!userRepository.existsByUsername(username)) {
            User deliveryUser = new User();
            deliveryUser.setUsername(username);
            deliveryUser.setEmail(email);
            deliveryUser.setPassword(passwordEncoder.encode("delivery123"));
            deliveryUser.setRolId(rolId);
            deliveryUser.setTelefono(telefono);

            userRepository.save(deliveryUser);
            logger.info(" Usuario repartidor creado exitosamente:");
            logger.info("   Username: {}", username);
            logger.info("   Password: delivery123");
            logger.info("   Email: {}", email);
            logger.info("   Rol: DELIVERY (rol_id: {})", rolId);
        }
    }

    private void createRoleIfNotExists(JdbcTemplate jdbcTemplate, String roleName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rol WHERE nombre = ?",
                Integer.class,
                roleName);

        if (count == null || count == 0) {
            // Para DELIVERY, asegurar que tenga ID 35
            if ("DELIVERY".equals(roleName)) {
                jdbcTemplate.update("INSERT INTO rol (rol_id, nombre) VALUES (35, ?)", roleName);
            } else {
                jdbcTemplate.update("INSERT INTO rol (nombre) VALUES (?)", roleName);
            }
            logger.info(" Rol '{}' creado exitosamente", roleName);
        } else {
            logger.info("ℹ El rol '{}' ya existe en la base de datos", roleName);
        }
    }

    private Integer getRoleId(JdbcTemplate jdbcTemplate, String roleName) {
        return jdbcTemplate.queryForObject(
                "SELECT rol_id FROM rol WHERE nombre = ?",
                Integer.class,
                roleName);
    }
}
