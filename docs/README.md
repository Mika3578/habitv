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
- [Environment Variables](ENVIRONMENT_VARIABLES.md) - Environment variable configuration
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
- [Plugin Publishing Guide](PLUGIN_PUBLISHING_GUIDE.md) - Publish plugins to repository

### Update & Repository Management
- **Automatic Updates**: Keep habiTv and plugins up to date
- [Update & Repository Management](UPDATE_AND_REPO_MANAGEMENT.md) - Complete guide to updates and repositories
- **Security-First**: HTTPS repositories with integrity verification
- **Automated Maintenance**: GitHub Actions for repository updates

## Advanced Topics

### Development
- [Developer Guide](DEVELOPER_GUIDE.md) - Development setup and guidelines
- [API Reference](API_REFERENCE.md) - Complete API documentation
- [Contributing Guidelines](CONTRIBUTING.md) - How to contribute

### User Guides
- [User Guide](USER_GUIDE.md) - Complete user manual
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Common problems and solutions

### Security
- [Security Guide](SECURITY.md) - Security best practices
- **Plugin Auto-Update**: Disabled by default for enhanced security
- **HTTPS Enforcement**: All downloads use encrypted connections
- **Certificate Validation**: Automatic SSL certificate verification

## Documentation Structure

### User Documentation
- **Installation**: Setup and installation guides
- **Configuration**: Configuration options and examples
- **Usage**: User interface and command-line usage
- **Troubleshooting**: Common issues and solutions

### Developer Documentation
- **API Reference**: Complete API documentation
- **Plugin Development**: Plugin creation and development
- **Contributing**: Guidelines for contributors
- **Architecture**: System design and components

### Reference Documentation
- **Configuration Reference**: All configuration options
- **Environment Variables**: Environment variable reference
- **Update & Repository Management**: Repository setup and maintenance
- **Security**: Security configuration and best practices

## Repository Management

### External Tools
habiTv requires external tools for video processing and downloading. These are managed through a repository system:

- **Primary Repository**: `http://dabiboo.free.fr/repository` (HTTP - legacy)
- **GitHub Pages Mirror**: `https://mika3578.github.io/habitv/repository/` (HTTPS - recommended)

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
       <repositoryUrl>https://mika3578.github.io/habitv/repository/</repositoryUrl>
   </updateConfig>
   ```

2. **Environment Variable**:
   ```bash
   export HABITV_REPOSITORY_URL="https://mika3578.github.io/habitv/repository/"
   ```

3. **Command Line**:
   ```bash
   java -jar habiTv.jar --repository-url="https://mika3578.github.io/habitv/repository/"
   ```

For detailed setup instructions, see [Update & Repository Management](UPDATE_AND_REPO_MANAGEMENT.md).

## Plugin Development

### Getting Started
- [Plugin Development Guide](PLUGIN_DEVELOPMENT.md) - Complete development tutorial
- [Plugin Examples](PLUGIN_EXAMPLES.md) - Working examples to learn from
- [Plugin Publishing Guide](PLUGIN_PUBLISHING_GUIDE.md) - Publish plugins to repository

### Plugin Types
- **Download Plugins**: Download videos from specific platforms
- **Processing Plugins**: Process downloaded content
- **Export Plugins**: Export to different formats
- **Integration Plugins**: Integrate with external services

### Best Practices
- **Security**: Follow security best practices for plugin development
- **Testing**: Comprehensive testing before publishing
- **Documentation**: Clear documentation for users
- **Performance**: Optimize for performance and resource usage

## Configuration Reference

### Core Configuration
- [Configuration Reference](CONFIGURATION_REFERENCE.md) - Complete configuration options
- [Environment Variables](ENVIRONMENT_VARIABLES.md) - Environment variable reference
- [Security Configuration](SECURITY.md) - Security settings

### Advanced Configuration
- **Network Configuration**: Proxy settings and network options
- **Storage Configuration**: File storage and organization
- **Performance Configuration**: Performance tuning options
- **Update Configuration**: Repository and update settings

## Security and Privacy

### Security Features
- **HTTPS Downloads**: Encrypted downloads from repositories
- **Certificate Validation**: Automatic SSL certificate verification
- **Integrity Checks**: File integrity verification
- **Plugin Auto-Update**: Disabled by default for security

### Privacy Protection
- **Data Minimization**: Only collect necessary data
- **Local Processing**: Process data locally when possible
- **Configurable Logging**: Control what gets logged
- **Secure Storage**: Encrypted storage for sensitive data

## Performance Optimization

### System Tuning
- **Concurrent Downloads**: Optimize download concurrency
- **Memory Management**: Efficient memory usage
- **Disk I/O**: Optimize file operations
- **Network Optimization**: Configure network settings

### Plugin Performance
- **Caching**: Implement effective caching strategies
- **Resource Management**: Efficient resource usage
- **Async Processing**: Use asynchronous operations
- **Error Handling**: Robust error handling and recovery

## Community and Support

### Getting Help
- **Documentation**: Check this documentation first
- **Troubleshooting**: Review [Troubleshooting Guide](TROUBLESHOOTING.md)
- **Issues**: Report bugs on GitHub
- **Community**: Join the habiTv community

### Resources
- **API Documentation**: Complete API reference
- **Code Examples**: Working code examples
- **Security Guide**: Security best practices
- **Performance Guide**: Performance optimization

## Version Information

### Current Version
- **Version**: 4.1.0-SNAPSHOT
- **Release Date**: June 15, 2025
- **Java Version**: Java 8 or higher
- **License**: GPL v3

### Changelog
- [Changelog](../CHANGELOG.md) - Complete version history
- **Migration Notes**: See individual documentation for migration guides
- **Deprecation Notices**: Check documentation for deprecated features

## Legal and Licensing

### License Information
- **License**: GNU General Public License v3.0
- **Copyright**: habiTv Development Team
- **Contributors**: See [Contributing Guidelines](CONTRIBUTING.md)

---

**Note**: This documentation is continuously updated. For the latest information, check the [GitHub repository](https://github.com/mika3578/habitv).

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