# FarmaYa 🏥

Proyecto Final para el curso de Integrador I - Sistema de E-commerce para Farmacia

## 📋 Descripción

FarmaYa es una plataforma de comercio electrónico completa diseñada para farmacias, que permite a los usuarios navegar por un catálogo de productos farmacéuticos, gestionar carritos de compra, realizar pedidos y realizar pagos de manera segura. El sistema incluye interfaces separadas para clientes, administradores y personal de entrega.

## ✨ Características

### Para Clientes

- **Registro y Autenticación**: Sistema seguro de login/registro con JWT
- **Catálogo de Productos**: Navegación por categorías, búsqueda y filtros
- **Carrito de Compras**: Agregar, modificar y eliminar productos
- **Gestión de Pedidos**: Historial de pedidos y seguimiento de estado
- **Direcciones de Entrega**: Múltiples direcciones por usuario

### Para Administradores

- **Dashboard Administrativo**: Panel de control completo
- **Gestión de Productos**: CRUD completo de productos con imágenes
- **Gestión de Usuarios**: Administración de clientes y roles
- **Gestión de Pedidos**: Actualización de estados y procesamiento
- **Reportes de Ventas**: Análisis y reportes de ventas

### Para Personal de Entrega

- **Interfaz de Entrega**: Gestión de pedidos asignados
- **Actualización de Estados**: Cambio de estado de pedidos

## 🛠️ Tecnologías Utilizadas

### Backend

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **JWT (JSON Web Tokens)** - Autenticación stateless
- **MySQL** - Base de datos relacional
- **Swagger/OpenAPI** - Documentación de API
- **Maven** - Gestión de dependencias

### Frontend

- **HTML5** - Estructura de páginas
- **CSS3** - Estilos y diseño
- **JavaScript (ES6)** - Lógica del cliente
- **Bootstrap 5** - Framework CSS responsivo

### Base de Datos

- **MySQL 8.0+** - Sistema de gestión de base de datos
- **Hibernate** - ORM para Java

## 📋 Prerrequisitos

Antes de ejecutar el proyecto, asegúrate de tener instalados:

- **Java 21** o superior
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git** (para clonar el repositorio)
- Navegador web moderno (Chrome, Firefox, Edge)

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/LIUNKS/FarmaYa.git
cd FarmaYa
```

### 2. Configurar la Base de Datos

```bash
# Crear la base de datos MySQL
mysql -u root -p < BD/farmaYa.sql
```

### 3. Configurar el Backend

```bash
cd backend

# Editar application.properties si es necesario
# Cambiar las credenciales de la base de datos
nano src/main/resources/application.properties
```

**Nota**: Las credenciales por defecto están configuradas para:

- Usuario: `root`
- Contraseña: `Johan12315912`
- Base de datos: `farmaya`

### 4. Ejecutar el Backend

```bash
# Compilar y ejecutar
mvn spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

### 5. Ejecutar el Frontend

```bash
cd ../frontend/farmacia-merysalud

# Abrir index.html en el navegador
# O usar un servidor local (opcional)
python -m http.server 3000
```

El frontend estará disponible en: `http://localhost:3000` (o directamente abriendo los archivos HTML)

## 📖 Uso

### Acceso al Sistema

1. **Cliente**: Registrarse o iniciar sesión en `/login.html`
2. **Administrador**: Usar credenciales de admin para acceder al dashboard
3. **Repartidor**: Usar credenciales de delivery para gestión de pedidos

### API Documentation

La documentación completa de la API está disponible en Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints Principales

- `POST /api/auth/login` - Autenticación
- `GET /api/products` - Listar productos
- `POST /api/cart/add` - Agregar al carrito
- `POST /api/orders` - Crear pedido
- `GET /api/orders` - Listar pedidos del usuario

## 🏗️ Arquitectura del Proyecto

```
FarmaYa/
├── backend/                 # API REST con Spring Boot
│   ├── src/main/java/com/farma_ya/
│   │   ├── controller/      # Controladores REST
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositorios de datos
│   │   ├── service/        # Lógica de negocio
│   │   ├── security/       # Configuración de seguridad
│   │   └── config/         # Configuraciones generales
│   └── src/main/resources/
│       └── application.properties
├── frontend/               # Interfaz de usuario
│   └── farmacia-merysalud/
│       ├── assets/         # CSS, JS, imágenes
│       ├── components/     # Componentes HTML reutilizables
│       ├── admin/          # Páginas de administración
│       └── delivery/       # Páginas de entrega
└── BD/                     # Scripts de base de datos
    └── farmaYa.sql
```

## Documentación Técnica

Para el informe académico, se han creado documentos detallados sobre la implementación:

- **[Principios de Diseño](principios-diseno.md)**: Aplicación de MVC, TDD, DAO y SOLID
- **[Librerías y Dependencias](librerias-dependencias.md)**: Recursos Java utilizados
- **[Porcentaje del Proyecto](porcentaje-proyecto.md)**: Distribución del código fuente
- **[Control de Versiones](control-versiones.md)**: Análisis del repositorio Git

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.
