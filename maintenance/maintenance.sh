#!/bin/bash

################################################################################
# Script Maestro de Mantenimiento - FarmaYa
# Descripción: Ejecuta todas las tareas de mantenimiento del sistema
# Autor: FarmaYa Team
# Fecha: Diciembre 2025
################################################################################

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # Sin color

# Configuración
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/maintenance-master-$(date +%Y%m%d).log"

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
# Función: print_banner
################################################################################
print_banner() {
    clear
    echo -e "${CYAN}"
    cat << "EOF"
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║   ███████╗ █████╗ ██████╗ ███╗   ███╗ █████╗ ██╗   ██╗ █████╗║
║   ██╔════╝██╔══██╗██╔══██╗████╗ ████║██╔══██╗╚██╗ ██╔╝██╔══██║
║   █████╗  ███████║██████╔╝██╔████╔██║███████║ ╚████╔╝ ███████║
║   ██╔══╝  ██╔══██║██╔══██╗██║╚██╔╝██║██╔══██║  ╚██╔╝  ██╔══██║
║   ██║     ██║  ██║██║  ██║██║ ╚═╝ ██║██║  ██║   ██║   ██║  ██║
║   ╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝
║                                                               ║
║              SISTEMA DE MANTENIMIENTO INTEGRAL               ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}\n"
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
# Función: print_menu
################################################################################
print_menu() {
    print_header "MENÚ PRINCIPAL"
    
    echo -e "${CYAN}BACKUPS:${NC}"
    echo -e "  ${GREEN}1${NC}) Backup completo (Base de datos + Archivos)"
    echo -e "  ${GREEN}2${NC}) Backup de base de datos"
    echo -e "  ${GREEN}3${NC}) Backup de archivos"
    echo -e "  ${GREEN}4${NC}) Restaurar base de datos"
    echo ""
    
    echo -e "${CYAN}MANTENIMIENTO:${NC}"
    echo -e "  ${GREEN}5${NC}) Mantenimiento de base de datos"
    echo -e "  ${GREEN}6${NC}) Limpieza del sistema"
    echo -e "  ${GREEN}7${NC}) Mantenimiento completo (BD + Sistema)"
    echo ""
    
    echo -e "${CYAN}MONITOREO:${NC}"
    echo -e "  ${GREEN}8${NC}) Monitoreo del sistema"
    echo -e "  ${GREEN}9${NC}) Monitoreo continuo"
    echo -e "  ${GREEN}10${NC}) Verificar estado de servicios"
    echo ""
    
    echo -e "${CYAN}UTILIDADES:${NC}"
    echo -e "  ${GREEN}11${NC}) Listar backups disponibles"
    echo -e "  ${GREEN}12${NC}) Ver logs de mantenimiento"
    echo -e "  ${GREEN}13${NC}) Generar reporte completo"
    echo ""
    
    echo -e "${CYAN}AUTOMATIZACIÓN:${NC}"
    echo -e "  ${GREEN}14${NC}) Mantenimiento automático (Recomendado)"
    echo ""
    
    echo -e "  ${RED}0${NC}) Salir"
    echo ""
}

################################################################################
# Función: run_script
################################################################################
run_script() {
    local script_name=$1
    local script_path="${SCRIPT_DIR}/${script_name}"
    
    if [ ! -f "$script_path" ]; then
        echo -e "${RED}✗ Error: Script no encontrado: $script_name${NC}"
        log_message "ERROR" "Script no encontrado: $script_name"
        return 1
    fi
    
    log_message "INFO" "Ejecutando: $script_name"
    
    if bash "$script_path" "${@:2}"; then
        log_message "INFO" "Completado: $script_name"
        return 0
    else
        log_message "ERROR" "Error en: $script_name"
        return 1
    fi
}

################################################################################
# Función: full_backup
################################################################################
full_backup() {
    print_header "BACKUP COMPLETO"
    
    echo -e "${BLUE}Realizando backup completo del sistema...${NC}\n"
    
    # Backup de base de datos
    echo -e "${CYAN}[1/2] Backup de base de datos${NC}"
    if run_script "backup-database.sh"; then
        echo -e "${GREEN}✓ Backup de BD completado${NC}\n"
    else
        echo -e "${RED}✗ Error en backup de BD${NC}\n"
    fi
    
    # Backup de archivos
    echo -e "${CYAN}[2/2] Backup de archivos${NC}"
    if run_script "backup-files.sh"; then
        echo -e "${GREEN}✓ Backup de archivos completado${NC}\n"
    else
        echo -e "${RED}✗ Error en backup de archivos${NC}\n"
    fi
    
    print_header "BACKUP COMPLETO FINALIZADO"
}

################################################################################
# Función: full_maintenance
################################################################################
full_maintenance() {
    print_header "MANTENIMIENTO COMPLETO"
    
    echo -e "${BLUE}Ejecutando mantenimiento completo del sistema...${NC}\n"
    
    # Mantenimiento de base de datos
    echo -e "${CYAN}[1/2] Mantenimiento de base de datos${NC}"
    if run_script "maintenance-database.sh"; then
        echo -e "${GREEN}✓ Mantenimiento de BD completado${NC}\n"
    else
        echo -e "${RED}✗ Error en mantenimiento de BD${NC}\n"
    fi
    
    # Limpieza del sistema
    echo -e "${CYAN}[2/2] Limpieza del sistema${NC}"
    if run_script "cleanup-system.sh"; then
        echo -e "${GREEN}✓ Limpieza completada${NC}\n"
    else
        echo -e "${RED}✗ Error en limpieza${NC}\n"
    fi
    
    print_header "MANTENIMIENTO COMPLETO FINALIZADO"
}

################################################################################
# Función: automatic_maintenance
################################################################################
automatic_maintenance() {
    print_header "MANTENIMIENTO AUTOMÁTICO"
    
    echo -e "${BLUE}Ejecutando rutina de mantenimiento automático...${NC}\n"
    
    local start_time=$(date +%s)
    
    # 1. Monitoreo previo
    echo -e "${CYAN}[1/5] Monitoreo inicial${NC}"
    run_script "monitor-system.sh" "--services-only"
    echo ""
    
    # 2. Backup completo
    echo -e "${CYAN}[2/5] Creando backups${NC}"
    run_script "backup-database.sh"
    echo ""
    
    # 3. Mantenimiento de BD
    echo -e "${CYAN}[3/5] Optimizando base de datos${NC}"
    run_script "maintenance-database.sh"
    echo ""
    
    # 4. Limpieza del sistema
    echo -e "${CYAN}[4/5] Limpiando sistema${NC}"
    run_script "cleanup-system.sh"
    echo ""
    
    # 5. Monitoreo final
    echo -e "${CYAN}[5/5] Verificación final${NC}"
    run_script "monitor-system.sh" "--services-only"
    echo ""
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    local minutes=$((duration / 60))
    local seconds=$((duration % 60))
    
    print_header "MANTENIMIENTO AUTOMÁTICO COMPLETADO"
    echo -e "${GREEN}✓ Tiempo total: ${minutes}m ${seconds}s${NC}\n"
    
    log_message "INFO" "Mantenimiento automático completado en ${minutes}m ${seconds}s"
}

################################################################################
# Función: list_backups
################################################################################
list_backups() {
    print_header "BACKUPS DISPONIBLES"
    
    local backup_db_dir="${SCRIPT_DIR}/backups/database"
    local backup_files_dir="${SCRIPT_DIR}/backups/files"
    
    echo -e "${CYAN}BACKUPS DE BASE DE DATOS:${NC}\n"
    if [ -d "$backup_db_dir" ] && [ "$(ls -A $backup_db_dir/*.sql.gz 2>/dev/null)" ]; then
        ls -lth "$backup_db_dir"/*.sql.gz 2>/dev/null | awk '{print "  • " $9 " - " $5 " - " $6 " " $7 " " $8}'
    else
        echo -e "  ${YELLOW}No hay backups de base de datos${NC}"
    fi
    
    echo -e "\n${CYAN}BACKUPS DE ARCHIVOS:${NC}\n"
    if [ -d "$backup_files_dir" ] && [ "$(ls -A $backup_files_dir/*.tar.gz 2>/dev/null)" ]; then
        ls -lth "$backup_files_dir"/*.tar.gz 2>/dev/null | awk '{print "  • " $9 " - " $5 " - " $6 " " $7 " " $8}'
    else
        echo -e "  ${YELLOW}No hay backups de archivos${NC}"
    fi
    
    echo ""
}

################################################################################
# Función: view_logs
################################################################################
view_logs() {
    print_header "LOGS DE MANTENIMIENTO"
    
    echo -e "${CYAN}Logs disponibles:${NC}\n"
    
    local count=1
    for log_file in $(ls -t "$LOG_DIR"/*.log 2>/dev/null | head -10); do
        echo -e "  ${count}) $(basename "$log_file")"
        ((count++))
    done
    
    echo ""
    read -p "Selecciona un log para ver (0 para volver): " selection
    
    if [ "$selection" = "0" ] || [ -z "$selection" ]; then
        return
    fi
    
    local selected_log=$(ls -t "$LOG_DIR"/*.log 2>/dev/null | sed -n "${selection}p")
    
    if [ -f "$selected_log" ]; then
        echo -e "\n${CYAN}Mostrando últimas 50 líneas de: $(basename "$selected_log")${NC}\n"
        tail -50 "$selected_log"
        echo ""
        read -p "Presiona Enter para continuar..."
    else
        echo -e "${RED}✗ Log no encontrado${NC}"
    fi
}

################################################################################
# Función: generate_full_report
################################################################################
generate_full_report() {
    print_header "GENERANDO REPORTE COMPLETO"
    
    local report_file="${LOG_DIR}/full_report_$(date +%Y%m%d_%H%M%S).txt"
    
    echo -e "${BLUE}Generando reporte completo del sistema...${NC}\n"
    
    cat > "$report_file" << EOF
═══════════════════════════════════════════════════════════════
  REPORTE COMPLETO DEL SISTEMA - FARMAYA
  Fecha: $(date '+%Y-%m-%d %H:%M:%S')
═══════════════════════════════════════════════════════════════

INFORMACIÓN DEL SISTEMA:
  • Hostname: $(hostname)
  • Sistema Operativo: $(uname -s)
  • Uptime: $(uptime -p 2>/dev/null || echo "N/A")
  • Usuario: $(whoami)

ESTADO DE SERVICIOS:
EOF

    # Verificar servicios
    echo "  • Backend: $(curl -s -f http://localhost:8080 > /dev/null 2>&1 && echo "✓ ACTIVO" || echo "✗ INACTIVO")" >> "$report_file"
    echo "  • Base de Datos: $(mysql -hlocalhost -P3306 -uroot -pJohan12315912 -e "USE farmaya" 2>/dev/null && echo "✓ ACTIVO" || echo "✗ INACTIVO")" >> "$report_file"
    
    cat >> "$report_file" << EOF

RECURSOS DEL SISTEMA:
  • Uso de CPU: $(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}')%
  • Uso de Memoria: $(free | awk 'NR==2 {printf "%.1f%%", $3/$2 * 100.0}')
  • Uso de Disco: $(df -h / | awk 'NR==2 {print $5}')

ESTADÍSTICAS DE BACKUPS:
  • Backups de BD: $(ls -1 "${SCRIPT_DIR}/backups/database"/*.sql.gz 2>/dev/null | wc -l)
  • Backups de archivos: $(ls -1 "${SCRIPT_DIR}/backups/files"/*.tar.gz 2>/dev/null | wc -l)
  • Espacio usado en backups: $(du -sh "${SCRIPT_DIR}/backups" 2>/dev/null | cut -f1 || echo "0")

LOGS Y MANTENIMIENTO:
  • Total de logs: $(find "$LOG_DIR" -name "*.log*" 2>/dev/null | wc -l)
  • Espacio usado en logs: $(du -sh "$LOG_DIR" 2>/dev/null | cut -f1)
  • Último backup: $(ls -t "${SCRIPT_DIR}/backups/database"/*.sql.gz 2>/dev/null | head -1 | xargs basename || echo "N/A")

═══════════════════════════════════════════════════════════════
EOF

    echo -e "${GREEN}✓ Reporte generado: $report_file${NC}"
    echo -e "\n${BLUE}Mostrando contenido:${NC}\n"
    cat "$report_file"
    echo ""
    
    log_message "INFO" "Reporte completo generado: $report_file"
}

################################################################################
# Función: pause
################################################################################
pause() {
    echo ""
    read -p "Presiona Enter para continuar..."
}

################################################################################
# FUNCIÓN PRINCIPAL
################################################################################
main() {
    while true; do
        print_banner
        print_menu
        
        read -p "Selecciona una opción: " option
        echo ""
        
        case $option in
            1)
                full_backup
                pause
                ;;
            2)
                run_script "backup-database.sh"
                pause
                ;;
            3)
                run_script "backup-files.sh"
                pause
                ;;
            4)
                run_script "restore-database.sh"
                pause
                ;;
            5)
                run_script "maintenance-database.sh"
                pause
                ;;
            6)
                run_script "cleanup-system.sh"
                pause
                ;;
            7)
                full_maintenance
                pause
                ;;
            8)
                run_script "monitor-system.sh"
                pause
                ;;
            9)
                run_script "monitor-system.sh" "--continuous"
                ;;
            10)
                run_script "monitor-system.sh" "--services-only"
                pause
                ;;
            11)
                list_backups
                pause
                ;;
            12)
                view_logs
                ;;
            13)
                generate_full_report
                pause
                ;;
            14)
                automatic_maintenance
                pause
                ;;
            0)
                print_header "¡HASTA PRONTO!"
                echo -e "${GREEN}Gracias por usar el Sistema de Mantenimiento de FarmaYa${NC}\n"
                log_message "INFO" "Sistema de mantenimiento cerrado"
                exit 0
                ;;
            *)
                echo -e "${RED}✗ Opción inválida${NC}"
                pause
                ;;
        esac
    done
}

# Iniciar aplicación
log_message "INFO" "Sistema de mantenimiento iniciado"
main "$@"
