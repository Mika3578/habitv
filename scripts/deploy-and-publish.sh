#!/bin/bash
# Combined deploy and publish script for habiTv
# This script runs 'mvn deploy' and then copies artifacts to GitHub Pages

# Default values
MAVEN_GOALS="clean deploy"
COMMIT_MESSAGE="Deploy Maven artifacts to GitHub Pages"

# Function to print colored output
print_info() {
    echo -e "\033[32m=== habiTv Deploy and Publish Script ===\033[0m"
}

print_error() {
    echo -e "\033[31mERROR: $1\033[0m"
}

print_success() {
    echo -e "\033[32m$1\033[0m"
}

print_warning() {
    echo -e "\033[33m$1\033[0m"
}

print_cyan() {
    echo -e "\033[36m$1\033[0m"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --maven-goals)
            MAVEN_GOALS="$2"
            shift 2
            ;;
        --commit-message)
            COMMIT_MESSAGE="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --maven-goals GOALS    Maven goals to run (default: clean deploy)"
            echo "  --commit-message MSG   Git commit message"
            echo "  -h, --help             Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

print_info

# Step 1: Run Maven deploy
print_warning "Step 1: Running Maven deploy..."
print_cyan "Command: mvn $MAVEN_GOALS"

if mvn $MAVEN_GOALS; then
    print_success "Maven deploy completed successfully!"
else
    print_error "Maven deploy failed with exit code $?"
    print_warning "Please fix any build errors and try again."
    exit 1
fi

# Step 2: Run post-deploy script
print_warning "Step 2: Publishing artifacts to GitHub Pages..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
POST_DEPLOY_SCRIPT="$SCRIPT_DIR/post-deploy.sh"

if [[ -f "$POST_DEPLOY_SCRIPT" ]]; then
    if "$POST_DEPLOY_SCRIPT" --commit-message "$COMMIT_MESSAGE"; then
        print_success "Post-deploy script completed successfully!"
    else
        print_error "Post-deploy script failed with exit code $?"
        exit 1
    fi
else
    print_error "Post-deploy script not found: $POST_DEPLOY_SCRIPT"
    exit 1
fi

print_success "=== Deploy and publish completed successfully ==="
print_cyan "Your Maven artifacts are now available at: https://mika3578.github.io/habitv/repository/" 