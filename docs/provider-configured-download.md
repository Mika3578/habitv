# Configured download path (provider pattern)

Short maintainer note. Reference instance: `plugins/tf1plus`.
Legal and status: [`providers.md`](providers.md). Layout:
[`architecture.md`](architecture.md).

## When to use it

Most providers browse a public catalogue and download through an existing
downloader plugin (usually yt-dlp). Some hosts need an **extra,
user-configured** download path that stays **off** until local settings
exist.

The shared execution base lives in
[`ConfiguredScriptCmdExecutor`](../fwk/framework/src/com/dabi/habitv/framework/plugin/utils/ConfiguredScriptCmdExecutor.java)
(framework module): argument-array execution, command-processor template
substitution, environment overrides. Keep it thin; provider-specific
logic stays in each plugin module.

## Reference projects

How comparable open-source projects shape this:

| Project | Shape | Take-away |
|---------|-------|-----------|
| [OqeeRewind](https://github.com/NohamR/OqeeRewind) | One Python project per service (auth → manifest → segments → keys → mux) | Ships the generic code only; the user sources the device file locally |
| [Catch-up-TV-and-More](https://github.com/Catch-up-TV-and-More/plugin.video.catchuptvandmore) | Thin per-provider module; the player performs the handshake | Keep per-provider code minimal; centralize the heavy lifting |
| [MediaFlow Proxy](https://github.com/mhdzumair/mediaflow-proxy) | Self-hosted proxy; providers supply manifest + keys | One central point converts and processes streams for all providers |

HabiTV follows the same split: thin per-provider resolvers in plugin
modules, one shared executor base in the framework, and (optionally) a
local helper script or a MediaFlow instance as the execution backend.

## Layers

| Layer | Responsibility |
|-------|----------------|
| Catalogue | Categories and episodes (offline fixtures preferred) |
| Public download | Delegate to yt-dlp / curl / ffmpeg when that is enough |
| Configured download | Optional path: local settings → Java executor → helper → external tools |
| Alias / migration | Optional rename of an old plugin id in grab-config |

The configured path must fail with a clear user-facing message when
settings are missing. It must not read browser profiles or commit
secrets.

## Typical pieces (per plugin)

1. **Provider module** — catalogue + routing to public or configured
   download.
2. **Local settings** — `configuration.xml` and/or UI fields; values also
   resolvable from environment variables. Applied at startup as JVM
   properties when needed.
3. **Executor** — starts the helper as a child process, maps progress
   lines into Habitv UI.
4. **Helper script** (optional) — lives under `plugins/<id>/scripts/`,
   bundled in the JAR; dependencies documented in that module only.
5. **External tools** — paths the user installs locally (never committed).

Keep host-specific details in the plugin code and private notes. Public
git text stays short and generic ([`AGENTS.md`](../AGENTS.md)).

## PR and validation rules

- **One provider / one PR** (example: `feat/arte`, not a multi-host
  bundle).
- Prefer offline fixture tests in CI; live network tests stay opt-in.
- Mark **working** in [`providers.md`](providers.md) only after a real
  local download succeeds.
- Do not rename an existing GitHub PR head branch.

## Follow-ups

Candidates for a similar shape are listed under “Needs rewrite /
investigation” in [`providers.md`](providers.md). Each stays a separate
scoped change after its own E2E check.

## Roadmap (entitled replay providers)

Providers whose content needs a user-configured path reuse this pattern:

1. Thin resolver in the plugin module: authentication, manifest and
   request candidates (provider-specific, unavoidable).
2. Shared execution: `ConfiguredScriptCmdExecutor` in the framework.
3. One local helper script per provider, or a shared helper with
   subcommands; alternatively route through a local MediaFlow instance.
4. Device files, credentials, and helper scripts stay local — never
   committed (see OqeeRewind's precedent).
