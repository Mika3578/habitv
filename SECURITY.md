# Security policy

## Supported branches

Security fixes are accepted against **`develop`**, the active integration branch.

Legacy branches such as `master` are kept for history and release alignment only
unless a maintainer explicitly states otherwise in a security advisory or release
note.

## Reporting a vulnerability

**Do not** open a public GitHub issue for security vulnerabilities before triage.

Preferred path:

1. Open a **private security advisory** on this repository
   (**Security → Advisories → Report a vulnerability**) when GitHub Private
   Vulnerability Reporting is enabled.
2. If private reporting is unavailable, contact the repository owner through a
   private channel documented in the repository profile or release notes.

Include enough detail for reproduction (affected module, version or commit,
steps, impact). Do not attach secrets or live credentials.

## Sensitive data

Never include in issues, pull requests, comments, logs, or attachments:

- Passwords, API keys, tokens, or personal access tokens
- Cookies or session identifiers
- Private replay-service or provider credentials
- Personal data unrelated to the defect

Redact logs before posting. Use placeholders for secrets.

## Response expectations

Maintainers will acknowledge reports when possible, assess severity, and
coordinate a fix on `develop` before any public disclosure.
