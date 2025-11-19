#!/bin/bash

# ==============================================================================
# Health Check Script para Padel Club Pro
# ==============================================================================

# Configuration
APP_URL="${APP_URL:-http://localhost:8080}"
MAX_RETRIES=30
RETRY_DELAY=2

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check health endpoint
check_health() {
    local url="$APP_URL/actuator/health"
    local response

    response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null)
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | head -n-1)

    if [ "$http_code" = "200" ]; then
        local status=$(echo "$body" | grep -o '"status":"[^"]*' | cut -d'"' -f4)
        if [ "$status" = "UP" ]; then
            return 0
        fi
    fi
    return 1
}

# Check database connectivity
check_database() {
    local url="$APP_URL/actuator/health/db"
    local response

    response=$(curl -s "$url" 2>/dev/null)
    local status=$(echo "$response" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

    if [ "$status" = "UP" ]; then
        log_info "Database: OK"
        return 0
    else
        log_error "Database: FAIL"
        return 1
    fi
}

# Check disk space
check_disk_space() {
    local url="$APP_URL/actuator/health/diskSpace"
    local response

    response=$(curl -s "$url" 2>/dev/null)
    local status=$(echo "$response" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

    if [ "$status" = "UP" ]; then
        log_info "Disk Space: OK"
        return 0
    else
        log_error "Disk Space: FAIL"
        return 1
    fi
}

# Wait for application to be healthy
wait_for_health() {
    log_info "Waiting for application to be healthy..."

    local retries=0
    while [ $retries -lt $MAX_RETRIES ]; do
        if check_health; then
            log_info "Application is healthy!"
            return 0
        fi

        retries=$((retries + 1))
        if [ $retries -lt $MAX_RETRIES ]; then
            echo -n "."
            sleep $RETRY_DELAY
        fi
    done

    log_error "Application failed to become healthy after $MAX_RETRIES retries"
    return 1
}

# Get application metrics
get_metrics() {
    local url="$APP_URL/actuator/metrics"

    log_info "Fetching application metrics..."

    # JVM Memory
    local jvm_memory=$(curl -s "$url/jvm.memory.used" | grep -o '"value":[0-9.]*' | cut -d: -f2)
    local jvm_memory_mb=$(echo "scale=2; $jvm_memory / 1048576" | bc)
    log_info "JVM Memory Used: ${jvm_memory_mb} MB"

    # HTTP Requests
    local http_requests=$(curl -s "$url/http.server.requests" | grep -o '"count":[0-9]*' | head -1 | cut -d: -f2)
    log_info "Total HTTP Requests: ${http_requests:-0}"

    # Active Threads
    local threads=$(curl -s "$url/jvm.threads.live" | grep -o '"value":[0-9.]*' | cut -d: -f2)
    log_info "Active Threads: ${threads:-0}"
}

# Get application info
get_info() {
    local url="$APP_URL/actuator/info"

    log_info "Application Information:"
    curl -s "$url" | grep -E '"(name|version|description)"' | sed 's/^/  /'
}

# Full health check
full_check() {
    echo "======================================"
    echo "  Padel Club Pro - Health Check"
    echo "  URL: $APP_URL"
    echo "======================================"
    echo ""

    # Basic health
    log_info "Checking application health..."
    if check_health; then
        log_info "Overall Health: UP"
    else
        log_error "Overall Health: DOWN"
        return 1
    fi

    echo ""

    # Database
    log_info "Checking database..."
    check_database

    echo ""

    # Disk space
    log_info "Checking disk space..."
    check_disk_space

    echo ""

    # Metrics
    get_metrics

    echo ""

    # Info
    get_info

    echo ""
    echo "======================================"
    log_info "Health check completed"
    echo "======================================"
}

# Main execution
main() {
    case "${1:-check}" in
        wait)
            wait_for_health
            ;;
        full)
            full_check
            ;;
        check)
            if check_health; then
                log_info "Application is healthy"
                exit 0
            else
                log_error "Application is not healthy"
                exit 1
            fi
            ;;
        metrics)
            get_metrics
            ;;
        info)
            get_info
            ;;
        *)
            echo "Usage: $0 {check|wait|full|metrics|info}"
            echo ""
            echo "  check   - Quick health check (default)"
            echo "  wait    - Wait for application to be healthy"
            echo "  full    - Full health check with details"
            echo "  metrics - Display application metrics"
            echo "  info    - Display application info"
            exit 1
            ;;
    esac
}

main "$@"
