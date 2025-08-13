package dev.nitron.wayfinder.datagen;

import dev.nitron.wayfinder.registries.WayfinderBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class WayfinderLootTableProvider extends FabricBlockLootTableProvider {

    public WayfinderLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(WayfinderBlocks.SIGNAL_ARRAY);
    }
}
