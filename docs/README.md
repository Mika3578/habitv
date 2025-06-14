# habiTv Documentation

This directory contains comprehensive documentation for the habiTv project.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: June 15, 2025

## Overview

habiTv is a comprehensive video downloader and manager that supports multiple platforms and formats. This documentation provides comprehensive information about installation, configuration, plugin development, and advanced usage.

## Quick Start

### Installation
- [Installation Guide](INSTALLATION.md) - Complete installation instructions
- [System Requirements](INSTALLATION.md#system-requirements) - Hardware and software requirements
- [Dependencies](INSTALLATION.md#dependencies) - Required external tools

### Configuration
- [Configuration Reference](CONFIGURATION_REFERENCE.md) - Complete configuration options
- [Plugin Configuration](PLUGIN_CONFIGURATION.md) - Plugin-specific settings
- [Security Settings](SECURITY.md) - Security and privacy configuration

## Core Features

### Video Download
- **Multi-Platform Support**: YouTube, Arte, and many other platforms
- **Format Selection**: Choose from multiple video and audio formats
- **Quality Control**: Download in preferred quality settings
- **Batch Processing**: Download multiple videos simultaneously

### Plugin System
- **Extensible Architecture**: Easy to add new platforms
- [Plugin Development Guide](PLUGIN_DEVELOPMENT.md) - Create custom plugins
- [Plugin Examples](PLUGIN_EXAMPLES.md) - Working plugin examples
- [Plugin API Reference](PLUGIN_API.md) - Complete API documentation

### Update System
- **Automatic Updates**: Keep habiTv and plugins up to date
- [Startup Update System](STARTUP_UPDATE_SYSTEM.md) - How updates work
- **Repository Management**: Manage external tool repositories
- [GitHub Pages HTTPS Mirror](GITHUB_PAGES_MIRROR.md) - Secure repository mirror

## Advanced Topics

### Development
- [Plugin Development](PLUGIN_DEVELOPMENT.md) - Create and extend plugins
- [API Reference](API_REFERENCE.md) - Complete API documentation
- [Architecture Overview](ARCHITECTURE.md) - System design and components
- [Contributing Guidelines](CONTRIBUTING.md) - How to contribute

### Deployment
- [Plugin Deployment](PLUGIN_DEPLOYMENT.md) - Deploy custom plugins
- [Plugin Publishing](PLUGIN_PUBLISHING.md) - Publish plugins to repository
- [Repository Management](REPOSITORY_MANAGEMENT.md) - Manage tool repositories

### Security
- [Security Guide](SECURITY.md) - Security best practices
- [Privacy Configuration](PRIVACY.md) - Privacy settings and data handling
- [Authentication](AUTHENTICATION.md) - User authentication and authorization

## Troubleshooting

### Common Issues
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Common problems and solutions
- [Error Codes](ERROR_CODES.md) - Error code reference
- [Log Analysis](LOG_ANALYSIS.md) - Understanding log files
- [Performance Tuning](PERFORMANCE.md) - Optimize performance

### Support
- [FAQ](FAQ.md) - Frequently asked questions
- [Known Issues](KNOWN_ISSUES.md) - Current known problems
- [Community Support](COMMUNITY.md) - Get help from the community

## Repository Management

### External Tools
habiTv requires external tools for video processing and downloading. These are managed through a repository system:

- **Primary Repository**: `http://dabiboo.free.fr/repository` (HTTP)
- **GitHub Pages Mirror**: `https://[username].github.io/[repository-name]/` (HTTPS - Recommended)

### Repository Benefits
- **Security**: HTTPS encryption for all downloads
- **Reliability**: GitHub's high-availability infrastructure
- **Automation**: GitHub Actions for automatic updates
- **Version Control**: Git-based repository management

### Migration to HTTPS Mirror
To use the secure GitHub Pages mirror:

1. **Update Configuration**:
   ```xml
   <updateConfig>
       <repositoryUrl>https://[username].github.io/[repository-name]/</repositoryUrl>
   </updateConfig>
   ```

2. **Environment Variable**:
   ```bash
   export HABITV_REPOSITORY_URL="https://[username].github.io/[repository-name]/"
   ```

3. **Command Line**:
   ```bash
   java -jar habiTv.jar --repository-url="https://[username].github.io/[repository-name]/"
   ```

For detailed setup instructions, see [GitHub Pages Mirror Guide](GITHUB_PAGES_MIRROR.md).

## Plugin Development

### Getting Started
- [Plugin Development Guide](PLUGIN_DEVELOPMENT.md) - Complete development tutorial
- [Plugin Examples](PLUGIN_EXAMPLES.md) - Working examples to learn from
- [Plugin API Reference](PLUGIN_API.md) - Complete API documentation

### Plugin Types
- **Download Plugins**: Download videos from specific platforms
- **Processing Plugins**: Process downloaded content
- **Export Plugins**: Export to different formats
- **Integration Plugins**: Integrate with external services

### Best Practices
- [Plugin Best Practices](PLUGIN_BEST_PRACTICES.md) - Development guidelines
- [Testing Plugins](PLUGIN_TESTING.md) - How to test your plugins
- [Performance Optimization](PLUGIN_PERFORMANCE.md) - Optimize plugin performance

## Configuration Reference

### Core Configuration
- [Configuration Reference](CONFIGURATION_REFERENCE.md) - Complete configuration options
- [Plugin Configuration](PLUGIN_CONFIGURATION.md) - Plugin-specific settings
- [Security Configuration](SECURITY_CONFIGURATION.md) - Security settings

### Advanced Configuration
- [Network Configuration](NETWORK_CONFIGURATION.md) - Network and proxy settings
- [Storage Configuration](STORAGE_CONFIGURATION.md) - File storage settings
- [Performance Configuration](PERFORMANCE_CONFIGURATION.md) - Performance tuning

## Security and Privacy

### Security Features
- **HTTPS Downloads**: Encrypted downloads from repositories
- **Certificate Validation**: Automatic SSL certificate verification
- **Integrity Checks**: File integrity verification
- **Access Control**: User authentication and authorization

### Privacy Protection
- **Data Minimization**: Only collect necessary data
- **Local Processing**: Process data locally when possible
- **Configurable Logging**: Control what gets logged
- **Secure Storage**: Encrypted storage for sensitive data

## Performance Optimization

### System Tuning
- [Performance Guide](PERFORMANCE.md) - Complete performance optimization
- **Concurrent Downloads**: Optimize download concurrency
- **Memory Management**: Efficient memory usage
- **Disk I/O**: Optimize file operations

### Plugin Performance
- [Plugin Performance](PLUGIN_PERFORMANCE.md) - Optimize plugin performance
- **Caching**: Implement effective caching strategies
- **Resource Management**: Efficient resource usage
- **Async Processing**: Use asynchronous operations

## Community and Support

### Getting Help
- [Community Support](COMMUNITY.md) - Community resources
- [Bug Reports](BUG_REPORTS.md) - How to report bugs
- [Feature Requests](FEATURE_REQUESTS.md) - Request new features
- [Contributing](CONTRIBUTING.md) - How to contribute

### Resources
- [API Documentation](API_REFERENCE.md) - Complete API reference
- [Code Examples](CODE_EXAMPLES.md) - Working code examples
- [Video Tutorials](VIDEO_TUTORIALS.md) - Video tutorials and guides
- [Blog Posts](BLOG_POSTS.md) - Latest news and updates

## Version Information

### Current Version
- **Version**: 4.1.0-SNAPSHOT
- **Release Date**: June 15, 2025
- **Java Version**: Java 8 or higher
- **License**: GPL v3

### Changelog
- [Changelog](../CHANGELOG.md) - Complete version history
- [Migration Guide](MIGRATION_GUIDE.md) - Upgrade instructions
- [Deprecation Notices](DEPRECATION_NOTICES.md) - Deprecated features

## Legal and Licensing

### License Information
- **License**: GNU General Public License v3.0
- **Copyright**: habiTv Development Team
- **Contributors**: See [Contributors](../CONTRIBUTORS.md)

### Third-Party Licenses
- [Third-Party Licenses](THIRD_PARTY_LICENSES.md) - Licenses of included libraries
- [Attributions](ATTRIBUTIONS.md) - Third-party attributions
- [Dependencies](DEPENDENCIES.md) - Dependency information

---

## Quick Navigation

### Essential Documents
- [Installation](INSTALLATION.md) - Get started with habiTv
- [Configuration](CONFIGURATION_REFERENCE.md) - Configure habiTv
- [Plugin Development](PLUGIN_DEVELOPMENT.md) - Create plugins
- [Troubleshooting](TROUBLESHOOTING.md) - Solve problems

### Advanced Topics
- [Architecture](ARCHITECTURE.md) - System design
- [API Reference](API_REFERENCE.md) - Complete API
- [Security](SECURITY.md) - Security guide
- [Performance](PERFORMANCE.md) - Optimization

### Repository Management
- [GitHub Pages Mirror](GITHUB_PAGES_MIRROR.md) - HTTPS repository setup
- [Repository Management](REPOSITORY_MANAGEMENT.md) - Repository administration
- [Startup Update System](STARTUP_UPDATE_SYSTEM.md) - Update mechanism

---

**Need Help?** Check the [Troubleshooting Guide](TROUBLESHOOTING.md) or visit our [Community Support](COMMUNITY.md) page.

## Documentation Structure

```text
docs/
├── README.md                 # This file - documentation index
├── INSTALLATION.md          # Installation instructions
├── USER_GUIDE.md            # User manual
├── CONFIGURATION_REFERENCE.md # Complete configuration guide
├── TROUBLESHOOTING.md       # Problem solving guide
├── DEVELOPER_GUIDE.md       # Developer documentation
├── API_REFERENCE.md         # API documentation
├── PLUGIN_DEVELOPMENT.md    # Plugin development guide
├── PLUGIN_PUBLISHING_GUIDE.md # Plugin publishing guide
├── PLUGIN_EXAMPLES.md       # Plugin code examples
├── SECURITY.md              # Security policies and practices
└── TODO.md                  # Development roadmap and tasks

../ (root directory)
├── README.md                # Project overview
├── CHANGELOG.md             # Version history
├── ENVIRONMENT_VARIABLES.md # Environment variables
└── BUILD_SUMMARY.md         # Build information
```

## Documentation Features

### Code Examples

All documentation includes properly formatted code examples with syntax highlighting:

- **Java**: Proper syntax highlighting for Java code
- **XML**: Configuration file examples
- **Bash**: Shell commands for Linux/Mac
- **CMD**: Windows command examples
- **Properties**: Configuration properties

### Cross-References

Documentation files are cross-referenced for easy navigation:

- Links between related documents
- Consistent terminology
- Cross-referenced sections
- Related topic suggestions

### Version Information

Each document includes:

- **Version**: Current project version
- **Last Updated**: Last modification date
- **Compatibility**: Version compatibility notes

## Getting Help

### Documentation Issues

1. **Check existing issues** - Search for similar problems
2. **Read related docs** - Check linked documentation
3. **Check troubleshooting** - Review troubleshooting guide

### Contributing to Documentation

1. **Fork the repository**
2. **Create a feature branch**
3. **Make your changes**
4. **Submit a pull request**

### Documentation Categories

#### User Documentation

- **Installation guides** - Step-by-step installation instructions
- **User manuals** - How to use the application
- **Configuration guides** - Setting up and configuring
- **Troubleshooting guides** - Problem solving

#### Developer Documentation

- **Architecture overview** - System design and structure
- **API documentation** - Programming interfaces
- **Plugin development** - Creating custom plugins
- **Code examples** - Working code samples

#### Contributor Documentation

- **Setup guides** - Development environment setup
- **Contributing guidelines** - How to contribute
- **Code standards** - Coding conventions
- **Review process** - Code review guidelines

## Documentation Standards

### Writing Guidelines

- **Clear and concise** - Easy to understand language
- **Consistent terminology** - Use same terms throughout
- **Proper formatting** - Follow markdown standards
- **Code examples** - Include working examples

### Quality Standards

- **Accuracy** - Information must be correct
- **Completeness** - Cover all necessary topics
- **Up-to-date** - Keep documentation current
- **Accessible** - Easy to find and navigate

### Maintenance

- **Regular reviews** - Monthly documentation review
- **Version updates** - Update with each release
- **User feedback** - Incorporate user suggestions
- **Continuous improvement** - Ongoing enhancements

## Support and Feedback

### Getting Help

- **Search existing issues** or create a new one
- **Check user documentation** for common solutions
- **Review troubleshooting guide** for known issues
- **Ask in community forums** for additional help

### Providing Feedback

- **Report documentation issues** - Create GitHub issues
- **Suggest improvements** - Submit enhancement requests
- **Contribute fixes** - Submit pull requests
- **Share examples** - Provide additional examples

### Community Resources

- **GitHub Discussions** - Community discussions
- **Issue Tracker** - Bug reports and feature requests
- **Wiki Pages** - Community-maintained documentation
- **Code Examples** - Community-contributed examples

## Documentation Index

### Core Documentation
- **[README.md](../README.md)** - Project overview and quick start
- **[INSTALLATION.md](INSTALLATION.md)** - Installation and setup guide
- **[USER_GUIDE.md](USER_GUIDE.md)** - User manual and features
- **[DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md)** - Development setup and guidelines

### System Documentation
- **[STARTUP_UPDATE_SYSTEM.md](STARTUP_UPDATE_SYSTEM.md)** - Automatic update system
- **[REPOSITORY_MANAGEMENT.md](REPOSITORY_MANAGEMENT.md)** - Repository management challenges and solutions
- **[API_REFERENCE.md](API_REFERENCE.md)** - API documentation
- **[CONFIGURATION_REFERENCE.md](CONFIGURATION_REFERENCE.md)** - Configuration options

### Plugin Development
- **[PLUGIN_DEVELOPMENT.md](PLUGIN_DEVELOPMENT.md)** - Plugin development guide
- **[PLUGIN_EXAMPLES.md](PLUGIN_EXAMPLES.md)** - Plugin code examples
- **[PLUGIN_PUBLISHING_GUIDE.md](PLUGIN_PUBLISHING_GUIDE.md)** - Publishing plugins

### Security and Troubleshooting
- **[SECURITY.md](SECURITY.md)** - Security policies and best practices
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Problem solving guide

### Project Management
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contributing guidelines
- **[TODO.md](TODO.md)** - Development roadmap and tasks

This documentation provides comprehensive information for users, developers, and contributors to the habiTv project. 