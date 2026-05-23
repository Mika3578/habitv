# Dependency security audit baseline

> **Tracker:** `dependency-security-audit`  
> **Legacy code:** `HBTV-016` (historical reference only)  
> **Branch:** `security/dependabot-audit-baseline` (legacy prefix; historical reference only)  
> **Last refresh:** 2026-05-20

## GitHub warning summary

GitHub reports **294 vulnerabilities** on the default branch:

| Severity | Count |
|----------|------:|
| Critical | 87 |
| High | 90 |
| Moderate | 89 |
| Low | 28 |

These counts come from the repository **Security → Dependabot** dashboard.
They include transitive dependencies across the Maven reactor and may
overlap with alerts that cannot be fixed without coordinated upgrades.

### Dependabot API export (local only)

When authenticated, export alerts for offline triage:

```powershell
New-Item -ItemType Directory -Force -Path target | Out-Null
gh api --paginate /repos/Mika3578/habitv/dependabot/alerts `
  | Set-Content -Encoding utf8 target/dependabot-alerts.json
```

`target/` is gitignored (`**/target/`). Do not commit the export.
A successful local export on 2026-05-20 produced a large JSON payload
(suitable for scripted grouping by package and severity).

If `gh api` fails (missing auth, insufficient `security_events` scope, or
private-repo restrictions), use the GitHub UI under **Security →
Dependabot alerts** and record triage notes in follow-up PRs.

## Scope

This PR creates the **security audit baseline only**. It does not attempt
mass upgrades, change runtime behavior, or modify provider plugins.

Follow-up work must stay in focused PRs (build plugins, logging, HTTP
repositories, then runtime libraries module by module).

## Risk

Large dependency upgrades may break:

- **Java 8 compatibility** — compiler `source`/`target` 1.8 across the reactor.
- **JAXB generation** — `com.sun.tools.xjc.maven2:maven-jaxb-plugin` 1.1.1 in
  `application/core` with XSD-driven packages.
- **JavaFX runtime** — `habiTv-linux` / `habiTv-windows` (out of reactor) use
  legacy JavaFX 2.x tooling.
- **Legacy plugins** — 22 provider/downloader modules with varied own-version
  coordinates and live-network tests.

Blind version bumps on parent `dependencyManagement` can shift transitive
graphs for all modules at once.

## Maven module inventory

**36** `pom.xml` files (reactor + out-of-reactor + static-repo publisher):

| Area | Paths |
|------|--------|
| Root | `pom.xml` |
| Framework | `fwk/pom.xml`, `fwk/api/pom.xml`, `fwk/framework/pom.xml` |
| Application | `application/pom.xml`, `application/core/pom.xml`, `application/consoleView/pom.xml`, `application/trayView/pom.xml`, `application/habiTv/pom.xml`, `application/habiTv-linux/pom.xml`, `application/habiTv-windows/pom.xml` |
| Plugins aggregator | `plugins/pom.xml` + 22 plugin modules + `plugins/plugin-tester/pom.xml` |
| Build | `build/static-repo-publisher/pom.xml` (profile `static-repo-publish` only) |

### Parent `dependencyManagement` (root `pom.xml`)

Central versions for third-party libraries (children omit versions when
they import from parent):

| Artifact | Version | Notes |
|----------|---------|--------|
| `com.dabi.habitv:api` / `framework` | `${project.parent.version}` | Internal |
| `javax.mail:mail` | 1.4.7 | Legacy JavaMail |
| `com.google.guava:guava` | 20.0 | Pre-Java-8-module era |
| `commons-cli:commons-cli` | 1.11.0 | |
| `rome:rome` | 1.0 | Very old RSS |
| `org.jsoup:jsoup` | 1.22.2 | HTML parsing |
| `jackson-core` / `jackson-databind` | 2.21.3 | JSON |
| `jaxb-api` | 2.3.1 | javax namespace |
| `jaxb-runtime` | 2.3.9 | GlassFish runtime |
| `commons-codec` | 1.22.0 | |
| `commons-lang:commons-lang` | 2.6 | **2.x line (not commons-lang3)** |
| `log4j:log4j` | 1.2.17 | **Log4j 1.x (EOL)** |
| `junit:junit` | 4.13.2 | JUnit 4 |

No `xstream` coordinates appear in the repository POMs (grep baseline).

### Root build plugins (not in `pluginManagement`)

Declared directly under `<build><plugins>` in root `pom.xml`:

| Plugin | Version |
|--------|---------|
| `maven-compiler-plugin` | 3.15.0 |
| `maven-jar-plugin` | 2.6 |
| `maven-deploy-plugin` | 3.1.2 |
| `maven-surefire-plugin` | 3.2.5 |

### Notable child-only plugin versions

| Module | Plugin | Version |
|--------|--------|---------|
| `application/core` | `maven-jaxb-plugin` (XJC) | 1.1.1 |
| `application/core` | `build-helper-maven-plugin` | 3.6.0 |
| `application/habiTv`, `application/consoleView` | `maven-shade-plugin` | 2.4.3 |
| `application/trayView` | `javafx-maven-plugin` (`com.zenjava`) | 2.0 |
| `application/habiTv-linux` / `habiTv-windows` | `maven-dependency-plugin` | 2.8 |
| `application/habiTv-linux` / `habiTv-windows` | `maven-antrun-plugin` | 1.6 |
| `build/static-repo-publisher` | `exec-maven-plugin` (`org.codehaus.mojo`) | 3.5.0 |

### Repositories

Root `pom.xml` declares **HTTPS** `habitv-static-repo` at
`https://mika3578.github.io/habitv-repo/repository`. Legacy HTTP/FTP
hosts were removed under `legacy-url-migration`; keep resolution
HTTPS-only in future security PRs.

### Direct dependencies without parent version

Most modules inherit versions from parent `dependencyManagement`. Modules
that declare `com.dabi.habitv:*` artifacts typically use
`${project.version}` or `${project.parent.version}`. Own-version plugin
modules (`beinsport`, `francetv`, `ffmpeg`, etc.) pin internal artifacts
explicitly — see `own-version-deps-align` notes.

## Recommended remediation order

1. **Fix build reproducibility first** — deterministic `mvn validate` /
   `compile`, no blocked HTTP repositories, CI green on `develop`.
2. **Replace or isolate known abandoned dependencies** — e.g. `log4j` 1.x,
   `rome` 1.0, `commons-lang` 2.6, ancient JavaMail.
3. **Upgrade Maven plugins safely** — compiler, surefire, jar, assembly,
   JAXB plugin (highest risk: XSD/codegen compatibility).
4. **Upgrade runtime dependencies module by module** — `fwk/framework`,
   `application/core`, then individual plugins with focused tests.
5. **Re-run Dependabot and Maven validation** after each focused PR.

## First fix candidates

| Area | Rationale |
|------|-----------|
| Maven plugins and build-only dependencies | Limited runtime blast radius; improves CI and reproducibility |
| Log4j 1.x migration or containment plan | EOL logging; many Dependabot alerts reference logging stacks |
| HTTP repository removal / HTTPS-only resolution | Prevents MITM and blocked-resolution failures |
| Provider/plugin dependencies that are unreachable or obsolete | Align with `provider-inventory`; drop dead coordinates before version bumps |

Use `scripts/security/maven-dependency-inventory.ps1` to regenerate
`target/dependency-tree.txt` before each remediation PR.

## Out of scope

- Java 17 / 21 migration
- Provider rewrite
- UI rewrite
- `yt-dlp` integration (`ytdlp-migration`)
- Runtime updater redesign (`static-repo-publish` behavior)

## Validation commands

```bash
mvn -B -ntp -DskipTests validate
mvn -B -ntp dependency:tree -DoutputFile=target/dependency-tree.txt
```

See the opening PR **Validation** section for exact command output from
the baseline branch.
