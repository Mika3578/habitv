# Contributing to habiTv

Thank you for your interest in contributing to habiTv! This guide explains how to contribute to the project.

**Version**: 4.1.0-SNAPSHOT  
**Last Updated**: December 19, 2024

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- **Java 8 or higher** installed
- **Maven 3.6+** for building the project
- **Git** for version control
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Development Environment Setup

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/your-username/habitv.git
   cd habitv
   ```

3. **Add the upstream remote**:
   ```bash
   git remote add upstream https://github.com/mika3578/habitv.git
   ```

4. **Build the project**:
   ```bash
   mvn clean compile
   ```

5. **Run tests** to ensure everything works:
   ```bash
   mvn test
   ```

## Development Workflow

### 1. Create a Feature Branch

Always work on a feature branch, never directly on `main`:

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes

- **Follow the coding standards** (see below)
- **Write tests** for new functionality
- **Update documentation** as needed
- **Keep commits atomic** and well-described

### 3. Test Your Changes

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=TestClassName

# Run integration tests
mvn verify
```

### 4. Commit Your Changes

Use conventional commit messages:

```bash
git commit -m "feat: add new content provider plugin"
git commit -m "fix: resolve download timeout issue"
git commit -m "docs: update installation guide"
```

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:
- Clear description of changes
- Link to related issues
- Screenshots (if UI changes)
- Test results

## Coding Standards

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Naming**: Follow Java conventions
- **Comments**: Use Javadoc for public methods

### Code Example

```java
/**
 * Downloads content from a specific URL.
 *
 * @param url the URL to download from
 * @param outputPath the output file path
 * @throws DownloadFailedException if download fails
 */
public void downloadContent(String url, String outputPath) throws DownloadFailedException {
    try {
        // Implementation
        processDownload(url, outputPath);
    } catch (IOException e) {
        throw new DownloadFailedException("Failed to download: " + url, e);
    }
}
```

### Error Handling

- **Use specific exceptions** from the API
- **Include meaningful error messages**
- **Log errors appropriately**
- **Don't swallow exceptions**

### Logging

```java
import org.apache.log4j.Logger;

public class MyClass {
    private static final Logger LOG = Logger.getLogger(MyClass.class);
    
    public void doSomething() {
        LOG.debug("Starting operation");
        try {
            // Implementation
            LOG.info("Operation completed successfully");
        } catch (Exception e) {
            LOG.error("Operation failed", e);
            throw new TechnicalException("Operation failed", e);
        }
    }
}
```

## Testing Guidelines

### Unit Tests

- **Test all public methods**
- **Use descriptive test names**
- **Test both success and failure cases**
- **Mock external dependencies**

```java
@Test
public void testDownloadWithValidUrl() throws DownloadFailedException {
    // Given
    String validUrl = "https://example.com/video.mp4";
    String outputPath = "/tmp/test.mp4";
    
    // When
    downloader.download(validUrl, outputPath);
    
    // Then
    assertTrue(new File(outputPath).exists());
}

@Test(expected = DownloadFailedException.class)
public void testDownloadWithInvalidUrl() throws DownloadFailedException {
    // Given
    String invalidUrl = "https://invalid-url.com/video.mp4";
    
    // When
    downloader.download(invalidUrl, "/tmp/test.mp4");
    
    // Then
    // Exception expected
}
```

### Integration Tests

- **Test plugin integration**
- **Test end-to-end workflows**
- **Use test data and mocks**

### Test Data

- **Use small, focused test files**
- **Don't commit large test files**
- **Use test fixtures and builders**

## Documentation

### Code Documentation

- **Javadoc for all public APIs**
- **README updates for new features**
- **Inline comments for complex logic**

### User Documentation

- **Update user guides** for new features
- **Add examples** for new functionality
- **Update installation instructions**

## Types of Contributions

### Bug Fixes

1. **Reproduce the bug** consistently
2. **Write a test** that demonstrates the bug
3. **Fix the issue** with minimal changes
4. **Verify the fix** with tests
5. **Update documentation** if needed

### New Features

1. **Discuss the feature** in an issue first
2. **Design the implementation** approach
3. **Implement the feature** with tests
4. **Update documentation** and examples
5. **Add integration tests**

### Content Provider Plugins

1. **Research the content source** thoroughly
2. **Check for legal compliance**
3. **Implement robust error handling**
4. **Add comprehensive tests**
5. **Document usage and configuration**

### Documentation Improvements

1. **Identify unclear sections**
2. **Add examples and screenshots**
3. **Fix typos and grammar**
4. **Improve structure and organization**

## Pull Request Guidelines

### Before Submitting

- [ ] **Code compiles** without errors
- [ ] **All tests pass**
- [ ] **Code follows style guidelines**
- [ ] **Documentation is updated**
- [ ] **No sensitive data** is included

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Plugin addition
- [ ] Other (please describe)

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes introduced

## Related Issues
Closes #123
```

## Review Process

### Code Review Guidelines

**For Contributors:**
- **Respond promptly** to review comments
- **Make requested changes** or explain why not
- **Test changes** after addressing feedback
- **Keep discussions professional**

**For Reviewers:**
- **Be constructive** and helpful
- **Focus on code quality** and functionality
- **Check for security issues**
- **Verify tests are adequate**

### Review Checklist

- [ ] **Code quality** meets standards
- [ ] **Tests are comprehensive**
- [ ] **Documentation is updated**
- [ ] **No security vulnerabilities**
- [ ] **Performance impact** is acceptable
- [ ] **Backward compatibility** is maintained

## Release Process

### Version Management

- **Semantic versioning** (MAJOR.MINOR.PATCH)
- **Changelog** updated for each release
- **Release notes** with user-friendly descriptions

### Release Checklist

- [ ] **All tests pass**
- [ ] **Documentation is current**
- [ ] **Changelog is updated**
- [ ] **Version numbers updated**
- [ ] **Release notes prepared**
- [ ] **Packages built and tested**

## Community Guidelines

### Communication

- **Be respectful** and professional
- **Use inclusive language**
- **Help newcomers** get started
- **Report inappropriate behavior**

### Issue Reporting

When reporting issues, include:

- **Clear description** of the problem
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Environment details** (OS, Java version, etc.)
- **Log files** and error messages
- **Screenshots** if applicable

### Feature Requests

When requesting features:

- **Explain the use case**
- **Describe the expected behavior**
- **Consider alternatives**
- **Be patient** with implementation

## Getting Help

### Resources

- **Documentation**: Check the docs folder
- **Issues**: Search existing issues
- **Discussions**: Use GitHub Discussions
- **Wiki**: Check the project wiki

### Contact

- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For questions and help
- **Email**: For security issues (see SECURITY.md)

## Recognition

### Contributors

- **Contributors** are listed in CONTRIBUTORS.md
- **Significant contributions** are acknowledged in release notes
- **Long-term contributors** may be invited as maintainers

### Attribution

- **Code contributions** are credited to contributors
- **Documentation** includes contributor names
- **Release notes** mention significant contributors

## Legal

### License

By contributing, you agree that your contributions will be licensed under the same license as the project (GPL v3).

### Copyright

- **You retain copyright** to your contributions
- **You grant license** to the project
- **You certify** you have the right to contribute

---

Thank you for contributing to habiTv! Your contributions help make the project better for everyone. 