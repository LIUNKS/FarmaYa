#!/bin/bash

################################################################################
# Script de Backup de Archivos - FarmaYa
# Descripción: Realiza backup de archivos importantes del proyecto
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
BACKUP_DIR="${SCRIPT_DIR}/backups/files"
LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/backup-files-$(date +%Y%m%d).log"

# Configuración de Retención
RETENTION_DAYS="${RETENTION_DAYS:-30}"

# Directorios y archivos a respaldar
BACKUP_TARGETS=(
    "backend/src"
    "backend/pom.xml"
    "backend/Dockerfile"
    "frontend/farmacia-merysalud"
    "docker-compose.yml"
    "README.md"
    "librerias-dependencias.md"
    "principios-diseno.md"
)

# Directorios a excluir
EXCLUDE_PATTERNS=(
    "*/target/*"
    "*/node_modules/*"
    "*/.git/*"
    "*/logs/*"
    "*/*.log"
    "*/.DS_Store"
    "*/thumbs.db"
)

# Crear directorios si no existen
mkdir -p "$BACKUP_DIR"
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
# Función: check_dependencies
################################################################################
check_dependencies() {
    log_message "INFO" "Verificando dependencias..."
    
    if ! command -v tar &> /dev/null; then
        log_message "ERROR" "tar no está instalado"
        return 1
    fi
    
    if ! command -v gzip &> /dev/null; then
        log_message "ERROR" "gzip no está instalado"
        return 1
    fi
    
    log_message "INFO" "Dependencias verificadas exitosamente"
    return 0
}

################################################################################
# Función: calculate_project_size
################################################################################
calculate_project_size() {
    log_message "INFO" "Calculando tamaño del proyecto..."
    
    local total_size=0
    for target in "${BACKUP_TARGETS[@]}"; do
        local path="${PROJECT_ROOT}/${target}"
        if [ -e "$path" ]; then
            local size=$(du -sb "$path" 2>/dev/null | cut -f1)
            total_size=$((total_size + size))
        fi
    done
    
    # Convertir a formato legible
    if [ $total_size -gt 1073741824 ]; then
        echo "$(echo "scale=2; $total_size / 1073741824" | bc) GB"
    elif [ $total_size -gt 1048576 ]; then
        echo "$(echo "scale=2; $total_size / 1048576" | bc) MB"
    else
        echo "$(echo "scale=2; $total_size / 1024" | bc) KB"
    fi
}

################################################################################
# Función: backup_files
################################################################################
backup_files() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_file="${BACKUP_DIR}/farmaya_files_${timestamp}.tar.gz"
    
    print_header "INICIANDO BACKUP DE ARCHIVOS"
    
    log_message "INFO" "Iniciando backup de archivos del proyecto"
    log_message "INFO" "Archivo destino: $backup_file"
    
    # Construir parámetros de exclusión
    local exclude_params=""
    for pattern in "${EXCLUDE_PATTERNS[@]}"; do
        exclude_params="$exclude_params --exclude=$pattern"
    done
    
    # Crear archivo temporal con la lista de archivos
    local file_list="/tmp/farmaya_backup_list_${timestamp}.txt"
    > "$file_list"
    
    echo -e "${BLUE}Preparando archivos para backup:${NC}\n"
    
    for target in "${BACKUP_TARGETS[@]}"; do
        local path="${PROJECT_ROOT}/${target}"
        if [ -e "$path" ]; then
            echo "$target" >> "$file_list"
            echo -e "  ✓ ${GREEN}${target}${NC}"
            log_message "INFO" "Incluido: $target"
        else
            echo -e "  ✗ ${YELLOW}${target} (no encontrado)${NC}"
            log_message "WARN" "No encontrado: $target"
        fi
    done
    
    echo ""
    
    # Realizar el backup
    log_message "INFO" "Creando archivo comprimido..."
    echo -e "${BLUE}Comprimiendo archivos...${NC}"
    
    if tar -czf "$backup_file" \
        -C "$PROJECT_ROOT" \
        $exclude_params \
        --files-from="$file_list" 2>> "$LOG_FILE"; then
        
        local size=$(du -h "$backup_file" | cut -f1)
        echo -e "${GREEN}✓ Backup creado exitosamente: $size${NC}"
        log_message "INFO" "Backup creado exitosamente: $(basename $backup_file) ($size)"
        
        # Calcular checksum
        local checksum=$(md5sum "$backup_file" | cut -d' ' -f1)
        echo "$checksum" > "${backup_file}.md5"
        log_message "INFO" "Checksum MD5: $checksum"
        
        rm -f "$file_list"
        return 0
    else
        echo -e "${RED}✗ Error al crear el backup${NC}"
        log_message "ERROR" "Error al crear el backup"
        rm -f "$file_list"
        return 1
    fi
}

################################################################################
# Función: backup_configuration
################################################################################
backup_configuration() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local config_backup="${BACKUP_DIR}/farmaya_config_${timestamp}.tar.gz"
    
    log_message "INFO" "Creando backup de configuraciones..."
    
    local config_files=(
        "backend/src/main/resources/application.properties"
        "backend/src/main/resources/application-acceptance.properties"
        "backend/pom.xml"
        "docker-compose.yml"
        "frontend/farmacia-merysalud/assets/js/api-config.js"
    )
    
    local temp_list="/tmp/farmaya_config_${timestamp}.txt"
    > "$temp_list"
    
    for config in "${config_files[@]}"; do
        if [ -f "${PROJECT_ROOT}/${config}" ]; then
            echo "$config" >> "$temp_list"
        fi
    done
    
    if tar -czf "$config_backup" \
        -C "$PROJECT_ROOT" \
        --files-from="$temp_list" 2>> "$LOG_FILE"; then
        
        log_message "INFO" "Backup de configuración creado: $(basename $config_backup)"
        echo -e "${GREEN}✓ Backup de configuración creado${NC}"
    else
        log_message "ERROR" "Error al crear backup de configuración"
    fi
    
    rm -f "$temp_list"
}

################################################################################
# Función: cleanup_old_backups
################################################################################
cleanup_old_backups() {
    print_header "LIMPIEZA DE BACKUPS ANTIGUOS"
    
    log_message "INFO" "Buscando backups antiguos (> $RETENTION_DAYS días)..."
    
    local deleted_count=0
    while IFS= read -r file; do
        log_message "INFO" "Eliminando backup antiguo: $(basename "$file")"
        rm -f "$file" "${file}.md5"
        ((deleted_count++))
    done < <(find "$BACKUP_DIR" -name "farmaya_files_*.tar.gz" -type f -mtime +$RETENTION_DAYS)
    
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
################################################################################
list_backups() {
    print_header "BACKUPS DE ARCHIVOS DISPONIBLES"
    
    if [ ! "$(ls -A $BACKUP_DIR/*.tar.gz 2>/dev/null)" ]; then
        echo -e "${YELLOW}No hay backups disponibles${NC}"
        return
    fi
    
    echo -e "${BLUE}Backups encontrados:${NC}\n"
    
    local count=1
    for backup in $(ls -t "$BACKUP_DIR"/farmaya_files_*.tar.gz 2>/dev/null); do
        local size=$(du -h "$backup" | cut -f1)
        local date=$(stat -c %y "$backup" | cut -d' ' -f1,2 | cut -d'.' -f1)
        
        echo -e "  ${count}. $(basename $backup)"
        echo -e "     Tamaño: ${size} | Fecha: ${date}"
        echo ""
        ((count++))
    done
}

################################################################################
# Función: generate_backup_manifest
################################################################################
generate_backup_manifest() {
    local timestamp=$(date +%Y%m%d_%H%M%S)
    local manifest_file="${BACKUP_DIR}/manifest_${timestamp}.txt"
    
    cat > "$manifest_file" << EOF
═══════════════════════════════════════════════════════════════
  MANIFIESTO DE BACKUP - FARMAYA
  Fecha: $(date '+%Y-%m-%d %H:%M:%S')
═══════════════════════════════════════════════════════════════

ARCHIVOS Y DIRECTORIOS RESPALDADOS:
EOF

    for target in "${BACKUP_TARGETS[@]}"; do
        echo "  • $target" >> "$manifest_file"
    done
    
    cat >> "$manifest_file" << EOF

PATRONES EXCLUIDOS:
EOF

    for pattern in "${EXCLUDE_PATTERNS[@]}"; do
        echo "  • $pattern" >> "$manifest_file"
    done
    
    cat >> "$manifest_file" << EOF

ESTADÍSTICAS:
  • Total de backups: $(ls -1 "$BACKUP_DIR"/farmaya_files_*.tar.gz 2>/dev/null | wc -l)
  • Espacio usado: $(du -sh "$BACKUP_DIR" | cut -f1)
  • Política de retención: $RETENTION_DAYS días

═══════════════════════════════════════════════════════════════
EOF

    log_message "INFO" "Manifiesto generado: $manifest_file"
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    print_header "BACKUP DE ARCHIVOS FARMAYA"
    
    echo -e "${BLUE}Fecha y hora: $(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    
    # Verificar dependencias
    if ! check_dependencies; then
        echo -e "${RED}✗ Error: Faltan dependencias requeridas${NC}"
        exit 1
    fi
    
    # Mostrar tamaño estimado
    local project_size=$(calculate_project_size)
    echo -e "${BLUE}Tamaño estimado del proyecto: ${GREEN}${project_size}${NC}\n"
    
    # Realizar backup completo
    if ! backup_files; then
        echo -e "${RED}✗ Error al realizar el backup${NC}"
        exit 1
    fi
    
    # Backup de configuraciones
    backup_configuration
    
    # Limpiar backups antiguos
    cleanup_old_backups
    
    # Listar backups
    list_backups
    
    # Generar manifiesto
    generate_backup_manifest
    
    print_header "BACKUP COMPLETADO EXITOSAMENTE"
    
    log_message "INFO" "Proceso de backup de archivos finalizado exitosamente"
}

# Ejecutar función principal
main "$@"
