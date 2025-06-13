# HabiTV Maven Repository

This branch (`gh-pages`) serves as a GitHub Pages static Maven repository mirror for HabiTV.

It replaces the old HTTP repository that was available at: http://dabiboo.free.fr/repository/com/dabi/habitv/

## Usage

To use this repository in your Maven project, add the following repository configuration to your `pom.xml`:

```xml
<repository>
  <id>habitv-mirror</id>
  <url>https://mika3578.github.io/habitv/repository/</url>
</repository>
```

This repository contains all Maven artifacts from the original HabiTV repository, including:
- Core libraries
- Plugins
- Dependencies

The following third-party dependencies are mirrored in this repository:
- junit 4.11
- jsoup 1.6.3
- hamcrest-core 1.3
- activation 1.1
- jaxb-api 2.0
- plugin-tester 4.1.0

All artifacts are served over HTTPS through GitHub Pages.
