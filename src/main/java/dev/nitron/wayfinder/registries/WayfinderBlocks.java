package dev.nitron.wayfinder.registries;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block.SignalArrayBlock;
import dev.nitron.wayfinder.block.SignalBeaconBlock;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class WayfinderBlocks{
    public static BlockEntityType<SignalArrayBlockEntity> SIGNAL_ARRAY_BE;

    public static void init(){
        SIGNAL_ARRAY_BE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(Wayfinder.MOD_ID, "signal_array"),
                BlockEntityType.Builder.create(SignalArrayBlockEntity::new, SIGNAL_ARRAY).build());
    }

    public static final Block SIGNAL_ARRAY = registerBlock("signal_array", new SignalArrayBlock(AbstractBlock.Settings.copy(Blocks.CRAFTER).sounds(BlockSoundGroup.VAULT).nonOpaque().pistonBehavior(PistonBehavior.IGNORE)));
    public static final Block SIGNAL_BEACON = registerBlock("signal_beacon", new SignalBeaconBlock(AbstractBlock.Settings.copy(Blocks.CRAFTER).sounds(BlockSoundGroup.VAULT).nonOpaque().pistonBehavior(PistonBehavior.IGNORE)));

    public static Block registerBlock(String name, Block block){
        Registry.register(Registries.ITEM, Identifier.of(Wayfinder.MOD_ID, name), new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, Identifier.of(Wayfinder.MOD_ID, name), block);
    }
}
