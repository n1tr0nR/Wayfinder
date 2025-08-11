package dev.nitron.wayfinder;

import com.nitron.nitrogen.Nitrogen;
import dev.nitron.wayfinder.networking.c2s.SignalArrayC2SPayload;
import dev.nitron.wayfinder.registries.WayfinderBlocks;
import dev.nitron.wayfinder.registries.WayfinderItems;
import dev.nitron.wayfinder.registries.WayfinderSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

		Nitrogen.registerNitronMod(MOD_ID);

		PayloadTypeRegistry.playC2S().register(SignalArrayC2SPayload.ID, SignalArrayC2SPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SignalArrayC2SPayload.ID, new SignalArrayC2SPayload.Receiver());
	}
}