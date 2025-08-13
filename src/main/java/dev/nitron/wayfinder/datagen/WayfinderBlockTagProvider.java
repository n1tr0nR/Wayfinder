package dev.nitron.wayfinder.datagen;

import dev.nitron.wayfinder.registries.WayfinderBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class WayfinderBlockTagProvider extends FabricTagProvider<Block> {
    public WayfinderBlockTagProvider(FabricDataOutput output, RegistryKey<? extends Registry<Block>> registryKey, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registryKey, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(WayfinderBlocks.SIGNAL_ARRAY);
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL).add(WayfinderBlocks.SIGNAL_ARRAY);
    }
}
