package dev.nitron.wayfinder;

import com.nitron.nitrogen.Nitrogen;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.item.tooltip.SignalscopeTooltipComponent;
import dev.nitron.wayfinder.item.tooltip.SignalscopeTooltipData;
import dev.nitron.wayfinder.networking.c2s.SignalArrayC2SPayload;
import dev.nitron.wayfinder.registries.WayfinderBlocks;
import dev.nitron.wayfinder.registries.WayfinderDataComponents;
import dev.nitron.wayfinder.registries.WayfinderItems;
import dev.nitron.wayfinder.registries.WayfinderSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wayfinder implements ModInitializer {
	public static final String MOD_ID = "wayfinder";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WayfinderItems.init();
		WayfinderBlocks.init();
		WayfinderSounds.init();
		WayfinderDataComponents.init();

		Nitrogen.registerNitronMod(MOD_ID);

		PayloadTypeRegistry.playC2S().register(SignalArrayC2SPayload.ID, SignalArrayC2SPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SignalArrayC2SPayload.ID, new SignalArrayC2SPayload.Receiver());

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.addBefore(Items.MAP, WayfinderItems.PRIVACY_LENS);
			entries.addBefore(WayfinderItems.PRIVACY_LENS, WayfinderItems.CONCAVE_LENS);
			entries.addBefore(WayfinderItems.CONCAVE_LENS, WayfinderItems.SIGNALSCOPE);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
			entries.addBefore(Items.BELL, WayfinderBlocks.SIGNAL_ARRAY);
		});

		TooltipComponentCallback.EVENT.register(tooltipData -> {
			if (tooltipData instanceof SignalscopeTooltipData(SignalscopeComponent contents)){
				return new SignalscopeTooltipComponent(contents);
			}
			return null;
		});
	}

	public static void grantAdvancement(PlayerEntity player, Identifier identifier, String criterion){
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
		MinecraftServer server = serverPlayer.getServer();
		if (server == null) return;
		AdvancementEntry advancement = server.getAdvancementLoader().get(identifier);
		serverPlayer.getAdvancementTracker().grantCriterion(advancement, criterion);
	}
}