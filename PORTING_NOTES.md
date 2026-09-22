# Wayfinder 3.5 -> NeoForge 1.21.1 porting notes

Target: Minecraft 1.21.1 / NeoForge 21.1.249+.

Reference behavior and assets come from the pinned official Wayfinder 3.5 Fabric 1.21.10 JAR. The 2.3 NeoForge port was used only as an early compatibility baseline.

## Loader/API replacements

| Upstream concept | NeoForge 1.21.1 backport |
| --- | --- |
| Fabric entrypoints | `@Mod` constructors |
| Fabric registries | NeoForge `DeferredRegister` |
| Fabric networking | NeoForge custom payload API |
| world signal component | vanilla `SavedData` per dimension |
| client HUD callbacks | NeoForge GUI layer/event integration |
| client tick callbacks | `ClientTickEvent.Post` |
| screen mouse wheel handling | `ScreenEvent.MouseScrolled.Pre` |
| non-screen mouse wheel handling | `InputEvent.MouseScrollingEvent` |
| custom item model substitution | client `ItemRendererMixin` |
| advanced tooltip visuals | NeoForge tooltip components/factories and color events |

## 3.5 gameplay represented by the backport

- Signal Array signal storage, ownership and privacy
- Signal Tweaker color/name/network/frequency/type editing
- Signalscope frequency and gain state
- lens slot and upgrade slot
- standard, Privacy and Vantage lenses
- Signal Booster and Signal Expander
- updated HUD/audio signal behavior
- server/client synchronization of signal state

The 2.3-only Compass, Signal Beacon, Concave/Twisted lens workflow and Lifeform Analyzer are not compatibility targets for this branch.

## Signalscope ranges

Range follows the 3.5 gain model used by the port:

- no upgrade: 1000m to 2000m
- Signal Booster: 1500m to 3000m
- Signal Expander: 2500m to 5000m

Gain ranges from 30 to 40. Higher gain increases range while reducing positional accuracy.

The positional error is deterministic for a given signal position and gain so the indicator remains visually stable instead of receiving a new random offset every rendered frame. Once the real transmitter is identified, its frames converge and use the same color as its label. It renders last so another nearby signal cannot cover its frames.

Tooltip text, slots and controls participate in the native component layout; NeoForge supplies their final position. Backgrounds use a 12px outer inset to compensate for the transparent margins in the original textures, at the same depth as the native tooltip pass.

## Client input

Two different NeoForge input event families are required:

- inventory/container scrolling uses `ScreenEvent.MouseScrolled.Pre`
- held Signalscope frequency/gain scrolling uses `InputEvent.MouseScrollingEvent`, which NeoForge fires only outside screens

This distinction is required for Signal Tweaker `Alt + wheel` to work in inventory screens.

## Client model rendering

The Signalscope selects a dedicated GUI model for GUI/GROUND/FIXED contexts and a hand model for held contexts. The item-renderer injection is bound to the exact Minecraft 1.21.1 `ItemRenderer.render(..., BakedModel)` descriptor and the first `BakedModel` argument to avoid silently missing the inventory render path.

## Runtime dependencies

Only Minecraft and NeoForge are runtime requirements. Mixin support is provided by NeoForge itself.

## Validation

CI performs:

1. zero-external-mod-dependency guard,
2. Java 21 build,
3. pinned upstream JAR SHA-256 verification,
4. client startup smoke test for mixin/model/resource failures,
5. dedicated-server startup and Wayfinder datapack parsing smoke test,
6. JAR artifact upload.

`gradle build` also runs tooltip geometry and target-indicator regression checks. `gradle runClientValidation` performs an isolated client/integrated-server pass: module conservation (including crafting outputs), lens filters and range endpoints, physical Ctrl/Alt inventory scroll, packet synchronization, native inventory tooltips at GUI scales 2–4 with modifiers/advanced details, screen-edge placement, gain 30/32/40, multiple signals, camera views, custom colors and frame stability. It produces screenshots for visual inspection; it does not compare against pixel-identical Fabric screenshots or test third-party shader/mod combinations.
