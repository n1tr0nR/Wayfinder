package dev.nitron.wayfinder.mixin;

import dev.nitron.wayfinder.item.SignalscopeItem;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow
    private ItemStack lastToolHighlight;

    @ModifyVariable(
            method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int wayfinder$shiftSignalscopeTooltip(int yShift) {
        if (lastToolHighlight.getItem() instanceof SignalscopeItem) {
            return Math.max(yShift, 59) + 13;
        }
        return yShift;
    }
}
