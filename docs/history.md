# History

> Historical documentation. This describes earlier HabiTV versions and may
> not match the current codebase.

Last published release: **4.1.0** (2017). After a long pause,
`Mika3578/habitv` is the canonical repo and `develop` is the integration
branch.

## Infrastructure that is gone from active wiring

- Subversion on Assembla (`scm:svn`)
- HTTP Maven/update host `dabiboo.free.fr` and `last.php` “latest JAR”
  redirects
- FTP deploy to `ftpperso.free.fr`
- Provider id `pluzz` (code is `plugins/francetv`; users rename grab-config)

Current updates use
`https://mika3578.github.io/habitv-repo/repository/`.

## What did not change in kind

Still a Maven multi-module app: `fwk/api` contracts, `application/core`
orchestration, console + JavaFX UIs, plugin JARs, XML
`configuration.xml` / `grabconfig.xml`. See
[`architecture.md`](architecture.md) for the current map.

Do not copy 2017 endpoints, `youtube-dl`, or free.fr updater commands
into setup instructions.
