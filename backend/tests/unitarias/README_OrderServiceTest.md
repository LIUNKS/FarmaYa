# Documentación: OrderServiceTest.java

## Descripción General

El archivo `OrderServiceTest.java` es una suite completa de pruebas unitarias para la clase `OrderService` del proyecto FarmaYa. Este archivo contiene **10 tests unitarios** que validan todas las funcionalidades críticas del servicio de gestión de órdenes.

### Ubicación

```
backend/src/test/java/com/farma_ya/unit/service/OrderServiceTest.java
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
private OrderRepository orderRepository;

@Mock
private ICartService cartService;

@Mock
private InventoryService inventoryService;

@Mock
private DireccionRepository direccionRepository;

@InjectMocks
private OrderService orderService;
```

### Datos de Prueba (Setup)

```java
@BeforeEach
void setUp() {
    testUser = new User(1L, "testuser", "test@example.com", "password", Role.USER);

    testCart = new Cart();
    testCart.setId(1L);
    testCart.setUser(testUser);

    testOrder = new Order();
    testOrder.setId(1L);
    testOrder.setUser(testUser);
    testOrder.setStatus(OrderStatus.PENDIENTE);
    testOrder.setTotalAmount(BigDecimal.valueOf(100.0));
}
```

---

## Tests Detallados

### 1. `getOrderById_ExistingOrder_ReturnsOrder`

**Propósito**: Verificar que el servicio puede recuperar una orden existente por su ID.

**Código**:

```java
@Test
void getOrderById_ExistingOrder_ReturnsOrder() {
    // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

    // When
    Order result = orderService.getOrderById(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(orderRepository).findById(1L);
}
```

**Validaciones**:

- ✅ Retorna orden no nula
- ✅ ID de la orden es correcto
- ✅ Se llama al repository correctamente

---

### 2. `getOrderById_NonExistingOrder_ThrowsException`

**Propósito**: Validar el manejo de errores cuando se busca una orden inexistente.

**Código**:

```java
@Test
void getOrderById_NonExistingOrder_ThrowsException() {
    // Given
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> orderService.getOrderById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Orden no encontrada con ID: 999");
}
```

**Validaciones**:

- ✅ Lanza `ResourceNotFoundException`
- ✅ Mensaje de error es descriptivo

---

### 3. `updateOrderStatus_ValidStatus_UpdatesSuccessfully`

**Propósito**: Verificar la actualización exitosa del estado de una orden.

**Código**:

```java
@Test
void updateOrderStatus_ValidStatus_UpdatesSuccessfully() {
    // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // When
    Order result = orderService.updateOrderStatus(1L, "PROCESANDO");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESANDO);
    verify(orderRepository).save(testOrder);
}
```

**Validaciones**:

- ✅ Estado se actualiza correctamente
- ✅ Se persiste en la base de datos

---

### 4. `updateOrderStatus_InvalidStatus_ThrowsException`

**Propósito**: Validar que se rechazan estados inválidos.

**Código**:

```java
@Test
void updateOrderStatus_InvalidStatus_ThrowsException() {
    // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

    // When & Then
    assertThatThrownBy(() -> orderService.updateOrderStatus(1L, "INVALID_STATUS"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Estado de orden inválido");
}
```

**Validaciones**:

- ✅ Lanza `IllegalArgumentException` para estados inválidos
- ✅ Mensaje de error es claro

---

### 5. `updateOrderStatus_FrontendStatusConversion_ConvertsCorrectly`

**Propósito**: Verificar la conversión correcta de estados del frontend al backend.

**Código**:

```java
@Test
void updateOrderStatus_FrontendStatusConversion_ConvertsCorrectly() {
    // Given
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // When - Simular envío desde frontend con "EN_PROCESO"
    Order result = orderService.updateOrderStatus(1L, "EN_PROCESO");

    // Then - Debe convertirse a PROCESANDO
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESANDO);
    verify(orderRepository).save(testOrder);
}
```

**Validaciones**:

- ✅ Conversión "EN_PROCESO" → `PROCESANDO` funciona correctamente
- ✅ Integración frontend-backend funciona

---

### 6. `getOrdersByRepartidor_ReturnsOrdersForDeliveryUser`

**Propósito**: Verificar la obtención de órdenes asignadas a un repartidor.

**Código**:

```java
@Test
void getOrdersByRepartidor_ReturnsOrdersForDeliveryUser() {
    // Given
    User repartidor = new User(2L, "repartidor", "rep@example.com", "pass", Role.DELIVERY);
    List<Order> expectedOrders = Arrays.asList(testOrder);

    when(orderRepository.findByRepartidor(repartidor)).thenReturn(expectedOrders);

    // When
    List<Order> result = orderService.getOrdersByRepartidor(repartidor);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    verify(orderRepository).findByRepartidor(repartidor);
}
```

**Validaciones**:

- ✅ Retorna lista correcta de órdenes
- ✅ Funcionalidad específica para repartidores

---

### 7. `getDeliveryStats_CalculatesCorrectStats`

**Propósito**: Validar el cálculo correcto de estadísticas de entrega.

**Código**:

```java
@Test
void getDeliveryStats_CalculatesCorrectStats() {
    // Given
    User repartidor = new User(2L, "repartidor", "rep@example.com", "pass", Role.DELIVERY);

    Order order1 = createTestOrder(1L, OrderStatus.PENDIENTE, BigDecimal.valueOf(50.0));
    Order order2 = createTestOrder(2L, OrderStatus.PROCESANDO, BigDecimal.valueOf(75.0));
    Order order3 = createTestOrder(3L, OrderStatus.ENTREGADO, BigDecimal.valueOf(100.0));

    List<Order> orders = Arrays.asList(order1, order2, order3);
    when(orderRepository.findByRepartidor(repartidor)).thenReturn(orders);

    // When
    Map<String, Object> stats = orderService.getDeliveryStats(repartidor);

    // Then
    assertThat(stats).isNotNull();
    assertThat(stats.get("pedidosPendientes")).isEqualTo(1L);
    assertThat(stats.get("pedidosEnProceso")).isEqualTo(1L);
    assertThat(stats.get("pedidosEntregados")).isEqualTo(1L);
    assertThat(stats.get("totalGanancias")).isEqualTo(100.0);
}
```

**Validaciones**:

- ✅ Cálculo correcto de estadísticas
- ✅ Conteo por estados funciona
- ✅ Suma de ganancias es correcta

---

### 8. `assignRepartidor_ValidAssignment_AssignsSuccessfully`

**Propósito**: Verificar la asignación exitosa de repartidores a órdenes.

**Código**:

```java
@Test
void assignRepartidor_ValidAssignment_AssignsSuccessfully() {
    // Given
    User repartidor = new User(2L, "repartidor", "rep@example.com", "pass", Role.DELIVERY);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

    // When
    Order result = orderService.assignRepartidor(1L, repartidor);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getRepartidor()).isEqualTo(repartidor);
    verify(orderRepository).save(testOrder);
}
```

**Validaciones**:

- ✅ Asignación de repartidor funciona
- ✅ Se persiste la asignación

---

### 9. `getAllOrders_ReturnsAllOrders`

**Propósito**: Verificar la obtención de todas las órdenes del sistema.

**Código**:

```java
@Test
void getAllOrders_ReturnsAllOrders() {
    // Given
    List<Order> expectedOrders = Arrays.asList(testOrder);
    when(orderRepository.findAll()).thenReturn(expectedOrders);

    // When
    List<Order> result = orderService.getAllOrders();

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    verify(orderRepository).findAll();
}
```

**Validaciones**:

- ✅ Retorna todas las órdenes
- ✅ Funcionalidad básica de listado

---

### 10. `countOrdersByStatus_ReturnsCorrectCount`

**Propósito**: Validar el conteo correcto de órdenes por estado.

**Código**:

```java
@Test
void countOrdersByStatus_ReturnsCorrectCount() {
    // Given
    when(orderRepository.countByStatus(OrderStatus.PENDIENTE)).thenReturn(5L);

    // When
    long count = orderService.countOrdersByStatus(OrderStatus.PENDIENTE);

    // Then
    assertThat(count).isEqualTo(5L);
    verify(orderRepository).countByStatus(OrderStatus.PENDIENTE);
}
```

**Validaciones**:

- ✅ Conteo por estado funciona correctamente
- ✅ Funcionalidad de reporting

---

## Método Auxiliar

### `createTestOrder`

```java
private Order createTestOrder(Long id, OrderStatus status, BigDecimal total) {
    Order order = new Order();
    order.setId(id);
    order.setStatus(status);
    order.setTotalAmount(total);
    order.setCreatedAt(LocalDateTime.now());
    return order;
}
```

**Propósito**: Método helper para crear órdenes de prueba con diferentes parámetros, promoviendo reutilización de código.

---

## Aspectos Técnicos Importantes

### Patrón de Diseño

- **Given-When-Then**: Cada test sigue claramente las fases de preparación, ejecución y verificación
- **Unit Tests Puros**: Todas las dependencias externas están mockeadas
- **Aislamiento**: Cada test es independiente y no afecta a otros

### Cobertura de Escenarios

- ✅ **Casos Positivos**: Funcionalidad normal
- ✅ **Casos Negativos**: Manejo de errores
- ✅ **Casos Límite**: Estados inválidos, órdenes inexistentes
- ✅ **Integración**: Conversión frontend-backend

### Validaciones Implementadas

- **Resultado**: Verificación del retorno correcto
- **Estado**: Verificación de cambios de estado
- **Persistencia**: Verificación de llamadas a repositorio
- **Excepciones**: Verificación de manejo de errores

---

## Resultados de Ejecución

### Comando de Ejecución

```bash
cd "c:\Users\johan\OneDrive\Documentos\Programación\INTEGRADOR PROYECTO\FarmaYa\backend" && ./mvnw test -Dtest=OrderServiceTest
```

### Output de Ejecución

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.farma_ya.unit.service.OrderServiceTest
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users\johan\.m2\repository\net\bytebuddy\byte-buddy-agent\1.17.5\byte-buddy-agent-1.17.5.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.086 s -- in com.farma_ya.unit.service.OrderServiceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.252 s
[INFO] Finished at: 2025-11-04T20:27:36-05:00
[INFO] ------------------------------------------------------------------------
```

**Estado**: ✅ Todos los tests pasan exitosamente

---

## Importancia en el Proyecto

Esta suite de tests asegura que el `OrderService` funcione correctamente en todos sus aspectos críticos:

1. **Operaciones CRUD**: Crear, leer, actualizar órdenes
2. **Manejo de Estados**: Transiciones válidas de estado
3. **Lógica de Negocio**: Reglas específicas para repartidores
4. **Integración**: Comunicación correcta con otras capas
5. **Manejo de Errores**: Respuestas apropiadas a situaciones excepcionales

La implementación de estos tests garantiza la calidad y confiabilidad del sistema de gestión de órdenes en la aplicación FarmaYa.
