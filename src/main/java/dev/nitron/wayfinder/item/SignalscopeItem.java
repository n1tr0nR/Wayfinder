package dev.nitron.wayfinder.item;

import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderItems;
import dev.nitron.wayfinder.registry.WayfinderSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SignalscopeItem extends Item {
    public SignalscopeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static SignalscopeComponent component(ItemStack stack) {
        return stack.get(WayfinderDataComponents.SIGNALSCOPE.get());
    }

    private static boolean lens(ItemStack stack) {
        return stack.is(WayfinderItems.LENS.get())
                || stack.is(WayfinderItems.PRIVACY_LENS.get())
                || stack.is(WayfinderItems.VANTAGE_LENS.get());
    }

    private static boolean upgrade(ItemStack stack) {
        return stack.is(WayfinderItems.SIGNAL_BOOSTER.get())
                || stack.is(WayfinderItems.SIGNAL_EXPANDER.get());
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack scope,
            ItemStack carried,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess carriedAccess) {
        SignalscopeComponent component = component(scope);

        if (action != ClickAction.SECONDARY || component == null) {
            return super.overrideOtherStackedOnMe(
                    scope, carried, slot, action, player, carriedAccess);
        }

        // Editing a recipe preview bypasses ResultSlot.onTake and ingredient consumption.
        if (slot instanceof ResultSlot) {
            return super.overrideOtherStackedOnMe(
                    scope, carried, slot, action, player, carriedAccess);
        }

        ItemStack oldLens = component.lensItemstack();
        ItemStack oldUpgrade = component.upgradeItemstack();
        boolean upgradeSelected = component.selected() == 2;

        if (oldLens.isEmpty() && lens(carried)) {
            ItemStack inserted = carried.copy();
            inserted.setCount(1);

            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            inserted,
                            oldUpgrade.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldUpgrade.copy(),
                            1));

            carried.shrink(1);
            sound(player, WayfinderSounds.PLACE_LENS.get());
            return true;
        }

        if (!oldLens.isEmpty() && lens(carried)) {
            ItemStack inserted = carried.copy();
            inserted.setCount(1);

            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            inserted,
                            oldUpgrade.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldUpgrade.copy(),
                            1));

            carried.shrink(1);
            carriedAccess.set(oldLens.copy());
            sound(player, WayfinderSounds.PLACE_LENS.get());
            return true;
        }

        if (!oldLens.isEmpty() && carried.isEmpty() && !upgradeSelected) {
            carriedAccess.set(oldLens.copy());

            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            ItemStack.EMPTY,
                            oldUpgrade.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldUpgrade.copy(),
                            oldUpgrade.isEmpty() ? 0 : 2));

            sound(player, WayfinderSounds.TAKE_LENS.get());
            return true;
        }

        if (oldUpgrade.isEmpty() && upgrade(carried)) {
            ItemStack inserted = carried.copy();
            inserted.setCount(1);

            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            oldLens.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldLens.copy(),
                            inserted,
                            2));

            carried.shrink(1);
            sound(player, WayfinderSounds.PLACE_LENS.get());
            return true;
        }

        if (!oldUpgrade.isEmpty() && upgrade(carried)) {
            ItemStack inserted = carried.copy();
            inserted.setCount(1);

            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            oldLens.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldLens.copy(),
                            inserted,
                            2));

            carried.shrink(1);
            carriedAccess.set(oldUpgrade.copy());
            sound(player, WayfinderSounds.PLACE_LENS.get());
            return true;
        }

        if (!oldUpgrade.isEmpty() && carried.isEmpty() && upgradeSelected) {
            carriedAccess.set(oldUpgrade.copy());

            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            oldLens.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldLens.copy(),
                            ItemStack.EMPTY,
                            oldLens.isEmpty() ? 0 : 1));

            sound(player, WayfinderSounds.TAKE_LENS.get());
            return true;
        }

        if (!oldLens.isEmpty()) {
            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            oldLens.copy(),
                            oldUpgrade.isEmpty()
                                    ? ItemStack.EMPTY
                                    : oldUpgrade.copy(),
                            1));
        } else if (!oldUpgrade.isEmpty()) {
            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            ItemStack.EMPTY,
                            oldUpgrade.copy(),
                            2));
        } else {
            scope.set(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(
                            component.frequency(),
                            component.gain(),
                            ItemStack.EMPTY,
                            ItemStack.EMPTY,
                            0));
        }

        return super.overrideOtherStackedOnMe(
                scope, carried, slot, action, player, carriedAccess);
    }

    private static void sound(Player player, SoundEvent sound) {
        if (player.level().isClientSide) {
            player.playNotifySound(
                    sound,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack)
                .copy()
                .withStyle(style -> style.withColor(16243568));
    }

    @Override
    public boolean shouldCauseReequipAnimation(
            ItemStack oldStack,
            ItemStack newStack,
            boolean slotChanged) {
        return !oldStack.is(newStack.getItem());
    }
}
