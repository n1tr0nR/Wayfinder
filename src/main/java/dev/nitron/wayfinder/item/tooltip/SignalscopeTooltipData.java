package dev.nitron.wayfinder.item.tooltip;

import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import net.minecraft.item.tooltip.TooltipData;

public record SignalscopeTooltipData(SignalscopeComponent component) implements TooltipData {
    public SignalscopeTooltipData(SignalscopeComponent component) {
        this.component = component;
    }

    public SignalscopeComponent component() {
        return this.component;
    }
}
