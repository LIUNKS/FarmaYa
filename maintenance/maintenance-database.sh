#!/bin/bash

################################################################################
# Script de Mantenimiento de Base de Datos - FarmaYa
# Descripción: Optimiza y mantiene la base de datos MySQL
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
LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/maintenance-db-$(date +%Y%m%d).log"

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
# Función: check_mysql_connection
################################################################################
check_mysql_connection() {
    log_message "INFO" "Verificando conexión a MySQL..."
    
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME" 2>/dev/null; then
        log_message "ERROR" "No se puede conectar a la base de datos"
        return 1
    fi
    
    log_message "INFO" "Conexión a MySQL exitosa"
    return 0
}

################################################################################
# Función: analyze_tables
################################################################################
analyze_tables() {
    print_header "ANALIZANDO TABLAS"
    
    log_message "INFO" "Iniciando análisis de tablas..."
    
    local tables=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '$DB_NAME';" -sN)
    
    local count=0
    for table in $tables; do
        echo -e "${BLUE}Analizando tabla: ${GREEN}${table}${NC}"
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "ANALYZE TABLE $DB_NAME.$table;" >> "$LOG_FILE" 2>&1
        ((count++))
    done
    
    echo -e "${GREEN}✓ Se analizaron $count tablas${NC}"
    log_message "INFO" "Se analizaron $count tablas"
}

################################################################################
# Función: optimize_tables
################################################################################
optimize_tables() {
    print_header "OPTIMIZANDO TABLAS"
    
    log_message "INFO" "Iniciando optimización de tablas..."
    
    local tables=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '$DB_NAME' AND ENGINE = 'InnoDB';" -sN)
    
    local count=0
    local space_saved=0
    
    for table in $tables; do
        echo -e "${BLUE}Optimizando tabla: ${GREEN}${table}${NC}"
        
        # Obtener tamaño antes
        local size_before=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "SELECT (data_length + index_length) FROM information_schema.TABLES \
                WHERE TABLE_SCHEMA = '$DB_NAME' AND TABLE_NAME = '$table';" -sN)
        
        # Optimizar
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "OPTIMIZE TABLE $DB_NAME.$table;" >> "$LOG_FILE" 2>&1
        
        # Obtener tamaño después
        local size_after=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "SELECT (data_length + index_length) FROM information_schema.TABLES \
                WHERE TABLE_SCHEMA = '$DB_NAME' AND TABLE_NAME = '$table';" -sN)
        
        local saved=$((size_before - size_after))
        space_saved=$((space_saved + saved))
        
        ((count++))
    done
    
    local space_saved_mb=$(echo "scale=2; $space_saved / 1024 / 1024" | bc)
    echo -e "${GREEN}✓ Se optimizaron $count tablas${NC}"
    echo -e "${GREEN}✓ Espacio recuperado: ${space_saved_mb} MB${NC}"
    log_message "INFO" "Se optimizaron $count tablas. Espacio recuperado: ${space_saved_mb} MB"
}

################################################################################
# Función: check_table_integrity
################################################################################
check_table_integrity() {
    print_header "VERIFICANDO INTEGRIDAD DE TABLAS"
    
    log_message "INFO" "Verificando integridad de tablas..."
    
    local tables=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '$DB_NAME';" -sN)
    
    local errors=0
    local count=0
    
    for table in $tables; do
        echo -e "${BLUE}Verificando: ${GREEN}${table}${NC}"
        
        local result=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "CHECK TABLE $DB_NAME.$table;" 2>&1)
        
        if echo "$result" | grep -q "OK"; then
            echo -e "  ${GREEN}✓ OK${NC}"
        else
            echo -e "  ${RED}✗ ERROR${NC}"
            log_message "ERROR" "Error en tabla $table: $result"
            ((errors++))
        fi
        
        ((count++))
    done
    
    if [ $errors -eq 0 ]; then
        echo -e "\n${GREEN}✓ Todas las tablas están OK ($count verificadas)${NC}"
        log_message "INFO" "Todas las tablas verificadas correctamente"
    else
        echo -e "\n${YELLOW}⚠ Se encontraron $errors errores en $count tablas${NC}"
        log_message "WARN" "Se encontraron $errors errores"
    fi
}

################################################################################
# Función: repair_tables
################################################################################
repair_tables() {
    print_header "REPARANDO TABLAS"
    
    log_message "INFO" "Iniciando reparación de tablas..."
    
    local tables=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = '$DB_NAME';" -sN)
    
    local repaired=0
    
    for table in $tables; do
        echo -e "${BLUE}Reparando tabla: ${GREEN}${table}${NC}"
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
            -e "REPAIR TABLE $DB_NAME.$table;" >> "$LOG_FILE" 2>&1
        ((repaired++))
    done
    
    echo -e "${GREEN}✓ Se repararon $repaired tablas${NC}"
    log_message "INFO" "Se repararon $repaired tablas"
}

################################################################################
# Función: update_statistics
################################################################################
update_statistics() {
    print_header "ACTUALIZANDO ESTADÍSTICAS"
    
    log_message "INFO" "Actualizando estadísticas de la base de datos..."
    
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" <<EOF >> "$LOG_FILE" 2>&1
USE $DB_NAME;
ANALYZE TABLE users, productos, orders, order_items, cart, cart_items;
EOF
    
    echo -e "${GREEN}✓ Estadísticas actualizadas${NC}"
    log_message "INFO" "Estadísticas actualizadas exitosamente"
}

################################################################################
# Función: cleanup_expired_sessions
################################################################################
cleanup_expired_sessions() {
    print_header "LIMPIEZA DE SESIONES EXPIRADAS"
    
    log_message "INFO" "Limpiando sesiones expiradas..."
    
    # Aquí puedes agregar lógica específica para limpiar sesiones
    # Este es un ejemplo genérico
    local deleted=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
        -e "SELECT COUNT(*) FROM $DB_NAME.users WHERE last_login < DATE_SUB(NOW(), INTERVAL 90 DAY);" -sN 2>/dev/null || echo "0")
    
    echo -e "${GREEN}✓ Sesiones inactivas encontradas: $deleted${NC}"
    log_message "INFO" "Sesiones inactivas: $deleted"
}

################################################################################
# Función: generate_database_report
################################################################################
generate_database_report() {
    local report_file="${LOG_DIR}/db_maintenance_report_$(date +%Y%m%d).txt"
    
    log_message "INFO" "Generando reporte de mantenimiento..."
    
    cat > "$report_file" << EOF
═══════════════════════════════════════════════════════════════
  REPORTE DE MANTENIMIENTO DE BASE DE DATOS - FARMAYA
  Fecha: $(date '+%Y-%m-%d %H:%M:%S')
═══════════════════════════════════════════════════════════════

INFORMACIÓN GENERAL:
  • Host: $DB_HOST:$DB_PORT
  • Base de datos: $DB_NAME
  
ESTADÍSTICAS DE LA BASE DE DATOS:
EOF

    # Tamaño de la base de datos
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" <<EOF >> "$report_file" 2>/dev/null
SELECT 
    'Tamaño total' AS Métrica,
    CONCAT(ROUND(SUM(data_length + index_length) / 1024 / 1024, 2), ' MB') AS Valor
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME'
UNION ALL
SELECT 
    'Número de tablas' AS Métrica,
    COUNT(*) AS Valor
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME'
UNION ALL
SELECT 
    'Datos' AS Métrica,
    CONCAT(ROUND(SUM(data_length) / 1024 / 1024, 2), ' MB') AS Valor
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME'
UNION ALL
SELECT 
    'Índices' AS Métrica,
    CONCAT(ROUND(SUM(index_length) / 1024 / 1024, 2), ' MB') AS Valor
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME';
EOF

    cat >> "$report_file" << EOF

TABLAS MÁS GRANDES:
EOF

    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" <<EOF >> "$report_file" 2>/dev/null
SELECT 
    TABLE_NAME AS Tabla,
    CONCAT(ROUND((data_length + index_length) / 1024 / 1024, 2), ' MB') AS Tamaño
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = '$DB_NAME'
ORDER BY (data_length + index_length) DESC
LIMIT 10;
EOF

    cat >> "$report_file" << EOF

═══════════════════════════════════════════════════════════════
EOF

    echo -e "${GREEN}✓ Reporte generado: $report_file${NC}"
    log_message "INFO" "Reporte generado: $report_file"
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    print_header "MANTENIMIENTO DE BASE DE DATOS FARMAYA"
    
    echo -e "${BLUE}Fecha y hora: $(date '+%Y-%m-%d %H:%M:%S')${NC}\n"
    
    # Verificar conexión
    if ! check_mysql_connection; then
        echo -e "${RED}✗ Error: No se puede conectar a la base de datos${NC}"
        exit 1
    fi
    
    # Verificar integridad
    check_table_integrity
    
    # Analizar tablas
    analyze_tables
    
    # Optimizar tablas
    optimize_tables
    
    # Actualizar estadísticas
    update_statistics
    
    # Limpiar sesiones expiradas
    cleanup_expired_sessions
    
    # Generar reporte
    generate_database_report
    
    print_header "MANTENIMIENTO COMPLETADO EXITOSAMENTE"
    
    log_message "INFO" "Proceso de mantenimiento finalizado exitosamente"
}

# Verificar parámetros
if [ "$1" = "--repair" ]; then
    print_header "MODO REPARACIÓN"
    check_mysql_connection && repair_tables
    exit 0
fi

# Ejecutar función principal
main "$@"
