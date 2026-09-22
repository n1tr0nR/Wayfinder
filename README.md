# Wayfinder NeoForge 1.21.1 backport

Private, unofficial NeoForge 1.21.1 backport of **Wayfinder 3.5** by **Nitron / n1tr0n__**.

The gameplay target is the official Wayfinder 3.5 Fabric build for Minecraft 1.21.10, backported to Minecraft 1.21.1 / NeoForge 21.1.249+.

Upstream: https://github.com/n1tr0nR/Wayfinder  
Pinned reference build: https://modrinth.com/mod/wayfinder_rbn

## Status

Playable 3.5 backport with the current upstream workflow represented on NeoForge 1.21.1:

- Signal Array registration, persistence, ownership and privacy
- Signal Tweaker workflow
- Signalscope frequency and gain controls
- lens and upgrade insertion/removal
- standard, Privacy and Vantage lens behavior
- Signal Booster and Signal Expander range upgrades
- server to client signal synchronization
- directional signal audio and HUD
- context-sensitive hand and GUI Signalscope models
- original Wayfinder 3.5 assets/data pulled from the pinned upstream JAR at build time

The obsolete 2.3-only Compass, Signal Beacon and Lifeform Analyzer behavior is intentionally not part of the 3.5 backport.

## Runtime dependencies

Exactly:

- Minecraft 1.21.1
- NeoForge 21.1.249+

No Fabric API, Sinytra Connector, Forgified Fabric API, Cardinal Components API, Nitrogen, MidnightLib, Architectury, Cloth Config or other library mod is required.

## Upstream assets

Wayfinder is **All Rights Reserved**. This repository does not commit the upstream binary textures, sounds or models. During the build, Gradle downloads the official Wayfinder 3.5 Fabric 1.21.10 JAR from Modrinth, verifies its pinned SHA-256 and extracts the required `assets/wayfinder/**` and `data/wayfinder/**` resources.

Do not publish or redistribute this port without permission from the upstream author.

## Building

Java 21 is required.

```bash
gradle build
```

The resulting JAR is written to `build/libs/`.

`gradle build` includes dependency-free tooltip geometry and target-selection regression checks. CI additionally performs client mixin/resource startup and dedicated-server startup smoke tests.

Run `gradle runClientValidation` for an automated in-game pass. It recreates only the isolated `build/client-validation/saves/wayfinder-validation` world, tests item transfers and network controls, captures inventory/HUD scenarios, and exits with a validation result. Screenshots and `validation.txt` remain under `build/client-validation/`; they and the test mod are excluded from the shipped JAR. A graphical session is required (CI can use `xvfb-run`).

## Current client fixes

The current port includes fixes for:

- correct GUI model selection for the Signalscope and installed lens/upgrade variants
- inventory `Alt + mouse wheel` handling through NeoForge `ScreenEvent.MouseScrolled.Pre`
- stable long-range Signalscope indicators at high gain instead of per-frame random flicker
- tooltip panels, slots and controls sharing native tooltip positioning and depth
- identified targets converging in their own color, above other signal frames
- crafting result protection against module duplication
- physical Ctrl fine adjustment on macOS

## Migration provenance

Development originally happened temporarily inside the DeepWake modpack repository. The port was merged there in PR #19, then migrated into this standalone repository. The fixed migration snapshot is based on DeepWake commit `e52c417be942331c9c3981e5dfe65f0ccab22130`. DeepWake PR #21 removed the embedded Wayfinder source tree and its dedicated CI from the modpack repository.

See [PORTING_NOTES.md](PORTING_NOTES.md) and [PARITY_CHECKLIST.md](PARITY_CHECKLIST.md).
