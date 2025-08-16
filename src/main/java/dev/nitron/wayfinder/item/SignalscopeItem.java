package dev.nitron.wayfinder.item;

import com.nitron.nitrogen.util.interfaces.ColorableItem;
import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.item.tooltip.SignalscopeTooltipData;
import dev.nitron.wayfinder.registries.WayfinderDataComponents;
import dev.nitron.wayfinder.registries.WayfinderItems;
import dev.nitron.wayfinder.registries.WayfinderSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class SignalscopeItem extends Item implements ColorableItem {
    public SignalscopeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1.0F, 1.0F);
        Wayfinder.grantAdvancement(user, Identifier.of(Wayfinder.MOD_ID, "a_plus_hearing"), "incode");
        user.playSound(WayfinderSounds.SIGNALSCOPE, 0.5F, 0.9F + (world.getRandom().nextFloat() * 0.2F));
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        SignalscopeComponent component = stack.get(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE);
        if (clickType == ClickType.RIGHT) {
            if (component.item().isEmpty() && otherStack.getItem() instanceof LensItem){
                ItemStack inserted = otherStack.copyWithCount(1);
                player.playSoundToPlayer(WayfinderSounds.PLACE_LENS, SoundCategory.PLAYERS, 1.0F, 1.0F);
                stack.set(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE, new SignalscopeComponent(inserted));
                otherStack.decrement(1);
                return true;
            }
            if (!component.item().isEmpty() && cursorStackReference.get().isEmpty()) {
                cursorStackReference.set(component.item().copy());
                player.playSoundToPlayer(WayfinderSounds.TAKE_LENS, SoundCategory.PLAYERS, 1.0F, 1.0F);
                stack.set(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE, new SignalscopeComponent(ItemStack.EMPTY));
                return true;
            }
        }
        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (stack.contains(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE)) {
            SignalscopeComponent component = stack.get(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE);
            return Optional.of(new SignalscopeTooltipData(component));
        }
        return super.getTooltipData(stack);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
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
            tooltip.add(Text.literal("Hold down [").append(Text.literal("Shift").withColor(0xf7db70)).append("] for details.").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal(" "));
            tooltip.add(Text.literal("Can be used to track down ").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("signal placed by other players, ").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("using a ").append(Text.literal("Signal Array").formatted(Formatting.BOLD).withColor(0xf7db70)).append(Text.literal(".")).formatted(Formatting.GRAY));
        } else {
            tooltip.add(Text.literal("Hold down [").append(Text.literal("Shift").formatted(Formatting.GRAY)).append("] for details.").formatted(Formatting.DARK_GRAY));
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack).copy().withColor(0xf7db70);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int startColor(ItemStack itemStack) {
        return 0xFF752d07;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int endColor(ItemStack itemStack) {
        return 0xFF591804;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int backgroundColor(ItemStack itemStack) {
        return 0xF0140502;
    }
}
