# 📋 Sistema de Mantenimiento FarmaYa

## 📖 Índice

- [Descripción General](#descripción-general)
- [Estructura del Sistema](#estructura-del-sistema)
- [Scripts Disponibles](#scripts-disponibles)
- [Guía de Uso](#guía-de-uso)
- [Configuración](#configuración)
- [Políticas de Retención](#políticas-de-retención)
- [Mejores Prácticas](#mejores-prácticas)
- [Automatización](#automatización)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Descripción General

El Sistema de Mantenimiento de FarmaYa es una solución integral que proporciona herramientas automatizadas para el mantenimiento, backup y monitoreo de la aplicación. Este sistema implementa las mejores prácticas de la industria para asegurar la continuidad del servicio, la integridad de los datos y el rendimiento óptimo del sistema.

### ✨ Características Principales

- ✅ **Backups Automatizados**: Respaldo completo de base de datos y archivos
- ✅ **Restauración Confiable**: Proceso guiado de restauración con verificación de integridad
- ✅ **Mantenimiento de Base de Datos**: Optimización, análisis y verificación de integridad
- ✅ **Limpieza del Sistema**: Gestión de logs, archivos temporales y recursos
- ✅ **Monitoreo en Tiempo Real**: Supervisión continua de servicios y recursos
- ✅ **Reportes Detallados**: Generación automática de reportes de estado
- ✅ **Interfaz Interactiva**: Menú centralizado para todas las operaciones

---

## 📁 Estructura del Sistema

```
maintenance/
├── maintenance.sh                  # Script maestro (menú principal)
├── backup-database.sh             # Backup de base de datos
├── backup-files.sh                # Backup de archivos del proyecto
├── restore-database.sh            # Restauración de base de datos
├── maintenance-database.sh        # Mantenimiento de BD
├── cleanup-system.sh              # Limpieza del sistema
├── monitor-system.sh              # Monitoreo del sistema
├── backups/                       # Directorio de backups
│   ├── database/                  # Backups de base de datos
│   │   ├── farmaya_backup_*.sql.gz
│   │   ├── farmaya_structure_*.sql.gz
│   │   └── safety_backup_*.sql.gz
│   └── files/                     # Backups de archivos
│       ├── farmaya_files_*.tar.gz
│       └── farmaya_config_*.tar.gz
└── logs/                          # Logs de mantenimiento
    ├── backup-database-*.log
    ├── backup-files-*.log
    ├── restore-database-*.log
    ├── maintenance-db-*.log
    ├── cleanup-*.log
    ├── monitor-*.log
    └── maintenance-master-*.log
```

---

## 🛠️ Scripts Disponibles

### 1. 🎮 `maintenance.sh` - Script Maestro

Script principal que proporciona un menú interactivo para acceder a todas las funcionalidades del sistema.

**Uso:**

```bash
cd maintenance
chmod +x maintenance.sh
./maintenance.sh
```

**Funcionalidades:**

- Backup completo o parcial
- Restauración de base de datos
- Mantenimiento completo
- Monitoreo del sistema
- Generación de reportes
- Mantenimiento automático programable

---

### 2. 💾 `backup-database.sh` - Backup de Base de Datos

Realiza backups completos de la base de datos MySQL con compresión y verificación de integridad.

**Características:**

- ✅ Backup completo con todas las tablas, rutinas, triggers y eventos
- ✅ Compresión automática (gzip)
- ✅ Generación de checksums MD5 para verificación
- ✅ Backup adicional solo de estructura
- ✅ Limpieza automática de backups antiguos
- ✅ Reportes detallados

**Uso:**

```bash
./backup-database.sh
```

**Configuración mediante variables de entorno:**

```bash
DB_HOST=localhost \
DB_PORT=3306 \
DB_NAME=farmaya \
DB_USER=root \
DB_PASSWORD=your_password \
RETENTION_DAYS=30 \
./backup-database.sh
```

**Archivos generados:**

- `farmaya_backup_YYYYMMDD_HHMMSS.sql.gz` - Backup completo comprimido
- `farmaya_backup_YYYYMMDD_HHMMSS.sql.gz.md5` - Checksum MD5
- `farmaya_structure_YYYYMMDD_HHMMSS.sql.gz` - Solo estructura
- `backup_report_YYYYMMDD.txt` - Reporte del backup

---

### 3. 📦 `backup-files.sh` - Backup de Archivos

Realiza backups de archivos importantes del proyecto incluyendo código fuente, configuraciones y recursos.

**Directorios respaldados:**

- `backend/src` - Código fuente del backend
- `backend/pom.xml` - Configuración de Maven
- `backend/Dockerfile` - Configuración de Docker
- `frontend/farmacia-merysalud` - Frontend completo
- Archivos de configuración del proyecto

**Patrones excluidos:**

- `*/target/*` - Archivos compilados de Maven
- `*/node_modules/*` - Dependencias de Node.js
- `*/.git/*` - Repositorio Git
- `*/logs/*` - Logs
- Archivos temporales

**Uso:**

```bash
./backup-files.sh
```

**Archivos generados:**

- `farmaya_files_YYYYMMDD_HHMMSS.tar.gz` - Backup completo
- `farmaya_config_YYYYMMDD_HHMMSS.tar.gz` - Solo configuraciones
- `manifest_YYYYMMDD_HHMMSS.txt` - Manifiesto del backup

---

### 4. ♻️ `restore-database.sh` - Restauración de Base de Datos

Restaura backups de la base de datos con verificación de integridad y backup de seguridad.

**Características:**

- ✅ Listado interactivo de backups disponibles
- ✅ Verificación de checksums antes de restaurar
- ✅ Backup de seguridad automático antes de restaurar
- ✅ Verificación post-restauración
- ✅ Proceso guiado con confirmaciones

**Uso:**

```bash
./restore-database.sh
```

**Proceso:**

1. Seleccionar backup de la lista
2. Confirmar la operación
3. Verificar integridad del backup
4. Crear backup de seguridad
5. Restaurar base de datos
6. Verificar restauración exitosa

---

### 5. 🔧 `maintenance-database.sh` - Mantenimiento de Base de Datos

Ejecuta tareas de mantenimiento y optimización de la base de datos MySQL.

**Operaciones realizadas:**

- ✅ Verificación de integridad de tablas (CHECK TABLE)
- ✅ Análisis de tablas (ANALYZE TABLE)
- ✅ Optimización de tablas (OPTIMIZE TABLE)
- ✅ Actualización de estadísticas
- ✅ Limpieza de sesiones expiradas
- ✅ Generación de reportes de estado

**Uso:**

```bash
# Mantenimiento completo
./maintenance-database.sh

# Solo reparación de tablas
./maintenance-database.sh --repair
```

**Beneficios:**

- Mejora el rendimiento de consultas
- Recupera espacio en disco
- Previene corrupción de datos
- Actualiza índices y estadísticas

---

### 6. 🧹 `cleanup-system.sh` - Limpieza del Sistema

Limpia logs antiguos, archivos temporales y optimiza el espacio en disco.

**Operaciones de limpieza:**

- ✅ Eliminación de logs antiguos (>30 días)
- ✅ Compresión de logs grandes (>100 MB)
- ✅ Rotación de logs activos
- ✅ Limpieza de caché de Maven
- ✅ Eliminación de archivos temporales
- ✅ Limpieza de recursos Docker
- ✅ Análisis de uso de disco

**Uso:**

```bash
# Limpieza completa
./cleanup-system.sh

# Solo logs
./cleanup-system.sh --logs-only

# Solo Docker
./cleanup-system.sh --docker-only

# Solo análisis
./cleanup-system.sh --analyze
```

**Archivos temporales eliminados:**

- `*.tmp`, `*.temp` - Archivos temporales
- `*~` - Backups de editores
- `.DS_Store`, `Thumbs.db` - Archivos del sistema
- `*.swp`, `*.swo` - Archivos de Vim

---

### 7. 📊 `monitor-system.sh` - Monitoreo del Sistema

Monitorea el estado de servicios, recursos del sistema y genera alertas.

**Componentes monitoreados:**

- ✅ **Backend**: Disponibilidad y tiempo de respuesta
- ✅ **Base de Datos**: Conexiones activas y tamaño
- ✅ **Docker**: Estado de contenedores
- ✅ **Disco**: Uso de espacio (umbral: 85%)
- ✅ **Memoria**: Uso de RAM (umbral: 80%)
- ✅ **CPU**: Carga del sistema (umbral: 80%)
- ✅ **Puertos**: Disponibilidad de puertos críticos
- ✅ **Logs**: Análisis de errores recientes

**Uso:**

```bash
# Monitoreo único
./monitor-system.sh

# Monitoreo continuo (cada 30 segundos)
./monitor-system.sh --continuous

# Solo servicios
./monitor-system.sh --services-only

# Solo recursos
./monitor-system.sh --resources-only
```

**Reportes generados:**

- `health_report_YYYYMMDD_HHMMSS.txt` - Reporte de salud del sistema

---

## 📚 Guía de Uso

### 🚀 Inicio Rápido

#### 1. Preparación Inicial

```bash
# Navegar al directorio de mantenimiento
cd /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance

# Dar permisos de ejecución a todos los scripts
chmod +x *.sh

# Verificar la configuración
./monitor-system.sh --services-only
```

#### 2. Primer Backup

```bash
# Ejecutar el script maestro
./maintenance.sh

# Seleccionar opción 1: Backup completo
# O ejecutar directamente:
./backup-database.sh
./backup-files.sh
```

#### 3. Mantenimiento Regular

```bash
# Ejecutar mantenimiento automático (recomendado)
./maintenance.sh
# Seleccionar opción 14: Mantenimiento automático
```

---

### 🔄 Flujos de Trabajo Comunes

#### Rutina de Mantenimiento Semanal

```bash
#!/bin/bash
# Script: weekly-maintenance.sh

# 1. Backup completo
echo "Realizando backup..."
./backup-database.sh
./backup-files.sh

# 2. Mantenimiento de BD
echo "Optimizando base de datos..."
./maintenance-database.sh

# 3. Limpieza
echo "Limpiando sistema..."
./cleanup-system.sh

# 4. Verificación
echo "Verificando estado..."
./monitor-system.sh

echo "Mantenimiento semanal completado!"
```

#### Recuperación ante Desastres

```bash
# 1. Verificar backups disponibles
./restore-database.sh
# (Cancelar para ver lista)

# 2. Detener servicios
docker-compose down

# 3. Restaurar base de datos
./restore-database.sh
# Seleccionar el backup más reciente

# 4. Verificar integridad
./maintenance-database.sh

# 5. Reiniciar servicios
docker-compose up -d

# 6. Monitorear
./monitor-system.sh --continuous
```

---

## ⚙️ Configuración

### Variables de Entorno

Crea un archivo `.env` en el directorio `maintenance/` para configurar las variables:

```bash
# Configuración de Base de Datos
DB_HOST=localhost
DB_PORT=3306
DB_NAME=farmaya
DB_USER=root
DB_PASSWORD=Johan12315912

# Configuración de Backups
RETENTION_DAYS=30
MAX_LOG_SIZE_MB=100

# Configuración de URLs
BACKEND_URL=http://localhost:8080
FRONTEND_URL=http://localhost:3000

# Umbrales de Monitoreo
CPU_THRESHOLD=80
MEMORY_THRESHOLD=80
DISK_THRESHOLD=85
```

### Cargar Variables de Entorno

```bash
# En cada script, agregar al inicio:
if [ -f "${SCRIPT_DIR}/.env" ]; then
    source "${SCRIPT_DIR}/.env"
fi
```

---

## 📅 Políticas de Retención

### Backups de Base de Datos

| Tipo              | Frecuencia | Retención  | Almacenamiento    |
| ----------------- | ---------- | ---------- | ----------------- |
| Completo          | Diario     | 30 días    | Comprimido (gzip) |
| Estructura        | Semanal    | 90 días    | Comprimido (gzip) |
| Pre-actualización | Manual     | Permanente | Comprimido (gzip) |

### Backups de Archivos

| Tipo          | Frecuencia | Retención | Almacenamiento |
| ------------- | ---------- | --------- | -------------- |
| Completo      | Semanal    | 30 días   | Tar + gzip     |
| Configuración | Diario     | 60 días   | Tar + gzip     |

### Logs

| Tipo                  | Retención | Rotación              |
| --------------------- | --------- | --------------------- |
| Logs de aplicación    | 30 días   | Cuando superan 100 MB |
| Logs de mantenimiento | 30 días   | Cuando superan 100 MB |
| Logs de errores       | 90 días   | Cuando superan 50 MB  |

---

## 🎯 Mejores Prácticas

### 1. Backups

✅ **Hacer:**

- Realizar backups antes de actualizaciones importantes
- Verificar la integridad de los backups regularmente
- Almacenar backups críticos fuera del servidor
- Probar el proceso de restauración periódicamente
- Mantener al menos 3 copias de backups críticos

❌ **Evitar:**

- Ejecutar backups durante horarios de alta carga
- Almacenar backups solo en el mismo servidor
- Ignorar las verificaciones de checksum
- Eliminar backups sin verificar la integridad de los nuevos

### 2. Mantenimiento de Base de Datos

✅ **Hacer:**

- Ejecutar ANALYZE TABLE después de cambios masivos
- Optimizar tablas durante horarios de baja actividad
- Monitorear el crecimiento de tablas
- Mantener estadísticas actualizadas

❌ **Evitar:**

- Ejecutar OPTIMIZE TABLE en producción sin probar
- Ignorar avisos de corrupción de tablas
- Posponer el mantenimiento preventivo

### 3. Limpieza del Sistema

✅ **Hacer:**

- Establecer políticas claras de retención de logs
- Comprimir logs antes de archivarlos
- Limpiar archivos temporales regularmente
- Monitorear el uso de disco constantemente

❌ **Evitar:**

- Eliminar todos los logs sin revisar
- Llenar el disco al 100%
- Ignorar el crecimiento de directorios temporales

### 4. Monitoreo

✅ **Hacer:**

- Revisar los reportes de monitoreo diariamente
- Configurar alertas para umbrales críticos
- Documentar incidentes y resoluciones
- Mantener un histórico de métricas

❌ **Evitar:**

- Ignorar alertas recurrentes
- No investigar degradaciones de rendimiento
- Desactivar el monitoreo durante mantenimiento

---

## 🤖 Automatización

### Cron Jobs Recomendados

Edita el crontab:

```bash
crontab -e
```

Agrega las siguientes líneas:

```bash
# Backup diario de base de datos a las 2:00 AM
0 2 * * * /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/backup-database.sh >> /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/logs/cron-backup-db.log 2>&1

# Backup semanal de archivos los domingos a las 3:00 AM
0 3 * * 0 /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/backup-files.sh >> /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/logs/cron-backup-files.log 2>&1

# Mantenimiento de base de datos los lunes a las 4:00 AM
0 4 * * 1 /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/maintenance-database.sh >> /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/logs/cron-maintenance-db.log 2>&1

# Limpieza del sistema diaria a las 5:00 AM
0 5 * * * /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/cleanup-system.sh >> /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/logs/cron-cleanup.log 2>&1

# Monitoreo cada hora
0 * * * * /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/monitor-system.sh --services-only >> /home/johann/Documents/FarmaYa/FARMAYAA/FarmaYa/maintenance/logs/cron-monitor.log 2>&1
```

### Script de Automatización Completo

Crea un archivo `automated-maintenance.sh`:

```bash
#!/bin/bash

################################################################################
# Mantenimiento Automatizado - FarmaYa
# Se ejecuta automáticamente vía cron
################################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_FILE="${SCRIPT_DIR}/logs/automated-$(date +%Y%m%d).log"

# Función de logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=== Iniciando mantenimiento automatizado ==="

# Backup de base de datos
log "Ejecutando backup de base de datos..."
"${SCRIPT_DIR}/backup-database.sh" >> "$LOG_FILE" 2>&1
if [ $? -eq 0 ]; then
    log "✓ Backup de BD completado"
else
    log "✗ Error en backup de BD"
fi

# Mantenimiento de BD (solo lunes)
if [ $(date +%u) -eq 1 ]; then
    log "Ejecutando mantenimiento de base de datos..."
    "${SCRIPT_DIR}/maintenance-database.sh" >> "$LOG_FILE" 2>&1
    if [ $? -eq 0 ]; then
        log "✓ Mantenimiento de BD completado"
    else
        log "✗ Error en mantenimiento de BD"
    fi
fi

# Limpieza del sistema
log "Ejecutando limpieza del sistema..."
"${SCRIPT_DIR}/cleanup-system.sh" --logs-only >> "$LOG_FILE" 2>&1
if [ $? -eq 0 ]; then
    log "✓ Limpieza completada"
else
    log "✗ Error en limpieza"
fi

# Monitoreo
log "Ejecutando monitoreo..."
"${SCRIPT_DIR}/monitor-system.sh" --services-only >> "$LOG_FILE" 2>&1

log "=== Mantenimiento automatizado completado ==="
```

---

## 🔧 Troubleshooting

### Problema: "Permission denied" al ejecutar scripts

**Solución:**

```bash
chmod +x maintenance/*.sh
```

### Problema: "Cannot connect to MySQL server"

**Solución:**

1. Verificar que MySQL esté corriendo:

   ```bash
   sudo systemctl status mysql
   # O para Docker:
   docker ps | grep mysql
   ```

2. Verificar credenciales en las variables de entorno

3. Probar conexión manualmente:
   ```bash
   mysql -h localhost -P 3306 -u root -p
   ```

### Problema: Backup falla por falta de espacio

**Solución:**

1. Verificar espacio disponible:

   ```bash
   df -h
   ```

2. Limpiar backups antiguos:

   ```bash
   ./cleanup-system.sh
   ```

3. Comprimir manualmente logs grandes:
   ```bash
   find maintenance/logs -name "*.log" -size +100M -exec gzip {} \;
   ```

### Problema: Restauración falla por checksum inválido

**Solución:**

1. Verificar integridad del backup:

   ```bash
   md5sum farmaya_backup_*.sql.gz
   cat farmaya_backup_*.sql.gz.md5
   ```

2. Si el backup está corrupto, usar un backup anterior

3. Regenerar checksum si es necesario:
   ```bash
   md5sum farmaya_backup_*.sql.gz > farmaya_backup_*.sql.gz.md5
   ```

### Problema: Scripts se quedan en "running" indefinidamente

**Solución:**

1. Verificar procesos:

   ```bash
   ps aux | grep maintenance
   ```

2. Si es necesario, terminar procesos:

   ```bash
   pkill -f maintenance
   ```

3. Revisar logs para identificar el problema

---

## 📞 Soporte y Contacto

Para problemas o sugerencias relacionadas con el sistema de mantenimiento:

- **Documentación**: Este archivo README.md
- **Logs**: Revisar `maintenance/logs/` para detalles de errores
- **Reportes**: Generar reporte completo con opción 13 del menú principal

---

## 📝 Changelog

### Versión 1.0.0 (Diciembre 2025)

- ✨ Lanzamiento inicial del sistema de mantenimiento
- ✅ Implementación de backups de BD y archivos
- ✅ Sistema de restauración con verificación
- ✅ Mantenimiento y optimización de BD
- ✅ Limpieza automatizada del sistema
- ✅ Monitoreo en tiempo real
- ✅ Script maestro con menú interactivo
- ✅ Documentación completa

---

## 📄 Licencia

Este sistema de mantenimiento es parte del proyecto FarmaYa y está sujeto a la misma licencia del proyecto principal.

---

**FarmaYa Team** - Sistema de Mantenimiento Integral v1.0.0
