# habiTv Deployment Scripts

This directory contains scripts for automating the Maven deployment workflow for habiTv.

## Overview

The deployment workflow copies Maven artifacts from the local repository (`~/.m2/repository`) to the GitHub Pages repository (`docs/repository`) and commits them to git for public distribution.

## Scripts

### PowerShell Scripts (Windows)

#### `deploy-and-publish.ps1`
Combined script that runs `mvn deploy` and then copies artifacts to GitHub Pages.

**Usage:**
```powershell
# Default deployment
.\deploy-and-publish.ps1

# With custom commit message
.\deploy-and-publish.ps1 -CommitMessage "Release version 4.1.0"

# With custom Maven goals
.\deploy-and-publish.ps1 -MavenGoals "clean deploy -DskipTests"
```

#### `post-deploy.ps1`
Post-deployment script that copies artifacts and commits them to git.

**Usage:**
```powershell
# After running mvn deploy manually
.\post-deploy.ps1

# With custom commit message
.\post-deploy.ps1 -CommitMessage "Custom commit message"
```

### Bash Scripts (Linux/macOS)

#### `deploy-and-publish.sh`
Combined script that runs `mvn deploy` and then copies artifacts to GitHub Pages.

**Usage:**
```bash
# Make executable (first time only)
chmod +x deploy-and-publish.sh

# Default deployment
./deploy-and-publish.sh

# With custom commit message
./deploy-and-publish.sh --commit-message "Release version 4.1.0"

# With custom Maven goals
./deploy-and-publish.sh --maven-goals "clean deploy -DskipTests"
```

#### `post-deploy.sh`
Post-deployment script that copies artifacts and commits them to git.

**Usage:**
```bash
# Make executable (first time only)
chmod +x post-deploy.sh

# After running mvn deploy manually
./post-deploy.sh

# With custom commit message
./post-deploy.sh --commit-message "Custom commit message"

# Show help
./post-deploy.sh --help
```

## Workflow

### Automated Workflow
1. Run the combined script (`deploy-and-publish.ps1` or `deploy-and-publish.sh`)
2. Script automatically:
   - Runs `mvn deploy` to build and deploy to local repository
   - Copies artifacts from `~/.m2/repository/com/dabi/habitv/` to `docs/repository/com/dabi/habitv/`
   - Commits changes to git
   - Pushes to GitHub Pages

### Manual Workflow
1. Run `mvn deploy` manually
2. Run the post-deploy script (`post-deploy.ps1` or `post-deploy.sh`)
3. Script automatically:
   - Copies artifacts to `docs/repository/`
   - Commits and pushes changes

### Manual Steps (No Scripts)
```bash
# Step 1: Deploy to local repository
mvn clean deploy

# Step 2: Copy artifacts
cp -r ~/.m2/repository/com/dabi/habitv/* docs/repository/com/dabi/habitv/

# Step 3: Commit and push
git add docs/repository/com/dabi/habitv/
git commit -m "Deploy Maven artifacts to GitHub Pages"
git push
```

## Configuration

### Script Parameters

#### PowerShell Scripts
- `-MavenGoals`: Maven goals to run (default: "clean deploy")
- `-CommitMessage`: Git commit message (default: "Deploy Maven artifacts to GitHub Pages")
- `-MavenRepoPath`: Local Maven repository path (default: `$env:USERPROFILE\.m2\repository`)
- `-TargetPath`: Target path in git repo (default: "docs\repository")
- `-GroupId`: Group ID path (default: "com\dabi\habitv")

#### Bash Scripts
- `--maven-goals`: Maven goals to run (default: "clean deploy")
- `--commit-message`: Git commit message (default: "Deploy Maven artifacts to GitHub Pages")
- `--maven-repo`: Local Maven repository path (default: `$HOME/.m2/repository`)
- `--target-path`: Target path in git repo (default: "docs/repository")
- `--group-id`: Group ID path (default: "com/dabi/habitv")

### Environment Variables

The scripts use standard environment variables:
- `USERPROFILE` (Windows) or `HOME` (Linux/macOS) for Maven repository path
- Git configuration for commit author information

## Troubleshooting

### Common Issues

1. **Script Not Found**: Ensure you're running from the project root directory
2. **Permission Denied**: On Linux/macOS, make scripts executable with `chmod +x scripts/*.sh`
3. **Maven Not Found**: Ensure Maven is installed and in your PATH
4. **Git Push Fails**: Check repository permissions and network connectivity
5. **Artifacts Not Found**: Ensure `mvn deploy` completed successfully

### Error Messages

- **"Source directory not found"**: Run `mvn deploy` first to generate artifacts
- **"No changes detected"**: Artifacts may already be up to date
- **"Failed to commit changes"**: Check git configuration and repository status
- **"Failed to push changes"**: Check network connectivity and repository permissions

### Verification

After successful deployment, verify artifacts are available at:
```
https://mika3578.github.io/habitv/repository/com/dabi/habitv/
```

## Security

- Scripts only copy artifacts from local Maven repository
- No external repositories or FTP servers required
- All distribution through secure GitHub Pages (HTTPS)
- Git-based version control for artifact management

## Support

For issues with the deployment workflow:
1. Check the [Developer Guide](../docs/DEVELOPER_GUIDE.md#deployment-workflow)
2. Review the [Installation Guide](../docs/INSTALLATION.md#deployment-workflow-for-maintainers)
3. Check the troubleshooting section above
4. Report issues on the GitHub repository 