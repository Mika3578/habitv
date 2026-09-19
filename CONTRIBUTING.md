# Contributing to HabiTV

HabiTV is in a controlled modernization. Keep changes small and
reviewable. Repository conventions live in [`AGENTS.md`](AGENTS.md).
Java, Maven, and CI: [`docs/development.md`](docs/development.md).

Canonical remote: `Mika3578/habitv`. Target branch: `develop`.

## Plugin versioning

Plugins inherit the parent version unless a **user-visible** change
requires an override:

- parser or retrieval behavior
- user-facing endpoint or channel slug
- user-facing configuration

Do not bump for fixtures, CI, docs, or internal-only refactors.
Inside a bumped plugin, depend on `api` / `framework` /
`plugin-tester` with `${project.parent.version}`.

## Out of scope unless explicitly requested

Reactor topology changes, Java baseline bump, JavaFX migration, JAXB
Jakarta move, provider rewrites, runtime updater URL changes.

Never commit secrets, tokens, credentials, or generated binaries.

## License

No `LICENSE` file is present. Treat sources as proprietary until the
maintainer adds one.
