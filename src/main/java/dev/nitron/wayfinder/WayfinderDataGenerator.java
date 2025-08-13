package dev.nitron.wayfinder;

import dev.nitron.wayfinder.datagen.WayfinderBlockTagProvider;
import dev.nitron.wayfinder.datagen.WayfinderLootTableProvider;
import dev.nitron.wayfinder.datagen.WayfinderModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;

public class WayfinderDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(WayfinderModelProvider::new);
		pack.addProvider(WayfinderLootTableProvider::new);
		pack.addProvider(((fabricDataOutput, completableFuture) ->
				new WayfinderBlockTagProvider(fabricDataOutput, RegistryKeys.BLOCK, completableFuture)));
	}
}
