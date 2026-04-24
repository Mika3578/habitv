# Security Dependency Scan Bootstrap

## Purpose
This project runs OWASP Dependency-Check to create a baseline view of dependency vulnerabilities across the Maven reactor.

## Why this is non-blocking (for now)
Dependency scanning is currently configured as **non-blocking** to allow modernization work to continue while we:
- stabilize scan runtime (NVD feed updates can be slow),
- triage legacy dependency findings,
- tune suppression and policy thresholds.

## Local commands
### Baseline reactor validation
```bash
mvn -B -ntp -DskipTests validate
```

### Dependency scan (pinned plugin version)
```bash
mvn -B -ntp org.owasp:dependency-check-maven:12.2.1:aggregate -Dformat=ALL -DfailBuildOnCVSS=11
```

### Dependency scan with NVD API key
```bash
mvn -B -ntp org.owasp:dependency-check-maven:12.2.1:aggregate -Dformat=ALL -DfailBuildOnCVSS=11 -Dnvd.apiKey="$NVD_API_KEY"
```

## CI behavior
- Workflow: `.github/workflows/security.yml`
- Java runtime: **Java 17** (required for modern dependency-check plugin compatibility)
- Dependency-check goal: `org.owasp:dependency-check-maven:12.2.1:aggregate`
- Formats: `ALL` (includes HTML + JSON)
- Fail threshold: `-DfailBuildOnCVSS=11` (effectively non-blocking at scanner threshold)
- Job mode: `continue-on-error: true` during bootstrap
- Optional secret: `NVD_API_KEY`

If `NVD_API_KEY` is set in repository secrets, CI passes it to the scan. If not set, CI still runs the scan and logs that first updates may be slow.

## Report artifacts
Reports are uploaded from:
- `**/target/dependency-check-report.html`
- `**/target/dependency-check-report.json`

Artifact name: `dependency-check-report`

If no files are found, CI emits a warning (`if-no-files-found: warn`) and an explicit warning step logs that no reports were generated.

## Known limitations
1. First NVD update can be slow or occasionally interrupted.
2. Legacy dependencies may produce a high initial finding volume.
3. Finding remediation is a separate stream from scan bootstrap.
4. Migrating Maven repo URLs does **not** migrate the runtime updater behavior:
   - Runtime updater still depends on `FrameworkConf.UPDATE_URL` and `FindArtifactUtils` directory parsing.
   - A dedicated follow-up PR is required for full runtime updater migration.
