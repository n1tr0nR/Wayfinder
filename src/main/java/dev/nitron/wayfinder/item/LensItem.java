package dev.nitron.wayfinder.item;

import dev.nitron.wayfinder.registries.WayfinderItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class LensItem extends Item {
    public LensItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (stack.isOf(WayfinderItems.PRIVACY_LENS)){
            tooltip.add(Text.literal("Hides every Signal that does").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("not belong to you").formatted(Formatting.GRAY));
        }
        if (stack.isOf(WayfinderItems.VANTAGE_LENS)){
            tooltip.add(Text.literal("Only shows Signals that do").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("not belong to you").formatted(Formatting.GRAY));
        }
        if (stack.isOf(WayfinderItems.TWISTED_LENS)){
            tooltip.add(Text.literal("Shows only signals that").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("have a strange sound...").formatted(Formatting.GRAY));
        }
        if (stack.isOf(WayfinderItems.CONCAVE_LENS)){
            tooltip.add(Text.literal("Increases the range of the").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Signalscope to be 3x as long").formatted(Formatting.GRAY));
        }
    }
}
