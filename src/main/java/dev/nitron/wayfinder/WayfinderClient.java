package dev.nitron.wayfinder;

import dev.nitron.wayfinder.client.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Wayfinder.MOD_ID, dist = Dist.CLIENT)
public final class WayfinderClient {
    public WayfinderClient(IEventBus modBus) {
        modBus.addListener(ClientEvents::onClientSetup);
        modBus.addListener(ClientEvents::onRegisterAdditionalModels);
        modBus.addListener(ClientEvents::onRegisterGuiLayers);
        modBus.addListener(ClientEvents::onRegisterTooltipFactories);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onScreenMouseScroll);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onGatherTooltip);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onTooltipColor);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onLogout);
    }
}
