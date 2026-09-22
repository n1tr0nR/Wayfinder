package dev.nitron.wayfinder.registry;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.blockentity.SignalArrayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WayfinderBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Wayfinder.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SignalArrayBlockEntity>> SIGNAL_ARRAY =
            BLOCK_ENTITIES.register("signal_array", () ->
                    BlockEntityType.Builder.of(SignalArrayBlockEntity::new, WayfinderBlocks.SIGNAL_ARRAY.get()).build(null));

    private WayfinderBlockEntities() {}
}
