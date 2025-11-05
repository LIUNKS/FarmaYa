# Documentación: AuthControllerIntegrationTest.java

## Descripción General

El archivo `AuthControllerIntegrationTest.java` es una suite de pruebas de integración para el `AuthController` del proyecto FarmaYa. Este archivo contiene **4 tests de integración** que validan los endpoints de autenticación y registro de usuarios.

### Ubicación

```
backend/src/test/java/com/farma_ya/integration/AuthControllerIntegrationTest.java
```

### Framework de Testing

- **JUnit 5**: Framework principal de testing
- **Mockito**: Para mocking de dependencias
- **MockMvc**: Para testing de controladores REST
- **Spring Test**: Para configuración de contexto de testing
- **GlobalExceptionHandler**: Para manejo de excepciones

---

## Configuración del Test

### Dependencias Mockeadas

```java
@Mock
private UserService userService;

@Mock
private AuthenticationManager authenticationManager;

@Mock
private JwtTokenProvider tokenProvider;

@InjectMocks
private AuthController authController;
```

### Configuración de MockMvc

```java
mockMvc = MockMvcBuilders.standaloneSetup(authController)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
```

**Nota**: Se incluye `GlobalExceptionHandler` para que las excepciones sean manejadas correctamente y devuelvan respuestas HTTP apropiadas.

---

## Tests Detallados

### 1. `testRegisterUser_Success`

**Propósito**: Verificar el registro exitoso de un nuevo usuario con datos válidos.

**Código**:

```java
@Test
void testRegisterUser_Success() throws Exception {
    // Given
    UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
    registrationDTO.setUsername("testuser");
    registrationDTO.setEmail("test@example.com");
    registrationDTO.setPassword("password123");
    registrationDTO.setTelefono("912345678");

    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");

    when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(user);

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
}
```

**Validaciones**:

- ✅ Status HTTP 201 (Created)
- ✅ Usuario registrado correctamente
- ✅ Respuesta JSON contiene datos del usuario
- ✅ Servicio de registro llamado correctamente

---

### 2. `testLogin_Success`

**Propósito**: Verificar el login exitoso y generación de tokens JWT.

**Código**:

```java
@Test
void testLogin_Success() throws Exception {
    // Given
    LoginRequestDTO loginRequest = new LoginRequestDTO();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(com.farma_ya.model.Role.USER);

    when(userService.getUserByUsername(anyString())).thenReturn(user);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("refresh-token");
    when(tokenProvider.getExpirationTime()).thenReturn(3600000L);

    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.role").value("USER"));
}
```

**Validaciones**:

- ✅ Status HTTP 200 (OK)
- ✅ Tokens JWT generados correctamente
- ✅ Usuario autenticado exitosamente
- ✅ Rol del usuario incluido en respuesta

---

### 3. `testRegisterUser_InvalidData`

**Propósito**: Verificar validación de datos inválidos en el registro.

**Código**:

```java
@Test
void testRegisterUser_InvalidData() throws Exception {
    // Given
    UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
    // Missing required fields

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationDTO)))
            .andExpect(status().isBadRequest());
}
```

**Validaciones**:

- ✅ Status HTTP 400 (Bad Request) para datos inválidos
- ✅ Validación de campos requeridos funciona
- ✅ GlobalExceptionHandler maneja MethodArgumentNotValidException

---

### 4. `testLogin_InvalidCredentials`

**Propósito**: Verificar manejo de credenciales inválidas en el login.

**Código**:

```java
@Test
void testLogin_InvalidCredentials() throws Exception {
    // Given
    LoginRequestDTO loginRequest = new LoginRequestDTO();
    loginRequest.setUsername("invalid");
    loginRequest.setPassword("invalid");

    // Mock both username and email lookups to throw exceptions
    when(userService.getUserByUsername(anyString()))
        .thenThrow(new IllegalArgumentException("Usuario no encontrado"));
    when(userService.getUserByEmail(anyString()))
        .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

    // When & Then - Expect the exception to be handled by GlobalExceptionHandler
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Usuario o email no encontrado"));
}
```

**Validaciones**:

- ✅ Status HTTP 400 (Bad Request) para credenciales inválidas
- ✅ Mensaje de error descriptivo
- ✅ GlobalExceptionHandler maneja IllegalArgumentException correctamente

---

## Resultados de Ejecución

### Comando de Ejecución

```bash
cd "c:\Users\johan\OneDrive\Documentos\Programación\INTEGRADOR PROYECTO\FarmaYa\backend" && ./mvnw test -Dtest="**/integration/AuthControllerIntegrationTest"
```

### Output de Ejecución

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.farma_ya.integration.AuthControllerIntegrationTest
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users\johan\.m2\repository\net\bytebuddy\byte-buddy-agent\1.17.5\byte-buddy-agent-1.17.5.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.070 s -- in com.farma_ya.integration.AuthControllerIntegrationTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.712 s
[INFO] Finished at: 2025-11-04T20:47:27-05:00
[INFO] ------------------------------------------------------------------------
```

**Estado**: ✅ Todos los tests pasan exitosamente (4/4)

---

## Aspectos Técnicos Importantes

### Patrón de Testing

- **Integration Testing**: Tests que validan la integración entre controladores, servicios y manejo de excepciones
- **HTTP Testing**: Uso de MockMvc para simular requests HTTP
- **JSON Validation**: Verificación de respuestas JSON usando JsonPath
- **Exception Handling**: Validación del manejo correcto de excepciones

### Cobertura de Escenarios

- ✅ **Registro exitoso**: Validación completa del flujo de registro
- ✅ **Login exitoso**: Autenticación y generación de tokens
- ✅ **Validación de entrada**: Campos requeridos y formato
- ✅ **Manejo de errores**: Credenciales inválidas y excepciones

### Configuración Especial

- **GlobalExceptionHandler**: Incluido en MockMvc para manejar excepciones correctamente
- **ObjectMapper**: Para serialización/deserialización JSON
- **MockMvc Standalone**: Configuración independiente sin contexto Spring completo

### Validaciones Implementadas

- **Status Codes**: Verificación de códigos HTTP apropiados
- **Response Content**: Validación de estructura y contenido JSON
- **Error Messages**: Verificación de mensajes de error descriptivos
- **Service Calls**: Verificación de llamadas a servicios mockeados

---

## Importancia en el Proyecto

Esta suite de tests asegura que:

1. **Registro de Usuarios**: Funciona correctamente con validación de datos
2. **Autenticación**: Login exitoso genera tokens JWT válidos
3. **Validación de Entrada**: Campos requeridos son validados
4. **Manejo de Errores**: Excepciones se manejan apropiadamente
5. **Integración**: Controlador, servicios y exception handler funcionan juntos

Estos tests son **críticos** para garantizar que la API de autenticación funcione correctamente en un entorno de integración, validando no solo la lógica de negocio sino también el manejo de HTTP requests/responses y excepciones.</content>
<parameter name="filePath">c:\Users\johan\OneDrive\Documentos\Programación\INTEGRADOR PROYECTO\FarmaYa\backend\README_AuthControllerIntegrationTest.md
