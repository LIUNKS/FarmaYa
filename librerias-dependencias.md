# 2.4. Uso de Recursos Java (Librerías y Dependencias)

El proyecto FarmaYa utiliza diversas librerías y dependencias de Java para implementar sus funcionalidades. A continuación se detallan las principales dependencias utilizadas, extraídas del archivo `pom.xml`.

## Dependencias Principales

### Spring Boot Starters

```xml
<!-- Spring Boot Web Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Uso**: Proporciona todas las dependencias necesarias para crear aplicaciones web con Spring MVC, incluyendo Tomcat embebido, Jackson para JSON, y validación.

### Spring Data JPA

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Uso**: Simplifica el acceso a datos relacionales mediante JPA. Proporciona repositorios CRUD automáticos y soporte para consultas personalizadas.

### Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Uso**: Framework de seguridad que proporciona autenticación, autorización, protección contra ataques comunes (CSRF, XSS) y gestión de sesiones.

### JWT (JSON Web Tokens)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

**Uso**: Implementa autenticación stateless mediante tokens JWT. Permite generar, parsear y validar tokens de acceso y refresh.

### Base de Datos

```xml
<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- H2 Database (para pruebas) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- PostgreSQL (opcional) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Uso**: Conectores JDBC para diferentes bases de datos. MySQL es la base de datos principal, H2 para pruebas unitarias, PostgreSQL como opción alternativa.

### Validación

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Uso**: Proporciona validación de beans usando Hibernate Validator, implementando JSR-303/JSR-349. Permite anotaciones como `@Valid`, `@NotNull`, `@Size`, etc.

### Jackson para JSON

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-hibernate6</artifactId>
</dependency>
```

**Uso**: Extensión de Jackson para manejar tipos de Hibernate (Lazy loading, proxies) en la serialización/deserialización JSON.

### Lombok

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.36</version>
    <scope>provided</scope>
</dependency>
```

**Uso**: Librería que reduce código boilerplate generando getters, setters, constructores, toString, equals, hashCode automáticamente mediante anotaciones en tiempo de compilación.

### Swagger/OpenAPI

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

**Uso**: Genera documentación automática de la API REST usando OpenAPI 3.0. Proporciona interfaz Swagger UI para probar endpoints interactivamente.

### Desarrollo y Testing

```xml
<!-- Spring Boot DevTools -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

<!-- Spring Boot Test Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Uso**: DevTools proporciona recarga automática durante desarrollo. Test Starter incluye JUnit, Mockito, AssertJ para pruebas unitarias e integración.

## Configuración del Proyecto

### Java Version

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

**Uso**: El proyecto utiliza Java 21, aprovechando las últimas características del lenguaje y mejoras de rendimiento.

### Plugins Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>21</source>
        <target>21</target>
        <release>21</release>
    </configuration>
</plugin>

<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```

**Uso**: Compiler plugin configura la compilación con Java 21. Spring Boot plugin permite ejecutar la aplicación y empaquetarla como JAR ejecutable.

## Arquitectura y Patrón de Diseño

Las dependencias elegidas siguen los principios de Spring Boot:

1. **Convención sobre Configuración**: Spring Boot starters reducen configuración manual
2. **Modularidad**: Dependencias específicas para cada funcionalidad
3. **Productividad**: Herramientas como Lombok y DevTools aceleran desarrollo
4. **Mantenibilidad**: Versiones gestionadas por Spring Boot Bill of Materials (BOM)

## Conclusión

La selección de dependencias refleja una arquitectura moderna y robusta para una aplicación empresarial Java. Spring Boot como framework principal, combinado con librerías especializadas, permite desarrollar una API REST completa con seguridad, persistencia y documentación en menos tiempo y con menos configuración manual.
