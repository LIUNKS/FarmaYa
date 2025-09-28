# 📚 **Documentación Automática con Swagger/OpenAPI - FarmaYa API**

### 🌐 **URLs de Acceso:**

#### **Swagger UI (Interfaz Visual)**

```
http://localhost:8080/swagger-ui.html
```

#### **OpenAPI JSON (Documentación en formato JSON)**

```
http://localhost:8080/v3/api-docs
```

---

## 📋 **Endpoints Documentados**

### 🔐 **1. Autenticación (`/api/auth`)**

- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión (retorna JWT)
- `GET /api/auth/me` - Obtener usuario actual (requiere JWT)

### 💊 **2. Productos (`/api/products`)**

- `GET /api/products` - Listar todos los productos
- `GET /api/products/{id}` - Obtener producto por ID
- `POST /api/products` - Crear producto (solo admin)
- `PUT /api/products/{id}` - Actualizar producto (solo admin)
- `DELETE /api/products/{id}` - Eliminar producto (solo admin)
- `GET /api/products/search?name=` - Buscar productos por nombre
- `GET /api/products/category/{category}` - Productos por categoría

### 🛒 **3. Carrito (`/api/cart`)**

- `GET /api/cart` - Obtener carrito del usuario
- `POST /api/cart/add?productId=&quantity=` - Agregar producto al carrito
- `DELETE /api/cart/remove/{productId}` - Remover producto del carrito
- `DELETE /api/cart/clear` - Vaciar carrito

### 📦 **4. Pedidos (`/api/orders`)**

- `POST /api/orders` - Crear pedido desde carrito
- `GET /api/orders` - Obtener pedidos del usuario
- `GET /api/orders/{id}` - Obtener pedido específico
- `PUT /api/orders/{id}/status?status=` - Actualizar estado (solo admin)

### 📊 **5. Reportes (`/api/reportes`)**

- `POST /api/reportes/generar-semanal` - Generar reporte semanal
- `POST /api/reportes/generar-automaticos/{semanas}` - Reportes automáticos
- `GET /api/reportes/por-año/{año}` - Reportes por año
- `GET /api/reportes/ultimos/{limite}` - Últimos reportes
- `GET /api/reportes/info` - Información de la API

---

## 🚀 **Cómo Usar Swagger UI**

### **1. Acceder a la Interfaz**

Abre tu navegador y ve a: `http://localhost:8080/swagger-ui.html`

### **2. Explorar Endpoints**

- **Categorías organizadas** por funcionalidad
- **Descripción detallada** de cada endpoint
- **Parámetros requeridos** y opcionales
- **Ejemplos de respuesta** para cada código de estado

### **3. Probar APIs en Vivo**

1. **Click en "Try it out"** en cualquier endpoint
2. **Completa los parámetros** requeridos
3. **Click "Execute"** para hacer la petición
4. **Ve la respuesta** en tiempo real

### **4. Autenticación JWT**

1. **Primero haz login** en `/api/auth/login`
2. **Copia el `accessToken`** de la respuesta
3. **Click en "Authorize"** (🔒) en la parte superior
4. **Introduce**: `Bearer {tu-token-aqui}`
5. **Ahora puedes usar** endpoints protegidos

---

## 📝 **Ejemplos de Uso**

### **Registro de Usuario**

```json
POST /api/auth/register
{
  "username": "juan123",
  "email": "juan@example.com",
  "password": "miPassword123"
}
```

### **Login**

```json
POST /api/auth/login
{
  "username": "juan123",
  "password": "miPassword123"
}
```

### **Crear Producto (con JWT)**

```json
POST /api/products
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
  "name": "Paracetamol 500mg",
  "description": "Analgésico y antipirético",
  "price": 15.50,
  "categoria": "Analgésicos",
  "stock": 100
}
```

---

## 🎨 **Configuración Personalizada**

### **Información de la API**

- **Título**: FarmaYa API
- **Descripción**: Sistema completo de farmacia
- **Versión**: 1.0.0
- **Contacto**: Equipo FarmaYa

### **Servidores**

- **Desarrollo**: `http://localhost:8080`
- **Producción**: `https://api.farmaya.com`

### **Configuraciones Especiales**

- Operaciones ordenadas por método HTTP
- Tags ordenados alfabéticamente
- "Try it out" habilitado por defecto
- Filtros de búsqueda disponibles

---

## 🛡️ **Seguridad**

### **Endpoints Públicos**

- `/api/auth/**` - Autenticación
- `/api/products/**` - Productos (solo lectura)
- `/swagger-ui/**` - Documentación
- `/v3/api-docs/**` - OpenAPI JSON

### **Endpoints Protegidos**

- `/api/cart/**` - Carrito (requiere JWT)
- `/api/orders/**` - Pedidos (requiere JWT)
- `/api/reportes/**` - Reportes (requiere JWT)
- Operaciones de administrador en productos

---
