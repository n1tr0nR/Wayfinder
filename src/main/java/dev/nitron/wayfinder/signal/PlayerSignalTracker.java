package dev.nitron.wayfinder.signal;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.blockentity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.network.SyncPlayerSignalStatePayload;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderSounds;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerSignalTracker {
    private static final String DATA_KEY = "wayfinder_player_component";

    private static final Map<UUID, PlayerSignalState> STATES =
            new HashMap<>();

    private static final Map<UUID, Integer> SELECTED_SLOTS =
            new HashMap<>();

    public static PlayerSignalState tick(ServerPlayer player) {
        trackSelectedSlot(player);

        PlayerSignalState previous =
                STATES.computeIfAbsent(
                        player.getUUID(),
                        ignored -> load(player));

        ItemStack scope = player.getMainHandItem();

        if (!(scope.getItem() instanceof SignalscopeItem)) {
            return previous;
        }

        SignalscopeComponent component =
                scope.get(WayfinderDataComponents.SIGNALSCOPE.get());

        if (component == null) {
            return previous;
        }

        float[] factors = new float[3];
        double maxDistance =
                SignalscopeHelper.maxDistance(component);

        for (SignalRecord signal :
                SignalSavedData.get(player.serverLevel()).all()) {
            if (!(player.serverLevel()
                    .getBlockEntity(signal.pos())
                    instanceof SignalArrayBlockEntity array)) {
                continue;
            }

            if (signal.powered()
                    && (array.owner() == null
                    || !player.getUUID().equals(array.owner()))) {
                continue;
            }

            if (signal.powered()
                    && !player.getUUID().equals(signal.owner())) {
                continue;
            }

            if (component.lensItemstack().isEmpty()) {
                continue;
            }

            if (!SignalscopeHelper.canShowSignal(
                    player,
                    signal,
                    component)) {
                continue;
            }

            if (SignalscopeHelper.distance(player, signal)
                    >= maxDistance) {
                continue;
            }

            int type = array.type();

            if (type < 0 || type > 2) {
                continue;
            }

            float factor =
                    SignalscopeHelper.lookFactor(
                            player,
                            signal.pos(),
                            SignalscopeHelper.TRACKING_ANGLE,
                            1.0F);

            factors[type] =
                    Math.max(
                            factors[type],
                            factor);
        }

        PlayerSignalState next =
                new PlayerSignalState(
                        factors[0],
                        factors[1],
                        factors[2]);

        STATES.put(player.getUUID(), next);
        save(player, next);

        PacketDistributor.sendToPlayer(
                player,
                new SyncPlayerSignalStatePayload(next));

        if (next.factor0() == 1.0F
                && next.factor1() == 1.0F
                && next.factor2() == 1.0F) {
            Wayfinder.grantAdvancement(
                    player,
                    ResourceLocation.fromNamespaceAndPath(
                            Wayfinder.MOD_ID,
                            "let_the_choir_sing"),
                    "incode");
        }

        return next;
    }

    private static void trackSelectedSlot(ServerPlayer player) {
        int current = player.getInventory().selected;

        Integer previous =
                SELECTED_SLOTS.put(
                        player.getUUID(),
                        current);

        if (previous == null || previous == current) {
            return;
        }

        Wayfinder.grantAdvancement(
                player,
                ResourceLocation.fromNamespaceAndPath(
                        Wayfinder.MOD_ID,
                        "a_plus_hearing"),
                "incode");

        ItemStack oldStack =
                player.getInventory().getItem(previous);

        ItemStack newStack =
                player.getMainHandItem();

        boolean wasScope =
                oldStack.getItem() instanceof SignalscopeItem;

        boolean isScope =
                newStack.getItem() instanceof SignalscopeItem;

        if (!wasScope && isScope) {
            player.playSound(
                    WayfinderSounds.SIGNALSCOPE.get(),
                    1.0F,
                    0.9F
                            + player.getRandom().nextFloat()
                            * 0.2F);

            player.playSound(
                    SoundEvents.SPYGLASS_USE,
                    1.0F,
                    1.0F);
        }
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(
                player,
                new SyncPlayerSignalStatePayload(
                        STATES.computeIfAbsent(
                                player.getUUID(),
                                ignored -> load(player))));
    }

    public static void remove(ServerPlayer player) {
        STATES.remove(player.getUUID());
        SELECTED_SLOTS.remove(player.getUUID());
    }

    public static void resetForClone(ServerPlayer player) {
        PlayerSignalState reset = new PlayerSignalState(0, 0, 0);
        STATES.put(player.getUUID(), reset);
        SELECTED_SLOTS.remove(player.getUUID());
        save(player, reset);
        PacketDistributor.sendToPlayer(
                player,
                new SyncPlayerSignalStatePayload(reset));
    }

    private static PlayerSignalState load(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();

        if (!root.contains(DATA_KEY)) {
            return new PlayerSignalState(0, 0, 0);
        }

        CompoundTag tag = root.getCompound(DATA_KEY);

        return new PlayerSignalState(
                tag.getFloat("factor0"),
                tag.getFloat("factor1"),
                tag.getFloat("factor2"));
    }

    private static void save(
            ServerPlayer player,
            PlayerSignalState state) {
        CompoundTag tag = new CompoundTag();

        tag.putFloat("factor0", state.factor0());
        tag.putFloat("factor1", state.factor1());
        tag.putFloat("factor2", state.factor2());

        player.getPersistentData()
                .put(DATA_KEY, tag);
    }

    private PlayerSignalTracker() {}
}
