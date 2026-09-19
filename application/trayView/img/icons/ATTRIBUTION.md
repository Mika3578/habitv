# UI provider and channel logos — attribution

Runtime assets are optimized transparent PNGs (32×32) under
`application/trayView/img/icons/`. They are loaded from the trayView
classpath only; Habitv never downloads logos at runtime.

## Copyright vs trademark

Bundled files were selected from **Wikimedia Commons** entries marked
**Public domain / PD-textlogo** (or **CC0** for TF1+). That status
addresses **copyright** for simple text/geometric logos.

These marks remain **trademarks** of their owners. Habitv uses them only
as small identification icons next to existing text labels in the
category tree (nominative identification of providers/channels). This is
not an endorsement by the broadcasters.

If a rights holder objects, remove the corresponding PNG and mapping
row; the UI falls back to text-only automatically.

## Source vs runtime

| Layer | Location |
|-------|----------|
| Research SVG downloads | local `agent_space/logo-research/` (gitignored) |
| Runtime PNG | `application/trayView/img/icons/...` (this tree) |

SVG originals are not shipped in the application JAR to avoid JavaFX SVG
dependencies. Conversion used CairoSVG → 32×32 RGBA PNG.

## Bundled runtime assets

| Runtime path | Identifies | Commons / source page | Author / editor | Copyright note | Trademark |
|--------------|------------|------------------------|-----------------|----------------|-----------|
| `icons/providers/francetv.png` | Provider `francetv` | [France.tv - logo 2022.svg](https://commons.wikimedia.org/wiki/File:France.tv_-_logo_2022.svg) (official source cited: france.tv) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-2.png` | Channel France 2 | [France 2 - logo 2018.svg](https://commons.wikimedia.org/wiki/File:France_2_-_logo_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-3.png` | Channel France 3 | [France 3 - logo 2018.svg](https://commons.wikimedia.org/wiki/File:France_3_-_logo_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-4.png` | Channel France 4 | [France 4 - logo 2018.svg](https://commons.wikimedia.org/wiki/File:France_4_-_logo_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-5.png` | Channel France 5 | [France 5 - logo 2018.svg](https://commons.wikimedia.org/wiki/File:France_5_-_logo_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/franceinfo.png` | Hub franceinfo | [Franceinfo.svg](https://commons.wikimedia.org/wiki/File:Franceinfo.svg) | France Info / France Télévisions | PD-textlogo | Trademarked |
| `icons/providers/6play.png` | Provider `6play` | [Logo M6 (2020, fond clair).svg](https://commons.wikimedia.org/wiki/File:Logo_M6_(2020,_fond_clair).svg) | Étienne Robial / Gédéon for M6 | PD-textlogo | Trademarked |
| `icons/channels/6play/m6.png` | Channel M6 | same as above | same | PD-textlogo | Trademarked |
| `icons/channels/6play/w9.png` | Channel W9 | [W9 2018.svg](https://commons.wikimedia.org/wiki/File:W9_2018.svg) (source cited: 6play.fr assets) | Gédéon / Groupe M6 | PD-textlogo | Trademarked |
| `icons/channels/6play/6ter.png` | Channel 6ter | [Logo 6ter 2016.svg](https://commons.wikimedia.org/wiki/File:Logo_6ter_2016.svg) | Groupe M6 | PD-textlogo | Trademarked |
| `icons/providers/wat.png` | Provider `wat` (TF1+) | [Logo TF1+.svg](https://commons.wikimedia.org/wiki/File:Logo_TF1%2B.svg) | Llempereur - 4uatre (Commons) | **CC0 1.0** | Trademarked (TF1+) |
| `icons/channels/wat/tf1.png` | Channel TF1 | [Logo TF1 2013.svg](https://commons.wikimedia.org/wiki/File:Logo_TF1_2013.svg) | Patrick Delobelle (Naked) for TF1 | PD-textlogo | Trademarked |
| `icons/providers/canalPlus.png` | Provider `canalPlus` | [Canal+ France 2011 logo.svg](https://commons.wikimedia.org/wiki/File:Canal%2B_France_2011_logo.svg) | Canal+ (unknown uploader on Commons) | PD-textlogo | Trademarked |

## Researched but not bundled

| Target | Reason |
|--------|--------|
| Arte / arte.tv | Commons has PD-textlogo SVGs, but [ARTE visual identity](https://corporate.arte.tv/en/visual-identity/) requires **prior authorisation** for logo use. Not committed. |
| TFX | Commons SVG present (PD-textlogo) but conversion failed (external entities / malformed SVG). Not committed. |
| TMC (current) | No suitable current SVG with clear PD/CC0 status found (JPEG / outdated logos only). |
| LCI | No suitable current SVG located on Commons. |
| CNews (France) | No clear French CNews PD SVG confirmed; `CNews logo.svg` on Commons is the Russian CNews.ru site. |
| BFM TV | Commons PD-textlogo SVG found; Habitv has no BFM provider plugin id to map yet. Kept in research only. |
| M6+ brand (distinct from M6) | No separate clear Commons SVG used; `6play` provider uses the M6 mark as the closest identifiable PD asset. |
| Official france.tv live URL `.../france-tv-black.svg` | HTTP 404 at research time; Commons file citing that URL was used instead. |

## Random logo aggregators

Logopedia / Fandom and similar sites were **not** used as authoritative
download sources for committed assets.
