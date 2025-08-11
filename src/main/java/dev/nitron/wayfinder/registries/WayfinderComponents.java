package dev.nitron.wayfinder.registries;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.cca.WayfinderEntityComponent;
import dev.nitron.wayfinder.cca.WayfinderWorldComponent;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class WayfinderComponents implements EntityComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<WayfinderEntityComponent> WAYFINDER = org.ladysnake.cca.api.v3.component.ComponentRegistry.getOrCreate(Identifier.of(Wayfinder.MOD_ID, "wayfinder"),
            WayfinderEntityComponent.class);

    public static final ComponentKey<WayfinderWorldComponent> WAYFINDER_W = org.ladysnake.cca.api.v3.component.ComponentRegistry.getOrCreate(Identifier.of(Wayfinder.MOD_ID, "wayfinder_w"),
            WayfinderWorldComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry entityComponentFactoryRegistry) {
        entityComponentFactoryRegistry.registerForPlayers(WAYFINDER, WayfinderEntityComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry worldComponentFactoryRegistry) {
        worldComponentFactoryRegistry.register(WAYFINDER_W, WayfinderWorldComponent::new);
    }
}
