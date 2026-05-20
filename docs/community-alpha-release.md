# Community Alpha / Technical Preview

**Release type:** Community Alpha — technical preview for testers and contributors.  
**Not** a stable public release. Provider compatibility, packaging polish, and dependency
remediation are still in progress.

**Tracker:** `community-alpha-release-readiness` (HBTV-016)

---

## Supported runtime

| Requirement | Detail |
|-------------|--------|
| Java | **Java 8** only (repository baseline; no Java 11+ support in this alpha) |
| JavaFX | Bundled **JavaFX 2.x** via `jfxrt.jar` on the JDK 8 install |
| Recommended JDK | [Azul Zulu 8](https://www.azul.com/downloads/?version=java-8-lts&package=jdk-fx) or another **JDK 8 distribution that includes JavaFX** (not a headless JRE) |
| Unsupported for alpha | Java 11/17/21-only runtimes, headless JRE without `jfxrt.jar`, OpenJFX-only layouts (tracked under `javafx-modernization`) |

The launcher resolves JavaFX from `java.home` or `-Dhabitv.jfxrt.path=<path-to-jfxrt.jar>`.
See `JavaFxRuntimeLocator` in `application/habiTv`.

---

## Build from source

From the repository root (Maven 3.6+, network for dependencies):

```bash
mvn -B -ntp clean package
```

Primary artifact (shaded application JAR):

```text
application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar
```

Copy external download tools (`yt-dlp`, `ffmpeg`, `curl`, etc.) into a `bin\` folder next to
the JAR or configure paths in `configuration.xml` (see sample under `application/core/configuration.xml`).

---

## Validation (maintainers and advanced testers)

Default CI-safe check:

```bash
mvn -B -ntp -DskipTests validate
```

Full unit test lifecycle (offline-focused; live provider tests excluded by default):

```bash
mvn -B -ntp test
```

Opt-in live provider tests (network required; not part of alpha acceptance):

```bash
mvn -B -ntp test -Plive-provider-tests
```

---

## Windows launch (GUI)

Prerequisites: JDK 8 with JavaFX, built or downloaded `habiTv-4.1.0-SNAPSHOT.jar`.

1. Open **Command Prompt** or **PowerShell**.
2. `cd` to the folder containing the JAR (and optional `bin\` tools).
3. Verify Java:

   ```bat
   java -version
   ```

   Output must report version **1.8** / **8**.

4. Start the tray GUI (no arguments):

   ```bat
   java -jar habiTv-4.1.0-SNAPSHOT.jar
   ```

5. If JavaFX is not found, set the runtime explicitly (adjust path to your JDK):

   ```bat
   java -Dhabitv.jfxrt.path="C:\Program Files\Zulu\zulu-8\jre\lib\ext\jfxrt.jar" -jar habiTv-4.1.0-SNAPSHOT.jar
   ```

6. **Console mode** (no GUI; useful when JavaFX is missing):

   ```bat
   java -jar habiTv-4.1.0-SNAPSHOT.jar --help
   ```

   See `ConsoleLauncher` for CLI options.

### Configuration and logs (Windows)

| Path | Purpose |
|------|---------|
| `%USERPROFILE%\habitv\configuration.xml` | User configuration |
| `%USERPROFILE%\habitv\grabconfig.xml` | Grab / provider subscriptions |
| `%USERPROFILE%\habitv\habiTv.log` | Application log (attach to issues) |

For a clean test, rename `%USERPROFILE%\habitv` before launching.

### Plugin updates

Default update repository: `https://mika3578.github.io/habitv-repo/repository/`  
(legacy `http://dabiboo.free.fr/repository` was removed — see `legacy-url-migration`).

Verify in logs or network traces that updates do **not** contact `dabiboo.free.fr`.

---

## Known limitations (alpha)

- **Providers:** Many French replay providers still target legacy HTTP/HTML endpoints.
  See [`provider-status.md`](provider-status.md) for alpha classification.
- **France TV / Pluzz:** Module id is `francetv`; legacy `pluzz` grab-config entries may need manual rename.
- **Canal+ / D8 / D17:** Embedded in `canalPlus` plugin; endpoints are obsolete pending rewrite.
- **6play / M6:** `6play` plugin targets legacy `6play.fr` markup; modern SPA site likely broken.
- **WAT / TF1-era:** `wat` plugin uses TF1/WAT-era URLs.
- **External tools:** `yt-dlp`, `ffmpeg`, `rtmpdump`, `aria2c`, `curl` must be installed and
  paths configured; versions are not bundled in the alpha JAR.
- **Security / dependencies:** Transitive dependency audit and remediation are **in progress**
  (`jaxb-launcher-recovery` notes; no full OWASP/SBOM gate yet).
- **Packaging:** No polished Windows installer in this alpha (`javafx-modernization` tracks native packaging).
- **Statistics:** Usage statistics to legacy hosts are disabled by default (`legacy-url-migration`).

---

## What testers should exercise

| Area | What to verify |
|------|----------------|
| Startup | GUI opens on JDK 8 + JavaFX; or console mode runs with `--help` |
| Configuration | `configuration.xml` loads; changes persist after restart |
| Plugin update | Update check uses GitHub Pages repo, not `dabiboo.free.fr` |
| YouTube / yt-dlp | Download path works when `bin\yt-dlp.exe` (or configured path) is present |
| RSS / manual URL | RSS plugin or manual URL flows if used in your setup |
| Reporting | Open issues with the **Community alpha test report** template; attach `habiTv.log` |

Prioritize providers listed as **Testable for alpha** in [`provider-status.md`](provider-status.md).

---

## What testers should not expect

- Stable compatibility across all TV providers listed in the legacy README.
- DRM bypass or circumvention of provider protections.
- Subscription-only or authenticated replay without your own credentials/setup.
- Production-grade installer, auto-update UX, or signed binaries.
- Complete dependency vulnerability remediation.

---

## Related documentation

- [`provider-status.md`](provider-status.md) — alpha provider matrix
- [`release-checklist.md`](release-checklist.md) — maintainer pre-release checklist
- [`provider-inventory.md`](provider-inventory.md) — full module inventory (HBTV-006)
- [`AGENTS.md`](../AGENTS.md) — build and validation policy

## Feedback

Use the GitHub issue template **Community alpha test report** (`.github/ISSUE_TEMPLATE/community-alpha-test-report.md`).
