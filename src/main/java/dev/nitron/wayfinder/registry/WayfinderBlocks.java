package dev.nitron.wayfinder.registry;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block.SignalArrayBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WayfinderBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Wayfinder.MOD_ID);
    public static final DeferredBlock<SignalArrayBlock> SIGNAL_ARRAY = BLOCKS.registerBlock(
            "signal_array", SignalArrayBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTER).sound(SoundType.VAULT).noOcclusion().pushReaction(PushReaction.IGNORE).forceSolidOn());
    private WayfinderBlocks() {}
}
