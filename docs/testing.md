# Testing Strategy

## Deterministic tests (default)

Deterministic unit tests run by default with standard Maven lifecycle commands, including:

- `mvn -B -ntp clean install`
- `mvn -B -ntp clean deploy` (when tests are not skipped, same rules apply)
- `mvn -B -ntp test`

**`mvn clean install` must stay deterministic:** it should not fail because a third-party streaming site returned no episodes, HTTP 403, or was unreachable.

Passing **plugin compilation** (`mvn clean -DskipTests compile`) proves the code compiles; it does **not** prove that provider plugins still work against live sites. Runtime provider health requires separate validation.

## Opt-in network smoke tests

Network-dependent smoke tests are isolated behind the Maven profile `network-tests`.

- Naming convention: `*IT.java` or `*NetworkIT.java`
- `application/core/test/com/dabi/habitv/core/updater/ListHttpNetworkIT.java` is intentionally excluded from default Surefire lifecycle by naming (no `Test*` prefix) and runs only through Failsafe in this profile.
- `fwk/framework/test/com/dabi/habitv/framework/plugin/utils/RetrieverUtilsNetworkIT.java` is a generic network smoke test and uses `https://example.com/` as a stable public URL.
- Execution command:

```bash
mvn -B -ntp clean -Pnetwork-tests verify
```

**`mvn -Pnetwork-tests verify` is the place for live provider smoke tests** (plugin `*IT.java` classes under Failsafe). Failures there are expected when the network is restricted, sites block automated access, or parsers are outdated; treat them as opt-in signals, not as default build gates.

This keeps default builds deterministic while allowing explicit, profile-gated checks when needed.

## Plugin/provider test policy

Provider tests that query live websites are not deterministic and must not run in the
default Surefire lifecycle.

- Going forward, live provider tests should use `*IT.java` naming and run only via
  `mvn -B -ntp -Pnetwork-tests verify`.
- Deterministic provider checks should rely on local fixtures or mocks and remain in
  default test execution.
- A provider parser update is out of scope for build stabilization unless fixture-backed
  tests prove deterministic behavior.
- Live provider smoke tests built on `BasePluginProviderTester` that hit real sites use
  `*IT.java` and run only under `network-tests`. Remaining `*Test` classes in plugins are
  reserved for deterministic tests (e.g. local fixtures/mocks). **Remaining provider
  rewrites and parser fixes must be tracked in product backlog work items, not conflated
  with the default build.**

## URL volatility policy

Provider/public URLs are volatile and must be tracked in `docs/url-inventory.md`.

When replacing HTTP with HTTPS or modernizing a provider landing URL:

1. Update the URL inventory.
2. Validate deterministic build remains green.
3. Run opt-in network tests if applicable.
4. Avoid claiming a provider plugin rewrite unless provider-specific tests/parsers are actually updated and validated.

Generic network smoke tests should use stable public test URLs such as `https://example.com/`.
Provider-specific URLs belong only in provider-specific integration tests and may return 403 or require dedicated parser rewrites.

## Scope guardrail

Updating a URL to HTTPS is maintenance hardening only. It does **not** imply full provider compatibility if APIs, page structures, or anti-bot behavior have changed.
