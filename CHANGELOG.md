# Changelog

## 3.5-neoforge.1.21.1-backport.3

- Render inventory module slots and Signal Tweaker controls as native tooltip components, using one layout and depth pass.
- Account for transparent texture borders so backgrounds and frames contain all tooltip content.
- Converge identified transmitter frames on the real target and use its custom color; draw the target last to prevent other red frames covering it. Keep deterministic gain error for other signals.
- Prevent extracting free modules from crafting result previews before consuming ingredients.
- Preserve upstream physical Ctrl fine adjustment on macOS.
- Add geometry/target regression checks and an isolated Minecraft client validation mod for inventory, rendering and network behavior. Test code is excluded from the shipped JAR.

## 3.5-neoforge.1.21.1-backport.2

- Fix Signalscope GUI/inventory model selection so the correct lens/upgrade model renders in the slot.
- Fix Signal Tweaker and Signalscope inventory mouse-wheel controls by using NeoForge screen scroll events.
- Stabilize high-gain long-range Signalscope indicators so positional inaccuracy no longer changes every rendered frame.
- Move development out of the DeepWake modpack repository into this standalone private repository.
- Add client mixin/model/resource and dedicated-server CI smoke tests.
