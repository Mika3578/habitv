# Testing Strategy

## Deterministic tests (default)

Deterministic unit tests run by default with standard Maven lifecycle commands, including:

- `mvn -B -ntp install`
- `mvn -B -ntp test`

These tests must not depend on live websites or mutable external network responses.

## Opt-in network smoke tests

Network-dependent smoke tests are isolated behind the Maven profile `network-tests`.

- Naming convention: `*IT.java` or `*NetworkIT.java`
- `application/core/test/com/dabi/habitv/core/updater/TestListHttpNetworkIT.java` is a network-dependent smoke test and should be run via the `network-tests` profile.
- Execution command:

```bash
mvn -B -ntp -Pnetwork-tests verify
```

This ensures default builds remain deterministic while allowing explicit provider smoke checks when needed.

## URL volatility policy

Provider/public URLs are volatile and must be tracked in `docs/url-inventory.md`.

When replacing HTTP with HTTPS or modernizing a provider landing URL:

1. Update the URL inventory.
2. Validate deterministic build remains green.
3. Run opt-in network tests if applicable.
4. Avoid claiming a provider plugin rewrite unless provider-specific tests/parsers are actually updated and validated.

## Scope guardrail

Updating a URL to HTTPS is maintenance hardening only. It does **not** imply full provider compatibility if APIs, page structures, or anti-bot behavior have changed.
