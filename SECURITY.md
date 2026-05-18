# 🔐 Security Policy

## 📡 Reporting a vulnerability

If you believe you have found a security issue in Habitv, please **do
not open a public GitHub issue**. Instead:

1. Open a **private** security advisory on GitHub:
   <https://github.com/Mika3578/habitv/security/advisories/new>
2. Include:
   - A clear description of the issue.
   - Steps to reproduce (or a proof-of-concept).
   - The affected commit SHA, branch, or release tag.
   - Your assessment of impact (data, credentials, runtime, build).
3. We aim to acknowledge within **7 days** and to triage within
   **14 days**. There is no formal bounty program.

For non-sensitive build/runtime bugs, please use the standard
**bug issue template** instead.

---

## 🎯 Supported surface

| Surface | Status |
|---------|--------|
| `develop` branch | 🟢 Actively maintained (modernization restart) |
| `master` branch | 🟡 Stable baseline, defensive fixes only |
| `legacy` branch | 🔴 Frozen; security reports archived only |
| Released `4.1.0` binaries (2017) | 🔴 No further updates |

---

## 🚨 Known sensitive areas

The repository is undergoing modernization. The following areas have
**known risks** tracked in [`docs/risk-register.md`](docs/risk-register.md)
and should not be relied on in production until remediated:

| Risk ID | Area | Status |
|---------|------|--------|
| `youtube-key-hardcoded` | Hardcoded YouTube Data API key in `plugins/youtube` | ✅ Mitigated (PR #29 merged) |
| `legacy-maven-repo` | Legacy plain-HTTP `dabiboo.free.fr` Maven repository | ✅ Active wiring removed (PR #36 merged) |
| `ftp-deploy` | FTP deployment with embedded credentials | 🟠 Migration planned (`legacy-url-migration`) |
| `legacy-update-pull` | Auto-update can pull artifacts from unmaintained host | 🟠 Opt-in guard added; cutover pending (`static-repo-publish`) |
| — | Hardcoded Gmail POP3/IMAP credentials in `plugins/email` tests | 🔴 Pending dedicated remediation PR |
| — | Sample FTP credentials in `application/consoleView/config.xml` | 🔴 Sample is illustrative; do not reuse |

If your security report concerns one of these already-tracked items,
please reference the risk slug in your advisory.

---

## 🔒 Sensitive data

Never include in issues, pull requests, comments, logs, or attachments:

- Passwords, API keys, tokens, or personal access tokens
- Cookies or session identifiers
- Private replay-service or provider credentials
- Personal data unrelated to the defect

Redact logs before posting. Use placeholders for secrets.

---

## ✅ What we treat as in-scope

- Code execution, command injection, path traversal vulnerabilities in
  the Habitv application, framework, or plugins.
- Secrets exposure (current or historical) in committed files.
- Vulnerabilities in build/CI configuration that would impact users
  building from source.
- Insecure default network behavior (e.g. plain HTTP, certificate
  validation bypasses) in production code paths.

## ❌ What we treat as out of scope

- The legacy `master`-era 2015–2017 source code lines, except for
  ports that are still active on `develop`.
- Issues that require a malicious local plugin or modified
  configuration provided by the user.
- Theoretical issues without a concrete impact path.

---

## 🤝 Disclosure

Once a fix has shipped, the reporter is credited in the relevant
`CHANGELOG.md` entry unless they request anonymity.
