#!/bin/bash
# Post-deploy script for habiTv Maven artifacts
# This script copies updated artifacts from ~/.m2/repository to docs/repository
# and commits them to git for GitHub Pages deployment

# Default values
MAVEN_REPO_PATH="${HOME}/.m2/repository"
TARGET_PATH="docs/repository"
GROUP_ID="com/dabi/habitv"
COMMIT_MESSAGE="Deploy Maven artifacts to GitHub Pages"

# Function to print colored output
print_info() {
    echo -e "\033[32m=== habiTv Post-Deploy Script ===\033[0m"
    echo -e "\033[33mCopying Maven artifacts to GitHub Pages repository...\033[0m"
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
        --maven-repo)
            MAVEN_REPO_PATH="$2"
            shift 2
            ;;
        --target-path)
            TARGET_PATH="$2"
            shift 2
            ;;
        --group-id)
            GROUP_ID="$2"
            shift 2
            ;;
        --commit-message)
            COMMIT_MESSAGE="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --maven-repo PATH     Maven repository path (default: ~/.m2/repository)"
            echo "  --target-path PATH    Target path in git repo (default: docs/repository)"
            echo "  --group-id ID         Group ID path (default: com/dabi/habitv)"
            echo "  --commit-message MSG  Git commit message"
            echo "  -h, --help            Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

print_info

# Construct source and target paths
SOURCE_PATH="${MAVEN_REPO_PATH}/${GROUP_ID}"
TARGET_GROUP_PATH="${TARGET_PATH}/${GROUP_ID}"

# Check if source directory exists
if [[ ! -d "$SOURCE_PATH" ]]; then
    print_error "Source directory not found: $SOURCE_PATH"
    print_warning "Make sure you have run 'mvn deploy' first to generate artifacts."
    exit 1
fi

# Create target directory if it doesn't exist
if [[ ! -d "$TARGET_GROUP_PATH" ]]; then
    mkdir -p "$TARGET_GROUP_PATH"
    print_cyan "Created target directory: $TARGET_GROUP_PATH"
fi

# Copy artifacts recursively
if cp -r "$SOURCE_PATH"/* "$TARGET_GROUP_PATH"/ 2>/dev/null; then
    print_success "Successfully copied artifacts from $SOURCE_PATH to $TARGET_GROUP_PATH"
else
    print_error "Failed to copy artifacts"
    exit 1
fi

# Check if there are any changes to commit
if [[ -z "$(git status --porcelain "$TARGET_PATH" 2>/dev/null)" ]]; then
    print_warning "No changes detected in $TARGET_PATH"
    print_warning "Artifacts may already be up to date."
    exit 0
fi

# Add changes to git
print_warning "Adding changes to git..."
if ! git add "$TARGET_PATH"/*; then
    print_error "Failed to add changes to git"
    exit 1
fi

# Commit changes
print_warning "Committing changes..."
if git commit -m "$COMMIT_MESSAGE"; then
    print_success "Successfully committed changes"
else
    print_error "Failed to commit changes"
    exit 1
fi

# Push to remote repository
print_warning "Pushing to remote repository..."
if git push; then
    print_success "Successfully pushed changes to remote repository"
    print_cyan "Maven artifacts are now available at: https://mika3578.github.io/habitv/repository/"
else
    print_error "Failed to push changes to remote repository"
    exit 1
fi

print_success "=== Post-deploy script completed successfully ===" 