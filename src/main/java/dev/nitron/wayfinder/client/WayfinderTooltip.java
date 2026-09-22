package dev.nitron.wayfinder.client;

import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Client-only tooltip rows; these are not saved or sent over the network. */
public sealed interface WayfinderTooltip extends TooltipComponent {
    record Modules(SignalscopeComponent value) implements WayfinderTooltip {}
    record Colors(SignalTweakerComponent value) implements WayfinderTooltip {}
    record Network(SignalTweakerComponent value) implements WayfinderTooltip {}
    record Data(SignalTweakerComponent value) implements WayfinderTooltip {}
}
