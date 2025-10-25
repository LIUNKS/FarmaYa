# 2.3. Aplicación de los Principios de MVC, TDD, DAO y SOLID

Este documento detalla cómo se aplicaron **todos** los principios de diseño de software identificados en el proyecto FarmaYa, incluyendo fragmentos de código relevantes de todas las entidades y componentes del sistema.

## MVC (Model-View-Controller)

El patrón MVC se implementa utilizando Spring Boot, donde:

- **Model**: Representado por las entidades JPA (Product, User, Order, Cart, etc.)
- **View**: Interfaz de usuario en HTML/CSS/JS
- **Controller**: Controladores REST que manejan las solicitudes HTTP

### Controladores (Controller Layer)

#### ProductController - Gestión de Productos

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // ... otros métodos para actualizar, eliminar, buscar
}
```

#### AuthController - Autenticación y Usuarios

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        // Lógica de autenticación
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword()));

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return ResponseEntity.ok(new JwtResponseDTO(accessToken, refreshToken, ...));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}
```

#### CartController - Gestión del Carrito

```java
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartService.getCartByUser(currentUser);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        User currentUser = getCurrentUser();
        Cart cart = cartService.addToCart(currentUser, productId, quantity);
        return ResponseEntity.ok(cart);
    }
}
```

#### OrderController - Gestión de Pedidos

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder() {
        User currentUser = getCurrentUser();
        Order order = orderService.createOrderFromCart(currentUser);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders() {
        User currentUser = getCurrentUser();
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        return ResponseEntity.ok(orders);
    }
}
```

### Servicios (Service Layer/Model)

#### ProductService - Lógica de Productos

```java
@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    public ProductService(ProductRepository productRepository, ProductValidator productValidator) {
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(Product product) {
        productValidator.validateProduct(product);
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = getProductById(id);
        existingProduct.setName(productDetails.getName());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStock(productDetails.getStock());

        productValidator.validateForUpdate(getProductById(id), existingProduct);
        return productRepository.save(existingProduct);
    }
}
```

#### CartService - Lógica del Carrito

```java
@Service
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Cart addToCart(User user, Long productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        // Lógica para agregar o actualizar item en carrito
        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }
}
```

#### UserService - Lógica de Usuarios

```java
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }
}
```

## DAO (Data Access Object)

Los repositorios de Spring Data JPA actúan como DAOs, encapsulando el acceso a datos.

### ProductRepository (DAO)

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoriaContainingIgnoreCase(String categoria);
    List<Product> findByStockGreaterThanEqual(int stock);
}
```

### UserRepository (DAO)

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### CartRepository (DAO)

```java
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.user = :user")
    Optional<Cart> findByUserWithItemsAndProducts(@Param("user") User user);
}
```

### OrderRepository (DAO)

```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findByNumeroPedido(String numeroPedido);
}
```

## SOLID Principles

### S - Single Responsibility Principle (SRP)

Cada clase tiene una única responsabilidad claramente definida.

#### ProductService - Solo lógica de productos

```java
@Service
public class ProductService implements IProductService {
    // ÚNICA RESPONSABILIDAD: Gestionar lógica de negocio de productos
    // NO maneja: autenticación, envío de emails, logging, etc.
}
```

#### ProductValidator - Solo validaciones

```java
@Component
public class ProductValidator {
    // ÚNICA RESPONSABILIDAD: Validar reglas de negocio de productos

    public void validateProduct(Product product) {
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateRequiredFields(product);
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser un valor positivo");
        }
    }
}
```

#### GlobalExceptionHandler - Solo manejo de excepciones

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // ÚNICA RESPONSABILIDAD: Manejar excepciones de manera centralizada

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Manejar errores de validación
    }
}
```

### O - Open/Closed Principle (OCP)

Las clases están abiertas para extensión pero cerradas para modificación.

#### ProductValidator - Extensible sin modificar

```java
@Component
public class ProductValidator {
    // CLASE CERRADA PARA MODIFICACIÓN
    public void validateProduct(Product product) {
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateRequiredFields(product);
    }

    // EXTENSIÓN: Nuevo método sin modificar validateProduct
    public void validateForUpdate(Product existingProduct, Product updatedProduct) {
        validateProduct(updatedProduct); // Reutiliza validación base

        // Reglas específicas para actualización
        if (existingProduct.getSku() != null &&
            !existingProduct.getSku().equals(updatedProduct.getSku())) {
            // Validación adicional para updates
        }
    }
}
```

#### OrderStatusConverter - Maneja múltiples formatos

```java
@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    // CLASE CERRADA: Lógica de conversión no cambia
    // ABIERTA PARA EXTENSIÓN: Maneja diferentes formatos de BD

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        // Convierte a formato capitalizado para BD
        String name = attribute.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        // Lee tolerante a mayúsculas/minúsculas
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(dbData)) {
                return status;
            }
        }
        return null;
    }
}
```

### L - Liskov Substitution Principle (LSP)

Las subclases pueden reemplazar a sus clases base sin alterar el comportamiento.

#### User implementa UserDetails

```java
@Entity
public class User implements UserDetails {
    // User PUEDE SUSTITUIR a cualquier UserDetails
    // Cumple contrato completo de UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Lógica específica pero cumple contrato
    }

    // ... otros métodos de UserDetails
}
```

#### Interfaces permiten sustitución

```java
public interface IProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    // Contrato claro para cualquier implementación
}

@Service
public class ProductService implements IProductService {
    // PUEDE SER SUSTITUIDO por cualquier otra implementación
    // que cumpla la interfaz
}
```

### I - Interface Segregation Principle (ISP)

Interfaces específicas y no forzadas a implementar métodos innecesarios.

#### IProductService - Solo productos

```java
public interface IProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product productDetails);
    void deleteProduct(Long id);
    List<Product> searchProducts(String name);
    List<Product> getProductsByCategory(String category);
    // SOLO métodos relacionados con productos
}
```

#### ICartService - Solo carrito

```java
public interface ICartService {
    Cart getCartByUser(User user);
    Cart addToCart(User user, Long productId, int quantity);
    Cart removeFromCart(User user, Long productId);
    Cart clearCart(User user);
    // SOLO métodos relacionados con carrito
    // NO incluye métodos de productos, pedidos, etc.
}
```

#### IOrderService - Solo pedidos

```java
public interface IOrderService {
    Order createOrderFromCart(User user);
    List<Order> getOrdersByUser(User user);
    Order getOrderById(Long orderId);
    Order updateOrderStatus(Long orderId, OrderStatus status);
    // SOLO métodos relacionados con pedidos
}
```

### D - Dependency Inversion Principle (DIP)

Las clases dependen de abstracciones, no de implementaciones concretas.

#### Inyección de dependencias en servicios

```java
@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository; // INTERFAZ, no implementación
    private final ProductValidator productValidator;   // ABSTRACCIÓN

    public ProductService(ProductRepository productRepository, ProductValidator productValidator) {
        this.productRepository = productRepository;
        this.productValidator = productValidator;
    }
}
```

#### Controladores dependen de interfaces

```java
@RestController
public class ProductController {

    private final IProductService productService; // ABSTRACCIÓN

    public ProductController(IProductService productService) {
        this.productService = productService;
    }
}
```

#### Configuración de seguridad

```java
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // ABSTRACCIÓN
    private final JwtAuthenticationEntryPoint unauthorizedHandler; // ABSTRACCIÓN

    // ...
}
```

## Modelos/Entidades (Encapsulamiento y Abstracción)

### Product - Encapsulamiento completo

```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    // Getters y setters para encapsulamiento
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
```

### User - Implementa interfaz y encapsula lógica

```java
@Entity
public class User implements UserDetails {

    @Column(name = "rol_id")
    private Integer rolId;

    @Transient
    private Role role = Role.USER;

    // Encapsula conversión rolId <-> Role
    public Role getRole() {
        return (rolId != null && rolId == 1) ? Role.ADMIN : Role.USER;
    }

    public void setRole(Role role) {
        this.role = role;
        this.rolId = (role == Role.ADMIN) ? 1 : 2;
    }

    // Implementa métodos de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + getRole().name()));
    }
}
```

### Order - Maneja relaciones complejas

```java
@Entity
public class Order {

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "direccion_entrega_id")
    private Direccion shippingAddress;

    // Encapsula cálculo de total
    public double getCalculatedTotalAmount() {
        return items.stream()
            .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
            .sum();
    }
}
```

## Polimorfismo y Herencia

### Exception Handling - Polimorfismo

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // POLIMORFISMO: Maneja cualquier Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error interno del servidor");
    }
}
```

### Enums - Herencia implícita

```java
public enum Role {
    USER, ADMIN
}

public enum OrderStatus {
    PENDIENTE, PROCESANDO, ENVIADO, ENTREGADO, CANCELADO
}
```

## TDD (Test-Driven Development)

**Nota**: En este proyecto, no se implementó TDD de manera estricta. No se encontraron pruebas unitarias en el directorio `src/test/`.

Sin embargo, el proyecto está preparado para TDD con:

- Dependencia `spring-boot-starter-test` en `pom.xml`
- Estructura modular que facilita las pruebas unitarias
- Interfaces que permiten mocking

### Ejemplo de cómo debería implementarse TDD

```java
// Ejemplo de prueba unitaria que debería existir
@SpringBootTest
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    @InjectMocks
    private ProductService productService;

    @Test
    public void createProduct_ValidProduct_ShouldReturnCreatedProduct() {
        // Arrange
        Product product = new Product("Test Product", BigDecimal.valueOf(10.99));
        when(productRepository.save(product)).thenReturn(product);

        // Act
        Product result = productService.createProduct(product);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productValidator).validateProduct(product);
        verify(productRepository).save(product);
    }

    @Test
    public void getProductById_ProductExists_ShouldReturnProduct() {
        // Arrange
        Long productId = 1L;
        Product expectedProduct = new Product("Test Product", BigDecimal.valueOf(10.99));
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        // Act
        Product result = productService.getProductById(productId);

        // Assert
        assertEquals(expectedProduct, result);
    }

    @Test
    public void getProductById_ProductNotFound_ShouldThrowException() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(productId);
        });
    }
}
```

## Conclusión

El proyecto FarmaYa aplica correctamente todos los principios SOLID de manera consistente:

- **SRP**: Cada clase tiene una responsabilidad única y bien definida
- **OCP**: Las clases están abiertas para extensión pero cerradas para modificación
- **LSP**: Las implementaciones pueden sustituir a sus abstracciones sin problemas
- **ISP**: Interfaces específicas evitan dependencias innecesarias
- **DIP**: Todas las clases dependen de abstracciones, no de implementaciones concretas

El patrón MVC está correctamente implementado con separación clara entre controladores, servicios y repositorios. Los DAOs están bien encapsulados mediante interfaces de Spring Data JPA.

La única área no implementada es TDD, aunque la arquitectura del proyecto lo facilita para futuras implementaciones de pruebas unitarias.

Esta estructura de diseño hace que el código sea mantenible, extensible, testable y fácil de entender para futuros desarrolladores.
