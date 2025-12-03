#!/bin/bash

################################################################################
# Script de Limpieza del Sistema - FarmaYa
# Descripción: Limpia logs, archivos temporales y optimiza el proyecto
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
LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/cleanup-$(date +%Y%m%d).log"

# Configuración de limpieza
LOG_RETENTION_DAYS=30
MAX_LOG_SIZE_MB=100

# Crear directorio de logs si no existe
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
# Función: get_size_mb
################################################################################
get_size_mb() {
    local path=$1
    if [ -e "$path" ]; then
        du -sm "$path" 2>/dev/null | cut -f1
    else
        echo "0"
    fi
}

################################################################################
# Función: cleanup_old_logs
################################################################################
cleanup_old_logs() {
    print_header "LIMPIANDO LOGS ANTIGUOS"
    
    log_message "INFO" "Buscando logs antiguos (> $LOG_RETENTION_DAYS días)..."
    
    local deleted_count=0
    local space_freed=0
    
    # Limpiar logs de mantenimiento
    while IFS= read -r file; do
        local size=$(get_size_mb "$file")
        space_freed=$((space_freed + size))
        log_message "INFO" "Eliminando log antiguo: $(basename "$file")"
        rm -f "$file"
        ((deleted_count++))
    done < <(find "$LOG_DIR" -name "*.log" -type f -mtime +$LOG_RETENTION_DAYS)
    
    # Limpiar logs del backend
    if [ -d "${PROJECT_ROOT}/backend/logs" ]; then
        while IFS= read -r file; do
            local size=$(get_size_mb "$file")
            space_freed=$((space_freed + size))
            log_message "INFO" "Eliminando log de backend: $(basename "$file")"
            rm -f "$file"
            ((deleted_count++))
        done < <(find "${PROJECT_ROOT}/backend/logs" -name "*.log" -type f -mtime +$LOG_RETENTION_DAYS 2>/dev/null)
    fi
    
    if [ $deleted_count -gt 0 ]; then
        echo -e "${GREEN}✓ Se eliminaron $deleted_count archivos de log${NC}"
        echo -e "${GREEN}✓ Espacio liberado: ${space_freed} MB${NC}"
        log_message "INFO" "Se eliminaron $deleted_count logs. Espacio liberado: ${space_freed} MB"
    else
        echo -e "${GREEN}✓ No hay logs antiguos para eliminar${NC}"
        log_message "INFO" "No hay logs antiguos para eliminar"
    fi
}

################################################################################
# Función: compress_large_logs
################################################################################
compress_large_logs() {
    print_header "COMPRIMIENDO LOGS GRANDES"
    
    log_message "INFO" "Buscando logs grandes (> ${MAX_LOG_SIZE_MB} MB)..."
    
    local compressed_count=0
    local space_saved=0
    
    while IFS= read -r file; do
        local size_before=$(get_size_mb "$file")
        
        if [ $size_before -gt $MAX_LOG_SIZE_MB ]; then
            echo -e "${BLUE}Comprimiendo: $(basename "$file") (${size_before} MB)${NC}"
            
            if gzip -f "$file"; then
                local size_after=$(get_size_mb "${file}.gz")
                local saved=$((size_before - size_after))
                space_saved=$((space_saved + saved))
                
                log_message "INFO" "Comprimido: $(basename "$file") (${saved} MB ahorrados)"
                ((compressed_count++))
            fi
        fi
    done < <(find "$LOG_DIR" -name "*.log" -type f)
    
    if [ $compressed_count -gt 0 ]; then
        echo -e "${GREEN}✓ Se comprimieron $compressed_count logs${NC}"
        echo -e "${GREEN}✓ Espacio ahorrado: ${space_saved} MB${NC}"
        log_message "INFO" "Se comprimieron $compressed_count logs. Espacio ahorrado: ${space_saved} MB"
    else
        echo -e "${GREEN}✓ No hay logs grandes para comprimir${NC}"
        log_message "INFO" "No hay logs grandes para comprimir"
    fi
}

################################################################################
# Función: cleanup_maven_cache
################################################################################
cleanup_maven_cache() {
    print_header "LIMPIANDO CACHÉ DE MAVEN"
    
    local backend_dir="${PROJECT_ROOT}/backend"
    
    if [ ! -d "$backend_dir" ]; then
        echo -e "${YELLOW}⚠ Directorio backend no encontrado${NC}"
        return
    fi
    
    log_message "INFO" "Limpiando target de Maven..."
    
    if [ -d "${backend_dir}/target" ]; then
        local size_before=$(get_size_mb "${backend_dir}/target")
        rm -rf "${backend_dir}/target"
        
        echo -e "${GREEN}✓ Limpiado directorio target (${size_before} MB liberados)${NC}"
        log_message "INFO" "Limpiado target: ${size_before} MB liberados"
    else
        echo -e "${GREEN}✓ No hay caché de Maven para limpiar${NC}"
    fi
}

################################################################################
# Función: cleanup_temp_files
################################################################################
cleanup_temp_files() {
    print_header "LIMPIANDO ARCHIVOS TEMPORALES"
    
    log_message "INFO" "Buscando archivos temporales..."
    
    local deleted_count=0
    local patterns=(
        "*.tmp"
        "*.temp"
        "*~"
        ".DS_Store"
        "Thumbs.db"
        "*.swp"
        "*.swo"
    )
    
    for pattern in "${patterns[@]}"; do
        while IFS= read -r file; do
            log_message "INFO" "Eliminando: $(basename "$file")"
            rm -f "$file"
            ((deleted_count++))
        done < <(find "$PROJECT_ROOT" -name "$pattern" -type f 2>/dev/null)
    done
    
    if [ $deleted_count -gt 0 ]; then
        echo -e "${GREEN}✓ Se eliminaron $deleted_count archivos temporales${NC}"
        log_message "INFO" "Se eliminaron $deleted_count archivos temporales"
    else
        echo -e "${GREEN}✓ No hay archivos temporales para eliminar${NC}"
        log_message "INFO" "No hay archivos temporales"
    fi
}

################################################################################
# Función: cleanup_docker
################################################################################
cleanup_docker() {
    print_header "LIMPIANDO RECURSOS DE DOCKER"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${YELLOW}⚠ Docker no está instalado${NC}"
        return
    fi
    
    log_message "INFO" "Limpiando recursos de Docker..."
    
    echo -e "${BLUE}Eliminando contenedores detenidos...${NC}"
    local containers=$(docker ps -aq -f status=exited 2>/dev/null | wc -l)
    if [ $containers -gt 0 ]; then
        docker container prune -f >> "$LOG_FILE" 2>&1
        echo -e "${GREEN}✓ Se eliminaron $containers contenedores${NC}"
    fi
    
    echo -e "${BLUE}Eliminando imágenes sin uso...${NC}"
    docker image prune -f >> "$LOG_FILE" 2>&1
    echo -e "${GREEN}✓ Imágenes limpiadas${NC}"
    
    echo -e "${BLUE}Eliminando volúmenes sin uso...${NC}"
    docker volume prune -f >> "$LOG_FILE" 2>&1
    echo -e "${GREEN}✓ Volúmenes limpiados${NC}"
    
    log_message "INFO" "Limpieza de Docker completada"
}

################################################################################
# Función: analyze_disk_usage
################################################################################
analyze_disk_usage() {
    print_header "ANÁLISIS DE USO DE DISCO"
    
    log_message "INFO" "Analizando uso de disco del proyecto..."
    
    echo -e "${BLUE}Uso de disco por directorio:${NC}\n"
    
    # Backend
    if [ -d "${PROJECT_ROOT}/backend" ]; then
        local backend_size=$(du -sh "${PROJECT_ROOT}/backend" 2>/dev/null | cut -f1)
        echo -e "  • Backend: ${GREEN}${backend_size}${NC}"
    fi
    
    # Frontend
    if [ -d "${PROJECT_ROOT}/frontend" ]; then
        local frontend_size=$(du -sh "${PROJECT_ROOT}/frontend" 2>/dev/null | cut -f1)
        echo -e "  • Frontend: ${GREEN}${frontend_size}${NC}"
    fi
    
    # Backups
    if [ -d "${SCRIPT_DIR}/backups" ]; then
        local backup_size=$(du -sh "${SCRIPT_DIR}/backups" 2>/dev/null | cut -f1)
        local backup_count=$(find "${SCRIPT_DIR}/backups" -type f 2>/dev/null | wc -l)
        echo -e "  • Backups: ${GREEN}${backup_size}${NC} (${backup_count} archivos)"
    fi
    
    # Logs
    if [ -d "${LOG_DIR}" ]; then
        local log_size=$(du -sh "${LOG_DIR}" 2>/dev/null | cut -f1)
        local log_count=$(find "${LOG_DIR}" -name "*.log*" -type f 2>/dev/null | wc -l)
        echo -e "  • Logs: ${GREEN}${log_size}${NC} (${log_count} archivos)"
    fi
    
    echo ""
    
    # Espacio total del proyecto
    local total_size=$(du -sh "$PROJECT_ROOT" 2>/dev/null | cut -f1)
    echo -e "${BLUE}Tamaño total del proyecto: ${GREEN}${total_size}${NC}"
}

################################################################################
# Función: rotate_logs
################################################################################
rotate_logs() {
    print_header "ROTACIÓN DE LOGS"
    
    log_message "INFO" "Rotando logs activos..."
    
    local rotated_count=0
    
    # Rotar logs del directorio de mantenimiento
    for log_file in "$LOG_DIR"/*.log; do
        if [ -f "$log_file" ] && [ $(get_size_mb "$log_file") -gt 10 ]; then
            local timestamp=$(date +%Y%m%d_%H%M%S)
            local new_name="${log_file%.log}_${timestamp}.log"
            
            mv "$log_file" "$new_name"
            gzip "$new_name"
            
            log_message "INFO" "Log rotado: $(basename "$log_file")"
            ((rotated_count++))
        fi
    done
    
    if [ $rotated_count -gt 0 ]; then
        echo -e "${GREEN}✓ Se rotaron $rotated_count logs${NC}"
        log_message "INFO" "Se rotaron $rotated_count logs"
    else
        echo -e "${GREEN}✓ No hay logs que requieran rotación${NC}"
        log_message "INFO" "No hay logs que requieran rotación"
    fi
}

################################################################################
# Función: generate_cleanup_report
################################################################################
generate_cleanup_report() {
    local report_file="${LOG_DIR}/cleanup_report_$(date +%Y%m%d).txt"
    
    cat > "$report_file" << EOF
═══════════════════════════════════════════════════════════════
  REPORTE DE LIMPIEZA DEL SISTEMA - FARMAYA
  Fecha: $(date '+%Y-%m-%d %H:%M:%S')
═══════════════════════════════════════════════════════════════

ANÁLISIS DE ESPACIO:
  • Tamaño del proyecto: $(du -sh "$PROJECT_ROOT" | cut -f1)
  • Tamaño de backups: $(du -sh "${SCRIPT_DIR}/backups" 2>/dev/null | cut -f1 || echo "0")
  • Tamaño de logs: $(du -sh "$LOG_DIR" | cut -f1)

ARCHIVOS DE LOG:
  • Total de logs: $(find "$LOG_DIR" -name "*.log*" | wc -l)
  • Logs sin comprimir: $(find "$LOG_DIR" -name "*.log" | wc -l)
  • Logs comprimidos: $(find "$LOG_DIR" -name "*.log.gz" | wc -l)

CONFIGURACIÓN:
  • Retención de logs: $LOG_RETENTION_DAYS días
  • Tamaño máximo de log: $MAX_LOG_SIZE_MB MB

═══════════════════════════════════════════════════════════════
EOF

    log_message "INFO" "Reporte generado: $report_file"
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    print_header "LIMPIEZA DEL SISTEMA FARMAYA"
    
    echo -e "${BLUE}Fecha y hora: $(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    
    # Análisis inicial
    analyze_disk_usage
    
    # Limpiar logs antiguos
    cleanup_old_logs
    
    # Comprimir logs grandes
    compress_large_logs
    
    # Rotar logs activos
    rotate_logs
    
    # Limpiar archivos temporales
    cleanup_temp_files
    
    # Limpiar caché de Maven
    cleanup_maven_cache
    
    # Limpiar Docker (si está disponible)
    cleanup_docker
    
    # Generar reporte
    generate_cleanup_report
    
    # Análisis final
    analyze_disk_usage
    
    print_header "LIMPIEZA COMPLETADA EXITOSAMENTE"
    
    log_message "INFO" "Proceso de limpieza finalizado exitosamente"
}

# Verificar parámetros
case "$1" in
    --logs-only)
        print_header "MODO: SOLO LOGS"
        cleanup_old_logs
        compress_large_logs
        rotate_logs
        ;;
    --docker-only)
        print_header "MODO: SOLO DOCKER"
        cleanup_docker
        ;;
    --analyze)
        print_header "MODO: SOLO ANÁLISIS"
        analyze_disk_usage
        ;;
    *)
        main "$@"
        ;;
esac
