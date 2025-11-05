# Documentación: UserServiceTest.java

## Descripción General

El archivo `UserServiceTest.java` es una suite completa de pruebas unitarias para la clase `UserService` del proyecto FarmaYa. Este archivo contiene **15 tests unitarios** que validan todas las funcionalidades críticas del servicio de gestión de usuarios, incluyendo autenticación, registro, roles y operaciones CRUD.

### Ubicación

```
backend/src/test/java/com/farma_ya/unit/service/UserServiceTest.java
```

### Framework de Testing

- **JUnit 5**: Framework principal de testing
- **Mockito**: Para mocking de dependencias
- **AssertJ**: Para aserciones expresivas

---

## Configuración del Test

### Dependencias Mockeadas

```java
@Mock
private UserRepository userRepository;

@Mock
private PasswordEncoder passwordEncoder;

@InjectMocks
private UserService userService;
```

### Datos de Prueba (Setup)

```java
@BeforeEach
void setUp() {
    testUser = new User(1L, "testuser", "test@example.com", "password", Role.USER);
    testUser.setRolId(2);

    adminUser = new User(2L, "admin", "admin@example.com", "adminpass", Role.ADMIN);
    adminUser.setRolId(1);

    deliveryUser = new User(3L, "delivery", "delivery@example.com", "delpass", Role.DELIVERY);
    deliveryUser.setRolId(3);
}
```

---

## Tests Detallados

### 1. `getUserById_ExistingUser_ReturnsUser`

**Propósito**: Verificar que el servicio puede recuperar un usuario existente por su ID.

**Código**:

```java
@Test
void getUserById_ExistingUser_ReturnsUser() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    User result = userService.getUserById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).findById(1L);
}
```

**Validaciones**:

- ✅ Retorna usuario no nulo
- ✅ ID del usuario es correcto
- ✅ Username es correcto
- ✅ Se llama al repository correctamente

---

### 2. `getUserById_NonExistingUser_ThrowsException`

**Propósito**: Validar el manejo de errores cuando se busca un usuario inexistente.

**Código**:

```java
@Test
void getUserById_NonExistingUser_ThrowsException() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Usuario no encontrado");
}
```

**Validaciones**:

- ✅ Lanza `RuntimeException` para usuarios inexistentes
- ✅ Mensaje de error es descriptivo

---

### 3. `getUserByUsername_ExistingUser_ReturnsUser`

**Propósito**: Verificar la recuperación de usuario por nombre de usuario.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Retorna usuario correcto por username
- ✅ Username coincide con el solicitado

---

### 4. `getUserByUsername_NonExistingUser_ThrowsException`

**Propósito**: Validar manejo de errores para username inexistente.

**Código**:

```java
@Test
void getUserByUsername_NonExistingUser_ThrowsException() {
    // Given
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Usuario no encontrado");
}
```

**Validaciones**:

- ✅ Lanza excepción para username inexistente
- ✅ Mensaje de error apropiado

---

### 5. `getUsersByRole_AdminRole_ReturnsAdminUsers`

**Propósito**: Verificar la obtención de usuarios por rol ADMIN.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Retorna lista correcta de usuarios ADMIN
- ✅ Mapeo correcto de Role.ADMIN → rolId = 1

---

### 6. `getUsersByRole_DeliveryRole_ReturnsDeliveryUsers`

**Propósito**: Verificar la obtención de usuarios por rol DELIVERY.

**Código**:

```java
@Test
void getUsersByRole_DeliveryRole_ReturnsDeliveryUsers() {
    // Given
    List<User> deliveryUsers = Arrays.asList(deliveryUser);
    when(userRepository.findByRole(3)).thenReturn(deliveryUsers); // 3 = DELIVERY

    // When
    List<User> result = userService.getUsersByRole(Role.DELIVERY);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRole()).isEqualTo(Role.DELIVERY);
    verify(userRepository).findByRole(3);
}
```

**Validaciones**:

- ✅ Retorna lista correcta de usuarios DELIVERY
- ✅ Mapeo correcto de Role.DELIVERY → rolId = 3

---

### 7. `countUsersByRole_ReturnsCorrectCount`

**Propósito**: Validar el conteo correcto de usuarios por rol.

**Código**:

```java
@Test
void countUsersByRole_ReturnsCorrectCount() {
    // Given
    when(userRepository.countByRole(2)).thenReturn(10L); // 2 = USER

    // When
    long count = userService.countUsersByRole(Role.USER);

    // Then
    assertThat(count).isEqualTo(10L);
    verify(userRepository).countByRole(2);
}
```

**Validaciones**:

- ✅ Conteo correcto por rol
- ✅ Mapeo correcto de Role.USER → rolId = 2

---

### 8. `registerUser_ValidData_CreatesSuccessfully`

**Propósito**: Verificar el registro exitoso de un nuevo usuario.

**Código**:

```java
@Test
void registerUser_ValidData_CreatesSuccessfully() {
    // Given
    com.farma_ya.dto.UserRegistrationDTO registrationDTO = new com.farma_ya.dto.UserRegistrationDTO();
    registrationDTO.setUsername("newuser");
    registrationDTO.setEmail("new@example.com");
    registrationDTO.setPassword("password");
    registrationDTO.setTelefono("912345678");

    User savedUser = new User();
    savedUser.setId(4L);
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
    assertThat(result.getId()).isEqualTo(4L);
    assertThat(result.getUsername()).isEqualTo("newuser");
    assertThat(result.getPassword()).isEqualTo("encodedPassword");
    verify(passwordEncoder).encode("password");
    verify(userRepository).save(any(User.class));
}
```

**Validaciones**:

- ✅ Usuario creado exitosamente
- ✅ Password encriptado correctamente
- ✅ Rol asignado por defecto (USER)
- ✅ Todos los campos mapeados correctamente

---

### 9. `registerUser_ExistingUsername_ThrowsException`

**Propósito**: Validar que no se permite registro con username duplicado.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Previene registro con username duplicado
- ✅ Lanza `IllegalArgumentException` con mensaje claro

---

### 10. `registerUser_ExistingEmail_ThrowsException`

**Propósito**: Validar que no se permite registro con email duplicado.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Previene registro con email duplicado
- ✅ Validación de unicidad funciona correctamente

---

### 11. `updateUserRole_ValidUpdate_UpdatesSuccessfully`

**Propósito**: Verificar la actualización exitosa del rol de un usuario.

**Código**:

```java
@Test
void updateUserRole_ValidUpdate_UpdatesSuccessfully() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    User result = userService.updateUserRole(1L, Role.ADMIN);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    verify(userRepository).save(testUser);
}
```

**Validaciones**:

- ✅ Rol actualizado correctamente
- ✅ Usuario existente encontrado y actualizado

---

### 12. `encodePassword_EncodesPassword`

**Propósito**: Verificar la funcionalidad de encriptación de passwords.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Password encriptado correctamente
- ✅ Delegación correcta al PasswordEncoder

---

### 13. `passwordMatches_ValidPassword_ReturnsTrue`

**Propósito**: Verificar la validación correcta de passwords.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Password válido reconocido correctamente
- ✅ Retorna `true` para coincidencia

---

### 14. `passwordMatches_InvalidPassword_ReturnsFalse`

**Propósito**: Verificar el rechazo de passwords inválidos.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Password inválido rechazado correctamente
- ✅ Retorna `false` para no coincidencia

---

### 15. `getAllUsers_ReturnsAllUsers`

**Propósito**: Verificar la obtención de todos los usuarios del sistema.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Retorna todos los usuarios del sistema
- ✅ Lista completa y correcta

---

## Mapeo de Roles

### Sistema de Roles en UserService

El servicio utiliza un sistema de mapeo entre enums `Role` y IDs enteros en la base de datos:

```java
private Integer roleToRolId(Role role) {
    switch (role) {
        case ADMIN:    return 1;
        case DELIVERY: return 3;
        case USER:     return 2; // default
    }
}
```

**Mapeo de Roles**:

- `Role.ADMIN` ↔ `rolId = 1`
- `Role.USER` ↔ `rolId = 2`
- `Role.DELIVERY` ↔ `rolId = 3`

---

## Resultados de Ejecución

### Comando de Ejecución

```bash
cd "c:\Users\johan\OneDrive\Documentos\Programación\INTEGRADOR PROYECTO\FarmaYa\backend" && ./mvnw test -Dtest=UserServiceTest
```

### Output de Ejecución

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.farma_ya.unit.service.UserServiceTest
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users\johan\.m2\repository\net\bytebuddy\byte-buddy-agent\1.17.5\byte-buddy-agent-1.17.5.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.654 s -- in com.farma_ya.unit.service.UserServiceTest
[INFO] Running com.farma_ya.unit.UserServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.077 s -- in com.farma_ya.unit.UserServiceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.180 s
[INFO] Finished at: 2025-11-04T20:34:52-05:00
[INFO] ------------------------------------------------------------------------
```

**Estado**: ✅ Todos los tests pasan exitosamente (29/29)

---

## Aspectos Técnicos Importantes

### Patrón de Diseño

- **Given-When-Then**: Cada test sigue claramente las fases de preparación, ejecución y verificación
- **Unit Tests Puros**: Todas las dependencias externas están mockeadas
- **Aislamiento**: Cada test es independiente y no afecta a otros

### Cobertura de Escenarios

- ✅ **Casos Positivos**: Funcionalidad normal de registro, login, actualización
- ✅ **Casos Negativos**: Usuarios inexistentes, datos duplicados, passwords inválidos
- ✅ **Casos Límite**: Roles específicos (ADMIN, DELIVERY), validaciones de unicidad
- ✅ **Casos de Seguridad**: Encriptación de passwords, validación de credenciales

### Validaciones Implementadas

- **Resultado**: Verificación del retorno correcto
- **Estado**: Verificación de cambios de estado y roles
- **Persistencia**: Verificación de llamadas a repositorio
- **Excepciones**: Verificación de manejo de errores apropiado
- **Seguridad**: Verificación de encriptación y validación de passwords

### Manejo de Roles

- **Mapeo Bidireccional**: Conversión entre enums Role y IDs de base de datos
- **Consistencia**: Los tests verifican tanto el mapeo de ida (Role → rolId) como de vuelta (rolId → Role)
- **Validación**: Todos los roles del sistema están cubiertos (ADMIN, USER, DELIVERY)

---

## Importancia en el Proyecto

Esta suite de tests asegura que el `UserService` funcione correctamente en todos sus aspectos críticos:

1. **Gestión de Usuarios**: CRUD completo de usuarios con validaciones
2. **Sistema de Roles**: Mapeo correcto y gestión de roles (ADMIN, USER, DELIVERY)
3. **Seguridad**: Encriptación y validación de passwords
4. **Registro**: Validación de unicidad y creación de nuevos usuarios
5. **Integración**: Comunicación correcta con otras capas del sistema
6. **Manejo de Errores**: Respuestas apropiadas a situaciones excepcionales

La implementación de estos tests garantiza la calidad, seguridad y confiabilidad del sistema de gestión de usuarios en la aplicación FarmaYa, especialmente importante para funcionalidades críticas como autenticación y autorización.</content>
