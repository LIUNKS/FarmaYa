#!/bin/bash

# Script de Automatización de Pruebas - FarmaYa Backend
# Ejecuta pruebas en los 4 niveles: Unitarias, Integración, Sistema y Aceptación

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"
REPORTS_DIR="$PROJECT_DIR/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Función para logging
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✓${NC} $1"
}

log_error() {
    echo -e "${RED}✗${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Función para verificar dependencias
check_dependencies() {
    log "Verificando dependencias..."

    if ! command -v java &> /dev/null; then
        log_error "Java no está instalado"
        exit 1
    fi

    if ! command -v mvn &> /dev/null; then
        log_error "Maven no está instalado"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        log_warning "Docker no está disponible - las pruebas de sistema pueden fallar"
    fi

    log_success "Dependencias verificadas"
}

# Función para limpiar reportes anteriores
clean_reports() {
    log "Limpiando reportes anteriores..."
    rm -rf "$REPORTS_DIR"
    mkdir -p "$REPORTS_DIR/unit"
    mkdir -p "$REPORTS_DIR/integration"
    mkdir -p "$REPORTS_DIR/system"
    mkdir -p "$REPORTS_DIR/acceptance"
    mkdir -p "$REPORTS_DIR/coverage"
    log_success "Reportes limpiados"
}

# Función para ejecutar pruebas unitarias
run_unit_tests() {
    log "Ejecutando pruebas unitarias..."

    cd "$BACKEND_DIR"

    # Ejecutar solo pruebas unitarias (excluyendo integración y sistema)
    mvn test \
        -Dtest="*Test" \
        -Dgroups="!integration,!system,!acceptance" \
        -Dspring.profiles.active=test \
        -Djacoco.skip=false \
        -Djacoco.destFile="$REPORTS_DIR/coverage/jacoco-unit.exec" \
        -Dmaven.test.failure.ignore=false \
        -q

    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        log_success "Pruebas unitarias completadas exitosamente"
    else
        log_error "Pruebas unitarias fallaron"
        return $exit_code
    fi
}

# Función para ejecutar pruebas de integración
run_integration_tests() {
    log "Ejecutando pruebas de integración..."

    cd "$BACKEND_DIR"

    # Ejecutar pruebas de integración
    mvn test \
        -Dtest="*IT,*IntegrationTest" \
        -Dgroups="integration" \
        -Dspring.profiles.active=test \
        -Djacoco.skip=false \
        -Djacoco.destFile="$REPORTS_DIR/coverage/jacoco-integration.exec" \
        -Dmaven.test.failure.ignore=false \
        -q

    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        log_success "Pruebas de integración completadas exitosamente"
    else
        log_error "Pruebas de integración fallaron"
        return $exit_code
    fi
}

# Función para ejecutar pruebas de sistema
run_system_tests() {
    log "Ejecutando pruebas de sistema..."

    cd "$BACKEND_DIR"

    # Verificar si Docker está disponible
    if ! command -v docker &> /dev/null; then
        log_warning "Docker no disponible - omitiendo pruebas de sistema"
        return 0
    fi

    # Ejecutar pruebas de sistema con TestContainers
    mvn test \
        -Dtest="*SystemTest,*E2ETest" \
        -Dgroups="system" \
        -Dspring.profiles.active=test \
        -Djacoco.skip=false \
        -Djacoco.destFile="$REPORTS_DIR/coverage/jacoco-system.exec" \
        -Dmaven.test.failure.ignore=false \
        -q

    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        log_success "Pruebas de sistema completadas exitosamente"
    else
        log_error "Pruebas de sistema fallaron"
        return $exit_code
    fi
}

# Función para ejecutar pruebas de aceptación
run_acceptance_tests() {
    log "Ejecutando pruebas de aceptación..."

    cd "$BACKEND_DIR"

    # Ejecutar pruebas BDD con Cucumber
    mvn test \
        -Dtest="*AcceptanceTest,*CucumberTest" \
        -Dcucumber.options="--plugin pretty --plugin html:$REPORTS_DIR/acceptance/cucumber-report.html --plugin json:$REPORTS_DIR/acceptance/cucumber-report.json" \
        -Dgroups="acceptance" \
        -Dspring.profiles.active=test \
        -Dmaven.test.failure.ignore=false \
        -q

    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        log_success "Pruebas de aceptación completadas exitosamente"
    else
        log_error "Pruebas de aceptación fallaron"
        return $exit_code
    fi
}

# Función para generar reportes de cobertura
generate_coverage_report() {
    log "Generando reporte de cobertura..."

    cd "$BACKEND_DIR"

    # Generar reporte de JaCoCo
    mvn jacoco:report \
        -Djacoco.dataFile="$REPORTS_DIR/coverage/jacoco-unit.exec" \
        -Djacoco.outputDirectory="$REPORTS_DIR/coverage/unit" \
        -q

    mvn jacoco:report \
        -Djacoco.dataFile="$REPORTS_DIR/coverage/jacoco-integration.exec" \
        -Djacoco.outputDirectory="$REPORTS_DIR/coverage/integration" \
        -q

    # Combinar reportes de cobertura
    mvn jacoco:merge \
        -Djacoco.sources="$PROJECT_DIR/backend/src/main/java" \
        -Djacoco.destFile="$REPORTS_DIR/coverage/jacoco-merged.exec" \
        -Djacoco.fileSets="$REPORTS_DIR/coverage/jacoco-unit.exec,$REPORTS_DIR/coverage/jacoco-integration.exec,$REPORTS_DIR/coverage/jacoco-system.exec" \
        -q 2>/dev/null || true

    mvn jacoco:report \
        -Djacoco.dataFile="$REPORTS_DIR/coverage/jacoco-merged.exec" \
        -Djacoco.outputDirectory="$REPORTS_DIR/coverage/merged" \
        -q

    log_success "Reporte de cobertura generado"
}

# Función para generar reporte final
generate_final_report() {
    log "Generando reporte final..."

    local report_file="$REPORTS_DIR/test-summary-$TIMESTAMP.md"

    cat > "$report_file" << EOF
# Reporte de Pruebas - FarmaYa Backend
**Fecha:** $(date)
**Proyecto:** FarmaYa E-commerce
**Versión:** 0.0.1-SNAPSHOT

## Resumen Ejecutivo

Se ejecutaron pruebas automatizadas en los 4 niveles requeridos:

### ✅ Pruebas Unitarias
- **Estado:** $([ -f "$REPORTS_DIR/unit/surefire-reports/TEST-*.xml" ] && echo "Completado" || echo "Pendiente")
- **Cobertura:** Ver reporte de JaCoCo en \`$REPORTS_DIR/coverage/unit/index.html\`

### ✅ Pruebas de Integración
- **Estado:** $([ -f "$REPORTS_DIR/integration/surefire-reports/TEST-*.xml" ] && echo "Completado" || echo "Pendiente")
- **Cobertura:** Ver reporte de JaCoCo en \`$REPORTS_DIR/coverage/integration/index.html\`

### ✅ Pruebas de Sistema
- **Estado:** $([ -f "$REPORTS_DIR/system/surefire-reports/TEST-*.xml" ] && echo "Completado" || echo "Pendiente")
- **Tecnología:** TestContainers con MySQL

### ✅ Pruebas de Aceptación
- **Estado:** $([ -f "$REPORTS_DIR/acceptance/cucumber-report.html" ] && echo "Completado" || echo "Pendiente")
- **Framework:** Cucumber BDD
- **Reporte:** \`$REPORTS_DIR/acceptance/cucumber-report.html\`

## Cobertura General
- **Reporte Combinado:** \`$REPORTS_DIR/coverage/merged/index.html\`

## Archivos de Reporte
- **Unitarias:** \`$REPORTS_DIR/unit/\`
- **Integración:** \`$REPORTS_DIR/integration/\`
- **Sistema:** \`$REPORTS_DIR/system/\`
- **Aceptación:** \`$REPORTS_DIR/acceptance/\`
- **Cobertura:** \`$REPORTS_DIR/coverage/\`

## Próximos Pasos
1. Revisar reportes de cobertura para identificar áreas sin testear
2. Implementar pruebas faltantes según prioridades
3. Configurar CI/CD para ejecución automática
4. Establecer umbrales mínimos de cobertura (recomendado: 80%)

---
*Reporte generado automáticamente por el script de pruebas*
EOF

    log_success "Reporte final generado: $report_file"
}

# Función principal
main() {
    log "🚀 Iniciando suite de pruebas automatizadas - FarmaYa Backend"
    log "=================================================="

    # Verificar argumentos
    local run_all=true
    local run_unit=false
    local run_integration=false
    local run_system=false
    local run_acceptance=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --unit)
                run_all=false
                run_unit=true
                shift
                ;;
            --integration)
                run_all=false
                run_integration=true
                shift
                ;;
            --system)
                run_all=false
                run_system=true
                shift
                ;;
            --acceptance)
                run_all=false
                run_acceptance=true
                shift
                ;;
            --help)
                echo "Uso: $0 [opciones]"
                echo ""
                echo "Opciones:"
                echo "  --unit         Ejecutar solo pruebas unitarias"
                echo "  --integration  Ejecutar solo pruebas de integración"
                echo "  --system       Ejecutar solo pruebas de sistema"
                echo "  --acceptance   Ejecutar solo pruebas de aceptación"
                echo "  --help         Mostrar esta ayuda"
                echo ""
                echo "Sin opciones ejecuta todas las pruebas"
                exit 0
                ;;
            *)
                log_error "Opción desconocida: $1"
                exit 1
                ;;
        esac
    done

    # Preparación
    check_dependencies
    clean_reports

    # Ejecutar pruebas según configuración
    local exit_code=0

    if [ "$run_all" = true ] || [ "$run_unit" = true ]; then
        run_unit_tests || exit_code=$?
    fi

    if [ "$run_all" = true ] || [ "$run_integration" = true ]; then
        run_integration_tests || exit_code=$?
    fi

    if [ "$run_all" = true ] || [ "$run_system" = true ]; then
        run_system_tests || exit_code=$?
    fi

    if [ "$run_all" = true ] || [ "$run_acceptance" = true ]; then
        run_acceptance_tests || exit_code=$?
    fi

    # Generar reportes
    generate_coverage_report
    generate_final_report

    # Resultado final
    if [ $exit_code -eq 0 ]; then
        log_success "🎉 Todas las pruebas completadas exitosamente!"
        log "📊 Reportes disponibles en: $REPORTS_DIR"
    else
        log_error "❌ Algunas pruebas fallaron. Revisa los reportes en: $REPORTS_DIR"
    fi

    return $exit_code
}

# Ejecutar función principal
main "$@"