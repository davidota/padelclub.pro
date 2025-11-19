#!/bin/bash

# ==============================================================================
# Script de Deployment para Padel Club Pro
# ==============================================================================

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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

# Check if required commands exist
check_requirements() {
    local missing_requirements=0

    for cmd in java mvn docker docker-compose; do
        if ! command -v $cmd &> /dev/null; then
            log_warn "Command not found: $cmd"
            missing_requirements=1
        fi
    done

    if [ $missing_requirements -eq 1 ]; then
        log_warn "Some requirements are missing. Deployment may fail."
    fi
}

# Load environment variables
load_env() {
    if [ -f .env ]; then
        log_info "Loading environment variables from .env"
        export $(cat .env | grep -v '#' | xargs)
    else
        log_warn ".env file not found. Using default values."
    fi
}

# Build application
build_app() {
    log_info "Building application..."
    mvn clean package -DskipTests -Pproduction

    if [ $? -eq 0 ]; then
        log_info "Build successful"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Run tests
run_tests() {
    log_info "Running tests..."
    mvn test

    if [ $? -eq 0 ]; then
        log_info "All tests passed"
    else
        log_error "Tests failed"
        exit 1
    fi
}

# Deploy with Docker
deploy_docker() {
    log_info "Deploying with Docker..."

    # Build Docker image
    docker build -t padelclub-pro:latest .

    # Stop existing container
    docker-compose -f docker-compose.production.yml down

    # Start new container
    docker-compose -f docker-compose.production.yml up -d

    # Wait for health check
    log_info "Waiting for application to start..."
    sleep 10

    # Check health
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        log_info "Application is healthy"
    else
        log_error "Application health check failed"
        exit 1
    fi
}

# Deploy manually
deploy_manual() {
    log_info "Deploying manually..."

    # Stop existing service
    if systemctl is-active --quiet padelclub; then
        log_info "Stopping existing service..."
        sudo systemctl stop padelclub
    fi

    # Backup current JAR
    if [ -f /opt/padelclub/app.jar ]; then
        log_info "Backing up current version..."
        sudo cp /opt/padelclub/app.jar /opt/padelclub/backups/app.jar.$(date +%Y%m%d_%H%M%S)
    fi

    # Copy new JAR
    log_info "Deploying new version..."
    sudo cp target/*.jar /opt/padelclub/app.jar

    # Start service
    log_info "Starting service..."
    sudo systemctl start padelclub

    # Check status
    sleep 5
    if systemctl is-active --quiet padelclub; then
        log_info "Service started successfully"
    else
        log_error "Service failed to start"
        sudo systemctl status padelclub
        exit 1
    fi
}

# Backup database
backup_database() {
    log_info "Creating database backup..."

    local backup_dir="./backups"
    mkdir -p $backup_dir

    local backup_file="$backup_dir/padelclub_$(date +%Y%m%d_%H%M%S).sql"

    pg_dump -U ${DATABASE_USERNAME:-padel_user} \
            -h ${DATABASE_HOST:-localhost} \
            ${DATABASE_NAME:-padelclub_prod} > $backup_file

    if [ $? -eq 0 ]; then
        log_info "Backup created: $backup_file"

        # Compress backup
        gzip $backup_file
        log_info "Backup compressed: $backup_file.gz"
    else
        log_error "Backup failed"
        exit 1
    fi
}

# Main menu
show_menu() {
    echo ""
    echo "======================================"
    echo "  Padel Club Pro - Deployment Script"
    echo "======================================"
    echo "1. Check Requirements"
    echo "2. Run Tests"
    echo "3. Build Application"
    echo "4. Deploy with Docker"
    echo "5. Deploy Manually (systemd)"
    echo "6. Backup Database"
    echo "7. Full Deployment (Docker)"
    echo "8. Full Deployment (Manual)"
    echo "9. Exit"
    echo "======================================"
    echo -n "Select option: "
}

# Main execution
main() {
    check_requirements
    load_env

    if [ $# -eq 0 ]; then
        # Interactive mode
        while true; do
            show_menu
            read option

            case $option in
                1) check_requirements ;;
                2) run_tests ;;
                3) build_app ;;
                4) deploy_docker ;;
                5) deploy_manual ;;
                6) backup_database ;;
                7)
                    backup_database
                    run_tests
                    build_app
                    deploy_docker
                    ;;
                8)
                    backup_database
                    run_tests
                    build_app
                    deploy_manual
                    ;;
                9)
                    log_info "Exiting..."
                    exit 0
                    ;;
                *)
                    log_error "Invalid option"
                    ;;
            esac

            echo ""
            read -p "Press Enter to continue..."
        done
    else
        # Command line mode
        case "$1" in
            test) run_tests ;;
            build) build_app ;;
            deploy-docker) deploy_docker ;;
            deploy-manual) deploy_manual ;;
            backup) backup_database ;;
            full-docker)
                backup_database
                run_tests
                build_app
                deploy_docker
                ;;
            full-manual)
                backup_database
                run_tests
                build_app
                deploy_manual
                ;;
            *)
                echo "Usage: $0 {test|build|deploy-docker|deploy-manual|backup|full-docker|full-manual}"
                exit 1
                ;;
        esac
    fi
}

# Run main function
main "$@"
