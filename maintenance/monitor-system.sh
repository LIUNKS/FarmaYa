#!/bin/bash

################################################################################
# Script de Monitoreo del Sistema - FarmaYa
# Descripción: Monitorea el estado de la aplicación y servicios
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
LOG_FILE="${LOG_DIR}/monitor-$(date +%Y%m%d).log"

# URLs de la aplicación
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
HEALTH_ENDPOINT="${BACKEND_URL}/actuator/health"

# Configuración de Base de Datos
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-farmaya}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-Johan12315912}"

# Umbrales
CPU_THRESHOLD=80
MEMORY_THRESHOLD=80
DISK_THRESHOLD=85

# Crear directorio de logs
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
# Función: print_status
################################################################################
print_status() {
    local name=$1
    local status=$2
    local details=$3
    
    if [ "$status" = "OK" ]; then
        echo -e "  • ${name}: ${GREEN}✓ OK${NC} ${details}"
    elif [ "$status" = "WARNING" ]; then
        echo -e "  • ${name}: ${YELLOW}⚠ WARNING${NC} ${details}"
    else
        echo -e "  • ${name}: ${RED}✗ ERROR${NC} ${details}"
    fi
}

################################################################################
# Función: check_backend
################################################################################
check_backend() {
    log_message "INFO" "Verificando backend..."
    
    if curl -s -f "$BACKEND_URL" > /dev/null 2>&1; then
        print_status "Backend" "OK" "(${BACKEND_URL})"
        log_message "INFO" "Backend está activo"
        return 0
    else
        print_status "Backend" "ERROR" "(${BACKEND_URL})"
        log_message "ERROR" "Backend no responde"
        return 1
    fi
}

################################################################################
# Función: check_database
################################################################################
check_database() {
    log_message "INFO" "Verificando base de datos..."
    
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME" 2>/dev/null; then
        
        # Obtener información de conexiones
        local connections=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "SHOW STATUS LIKE 'Threads_connected';" -sN 2>/dev/null | awk '{print $2}')
        
        # Obtener tamaño de la BD
        local db_size=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e \
            "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) \
             FROM information_schema.TABLES \
             WHERE table_schema = '$DB_NAME';" -sN 2>/dev/null)
        
        print_status "Base de Datos" "OK" "(Conexiones: ${connections}, Tamaño: ${db_size} MB)"
        log_message "INFO" "Base de datos está activa"
        return 0
    else
        print_status "Base de Datos" "ERROR" "($DB_HOST:$DB_PORT)"
        log_message "ERROR" "Base de datos no responde"
        return 1
    fi
}

################################################################################
# Función: check_docker
################################################################################
check_docker() {
    log_message "INFO" "Verificando contenedores Docker..."
    
    if ! command -v docker &> /dev/null; then
        print_status "Docker" "WARNING" "(No instalado)"
        return 1
    fi
    
    local containers_running=$(docker ps --filter "name=farmaya" --format "{{.Names}}" 2>/dev/null | wc -l)
    local containers_total=$(docker ps -a --filter "name=farmaya" --format "{{.Names}}" 2>/dev/null | wc -l)
    
    if [ $containers_running -gt 0 ]; then
        print_status "Docker" "OK" "(${containers_running}/${containers_total} contenedores activos)"
        
        echo -e "\n${BLUE}  Contenedores activos:${NC}"
        docker ps --filter "name=farmaya" --format "    • {{.Names}} - {{.Status}}" 2>/dev/null
        
        log_message "INFO" "Docker containers: ${containers_running}/${containers_total} activos"
        return 0
    else
        print_status "Docker" "WARNING" "(No hay contenedores activos)"
        log_message "WARN" "No hay contenedores Docker activos"
        return 1
    fi
}

################################################################################
# Función: check_disk_space
################################################################################
check_disk_space() {
    log_message "INFO" "Verificando espacio en disco..."
    
    local disk_usage=$(df -h "$PROJECT_ROOT" | awk 'NR==2 {print $5}' | sed 's/%//')
    
    if [ $disk_usage -lt $DISK_THRESHOLD ]; then
        print_status "Espacio en Disco" "OK" "(${disk_usage}% usado)"
        log_message "INFO" "Espacio en disco: ${disk_usage}%"
        return 0
    elif [ $disk_usage -lt 95 ]; then
        print_status "Espacio en Disco" "WARNING" "(${disk_usage}% usado - umbral: ${DISK_THRESHOLD}%)"
        log_message "WARN" "Espacio en disco: ${disk_usage}%"
        return 1
    else
        print_status "Espacio en Disco" "ERROR" "(${disk_usage}% usado - CRÍTICO)"
        log_message "ERROR" "Espacio en disco crítico: ${disk_usage}%"
        return 2
    fi
}

################################################################################
# Función: check_memory
################################################################################
check_memory() {
    log_message "INFO" "Verificando uso de memoria..."
    
    if command -v free &> /dev/null; then
        local memory_usage=$(free | awk 'NR==2 {printf "%.0f", $3/$2 * 100.0}')
        
        if [ $memory_usage -lt $MEMORY_THRESHOLD ]; then
            print_status "Memoria" "OK" "(${memory_usage}% usado)"
            log_message "INFO" "Uso de memoria: ${memory_usage}%"
            return 0
        else
            print_status "Memoria" "WARNING" "(${memory_usage}% usado - umbral: ${MEMORY_THRESHOLD}%)"
            log_message "WARN" "Uso de memoria alto: ${memory_usage}%"
            return 1
        fi
    else
        print_status "Memoria" "WARNING" "(No se puede verificar)"
        return 1
    fi
}

################################################################################
# Función: check_cpu
################################################################################
check_cpu() {
    log_message "INFO" "Verificando uso de CPU..."
    
    if command -v top &> /dev/null; then
        # Obtener uso de CPU promedio del último minuto
        local cpu_usage=$(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')
        local cpu_int=${cpu_usage%.*}
        
        if [ $cpu_int -lt $CPU_THRESHOLD ]; then
            print_status "CPU" "OK" "(${cpu_usage}% usado)"
            log_message "INFO" "Uso de CPU: ${cpu_usage}%"
            return 0
        else
            print_status "CPU" "WARNING" "(${cpu_usage}% usado - umbral: ${CPU_THRESHOLD}%)"
            log_message "WARN" "Uso de CPU alto: ${cpu_usage}%"
            return 1
        fi
    else
        print_status "CPU" "WARNING" "(No se puede verificar)"
        return 1
    fi
}

################################################################################
# Función: check_ports
################################################################################
check_ports() {
    log_message "INFO" "Verificando puertos..."
    
    echo -e "\n${BLUE}  Puertos en uso:${NC}"
    
    # Puerto backend (8080)
    if netstat -tuln 2>/dev/null | grep -q ":8080 "; then
        echo -e "    • Puerto 8080 (Backend): ${GREEN}✓ Abierto${NC}"
        log_message "INFO" "Puerto 8080 está abierto"
    else
        echo -e "    • Puerto 8080 (Backend): ${RED}✗ Cerrado${NC}"
        log_message "WARN" "Puerto 8080 está cerrado"
    fi
    
    # Puerto MySQL (3306)
    if netstat -tuln 2>/dev/null | grep -q ":3306 "; then
        echo -e "    • Puerto 3306 (MySQL): ${GREEN}✓ Abierto${NC}"
        log_message "INFO" "Puerto 3306 está abierto"
    else
        echo -e "    • Puerto 3306 (MySQL): ${RED}✗ Cerrado${NC}"
        log_message "WARN" "Puerto 3306 está cerrado"
    fi
}

################################################################################
# Función: check_logs
################################################################################
check_logs() {
    print_header "ANÁLISIS DE LOGS RECIENTES"
    
    log_message "INFO" "Analizando logs recientes..."
    
    # Buscar errores en logs del backend
    local backend_logs="${PROJECT_ROOT}/backend/logs"
    if [ -d "$backend_logs" ]; then
        local error_count=$(find "$backend_logs" -name "*.log" -mtime -1 -exec grep -i "ERROR" {} \; 2>/dev/null | wc -l)
        
        if [ $error_count -gt 0 ]; then
            echo -e "  ${YELLOW}⚠ Se encontraron ${error_count} errores en logs del backend (últimas 24h)${NC}"
            log_message "WARN" "Errores en logs: ${error_count}"
        else
            echo -e "  ${GREEN}✓ No se encontraron errores recientes${NC}"
        fi
    fi
    
    # Buscar errores en logs de mantenimiento
    local maintenance_errors=$(find "$LOG_DIR" -name "*.log" -mtime -1 -exec grep -i "ERROR" {} \; 2>/dev/null | wc -l)
    if [ $maintenance_errors -gt 0 ]; then
        echo -e "  ${YELLOW}⚠ Se encontraron ${maintenance_errors} errores en logs de mantenimiento${NC}"
    fi
}

################################################################################
# Función: generate_health_report
################################################################################
generate_health_report() {
    local report_file="${LOG_DIR}/health_report_$(date +%Y%m%d_%H%M%S).txt"
    
    cat > "$report_file" << EOF
═══════════════════════════════════════════════════════════════
  REPORTE DE SALUD DEL SISTEMA - FARMAYA
  Fecha: $(date '+%Y-%m-%d %H:%M:%S')
═══════════════════════════════════════════════════════════════

ESTADO DE SERVICIOS:
  • Backend: $(curl -s -f "$BACKEND_URL" > /dev/null 2>&1 && echo "ACTIVO" || echo "INACTIVO")
  • Base de Datos: $(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME" 2>/dev/null && echo "ACTIVO" || echo "INACTIVO")
  
RECURSOS DEL SISTEMA:
  • Uso de disco: $(df -h "$PROJECT_ROOT" | awk 'NR==2 {print $5}')
  • Uso de memoria: $(free | awk 'NR==2 {printf "%.0f%%", $3/$2 * 100.0}')
  
ESTADÍSTICAS:
  • Uptime del sistema: $(uptime -p 2>/dev/null || echo "N/A")
  • Tamaño del proyecto: $(du -sh "$PROJECT_ROOT" | cut -f1)
  • Logs de mantenimiento: $(find "$LOG_DIR" -name "*.log*" | wc -l) archivos

═══════════════════════════════════════════════════════════════
EOF

    log_message "INFO" "Reporte de salud generado: $report_file"
    echo -e "\n${GREEN}✓ Reporte guardado en: $report_file${NC}"
}

################################################################################
# Función: continuous_monitor
################################################################################
continuous_monitor() {
    print_header "MODO MONITOREO CONTINUO"
    
    echo -e "${BLUE}Presiona Ctrl+C para detener${NC}\n"
    
    while true; do
        clear
        print_header "MONITOREO EN TIEMPO REAL - $(date '+%H:%M:%S')"
        
        check_backend
        check_database
        check_docker
        check_disk_space
        check_memory
        check_cpu
        
        echo -e "\n${BLUE}Actualizando en 30 segundos...${NC}"
        sleep 30
    done
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    print_header "MONITOREO DEL SISTEMA FARMAYA"
    
    echo -e "${BLUE}Fecha y hora: $(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    
    # Verificar servicios
    print_header "ESTADO DE SERVICIOS"
    check_backend
    check_database
    check_docker
    
    # Verificar recursos
    print_header "RECURSOS DEL SISTEMA"
    check_disk_space
    check_memory
    check_cpu
    
    # Verificar puertos
    check_ports
    
    # Analizar logs
    check_logs
    
    # Generar reporte
    generate_health_report
    
    print_header "MONITOREO COMPLETADO"
    
    log_message "INFO" "Proceso de monitoreo finalizado"
}

# Verificar parámetros
case "$1" in
    --continuous)
        continuous_monitor
        ;;
    --services-only)
        print_header "VERIFICANDO SOLO SERVICIOS"
        check_backend
        check_database
        check_docker
        ;;
    --resources-only)
        print_header "VERIFICANDO SOLO RECURSOS"
        check_disk_space
        check_memory
        check_cpu
        ;;
    *)
        main "$@"
        ;;
esac
