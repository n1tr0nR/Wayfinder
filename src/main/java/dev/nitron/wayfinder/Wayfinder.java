package dev.nitron.wayfinder;

import dev.nitron.wayfinder.command.WayfinderCommands;
import dev.nitron.wayfinder.network.WayfinderNetwork;
import dev.nitron.wayfinder.registry.*;
import dev.nitron.wayfinder.signal.PlayerSignalTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(Wayfinder.MOD_ID)
public final class Wayfinder {
    public static final String MOD_ID = "wayfinder";

    public Wayfinder(IEventBus modBus) {
        WayfinderDataComponents.DATA_COMPONENTS.register(modBus);
        WayfinderBlocks.BLOCKS.register(modBus);
        WayfinderItems.ITEMS.register(modBus);
        WayfinderBlockEntities.BLOCK_ENTITIES.register(modBus);
        WayfinderSounds.SOUNDS.register(modBus);

        modBus.addListener(WayfinderNetwork::registerPayloads);
        modBus.addListener(Wayfinder::buildCreativeTabs);

        NeoForge.EVENT_BUS.addListener(Wayfinder::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(WayfinderNetwork::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(WayfinderNetwork::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(Wayfinder::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(Wayfinder::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(Wayfinder::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        WayfinderCommands.register(
                event.getDispatcher(),
                event.getBuildContext(),
                event.getCommandSelection());
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSignalTracker.tick(player);
        }
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSignalTracker.remove(player);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerSignalTracker.resetForClone(player);
        }
    }

    public static void grantAdvancement(Player player, ResourceLocation id, String criterion) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        var advancement = serverPlayer.getServer().getAdvancements().get(id);
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, criterion);
        }
    }

    private static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(
                    Items.SPYGLASS.getDefaultInstance(),
                    WayfinderItems.SIGNALSCOPE.get().getDefaultInstance(),
                    visibility);
            event.insertAfter(
                    WayfinderItems.SIGNALSCOPE.get().getDefaultInstance(),
                    WayfinderItems.SIGNAL_TWEAKER.get().getDefaultInstance(),
                    visibility);
            event.insertAfter(
                    WayfinderItems.SIGNAL_TWEAKER.get().getDefaultInstance(),
                    WayfinderItems.SIGNAL_BOOSTER.get().getDefaultInstance(),
                    visibility);
            event.insertAfter(
                    WayfinderItems.SIGNAL_BOOSTER.get().getDefaultInstance(),
                    WayfinderItems.SIGNAL_EXPANDER.get().getDefaultInstance(),
                    visibility);
            event.insertAfter(
                    WayfinderItems.SIGNAL_EXPANDER.get().getDefaultInstance(),
                    WayfinderItems.LENS.get().getDefaultInstance(),
                    visibility);
            event.insertAfter(
                    WayfinderItems.LENS.get().getDefaultInstance(),
                    WayfinderItems.PRIVACY_LENS.get().getDefaultInstance(),
                    visibility);
            event.insertAfter(
                    WayfinderItems.PRIVACY_LENS.get().getDefaultInstance(),
                    WayfinderItems.VANTAGE_LENS.get().getDefaultInstance(),
                    visibility);
        }

        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.insertAfter(
                    Items.CALIBRATED_SCULK_SENSOR.getDefaultInstance(),
                    WayfinderItems.SIGNAL_ARRAY.get().getDefaultInstance(),
                    visibility);
        }
    }
}
