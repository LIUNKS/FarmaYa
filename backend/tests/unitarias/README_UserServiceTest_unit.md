# Documentación: UserServiceTest.java (Paquete: com.farma_ya.unit)

## Descripción General

El archivo `UserServiceTest.java` ubicado en el paquete `com.farma_ya.unit` es una suite de pruebas unitarias para la clase `UserService` del proyecto FarmaYa. Este archivo contiene **14 tests unitarios** que validan las funcionalidades principales del servicio de gestión de usuarios.

### Ubicación

```
backend/src/test/java/com/farma_ya/unit/UserServiceTest.java
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
    testUser = new User(1L, "testuser", "test@example.com", "encodedPassword", Role.USER);
    adminUser = new User(2L, "admin", "admin@example.com", "encodedPassword", Role.ADMIN);
}
```

---

## Tests Detallados

### 1. `registerUser_ValidData_ShouldRegisterSuccessfully`

**Propósito**: Verificar el registro exitoso de un nuevo usuario con datos válidos.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Usuario registrado exitosamente
- ✅ Password encriptado correctamente
- ✅ Usuario guardado en repositorio

---

### 2. `registerUser_UsernameExists_ShouldThrowException`

**Propósito**: Validar que no se permite registro con username duplicado.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Previene registro con username duplicado
- ✅ Lanza `IllegalArgumentException` con mensaje correcto

---

### 3. `registerUser_EmailExists_ShouldThrowException`

**Propósito**: Validar que no se permite registro con email duplicado.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Previene registro con email duplicado
- ✅ Validación de unicidad funciona correctamente

---

### 4. `getUserById_ExistingUser_ShouldReturnUser`

**Propósito**: Verificar la recuperación de usuario existente por ID.

**Código**:

```java
@Test
void getUserById_ExistingUser_ShouldReturnUser() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // When
    User result = userService.getUserById(1L);

    // Then
    assertThat(result).isEqualTo(testUser);
}
```

**Validaciones**:

- ✅ Retorna usuario correcto por ID
- ✅ Funcionalidad básica de búsqueda funciona

---

### 5. `getUserById_NonExistingUser_ShouldThrowException`

**Propósito**: Validar manejo de errores para ID inexistente.

**Código**:

```java
@Test
void getUserById_NonExistingUser_ShouldThrowException() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Usuario no encontrado con ID: 999");
}
```

**Validaciones**:

- ✅ Lanza excepción para ID inexistente
- ✅ Mensaje de error descriptivo

---

### 6. `getUserByUsername_ExistingUser_ShouldReturnUser`

**Propósito**: Verificar la recuperación de usuario por nombre de usuario.

**Código**:

```java
@Test
void getUserByUsername_ExistingUser_ShouldReturnUser() {
    // Given
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // When
    User result = userService.getUserByUsername("testuser");

    // Then
    assertThat(result).isEqualTo(testUser);
}
```

**Validaciones**:

- ✅ Retorna usuario correcto por username
- ✅ Búsqueda por username funciona

---

### 7. `getUserByEmail_ExistingUser_ShouldReturnUser`

**Propósito**: Verificar la recuperación de usuario por email.

**Código**:

```java
@Test
void getUserByEmail_ExistingUser_ShouldReturnUser() {
    // Given
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    // When
    User result = userService.getUserByEmail("test@example.com");

    // Then
    assertThat(result).isEqualTo(testUser);
}
```

**Validaciones**:

- ✅ Retorna usuario correcto por email
- ✅ Búsqueda por email funciona

---

### 8. `getAllUsers_ShouldReturnAllUsers`

**Propósito**: Verificar la obtención de todos los usuarios del sistema.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Retorna todos los usuarios
- ✅ Lista contiene los usuarios esperados

---

### 9. `getUsersByRole_ShouldReturnUsersByRole`

**Propósito**: Verificar el filtrado de usuarios por rol.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Filtra correctamente por rol USER
- ✅ Mapeo Role.USER → rolId = 2 funciona

---

### 10. `updateUserRole_ShouldUpdateRoleSuccessfully`

**Propósito**: Verificar la actualización exitosa del rol de un usuario.

**Código**:

```java
@Test
void updateUserRole_ShouldUpdateRoleSuccessfully() {
    // Given
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    User result = userService.updateUserRole(1L, Role.ADMIN);

    // Then
    assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    verify(userRepository).save(testUser);
}
```

**Validaciones**:

- ✅ Rol actualizado correctamente
- ✅ Usuario guardado después de actualización

---

### 11. `passwordMatches_ValidPassword_ShouldReturnTrue`

**Propósito**: Verificar la validación correcta de passwords.

**Código**:

```java
@Test
void passwordMatches_ValidPassword_ShouldReturnTrue() {
    // Given
    when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

    // When
    boolean result = userService.passwordMatches("rawPassword", "encodedPassword");

    // Then
    assertThat(result).isTrue();
}
```

**Validaciones**:

- ✅ Password válido reconocido
- ✅ Retorna `true` para coincidencia correcta

---

### 12. `encodePassword_ShouldEncodePassword`

**Propósito**: Verificar la funcionalidad de encriptación de passwords.

**Código**:

```java
@Test
void encodePassword_ShouldEncodePassword() {
    // Given
    when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

    // When
    String result = userService.encodePassword("rawPassword");

    // Then
    assertThat(result).isEqualTo("encodedPassword");
}
```

**Validaciones**:

- ✅ Password encriptado correctamente
- ✅ Delegación al PasswordEncoder funciona

---

### 13. `saveUser_ShouldSaveUser`

**Propósito**: Verificar la funcionalidad básica de guardar usuario.

**Código**:

```java
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
```

**Validaciones**:

- ✅ Usuario guardado correctamente
- ✅ Retorna el usuario guardado

---

### 14. `countUsersByRole_ShouldReturnCount`

**Propósito**: Verificar el conteo correcto de usuarios por rol.

**Código**:

```java
@Test
void countUsersByRole_ShouldReturnCount() {
    // Given
    when(userRepository.countByRole(1)).thenReturn(5L); // ADMIN role = 1

    // When
    long result = userService.countUsersByRole(Role.ADMIN);

    // Then
    assertThat(result).isEqualTo(5L);
}
```

**Validaciones**:

- ✅ Conteo correcto por rol ADMIN
- ✅ Mapeo Role.ADMIN → rolId = 1 funciona

---

## Método Auxiliar

### `createRegistrationDTO`

```java
private com.farma_ya.dto.UserRegistrationDTO createRegistrationDTO(String username, String email, String password) {
    com.farma_ya.dto.UserRegistrationDTO dto = new com.farma_ya.dto.UserRegistrationDTO();
    dto.setUsername(username);
    dto.setEmail(email);
    dto.setPassword(password);
    dto.setTelefono("999888777");
    return dto;
}
```

**Propósito**: Método helper para crear objetos DTO de registro de usuario con datos de prueba, facilitando la reutilización en múltiples tests.

---

## Resultados de Ejecución

### Comando de Ejecución

```bash
cd "c:\Users\johan\OneDrive\Documentos\Programación\INTEGRADOR PROYECTO\FarmaYa\backend" && ./mvnw test -Dtest="**/unit/UserServiceTest"
```

### Output de Ejecución

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.farma_ya.unit.UserServiceTest
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users\johan\.m2\repository\net\bytebuddy\byte-buddy-agent\1.17.5\byte-buddy-agent-1.17.5.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.631 s -- in com.farma_ya.unit.UserServiceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.033 s
[INFO] Finished at: 2025-11-04T20:40:24-05:00
[INFO] ------------------------------------------------------------------------
```

**Estado**: ✅ Todos los tests pasan exitosamente (14/14)

---

## Diferencias con UserServiceTest (service)

Este archivo `UserServiceTest.java` en el paquete `com.farma_ya.unit` es **menos completo** que su contraparte en `com.farma_ya.unit.service.UserServiceTest`:

### Diferencias Principales:

- **14 tests** vs **15 tests** (falta un test de `passwordMatches` con password inválido)
- **Setup más simple**: Solo 2 usuarios de prueba vs 3 usuarios
- **Falta método auxiliar**: No tiene método para crear órdenes de prueba
- **Cobertura menor**: No cubre todos los escenarios de error
- **Estilo de nomenclatura**: Usa `Should` en lugar de `Returns`/`Throws`

### Tests Faltantes:

- ❌ `passwordMatches_InvalidPassword_ReturnsFalse`
- ❌ Tests más específicos de roles (ADMIN, DELIVERY)
- ❌ Tests más detallados de validación

---

## Aspectos Técnicos Importantes

### Patrón de Diseño

- **Given-When-Then**: Cada test sigue claramente las fases de preparación, ejecución y verificación
- **Unit Tests Puros**: Todas las dependencias externas están mockeadas
- **Aislamiento**: Cada test es independiente y no afecta a otros

### Cobertura de Escenarios

- ✅ **Casos Positivos**: Funcionalidad normal de registro, login, actualización
- ✅ **Casos Negativos**: Usuarios inexistentes, datos duplicados
- ✅ **Casos Límite**: Validaciones de unicidad, roles específicos
- ✅ **Casos de Seguridad**: Encriptación y validación de passwords

### Validaciones Implementadas

- **Resultado**: Verificación del retorno correcto
- **Estado**: Verificación de cambios de estado y roles
- **Persistencia**: Verificación de llamadas a repositorio
- **Excepciones**: Verificación de manejo de errores apropiado
- **Seguridad**: Verificación de encriptación de passwords

### Sistema de Roles

- **Mapeo**: Role.USER → rolId = 2, Role.ADMIN → rolId = 1
- **Consistencia**: Tests verifican el mapeo correcto
- **Simplicidad**: Solo cubre roles básicos (USER, ADMIN)

---

## Importancia en el Proyecto

Esta suite de tests asegura que el `UserService` funcione correctamente en sus operaciones básicas:

1. **Registro de Usuarios**: Validación y creación con encriptación
2. **Gestión de Usuarios**: CRUD básico de usuarios
3. **Sistema de Roles**: Filtrado y actualización de roles
4. **Seguridad**: Encriptación y validación de passwords
5. **Integración**: Comunicación correcta con repositorios

Aunque es **menos completo** que su contraparte en `unit.service`, este archivo proporciona una cobertura básica sólida de las funcionalidades críticas del UserService, siendo útil para validaciones rápidas y como base para expansiones futuras.
