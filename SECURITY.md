# Security Policy

## Supported Versions

We currently support the following versions with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 4.1.x   | :white_check_mark: |
| < 4.1   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability in habiTv, please follow these steps:

1. **Do NOT** open a public issue on GitHub
2. Email the maintainers directly with details about the vulnerability
3. Include the following information:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We will respond to your report within 48 hours and work with you to address the issue.

## Security Best Practices

When using habiTv:

- Keep your Java installation up to date
- Regularly update habiTv to the latest version
- Review and validate configuration files before use
- Be cautious when downloading from untrusted sources
- Use proxy settings if required by your network security policies

## Known Security Considerations

- habiTv uses Log4j 1.2.15 which has known vulnerabilities. Consider upgrading to Log4j 2.x or migrating to SLF4J.
- Some plugins may execute external commands. Only use plugins from trusted sources.
- Configuration files may contain sensitive information (proxy credentials, paths). Keep them secure.

## Disclosure Policy

- We will acknowledge receipt of your vulnerability report within 48 hours
- We will provide an initial assessment within 7 days
- We will keep you informed of our progress
- We will credit you for the discovery (unless you prefer to remain anonymous)
- We will coordinate public disclosure after a fix is available

Thank you for helping keep habiTv secure!

