#!/bin/bash

################################################################################
# Script de Backup de Base de Datos - FarmaYa
# Descripción: Realiza backup completo de la base de datos MySQL
# Autor: FarmaYa Team
# Fecha: Diciembre 2025
################################################################################

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # Sin color

# Configuración
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="${SCRIPT_DIR}/backups/database"
LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/backup-database-$(date +%Y%m%d).log"

# Configuración de Base de Datos
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-farmaya}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-Johan12315912}"

# Configuración de Retención
RETENTION_DAYS="${RETENTION_DAYS:-30}"

# Crear directorios si no existen
mkdir -p "$BACKUP_DIR"
mkdir -p "$LOG_DIR"

################################################################################
# Función: log_message
# Descripción: Registra mensajes en el log con timestamp
################################################################################
log_message() {
    local level=$1
    shift
    local message="$@"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[${timestamp}] [${level}] ${message}" | tee -a "$LOG_FILE"
}

################################################################################
# Función: print_header
# Descripción: Muestra un encabezado formateado
################################################################################
print_header() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
}

################################################################################
# Función: check_mysql_connection
# Descripción: Verifica la conexión a MySQL
################################################################################
check_mysql_connection() {
    log_message "INFO" "Verificando conexión a MySQL..."
    
    if ! command -v mysql &> /dev/null; then
        log_message "ERROR" "MySQL client no está instalado"
        return 1
    fi
    
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME" 2>/dev/null; then
        log_message "ERROR" "No se puede conectar a la base de datos"
        return 1
    fi
    
    log_message "INFO" "Conexión a MySQL exitosa"
    return 0
}

################################################################################
# Función: backup_database
# Descripción: Realiza el backup de la base de datos
################################################################################
backup_database() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="${BACKUP_DIR}/farmaya_backup_${timestamp}.sql"
    local backup_compressed="${backup_file}.gz"
    
    print_header "INICIANDO BACKUP DE BASE DE DATOS"
    
    log_message "INFO" "Iniciando backup de la base de datos: $DB_NAME"
    log_message "INFO" "Archivo destino: $backup_file"
    
    # Realizar el backup
    if mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        --complete-insert \
        --add-drop-database \
        --databases "$DB_NAME" > "$backup_file" 2>> "$LOG_FILE"; then
        
        log_message "INFO" "Backup creado exitosamente"
        
        # Comprimir el backup
        log_message "INFO" "Comprimiendo backup..."
        if gzip -f "$backup_file"; then
            local size=$(du -h "$backup_compressed" | cut -f1)
            log_message "INFO" "Backup comprimido exitosamente: $size"
            echo -e "${GREEN}✓ Backup completado: $(basename $backup_compressed) ($size)${NC}"
        else
            log_message "ERROR" "Error al comprimir el backup"
            return 1
        fi
        
        # Calcular checksum
        local checksum=$(md5sum "$backup_compressed" | cut -d' ' -f1)
        echo "$checksum" > "${backup_compressed}.md5"
        log_message "INFO" "Checksum MD5: $checksum"
        
    else
        log_message "ERROR" "Error al crear el backup"
        return 1
    fi
}

################################################################################
# Función: backup_database_structure
# Descripción: Backup solo de la estructura de la base de datos
################################################################################
backup_database_structure() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="${BACKUP_DIR}/farmaya_structure_${timestamp}.sql"
    
    log_message "INFO" "Creando backup de estructura..."
    
    if mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        --no-data \
        --routines \
        --triggers \
        --events \
        "$DB_NAME" > "$backup_file" 2>> "$LOG_FILE"; then
        
        gzip -f "$backup_file"
        log_message "INFO" "Backup de estructura creado: $(basename $backup_file).gz"
    else
        log_message "ERROR" "Error al crear backup de estructura"
    fi
}

################################################################################
# Función: get_database_info
# Descripción: Obtiene información de la base de datos
################################################################################
get_database_info() {
    log_message "INFO" "Obteniendo información de la base de datos..."
    
    local db_size=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e \
        "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)' \
         FROM information_schema.TABLES \
         WHERE table_schema = '$DB_NAME';" -sN 2>/dev/null)
    
    local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e \
        "SELECT COUNT(*) FROM information_schema.TABLES WHERE table_schema = '$DB_NAME';" -sN 2>/dev/null)
    
    echo -e "${BLUE}Información de la Base de Datos:${NC}"
    echo -e "  • Base de datos: ${GREEN}$DB_NAME${NC}"
    echo -e "  • Tamaño: ${GREEN}${db_size} MB${NC}"
    echo -e "  • Número de tablas: ${GREEN}${table_count}${NC}"
    
    log_message "INFO" "Tamaño DB: ${db_size} MB, Tablas: ${table_count}"
}

################################################################################
# Función: cleanup_old_backups
# Descripción: Elimina backups antiguos según la política de retención
################################################################################
cleanup_old_backups() {
    print_header "LIMPIEZA DE BACKUPS ANTIGUOS"
    
    log_message "INFO" "Buscando backups antiguos (> $RETENTION_DAYS días)..."
    
    local deleted_count=0
    while IFS= read -r file; do
        log_message "INFO" "Eliminando backup antiguo: $(basename "$file")"
        rm -f "$file" "${file}.md5"
        ((deleted_count++))
    done < <(find "$BACKUP_DIR" -name "farmaya_backup_*.sql.gz" -type f -mtime +$RETENTION_DAYS)
    
    if [ $deleted_count -gt 0 ]; then
        echo -e "${YELLOW}✓ Se eliminaron $deleted_count backup(s) antiguo(s)${NC}"
        log_message "INFO" "Se eliminaron $deleted_count backups antiguos"
    else
        echo -e "${GREEN}✓ No hay backups antiguos para eliminar${NC}"
        log_message "INFO" "No hay backups antiguos para eliminar"
    fi
}

################################################################################
# Función: list_backups
# Descripción: Lista todos los backups disponibles
################################################################################
list_backups() {
    print_header "BACKUPS DISPONIBLES"
    
    if [ ! "$(ls -A $BACKUP_DIR/*.sql.gz 2>/dev/null)" ]; then
        echo -e "${YELLOW}No hay backups disponibles${NC}"
        return
    fi
    
    echo -e "${BLUE}Backups encontrados:${NC}\n"
    
    local count=1
    for backup in $(ls -t "$BACKUP_DIR"/farmaya_backup_*.sql.gz 2>/dev/null); do
        local size=$(du -h "$backup" | cut -f1)
        local date=$(stat -c %y "$backup" | cut -d' ' -f1,2 | cut -d'.' -f1)
        local checksum_file="${backup}.md5"
        local checksum_status="❌"
        
        if [ -f "$checksum_file" ]; then
            checksum_status="✓"
        fi
        
        echo -e "  ${count}. $(basename $backup)"
        echo -e "     Tamaño: ${size} | Fecha: ${date} | Checksum: ${checksum_status}"
        echo ""
        ((count++))
    done
}

################################################################################
# Función: generate_report
# Descripción: Genera un reporte del backup
################################################################################
generate_report() {
    local report_file="${BACKUP_DIR}/backup_report_$(date +%Y%m%d).txt"
    
    cat > "$report_file" << EOF
═══════════════════════════════════════════════════════════════
  REPORTE DE BACKUP - FARMAYA
  Fecha: $(date '+%Y-%m-%d %H:%M:%S')
═══════════════════════════════════════════════════════════════

INFORMACIÓN DE LA BASE DE DATOS:
  • Host: $DB_HOST:$DB_PORT
  • Base de datos: $DB_NAME
  
BACKUPS REALIZADOS:
$(ls -lh "$BACKUP_DIR"/farmaya_backup_$(date +%Y%m%d)_*.sql.gz 2>/dev/null | awk '{print "  • " $9 " - " $5}')

ESTADÍSTICAS:
  • Total de backups: $(ls -1 "$BACKUP_DIR"/farmaya_backup_*.sql.gz 2>/dev/null | wc -l)
  • Espacio usado: $(du -sh "$BACKUP_DIR" | cut -f1)
  • Política de retención: $RETENTION_DAYS días

═══════════════════════════════════════════════════════════════
EOF

    log_message "INFO" "Reporte generado: $report_file"
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    print_header "BACKUP DE BASE DE DATOS FARMAYA"
    
    echo -e "${BLUE}Fecha y hora: $(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    
    # Verificar conexión
    if ! check_mysql_connection; then
        echo -e "${RED}✗ Error: No se puede conectar a la base de datos${NC}"
        exit 1
    fi
    
    # Mostrar información de la BD
    get_database_info
    echo ""
    
    # Realizar backup completo
    if ! backup_database; then
        echo -e "${RED}✗ Error al realizar el backup${NC}"
        exit 1
    fi
    
    # Realizar backup de estructura (opcional)
    backup_database_structure
    
    # Limpiar backups antiguos
    cleanup_old_backups
    
    # Listar backups
    list_backups
    
    # Generar reporte
    generate_report
    
    print_header "BACKUP COMPLETADO EXITOSAMENTE"
    
    log_message "INFO" "Proceso de backup finalizado exitosamente"
}

# Ejecutar función principal
main "$@"
