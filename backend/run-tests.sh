#!/bin/bash

# FarmaYa Backend - Comprehensive Test Suite Runner
# This script runs all 4 levels of automated testing: Unit, Integration, System, and Acceptance

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORTS_DIR="$PROJECT_DIR/target/test-reports"
COVERAGE_DIR="$REPORTS_DIR/coverage"
UNIT_REPORTS_DIR="$REPORTS_DIR/unit"
INTEGRATION_REPORTS_DIR="$REPORTS_DIR/integration"
SYSTEM_REPORTS_DIR="$REPORTS_DIR/system"
ACCEPTANCE_REPORTS_DIR="$REPORTS_DIR/acceptance"

# Test flags
RUN_UNIT=true
RUN_INTEGRATION=true
RUN_SYSTEM=true
RUN_ACCEPTANCE=true
GENERATE_COVERAGE=true

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --unit-only)
            RUN_INTEGRATION=false
            RUN_SYSTEM=false
            RUN_ACCEPTANCE=false
            shift
            ;;
        --integration-only)
            RUN_UNIT=false
            RUN_SYSTEM=false
            RUN_ACCEPTANCE=false
            shift
            ;;
        --system-only)
            RUN_UNIT=false
            RUN_INTEGRATION=false
            RUN_ACCEPTANCE=false
            shift
            ;;
        --acceptance-only)
            RUN_UNIT=false
            RUN_INTEGRATION=false
            RUN_SYSTEM=false
            shift
            ;;
        --no-coverage)
            GENERATE_COVERAGE=false
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --unit-only         Run only unit tests"
            echo "  --integration-only  Run only integration tests"
            echo "  --system-only       Run only system tests"
            echo "  --acceptance-only   Run only acceptance tests"
            echo "  --no-coverage       Skip coverage report generation"
            echo "  --help              Show this help message"
            echo ""
            echo "By default, runs all test levels with coverage."
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Use --help for usage information."
            exit 1
            ;;
    esac
done

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_header() {
    echo -e "${PURPLE}================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}================================${NC}"
}

# Check prerequisites
check_prerequisites() {
    log_header "Checking Prerequisites"

    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        exit 1
    fi

    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed or not in PATH"
        exit 1
    fi

    # Check if Docker is available (for system tests)
    if $RUN_SYSTEM && ! command -v docker &> /dev/null; then
        log_warning "Docker is not available. System tests will be skipped."
        RUN_SYSTEM=false
    fi

    log_success "Prerequisites check completed"
}

# Create reports directory
create_reports_dir() {
    log_info "Creating reports directory: $REPORTS_DIR"
    mkdir -p "$REPORTS_DIR"
    mkdir -p "$COVERAGE_DIR"
    mkdir -p "$UNIT_REPORTS_DIR"
    mkdir -p "$INTEGRATION_REPORTS_DIR"
    mkdir -p "$SYSTEM_REPORTS_DIR"
    mkdir -p "$ACCEPTANCE_REPORTS_DIR"
}

# Run unit tests
run_unit_tests() {
    if ! $RUN_UNIT; then
        log_info "Skipping unit tests"
        return 0
    fi

    log_header "Running Unit Tests"

    local start_time=$(date +%s)

    log_info "Executing unit tests with Maven Surefire..."
    if mvn test -Dtest="*Test" -DfailIfNoTests=false \
        -Dspring.profiles.active=test \
        -Dmaven.test.failure.ignore=true \
        -Dmaven.test.redirectTestOutputToFile=true \
        -Dmaven.test.reportsDirectory="$UNIT_REPORTS_DIR" \
        -q; then

        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_success "Unit tests completed successfully in ${duration}s"
        return 0
    else
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_error "Unit tests failed after ${duration}s"
        return 1
    fi
}

# Run integration tests
run_integration_tests() {
    if ! $RUN_INTEGRATION; then
        log_info "Skipping integration tests"
        return 0
    fi

    log_header "Running Integration Tests"

    local start_time=$(date +%s)

    log_info "Executing integration tests with Maven Failsafe..."
    if mvn verify -Dtest="*IntegrationTest" -DfailIfNoTests=false \
        -Dspring.profiles.active=test \
        -Dmaven.test.failure.ignore=true \
        -Dmaven.test.redirectTestOutputToFile=true \
        -Dmaven.failsafe.reportsDirectory="$INTEGRATION_REPORTS_DIR" \
        -q; then

        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_success "Integration tests completed successfully in ${duration}s"
        return 0
    else
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_error "Integration tests failed after ${duration}s"
        return 1
    fi
}

# Run system tests
run_system_tests() {
    if ! $RUN_SYSTEM; then
        log_info "Skipping system tests"
        return 0
    fi

    log_header "Running System Tests"

    local start_time=$(date +%s)

    log_info "Executing system tests with TestContainers..."
    if mvn test -Dtest="*SystemTest" -DfailIfNoTests=false \
        -Dspring.profiles.active=test \
        -Dmaven.test.failure.ignore=true \
        -Dmaven.test.redirectTestOutputToFile=true \
        -Dmaven.test.reportsDirectory="$SYSTEM_REPORTS_DIR" \
        -q; then

        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_success "System tests completed successfully in ${duration}s"
        return 0
    else
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_error "System tests failed after ${duration}s"
        return 1
    fi
}

# Run acceptance tests
run_acceptance_tests() {
    if ! $RUN_ACCEPTANCE; then
        log_info "Skipping acceptance tests"
        return 0
    fi

    log_header "Running Acceptance Tests (Cucumber BDD)"

    local start_time=$(date +%s)

    log_info "Executing acceptance tests with Cucumber..."
    if mvn test -Dtest="*CucumberTestRunner" -DfailIfNoTests=false \
        -Dspring.profiles.active=test \
        -Dmaven.test.failure.ignore=true \
        -Dmaven.test.redirectTestOutputToFile=true \
        -Dmaven.test.reportsDirectory="$ACCEPTANCE_REPORTS_DIR" \
        -Dcucumber.options="--plugin pretty --plugin html:$ACCEPTANCE_REPORTS_DIR/cucumber-report.html --plugin json:$ACCEPTANCE_REPORTS_DIR/cucumber-report.json" \
        -q; then

        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_success "Acceptance tests completed successfully in ${duration}s"
        return 0
    else
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_error "Acceptance tests failed after ${duration}s"
        return 1
    fi
}

# Generate coverage report
generate_coverage_report() {
    if ! $GENERATE_COVERAGE; then
        log_info "Skipping coverage report generation"
        return 0
    fi

    log_header "Generating Code Coverage Report"

    local start_time=$(date +%s)

    log_info "Generating JaCoCo coverage report..."
    if mvn jacoco:report -q; then
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_success "Coverage report generated in ${duration}s"
        log_info "Coverage report available at: $COVERAGE_DIR/index.html"
        return 0
    else
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        log_error "Coverage report generation failed after ${duration}s"
        return 1
    fi
}

# Generate summary report
generate_summary_report() {
    log_header "Test Execution Summary"

    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0

    # Count test results from surefire and failsafe reports
    if [ -d "$PROJECT_DIR/target/surefire-reports" ]; then
        while IFS= read -r line; do
            if [[ $line =~ Tests\ run:\ ([0-9]+),\ Failures:\ ([0-9]+),\ Errors:\ ([0-9]+),\ Skipped:\ ([0-9]+) ]]; then
                local tests=${BASH_REMATCH[1]}
                local failures=${BASH_REMATCH[2]}
                local errors=${BASH_REMATCH[3]}
                local skipped=${BASH_REMATCH[4]}

                total_tests=$((total_tests + tests))
                passed_tests=$((passed_tests + tests - failures - errors))
                failed_tests=$((failed_tests + failures + errors))
                skipped_tests=$((skipped_tests + skipped))
            fi
        done < <(find "$PROJECT_DIR/target/surefire-reports" -name "*.txt" -exec grep "Tests run:" {} \;)
    fi

    if [ -d "$PROJECT_DIR/target/failsafe-reports" ]; then
        while IFS= read -r line; do
            if [[ $line =~ Tests\ run:\ ([0-9]+),\ Failures:\ ([0-9]+),\ Errors:\ ([0-9]+),\ Skipped:\ ([0-9]+) ]]; then
                local tests=${BASH_REMATCH[1]}
                local failures=${BASH_REMATCH[2]}
                local errors=${BASH_REMATCH[3]}
                local skipped=${BASH_REMATCH[4]}

                total_tests=$((total_tests + tests))
                passed_tests=$((passed_tests + tests - failures - errors))
                failed_tests=$((failed_tests + failures + errors))
                skipped_tests=$((skipped_tests + skipped))
            fi
        done < <(find "$PROJECT_DIR/target/failsafe-reports" -name "*.txt" -exec grep "Tests run:" {} \;)
    fi

    echo ""
    echo -e "${CYAN}Test Results Summary:${NC}"
    echo -e "  Total Tests: $total_tests"
    echo -e "  ${GREEN}Passed: $passed_tests${NC}"
    echo -e "  ${RED}Failed: $failed_tests${NC}"
    echo -e "  ${YELLOW}Skipped: $skipped_tests${NC}"
    echo ""

    if [ -f "$PROJECT_DIR/target/site/jacoco/index.html" ]; then
        echo -e "${CYAN}Coverage Report:${NC} file://$PROJECT_DIR/target/site/jacoco/index.html"
    fi

    if [ -d "$ACCEPTANCE_REPORTS_DIR" ] && [ -f "$ACCEPTANCE_REPORTS_DIR/cucumber-report.html" ]; then
        echo -e "${CYAN}Cucumber Report:${NC} file://$ACCEPTANCE_REPORTS_DIR/cucumber-report.html"
    fi

    echo ""
    echo -e "${CYAN}Reports Directory:${NC} $REPORTS_DIR"
}

# Main execution
main() {
    local start_time=$(date +%s)
    local test_failures=0

    log_header "FarmaYa Backend Test Suite"
    log_info "Starting comprehensive test execution..."

    check_prerequisites
    create_reports_dir

    # Run test suites
    run_unit_tests || ((test_failures++))
    run_integration_tests || ((test_failures++))
    run_system_tests || ((test_failures++))
    run_acceptance_tests || ((test_failures++))

    # Generate reports
    generate_coverage_report

    # Generate summary
    generate_summary_report

    local end_time=$(date +%s)
    local total_duration=$((end_time - start_time))

    echo ""
    if [ $test_failures -eq 0 ]; then
        log_success "All test suites completed successfully in ${total_duration}s!"
        exit 0
    else
        log_error "$test_failures test suite(s) failed. Total execution time: ${total_duration}s"
        exit 1
    fi
}

# Run main function
main "$@"