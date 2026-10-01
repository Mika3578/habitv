# UI provider and channel logos — attribution

Runtime assets are optimized transparent PNGs (max side 256px, native
aspect ratio, no forced square crop) under `application/trayView/img/icons/`.
They are loaded from the trayView classpath only; Habitv never downloads
logos at runtime.

Channel marks prefer near-square on-air identities when available on
Commons. Wide wordmarks (providers) keep their aspect and render in a
wide tree slot — the same approach used by IPTV / media-center lists
(`object-contain` in a fixed box, transparent background).

## Copyright vs trademark

Bundled files come from two source families:

- **Wikimedia Commons** entries marked **Public domain / PD-textlogo**
  (or **CC0** for TF1+).
- **[paomedia/chaines-tv-francaises](https://github.com/paomedia/chaines-tv-francaises)**
  512×512 TNT marks. The repository carries **no explicit license file**;
  each committed file was cross-checked against its closest
  **Commons PD-textlogo equivalent** (same simple text/geometric mark) —
  the two render identically at tree size. Copyright status therefore
  follows the PD-textlogo analysis below. Removal path: delete the PNG
  and mapping row; the UI falls back to text-only automatically.

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
dependencies. Conversion uses Wikimedia rendered thumbs → RGBA PNG with
max side 256px (aspect preserved).

## Bundled runtime assets

| Runtime path | Identifies | Commons / source page | Author / editor | Copyright note | Trademark |
|--------------|------------|------------------------|-----------------|----------------|-----------|
| `icons/providers/francetv.png` | Provider `francetv` | [France.tv - logo 2022.svg](https://commons.wikimedia.org/wiki/File:France.tv_-_logo_2022.svg) (official source cited: france.tv) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-2.png` | Channel France 2 | [France 2 2018.svg](https://commons.wikimedia.org/wiki/File:France_2_2018.svg) (near-square on-air mark) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-3.png` | Channel France 3 | [France 3 2018.svg](https://commons.wikimedia.org/wiki/File:France_3_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-4.png` | Channel France 4 | [France 4 2018.svg](https://commons.wikimedia.org/wiki/File:France_4_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/france-5.png` | Channel France 5 | [France 5 2018.svg](https://commons.wikimedia.org/wiki/File:France_5_2018.svg) | France Télévisions | PD-textlogo | Trademarked |
| `icons/channels/francetv/franceinfo.png` | Hub franceinfo | [Franceinfo.svg](https://commons.wikimedia.org/wiki/File:Franceinfo.svg) | France Info / France Télévisions | PD-textlogo | Trademarked |
| `icons/providers/6play.png` | Provider `6play` | [Logo M6 (2020, fond clair).svg](https://commons.wikimedia.org/wiki/File:Logo_M6_(2020,_fond_clair).svg) | Étienne Robial / Gédéon for M6 | PD-textlogo | Trademarked |
| `icons/channels/6play/m6.png` | Channel M6 | same as above | same | PD-textlogo | Trademarked |
| `icons/channels/6play/w9.png` | Channel W9 | [W9 2018.svg](https://commons.wikimedia.org/wiki/File:W9_2018.svg) (source cited: 6play.fr assets) | Gédéon / Groupe M6 | PD-textlogo | Trademarked |
| `icons/channels/6play/6ter.png` | Channel 6ter | [Logo 6ter 2016.svg](https://commons.wikimedia.org/wiki/File:Logo_6ter_2016.svg) | Groupe M6 | PD-textlogo | Trademarked |
| `icons/providers/wat.png` | Provider `wat` (TF1+) | [Logo TF1+.svg](https://commons.wikimedia.org/wiki/File:Logo_TF1%2B.svg) | Llempereur - 4uatre (Commons) | **CC0 1.0** | Trademarked (TF1+) |
| `icons/providers/tf1plus.png` | Provider `tf1plus` | same mark as `wat` | same | CC0 1.0 | Trademarked (TF1+) |
| `icons/channels/wat/tf1.png` | Channel TF1 | [paomedia 01.png](https://github.com/paomedia/chaines-tv-francaises) (current color block) | TF1 Group | PD-textlogo (simple mark) | Trademarked |
| `icons/channels/wat/tfx.png` | Channel TFX | [paomedia 11.png](https://github.com/paomedia/chaines-tv-francaises) | TF1 Group | PD-textlogo (simple mark) | Trademarked |
| `icons/channels/wat/tmc.png` | Channel TMC | [paomedia 10.png](https://github.com/paomedia/chaines-tv-francaises) | TF1 Group | PD-textlogo (simple mark) | Trademarked |
| `icons/channels/wat/lci.png` | Channel LCI | [paomedia 26.png](https://github.com/paomedia/chaines-tv-francaises) | TF1 Group | PD-textlogo (simple mark) | Trademarked |
| `icons/channels/wat/tf1-series-films.png` (+ `tf1-sries-films.png` spelling variant) | Channel TF1 Séries Films | [paomedia 20.png](https://github.com/paomedia/chaines-tv-francaises) | TF1 Group | PD-textlogo (simple mark) | Trademarked |
| `icons/providers/arte.png` | Provider `arte` | [paomedia 07.png](https://github.com/paomedia/chaines-tv-francaises) (Commons ARTE marks are PD-textlogo; official policy requests prior authorisation — remove if objected) | ARTE | PD-textlogo | Trademarked |
| `icons/providers/canalPlus.png` | Provider `canalPlus` | [paomedia 04.png](https://github.com/paomedia/chaines-tv-francaises) (current CANAL+ block; replaces 2011 Commons mark) | Canal+ | PD-textlogo (simple mark) | Trademarked |

## Researched but not bundled

| Target | Reason |
|--------|--------|
| Arte / arte.tv (Commons SVG) | Superseded: current mark bundled from paomedia TNT set. Official ARTE policy still requests prior authorisation — remove if objected. |
| TFX | Superseded: current mark bundled from paomedia TNT set. |
| TMC (current) | Superseded: current mark bundled from paomedia TNT set. |
| LCI | Superseded: current mark bundled from paomedia TNT set. |
| France 2/3/4/5 paomedia white on-air variants | White-on-transparent, invisible on the white tree; colored Commons on-air marks used instead. |
| franceinfo paomedia variant | White-on-transparent; colored Commons wordmark used instead. |
| CNews (France) | No clear French CNews PD SVG confirmed; `CNews logo.svg` on Commons is the Russian CNews.ru site. |
| BFM TV | Commons PD-textlogo SVG found; Habitv has no BFM provider plugin id to map yet. Kept in research only. |
| M6+ brand (distinct from M6) | No separate clear Commons SVG used; `6play` provider uses the M6 mark as the closest identifiable PD asset. |
| Official france.tv live URL `.../france-tv-black.svg` | HTTP 404 at research time; Commons file citing that URL was used instead. |

## Random logo aggregators

Logopedia / Fandom and similar sites were **not** used as authoritative
download sources for committed assets.
