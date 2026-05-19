# Habitv runtime quickstart

This guide walks through running habitv with a working YouTube download
provider on a Linux machine. It assumes a clean checkout of the
modernized branch and Java 8+ on the PATH.

## Prerequisites

- JDK 8 or later (the reactor compiles with `<source>1.8</source>`)
- Maven 3.6+
- `yt-dlp` on the PATH (`apt install yt-dlp` on recent Ubuntu, or
  `pip install --user yt-dlp`)
- `curl` on the PATH (already standard on most distros)

## Build

```bash
mvn -B -ntp -DskipTests -pl '!application/trayView,!application/habiTv' package
```

The JavaFX-based GUI modules (`trayView`, `habiTv`) are excluded for
now because they depend on the JDK-bundled JavaFX runtime, which is
not part of OpenJDK 11+. The CLI launcher (`consoleView`) is fully
functional and ships everything needed for download/export pipelines.

Produces:

- `application/consoleView/target/consoleView-4.1.0-SNAPSHOT-all.jar`
  (fat jar with all runtime dependencies)
- One JAR per plugin under `plugins/<name>/target/<name>-4.1.0-SNAPSHOT.jar`

## Lay out the runtime directory

```bash
mkdir -p ~/habitv-runtime/{plugins,downloads,index,bin}
cp application/consoleView/target/consoleView-4.1.0-SNAPSHOT-all.jar ~/habitv-runtime/habitv.jar
cp plugins/youtube/target/youtube-4.1.0-SNAPSHOT.jar ~/habitv-runtime/plugins/
cp plugins/curl/target/curl-4.1.0-SNAPSHOT.jar    ~/habitv-runtime/plugins/
```

## Configure

Place the following at `~/habitv-runtime/configuration.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<tns:configuration xmlns:tns="http://www.dabi.com/habitv/configuration/entities">
    <osConfig>
        <cmdProcessor>/bin/sh -c #CMD#</cmdProcessor>
    </osConfig>
    <downloadConfig>
        <downloadOuput>/home/USER/habitv-runtime/downloads/#TVSHOW_NAME#-#EPISODE_NAME_CUT#.#EXTENSION#</downloadOuput>
        <maxAttempts>1</maxAttempts>
        <demonCheckTime>1800</demonCheckTime>
        <fileNameCutSize>50</fileNameCutSize>
        <downloaders>
            <youtube>/usr/bin/yt-dlp</youtube>
            <curl>/usr/bin/curl</curl>
        </downloaders>
    </downloadConfig>
    <updateConfig>
        <updateOnStartup>false</updateOnStartup>
        <autoriseSnapshot>false</autoriseSnapshot>
    </updateConfig>
    <taskDefinition>
        <category>5</category>
        <export>1</export>
        <retreive>10</retreive>
        <search>5</search>
        <download>1</download>
    </taskDefinition>
</tns:configuration>
```

The `<cmdProcessor>` element is required so that command lines
containing spaces (e.g. the `--user-agent "Mozilla/5.0 ..."` flag in
the curl plugin) are passed to a shell rather than parsed with
`Runtime.exec(String)`.

## Run

List loaded plugins:

```bash
cd ~/habitv-runtime
java -jar habitv.jar -lp
```

Expected output:

```
Plugin provider :
youtube

Plugin downloader :
youtube
curl
youtube-mp3

Plugin exporter :
curl
```

Trigger a manual download by passing a URL as the only argument:

```bash
java -jar habitv.jar "https://www.youtube.com/watch?v=jNQXAC9IVRw"
```

habitv:
1. Detects the URL is for YouTube and selects `YoutubePluginDownloader`.
2. Builds the command:
   `/usr/bin/yt-dlp "https://www.youtube.com/watch?v=..." -o "<dest>" --write-sub --write-auto-sub --no-check-certificate`
3. Executes it through `/bin/sh -c`.
4. The downloaded file appears under `~/habitv-runtime/downloads/`.

A non-YouTube URL falls back to the `curl` downloader:

```bash
java -jar habitv.jar "https://example.com/some/file.mp4"
```

## Notes for restricted networks

If outbound HTTPS to `youtube.com` is blocked at the egress firewall,
yt-dlp will fail with `SSL: CERTIFICATE_VERIFY_FAILED` or 403. The
plugin itself is fully wired (see
`plugins/youtube/test/.../YoutubePluginDownloaderCmdTest.java`); the
limitation is purely network policy of the runtime environment.

Startup telemetry is disabled by default. Plugin update checks are enabled by
default on startup for GUI and console launches.
Use `-Dhabitv.stat.enabled=true` and `-Dhabitv.stat.url=...` only when needed.
You can disable runtime plugin updates explicitly with
`-Dhabitv.update.enabled=false` (optional custom base:
`-Dhabitv.update.url=...`).

Development snapshot updates: keep `<autoriseSnapshot>false</autoriseSnapshot>`
in `configuration.xml` and pass `-Dhabitv.update.autoriseSnapshot=true` when you
need Maven timestamped SNAPSHOT plugin artifacts from the static repository.
This override must not be enabled for normal end users.

Reference launch command:

```bash
java -Dhabitv.update.enabled=true \
  -Dhabitv.update.autoriseSnapshot=true \
  -Dhabitv.update.url=https://mika3578.github.io/habitv-repo/repository/ \
  -jar application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar
```
