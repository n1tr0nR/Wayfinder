package dev.nitron.wayfinder.network;

import dev.nitron.wayfinder.item.SignalTweakerItem;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderSounds;
import dev.nitron.wayfinder.signal.PlayerSignalTracker;
import dev.nitron.wayfinder.signal.SignalSavedData;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class WayfinderNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("3");

        registrar.playToClient(
                SyncSignalsPayload.TYPE,
                SyncSignalsPayload.STREAM_CODEC,
                WayfinderNetwork::handleSyncSignals);

        registrar.playToClient(
                SyncPlayerSignalStatePayload.TYPE,
                SyncPlayerSignalStatePayload.STREAM_CODEC,
                WayfinderNetwork::handleSyncState);

        registrar.playToServer(
                SignalscopeChangePayload.TYPE,
                SignalscopeChangePayload.STREAM_CODEC,
                WayfinderNetwork::handleScopeChange);

        registrar.playToServer(
                SignalscopeSlotPayload.TYPE,
                SignalscopeSlotPayload.STREAM_CODEC,
                WayfinderNetwork::handleScopeSlot);

        registrar.playToServer(
                SignalTweakerSlotPayload.TYPE,
                SignalTweakerSlotPayload.STREAM_CODEC,
                WayfinderNetwork::handleTweakerSlot);
    }

    private static void handleSyncSignals(
            SyncSignalsPayload payload,
            IPayloadContext context) {
        ClientSignalStore.replace(payload.signals());
    }

    private static void handleSyncState(
            SyncPlayerSignalStatePayload payload,
            IPayloadContext context) {
        ClientPlayerSignalStore.replace(payload.state());
    }

    private static void handleScopeChange(
            SignalscopeChangePayload payload,
            IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SignalscopeItem)) return;

        SignalscopeComponent component =
                stack.get(WayfinderDataComponents.SIGNALSCOPE.get());

        if (component == null) return;

        stack.set(
                WayfinderDataComponents.SIGNALSCOPE.get(),
                new SignalscopeComponent(
                        payload.newFrequency(),
                        payload.newGain(),
                        component.lensItemstack().copy(),
                        component.upgradeItemstack().copy(),
                        component.selected()));

        if (payload.oldFrequency() != payload.newFrequency()
                || payload.oldGain() != payload.newGain()) {
            float pitch =
                    payload.oldFrequency() > payload.newFrequency()
                            || payload.oldGain() > payload.newGain()
                            ? 0.8F
                            : 0.95F;

            player.playNotifySound(
                    WayfinderSounds.SIGNALSCOPE_INCREASE.get(),
                    SoundSource.PLAYERS,
                    0.5F,
                    pitch + player.getRandom().nextFloat() * 0.1F);
        }
    }

    private static void handleScopeSlot(
            SignalscopeSlotPayload payload,
            IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || payload.slot() < 0
                || payload.slot() >= player.containerMenu.slots.size()) {
            return;
        }

        ItemStack stack =
                player.containerMenu.getSlot(payload.slot()).getItem();

        if (!(stack.getItem() instanceof SignalscopeItem)) return;

        SignalscopeComponent component =
                stack.get(WayfinderDataComponents.SIGNALSCOPE.get());

        if (component == null) return;

        int oldSelected = component.selected();
        int newSelected = payload.selected();

        stack.set(
                WayfinderDataComponents.SIGNALSCOPE.get(),
                new SignalscopeComponent(
                        component.frequency(),
                        component.gain(),
                        component.lensItemstack().copy(),
                        component.upgradeItemstack().copy(),
                        newSelected));

        if (oldSelected != newSelected) {
            float pitch =
                    oldSelected > newSelected
                            ? 0.8F
                            : 0.95F;

            player.playNotifySound(
                    WayfinderSounds.SIGNALSCOPE_INCREASE.get(),
                    SoundSource.PLAYERS,
                    0.5F,
                    pitch + player.getRandom().nextFloat() * 0.1F);
        }

        ItemStack selectedModule =
                newSelected == 2
                        ? component.upgradeItemstack()
                        : newSelected == 1
                        ? component.lensItemstack()
                        : ItemStack.EMPTY;

        if (!selectedModule.isEmpty()
                && oldSelected != newSelected) {
            float pitch =
                    oldSelected > newSelected
                            ? 0.8F
                            : 0.95F;

            player.playNotifySound(
                    SoundEvents.VAULT_ACTIVATE,
                    SoundSource.PLAYERS,
                    0.5F,
                    pitch + player.getRandom().nextFloat() * 0.1F);
        }
    }

    private static void handleTweakerSlot(
            SignalTweakerSlotPayload payload,
            IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || payload.slot() < 0
                || payload.slot() >= player.containerMenu.slots.size()) {
            return;
        }

        ItemStack stack =
                player.containerMenu.getSlot(payload.slot()).getItem();

        if (!(stack.getItem() instanceof SignalTweakerItem)) return;

        SignalTweakerComponent component =
                stack.get(WayfinderDataComponents.SIGNAL_TWEAKER.get());

        if (component == null) return;

        int direction = payload.direction();

        if (direction != 0) {
            player.playNotifySound(
                    WayfinderSounds.SIGNALSCOPE_INCREASE.get(),
                    SoundSource.PLAYERS,
                    0.5F,
                    0.95F + player.getRandom().nextFloat() * 0.1F);

            if (payload.alt()) {
                player.playNotifySound(
                        SoundEvents.VAULT_ACTIVATE,
                        SoundSource.PLAYERS,
                        0.5F,
                        0.95F + player.getRandom().nextFloat() * 0.1F);
            }
        }

        if (payload.alt()) {
            int selected = component.selected();

            if (direction > 0 && --selected < 0) {
                selected = 5;
            }

            if (direction < 0 && ++selected > 5) {
                selected = 0;
            }

            stack.set(
                    WayfinderDataComponents.SIGNAL_TWEAKER.get(),
                    new SignalTweakerComponent(
                            component.color(),
                            component.name(),
                            component.privateNetwork(),
                            component.freq(),
                            component.type(),
                            selected));

            return;
        }

        direction *= -1;

        int selected = component.selected();
        int red = component.color().getX();
        int green = component.color().getY();
        int blue = component.color().getZ();
        int frequency = component.freq();
        int type = component.type();
        boolean privateNetwork = component.privateNetwork();

        int step = payload.control() ? 1 : 5;

        if (selected == 0) {
            red = wrapColor(red, direction, step);
            playTweakerSound(player, SoundEvents.DYE_USE, 0.1F);
        }

        if (selected == 1) {
            green = wrapColor(green, direction, step);
            playTweakerSound(player, SoundEvents.DYE_USE, 0.1F);
        }

        if (selected == 2) {
            blue = wrapColor(blue, direction, step);
            playTweakerSound(player, SoundEvents.DYE_USE, 0.1F);
        }

        if (selected == 3) {
            if (direction != 0) {
                privateNetwork = !privateNetwork;
            }

            player.playNotifySound(
                    privateNetwork
                            ? SoundEvents.VAULT_CLOSE_SHUTTER
                            : SoundEvents.VAULT_OPEN_SHUTTER,
                    SoundSource.PLAYERS,
                    0.5F,
                    0.95F + player.getRandom().nextFloat() * 0.1F);
        }

        if (selected == 4) {
            if (direction > 0 && (frequency += 3) > 12) {
                frequency = 3;
            }

            if (direction < 0 && (frequency -= 3) < 3) {
                frequency = 12;
            }

            player.playNotifySound(
                    SoundEvents.NOTE_BLOCK_CHIME.value(),
                    SoundSource.PLAYERS,
                    0.3F,
                    0.95F + player.getRandom().nextFloat() * 0.1F);
        }

        if (selected == 5) {
            if (direction > 0 && ++type > 2) {
                type = 0;
            }

            if (direction < 0 && --type < 0) {
                type = 2;
            }

            player.playNotifySound(
                    SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.PLAYERS,
                    0.3F,
                    0.95F + player.getRandom().nextFloat() * 0.1F);
        }

        stack.set(
                WayfinderDataComponents.SIGNAL_TWEAKER.get(),
                new SignalTweakerComponent(
                        new Vec3i(red, green, blue),
                        component.name(),
                        privateNetwork,
                        frequency,
                        type,
                        selected));
    }

    private static void playTweakerSound(
            ServerPlayer player,
            net.minecraft.sounds.SoundEvent sound,
            float volume) {
        player.playNotifySound(
                sound,
                SoundSource.PLAYERS,
                volume,
                0.95F + player.getRandom().nextFloat() * 0.1F);
    }

    private static int wrapColor(
            int value,
            int direction,
            int step) {
        if (direction > 0) {
            value -= step;
            if (value < 0) value = 255;
        }

        if (direction < 0) {
            value += step;
            if (value > 255) value = 0;
        }

        return value;
    }

    public static void syncLevel(ServerLevel level) {
        SyncSignalsPayload payload =
                new SyncSignalsPayload(
                        SignalSavedData.get(level)
                                .all()
                                .stream()
                                .toList());

        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(
                player,
                new SyncSignalsPayload(
                        SignalSavedData.get(player.serverLevel())
                                .all()
                                .stream()
                                .toList()));
    }

    public static void onPlayerLogin(
            PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayer(player);
            PlayerSignalTracker.sync(player);
        }
    }

    public static void onPlayerChangedDimension(
            PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayer(player);
            PlayerSignalTracker.sync(player);
        }
    }

    private WayfinderNetwork() {}
}
