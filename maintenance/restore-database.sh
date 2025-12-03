#!/bin/bash

################################################################################
# Script de Restauración de Base de Datos - FarmaYa
# Descripción: Restaura backups de la base de datos MySQL
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
LOG_FILE="${LOG_DIR}/restore-database-$(date +%Y%m%d).log"

# Configuración de Base de Datos
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-farmaya}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-Johan12315912}"

# Crear directorios si no existen
mkdir -p "$LOG_DIR"

################################################################################
# Función: log_message
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
################################################################################
print_header() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
}

################################################################################
# Función: list_backups
################################################################################
list_backups() {
    print_header "BACKUPS DISPONIBLES PARA RESTAURACIÓN"
    
    if [ ! "$(ls -A $BACKUP_DIR/*.sql.gz 2>/dev/null)" ]; then
        echo -e "${RED}✗ No hay backups disponibles${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}Selecciona el backup a restaurar:${NC}\n"
    
    local count=1
    declare -g -A backup_files
    
    for backup in $(ls -t "$BACKUP_DIR"/farmaya_backup_*.sql.gz 2>/dev/null); do
        local size=$(du -h "$backup" | cut -f1)
        local date=$(stat -c %y "$backup" | cut -d' ' -f1,2 | cut -d'.' -f1)
        
        backup_files[$count]="$backup"
        echo -e "  ${count}. $(basename $backup)"
        echo -e "     Tamaño: ${size} | Fecha: ${date}"
        echo ""
        ((count++))
    done
    
    echo -e "  0. Cancelar\n"
}

################################################################################
# Función: verify_checksum
################################################################################
verify_checksum() {
    local backup_file=$1
    local checksum_file="${backup_file}.md5"
    
    if [ ! -f "$checksum_file" ]; then
        log_message "WARN" "No se encontró archivo de checksum"
        return 1
    fi
    
    log_message "INFO" "Verificando integridad del backup..."
    
    local expected_checksum=$(cat "$checksum_file")
    local actual_checksum=$(md5sum "$backup_file" | cut -d' ' -f1)
    
    if [ "$expected_checksum" = "$actual_checksum" ]; then
        echo -e "${GREEN}✓ Verificación de integridad exitosa${NC}"
        log_message "INFO" "Checksum verificado correctamente"
        return 0
    else
        echo -e "${RED}✗ Error: El checksum no coincide. El backup puede estar corrupto${NC}"
        log_message "ERROR" "Checksum no coincide"
        return 1
    fi
}

################################################################################
# Función: create_safety_backup
################################################################################
create_safety_backup() {
    print_header "CREANDO BACKUP DE SEGURIDAD"
    
    log_message "INFO" "Creando backup de seguridad antes de restaurar..."
    
    local safety_backup="${BACKUP_DIR}/safety_backup_$(date +%Y%m%d_%H%M%S).sql.gz"
    
    if mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        --single-transaction --databases "$DB_NAME" 2>> "$LOG_FILE" | gzip > "$safety_backup"; then
        
        echo -e "${GREEN}✓ Backup de seguridad creado: $(basename $safety_backup)${NC}"
        log_message "INFO" "Backup de seguridad creado exitosamente"
        return 0
    else
        echo -e "${YELLOW}⚠ No se pudo crear backup de seguridad${NC}"
        log_message "WARN" "No se pudo crear backup de seguridad"
        return 1
    fi
}

################################################################################
# Función: restore_database
################################################################################
restore_database() {
    local backup_file=$1
    local temp_file="/tmp/farmaya_restore_$(date +%Y%m%d_%H%M%S).sql"
    
    print_header "RESTAURANDO BASE DE DATOS"
    
    log_message "INFO" "Iniciando proceso de restauración..."
    log_message "INFO" "Backup seleccionado: $(basename $backup_file)"
    
    # Descomprimir backup
    echo -e "${BLUE}Descomprimiendo backup...${NC}"
    if gunzip -c "$backup_file" > "$temp_file" 2>> "$LOG_FILE"; then
        echo -e "${GREEN}✓ Backup descomprimido${NC}"
        log_message "INFO" "Backup descomprimido exitosamente"
    else
        echo -e "${RED}✗ Error al descomprimir el backup${NC}"
        log_message "ERROR" "Error al descomprimir el backup"
        rm -f "$temp_file"
        return 1
    fi
    
    # Restaurar base de datos
    echo -e "${BLUE}Restaurando base de datos...${NC}"
    echo -e "${YELLOW}⚠ Este proceso puede tomar varios minutos...${NC}"
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" < "$temp_file" 2>> "$LOG_FILE"; then
        echo -e "${GREEN}✓ Base de datos restaurada exitosamente${NC}"
        log_message "INFO" "Base de datos restaurada exitosamente"
        rm -f "$temp_file"
        return 0
    else
        echo -e "${RED}✗ Error al restaurar la base de datos${NC}"
        log_message "ERROR" "Error al restaurar la base de datos"
        rm -f "$temp_file"
        return 1
    fi
}

################################################################################
# Función: verify_restoration
################################################################################
verify_restoration() {
    print_header "VERIFICANDO RESTAURACIÓN"
    
    log_message "INFO" "Verificando tablas restauradas..."
    
    local table_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e \
        "SELECT COUNT(*) FROM information_schema.TABLES WHERE table_schema = '$DB_NAME';" -sN 2>/dev/null)
    
    local db_size=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e \
        "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)' \
         FROM information_schema.TABLES \
         WHERE table_schema = '$DB_NAME';" -sN 2>/dev/null)
    
    echo -e "${BLUE}Estado de la Base de Datos:${NC}"
    echo -e "  • Base de datos: ${GREEN}$DB_NAME${NC}"
    echo -e "  • Número de tablas: ${GREEN}${table_count}${NC}"
    echo -e "  • Tamaño: ${GREEN}${db_size} MB${NC}"
    
    log_message "INFO" "Tablas: ${table_count}, Tamaño: ${db_size} MB"
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    print_header "RESTAURACIÓN DE BASE DE DATOS FARMAYA"
    
    echo -e "${BLUE}Fecha y hora: $(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    
    # Listar backups disponibles
    list_backups
    
    # Solicitar selección
    read -p "Ingresa el número del backup a restaurar: " selection
    
    if [ "$selection" = "0" ]; then
        echo -e "${YELLOW}Operación cancelada${NC}"
        exit 0
    fi
    
    if [ -z "${backup_files[$selection]}" ]; then
        echo -e "${RED}✗ Selección inválida${NC}"
        exit 1
    fi
    
    local selected_backup="${backup_files[$selection]}"
    
    echo ""
    echo -e "${YELLOW}⚠ ADVERTENCIA:${NC}"
    echo -e "${YELLOW}  Esta operación sobrescribirá la base de datos actual.${NC}"
    echo -e "${YELLOW}  Backup seleccionado: $(basename $selected_backup)${NC}"
    echo ""
    read -p "¿Estás seguro de continuar? (escribe 'SI' para confirmar): " confirm
    
    if [ "$confirm" != "SI" ]; then
        echo -e "${YELLOW}Operación cancelada${NC}"
        exit 0
    fi
    
    # Verificar integridad
    if ! verify_checksum "$selected_backup"; then
        read -p "¿Continuar de todos modos? (s/n): " force_continue
        if [ "$force_continue" != "s" ]; then
            echo -e "${YELLOW}Operación cancelada${NC}"
            exit 1
        fi
    fi
    
    # Crear backup de seguridad
    create_safety_backup
    
    # Restaurar base de datos
    if restore_database "$selected_backup"; then
        verify_restoration
        print_header "RESTAURACIÓN COMPLETADA EXITOSAMENTE"
        log_message "INFO" "Proceso de restauración finalizado exitosamente"
    else
        echo -e "${RED}✗ La restauración falló${NC}"
        log_message "ERROR" "La restauración falló"
        exit 1
    fi
}

# Ejecutar función principal
main "$@"
