package dev.nitron.wayfinder.item;

import dev.nitron.wayfinder.registries.WayfinderSounds;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

public class SignalscopeItem extends Item {
    public SignalscopeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1.0F, 1.0F);
        user.playSound(WayfinderSounds.SIGNALSCOPE, 0.5F, 0.9F + (world.getRandom().nextFloat() * 0.2F));
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 1200;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPYGLASS;
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        this.playStopUsingSound(user);
        return stack;
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        this.playStopUsingSound(user);
    }

    private void playStopUsingSound(LivingEntity user) {
        user.playSound(SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown()){
            tooltip.add(Text.literal("Hold down [").append(Text.literal("Shift").formatted(Formatting.YELLOW)).append("] for details.").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal(" "));
            tooltip.add(Text.literal("Can be used to track down ").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("signal placed by other players, ").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("using the ").append(Text.literal("Signal Arrays").formatted(Formatting.BOLD).formatted(Formatting.YELLOW)).append(Text.literal(".")).formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.literal("Hold down [").append(Text.literal("Shift").formatted(Formatting.GRAY)).append("] for details.").formatted(Formatting.DARK_GRAY));
        }
    }
}
