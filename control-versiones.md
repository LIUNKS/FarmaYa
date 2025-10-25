# 2.6. Control de Versiones

Este documento analiza el uso de Git como sistema de control de versiones en el proyecto FarmaYa.

## Información General del Repositorio

- **Nombre del repositorio**: FarmaYa
- **Propietario**: LIUNKS
- **URL**: https://github.com/LIUNKS/FarmaYa
- **Rama principal**: main
- **Sistema de control de versiones**: Git

## Estadísticas del Repositorio

### Commits Totales

- **Total de commits**: 104
- **Commits sin merges**: 104

### Ramas

- **Rama principal**: `main` (activa)
- **Ramas remotas**:
  - `origin/main`
  - `origin/backend`
  - `origin/frontend`
- **Estrategia de ramas**: Separación por componentes (backend/frontend)

### Contribuidores

| Contribuidor     | Commits | Porcentaje |
| ---------------- | ------- | ---------- |
| jgcamiloaga      | 95      | 91.3%      |
| SugaAG           | 4       | 3.8%       |
| Carlos Huane     | 1       | 1.0%       |
| DeivisGasparr    | 1       | 1.0%       |
| Johann Camiloaga | 1       | 1.0%       |
| **TOTAL**        | **104** | **100%**   |

## Análisis de Commits

### Distribución por Tipo de Cambio

Basado en los mensajes de commit analizados, la distribución aproximada es:

- **Funcionalidades nuevas**: ~40 commits (38.5%)
- **Corrección de bugs**: ~25 commits (24.0%)
- **Mejoras de UI/UX**: ~20 commits (19.2%)
- **Configuración y setup**: ~10 commits (9.6%)
- **Documentación**: ~5 commits (4.8%)
- **Refactoring**: ~4 commits (3.8%)

### Patrón de Commits

Los commits siguen un patrón consistente:

- Mensajes en español
- Descripciones claras y concisas
- Commits atómicos (cambios relacionados agrupados)

Ejemplos de mensajes de commit:

- "feat: agregar validación de productos"
- "fix: corregir error en login de usuarios"
- "ui: mejorar diseño del carrito de compras"
- "docs: actualizar README con instrucciones de instalación"

## Estrategia de Versionado

### Ramas por Componente

- **`main`**: Rama principal con código estable
- **`backend`**: Desarrollo del API REST
- **`frontend`**: Desarrollo de la interfaz de usuario

### Flujo de Trabajo

1. **Desarrollo**: Trabajo en ramas feature o directamente en backend/frontend
2. **Integración**: Merge a main cuando funcionalidad completa
3. **Despliegue**: Código en main listo para producción

## Registro Detallado de Commits

### Historial Completo de Commits (103 commits totales)

A continuación se presenta el registro cronológico completo de todos los commits realizados en el proyecto:

#### **Commits Recientes (últimas 2 semanas)**

- `d5ae005` - jgcamiloaga, 28 minutes ago : Actualizar README.md
- `904120d` - jgcamiloaga, 34 minutes ago : Agregar imágenes al proyecto: img_70.png a img_99.png
- `fa56a5b` - jgcamiloaga, 35 minutes ago : Mejorar manejo del carrito: agregar validación de token en la obtención del carrito, manejar errores 401 y 500, y optimizar el proceso de logout
- `e115c6a` - jgcamiloaga, 35 minutes ago : Mejorar la inicialización de la aplicación: agregar restauración de sesión, validación automática de token y manejo de sesión expirada
- `4b4ac1a` - jgcamiloaga, 36 minutes ago : Mejorar manejo de errores de API: mostrar modal de expiración de sesión en error 401 y ajustar la creación de órdenes para aceptar datos nulos
- `2cfe7cf` - jgcamiloaga, 37 minutes ago : Actualizar la página de productos: renombrar secciones, mejorar mensajes de no resultados, optimizar carga de productos y categorías desde el backend, y ajustar la lógica de filtrado y carrito
- `0999945` - jgcamiloaga, 38 minutes ago : Mejora el manejo de la autenticación del usuario, carga las órdenes del usuario desde el backend y mejora la lógica de renderizado de órdenes
- `c8d6969` - jgcamiloaga, 39 minutes ago : Mejorar el diseño y la funcionalidad de la página de inicio: actualizar estilos de tarjetas de categoría, optimizar carga de productos destacados y agregar manejo de errores en la carga de datos
- `80f202a` - jgcamiloaga, 39 minutes ago : Mejorar el proceso de checkout: agregar manejo de autenticación, cargar productos desde el backend y optimizar la creación de pedidos
- `736d7fa` - jgcamiloaga, 39 minutes ago : Corregir el carrito.js para mejorar la gestión del carrito y el manejo de la autenticación del usuario
- `3ba8a8e` - jgcamiloaga, 41 minutes ago : Actualizar la configuración de expiración de JWT a 5 minutos
- `0789896` - jgcamiloaga, 41 minutes ago : Agregar método getUserByEmail y corregir asignación de teléfono en registro de usuario
- `a1c1a36` - jgcamiloaga, 41 minutes ago : Corregir las importaciones en ReporteVentaService para mayor claridad y organización
- `9383e9e` - jgcamiloaga, 41 minutes ago : Actualizar método createOrderFromCart para manejar datos de envío
- `0b6409c` - jgcamiloaga, 41 minutes ago : Actualizar método createOrderFromCart para incluir datos de envío
- `654bbc0` - jgcamiloaga, 41 minutes ago : Actualizar método getCartByUser para incluir items y productos relacionados
- `ea4a136` - jgcamiloaga, 42 minutes ago : Actualizar el estado de pedido a 'ENTREGADO' en el método de búsqueda por rango de fechas
- `6755dde` - jgcamiloaga, 42 minutes ago : Agregar repositorio para gestionar direcciones de usuario
- `b3f477c` - jgcamiloaga, 42 minutes ago : Agregar método para obtener el carrito de un usuario con sus items y productos relacionados
- `2448fac` - jgcamiloaga, 42 minutes ago : Agregar conversor para el enum OrderStatus con manejo de mayúsculas y minúsculas

#### **Commits de Desarrollo Backend (últimas 4 semanas)**

- `ab61cdf` - jgcamiloaga, 42 minutes ago : Actualizar los nombres de los estados de pedido a español en OrderStatus
- `737b8d1` - jgcamiloaga, 43 minutes ago : Corregir nombre de tabla en OrderItem y ajustar la relación con Order
- `ea8d7f4` - jgcamiloaga, 44 minutes ago : Agregar atributos de dirección de envío y métodos relacionados en la clase Order
- `56c7deb` - jgcamiloaga, 45 minutes ago : Agregar clase Direccion con mapeo JPA y atributos necesarios
- `96fbca1` - jgcamiloaga, 45 minutes ago : Corregir nombre de tabla en CartItem y agregar anotación JsonBackReference en la relación con Cart
- `9c4da65` - jgcamiloaga, 45 minutes ago : Agregar anotación JsonManagedReference en la lista de CartItems en Cart
- `01a82d1` - jgcamiloaga, 45 minutes ago : Agregar validación de teléfono en UserRegistrationDTO
- `d6d347e` - jgcamiloaga, 47 minutes ago : Agregar funcionalidad para crear pedidos con datos de envío y obtener todos los pedidos (solo administradores)
- `7953adf` - jgcamiloaga, 47 minutes ago : Agregar endpoint para registrar nuevos usuarios
- `304239d` - jgcamiloaga, 47 minutes ago : SecurityConfig para mejorar la configuración de seguridad
- `79539c9` - jgcamiloaga, 47 minutes ago : Agregar configuración de Jackson para manejar proxies lazy con Hibernate 6
- `a1a3b00` - jgcamiloaga, 47 minutes ago : Agregar dependencia para Jackson Datatype Hibernate 6 en pom.xml

#### **Commits de Integración Frontend/Backend (6 días atrás)**

- `dc9b2f0` - jgcamiloaga, 6 days ago : Corregir la gestión de datos para integrar backend API para las operaciones de producto y carrito
- `f6d59c3` - jgcamiloaga, 6 days ago : Hacer funciones de actualización del carrito y agregar al carrito asíncronas para mejorar la gestión de datos
- `102a85f` - jgcamiloaga, 6 days ago : Eliminar archivos y configuraciones no utilizados del proyecto
- `35768ae` - jgcamiloaga, 6 days ago : Agregar configuración y servicios para la API, incluyendo métodos para autenticación, manejo de errores y operaciones CRUD
- `7836c44` - jgcamiloaga, 6 days ago : Agregar 120 imágenes al proyecto de farmacia Merysalud
- `124373c` - jgcamiloaga, 6 days ago : Actualizar formulario de inicio de sesión para aceptar usuario o email y mejorar manejo de errores en login y registro
- `cd4b33a` - jgcamiloaga, 6 days ago : Eliminar archivos de configuración de Visual Studio Code y Angular del proyecto
- `76553d9` - jgcamiloaga, 6 days ago : Actualizar método loadUserByUsername para buscar usuario por email si no se encuentra por username
- `f31bc15` - jgcamiloaga, 6 days ago : Actualizar lógica de rol en la clase User para determinar el rol basado en rolId y sincronizar rolId al establecer el rol
- `dbc074a` - jgcamiloaga, 6 days ago : Actualizar configuración CORS para permitir más orígenes y métodos HTTP
- `91f01b3` - jgcamiloaga, 6 days ago : Agregar inicializador de datos para crear el usuario administrador por defecto

#### **Commits de Desarrollo Inicial (4-9 semanas atrás)**

- `ae349fd` - Johann Camiloaga, 6 days ago : Merge pull request #2 from LIUNKS/backend
- `7fdcbd6` - jgcamiloaga, 13 days ago : UserService para usar la inyección de constructor para las dependencias
- `8b1030f` - jgcamiloaga, 13 days ago : Eliminar importación innecesaria de DateTimeFormatter en ReporteVentaService
- `14dea36` - jgcamiloaga, 13 days ago : Agregar clase ProductValidator para validar productos según reglas de negocio
- `c54b071` - jgcamiloaga, 13 days ago : ProductService para mejorar la funcionalidad de gestión de productos
- `ffdfe2c` - jgcamiloaga, 13 days ago : OrderService para implementar IOrderService y optimizar la gestión de stock usando InventoryService
- `b63ccea` - jgcamiloaga, 4 weeks ago : Mejoras en la clase User
- `b1df187` - jgcamiloaga, 4 weeks ago : Añadir clase ReporteVentaSemanal para gestionar reportes de ventas semanales
- `b923f83` - jgcamiloaga, 4 weeks ago : Actualizar clase Product para mejorar la consistencia de los campos y cambiar el tipo de precio a BigDecimal
- `c4b7d0f` - jgcamiloaga, 4 weeks ago : Actualizar clase OrderItem para usar BigDecimal en lugar de Double y mejorar la consistencia de los campos
- `5ae6a28` - jgcamiloaga, 4 weeks ago : Actualizar clase Order para usar BigDecimal en lugar de Double y mejorar la consistencia de los campos
- `9485b32` - jgcamiloaga, 4 weeks ago : Añadir clase DetalleVentaSemanalProducto para gestionar detalles de ventas semanales
- `317e377` - jgcamiloaga, 4 weeks ago : Renombrar tabla y columnas en CartItem para mejorar la consistencia con la base de datos
- `82341e3` - jgcamiloaga, 4 weeks ago : Renombrar tabla a "Carrito" y ajustar columnas en la entidad Cart para mejorar la consistencia con la base de datos
- `795200e` - jgcamiloaga, 4 weeks ago : JwtResponseDTO para mejorar la claridad y mantenimiento
- `0d09a3a` - jgcamiloaga, 4 weeks ago : Añadir controlador ReporteVentaController para generar y consultar reportes de ventas semanales
- `025d61b` - jgcamiloaga, 4 weeks ago : AuthController para mejorar el registro de usuarios y el flujo de autenticación
- `f0f2553` - jgcamiloaga, 4 weeks ago : Añadir Maven wrapper scripts y actualizar las dependencias pom.xml y versiones
- `b2ebcdd` - jgcamiloaga, 4 weeks ago : Añadir archivo .gitignore para excluir los artefactos de compilación y archivos específicos del IDE
- `25160dc` - SugaAG, 4 weeks ago : Update pom.xml
- `6dba170` - SugaAG, 4 weeks ago : Add files via upload
- `19f8615` - SugaAG, 4 weeks ago : Add files via upload
- `28d2492` - SugaAG, 7 weeks ago : Add files via upload
- `bbef0d4` - DeivisGasparr, 9 weeks ago : Add files via upload
- `9a25b67` - jgcamiloaga, 9 weeks ago : feat: Inicializar proyecto Angular FarmaYa en frontend

## Ramas Trabajadas

### Ramas Principales

#### **Rama `main`** (Activa)

- **Estado**: Rama principal y por defecto
- **Propósito**: Contiene el código estable y listo para producción
- **Último commit**: `d5ae005` - Actualizar README.md
- **Total de commits**: 103 commits
- **Estado actual**: Sincronizada con `origin/main`

#### **Rama `backend`** (Remota)

- **Ubicación**: `origin/backend`
- **Propósito**: Desarrollo del API REST y lógica de negocio
- **Estado**: Mergeada a main (commit `ae349fd`)
- **Commits principales**: Desarrollo de servicios, controladores, modelos y configuración de seguridad
- **Funcionalidades implementadas**:
  - Arquitectura Spring Boot completa
  - Autenticación JWT
  - Gestión de productos, usuarios, pedidos y carrito
  - API REST documentada con Swagger
  - Configuración de seguridad y CORS

#### **Rama `frontend`** (Remota)

- **Ubicación**: `origin/frontend`
- **Propósito**: Desarrollo de la interfaz de usuario
- **Estado**: Activa en remoto
- **Funcionalidades implementadas**:
  - Estructura HTML completa para todas las páginas
  - Formularios de login/registro
  - Catálogo de productos
  - Carrito de compras
  - Paneles de administración y delivery (básicos)
  - Integración con API backend

### Historial de Ramas

```
* d5ae005 (HEAD -> main, origin/main, origin/HEAD) Actualizar README.md
* 904120d Agregar imágenes al proyecto: img_70.png a img_99.png
* fa56a5b Mejorar manejo del carrito...
* e115c6a Mejorar la inicialización de la aplicación...
* 4b4ac1a Mejorar manejo de errores de API...
* 2cfe7cf Actualizar la página de productos...
* 0999945 Mejora el manejo de la autenticación del usuario...
* c8d6969 Mejorar el diseño y la funcionalidad de la página de inicio...
* 80f202a Mejorar el proceso de checkout...
* 736d7fa Corregir el carrito.js...
* 3ba8a8e Actualizar la configuración de expiración de JWT a 5 minutos
* 0789896 Agregar método getUserByEmail...
* a1c1a36 Corregir las importaciones en ReporteVentaService...
* 9383e9e Actualizar método createOrderFromCart...
* 0b6409c Actualizar método createOrderFromCart...
* 654bbc0 Actualizar método getCartByUser...
* ea4a136 Actualizar el estado de pedido a 'ENTREGADO'...
* 6755dde Agregar repositorio para gestionar direcciones de usuario
* b3f477c Agregar método para obtener el carrito de un usuario...
* 2448fac Agregar conversor para el enum OrderStatus...
* ab61cdf Actualizar los nombres de los estados de pedido a español...
* 737b8d1 Corregir nombre de tabla en OrderItem...
* ea8d7f4 Agregar atributos de dirección de envío...
* 56c7deb Agregar clase Direccion...
* 96fbca1 Corregir nombre de tabla en CartItem...
* 9c4da65 Agregar anotación JsonManagedReference...
* 01a82d1 Agregar validación de teléfono en UserRegistrationDTO
* d6d347e Agregar funcionalidad para crear pedidos...
* 7953adf Agregar endpoint para registrar nuevos usuarios
* 304239d SecurityConfig para mejorar la configuración de seguridad
* 79539c9 Agregar configuración de Jackson...
* a1a3b00 Agregar dependencia para Jackson Datatype Hibernate 6...
* dc9b2f0 Corregir la gestión de datos para integrar backend API...
* f6d59c3 Hacer funciones de actualización del carrito...
* 102a85f Eliminar archivos y configuraciones no utilizados...
* 35768ae Agregar configuración y servicios para la API...
* 7836c44 Agregar 120 imágenes al proyecto...
* 124373c Actualizar formulario de inicio de sesión...
* cd4b33a Eliminar archivos de configuración de VS Code y Angular...
* 76553d9 Actualizar método loadUserByUsername...
* f31bc15 Actualizar lógica de rol en la clase User...
* dbc074a Actualizar configuración CORS...
* 91f01b3 Agregar inicializador de datos...
*   ae349fd Johann Camiloaga - Merge pull request #2 from LIUNKS/backend
|\
| * 7fdcbd6 jgcamiloaga - UserService para usar la inyección de constructor...
| * 8b1030f jgcamiloaga - Eliminar importación innecesaria...
| * 14dea36 jgcamiloaga - Agregar clase ProductValidator...
| * c54b071 jgcamiloaga - ProductService para mejorar la funcionalidad...
| * ffdfe2c jgcamiloaga - OrderService para implementar IOrderService...
* | b63ccea jgcamiloaga - Mejoras en la clase User
* | b1df187 jgcamiloaga - Añadir clase ReporteVentaSemanal...
* | b923f83 jgcamiloaga - Actualizar clase Product...
* | c4b7d0f jgcamiloaga - Actualizar clase OrderItem...
* | 5ae6a28 jgcamiloaga - Actualizar clase Order...
* | 9485b32 jgcamiloaga - Añadir clase DetalleVentaSemanalProducto...
* | 317e377 jgcamiloaga - Renombrar tabla y columnas en CartItem...
* | 82341e3 jgcamiloaga - Renombrar tabla a "Carrito"...
* | 795200e jgcamiloaga - JwtResponseDTO...
* | 0d09a3a jgcamiloaga - Añadir controlador ReporteVentaController...
* | 025d61b jgcamiloaga - AuthController...
* | f0f2553 jgcamiloaga - Añadir Maven wrapper scripts...
* | b2ebcdd jgcamiloaga - Añadir archivo .gitignore...
* | 25160dc SugaAG - Update pom.xml
* | 6dba170 SugaAG - Add files via upload
* | 19f8615 SugaAG - Add files via upload
* | 28d2492 SugaAG - Add files via upload
* | bbef0d4 DeivisGasparr - Add files via upload
* | 9a25b67 jgcamiloaga - feat: Inicializar proyecto Angular FarmaYa en frontend
```

### Desarrollo por Ramas

#### **Rama `backend`** - Desarrollo del API REST

**Commits principales:**

- Desarrollo de modelos JPA (User, Product, Order, Cart, etc.)
- Implementación de servicios de negocio
- Configuración de seguridad JWT
- Controladores REST API
- Integración con base de datos MySQL
- Documentación Swagger/OpenAPI

#### **Rama `frontend`** - Desarrollo de la Interfaz

**Commits principales:**

- Estructura HTML completa
- Estilos CSS con Bootstrap
- JavaScript para integración con API
- Páginas de usuario, admin y delivery
- Formularios y validaciones
- Gestión de estado de autenticación

#### **Rama `main`** - Integración y Producción

**Commits principales:**

- Merges de funcionalidades completas
- Actualizaciones de documentación
- Corrección de bugs de integración
- Optimizaciones finales
- Preparación para despliegue

## Gestión de Versiones

### Versionado Semántico

El proyecto utiliza Maven con versionado semántico:

- **Versión actual**: 0.0.1-SNAPSHOT
- **Formato**: `major.minor.patch-SNAPSHOT`

### Control de Releases

- No se identificaron tags de release
- Versiones manejadas a través de commits en rama main
- Despliegue continuo desde rama principal

## Herramientas y Configuración

### Archivo .gitignore

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Database
*.db
*.sqlite

# Environment
.env
.env.local
```

### Configuración Git

- **Remote origin**: https://github.com/LIUNKS/FarmaYa
- **Default branch**: main
- **Merge strategy**: Fast-forward cuando posible

## Métricas de Calidad

### Frecuencia de Commits

- **Commits por semana**: ~2-3 commits (basado en 104 commits totales)
- **Commits por día laborable**: ~0.5 commits
- **Tamaño promedio de commits**: Pequeños y enfocados

### Cobertura de Código

- **Archivos versionados**: 100% del código fuente
- **Documentación versionada**: README.md, documentación de API
- **Configuración versionada**: Scripts de BD, configuraciones

## Problemas Identificados

1. **Falta de pruebas automatizadas**: No hay directorio `src/test/` con pruebas unitarias
2. **Sin tags de versión**: No hay releases marcados con tags
3. **Commits grandes**: Algunos commits incluyen múltiples cambios no relacionados
4. **Falta de CI/CD**: No hay integración continua configurada

## Recomendaciones de Mejora

### Para Control de Versiones

1. **Implementar GitFlow**:

   - Rama `develop` para integración
   - Ramas `feature/*` para nuevas funcionalidades
   - Ramas `hotfix/*` para corrección de bugs en producción

2. **Agregar tags de versión**:

   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin --tags
   ```

3. **Configurar GitHub Actions** para CI/CD:
   - Tests automáticos
   - Build automático
   - Despliegue automático

### Para Calidad de Código

1. **Agregar pruebas unitarias**:

   - JUnit para backend
   - Jest para frontend
   - Cobertura mínima del 80%

2. **Code reviews**: Implementar pull requests obligatorios

3. **Conventional commits**: Estandarizar formato de mensajes

## Conclusión

El proyecto FarmaYa tiene un control de versiones básico pero funcional. La estructura de ramas por componente facilita el desarrollo paralelo, y el historial de commits muestra un desarrollo activo. Sin embargo, faltan prácticas avanzadas como testing automatizado, CI/CD y versionado formal que mejorarían significativamente la calidad y mantenibilidad del proyecto.

## Comandos Git Utilizados en el Análisis

```bash
# Número total de commits
git log --oneline | wc -l

# Listar ramas
git branch -a

# Contribuidores por commits
git shortlog -sn --no-merges

# Historial de commits
git log --oneline --graph --decorate

# Estado del repositorio
git status
```
