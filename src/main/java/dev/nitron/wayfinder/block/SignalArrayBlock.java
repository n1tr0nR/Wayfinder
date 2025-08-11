package dev.nitron.wayfinder.block;

import com.mojang.serialization.MapCodec;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.cca.WayfinderWorldComponent;
import dev.nitron.wayfinder.client.screen.SignalArrayScreen;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SignalArrayBlock extends BlockWithEntity {
    public SignalArrayBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(SignalArrayBlock::new);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);
            comp.addSignal(pos);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient) {
            WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);
            comp.removeSignal(pos);
        }
        return state;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient){
            if (player instanceof ClientPlayerEntity player1){
                MinecraftClient.getInstance().setScreen(new SignalArrayScreen((SignalArrayBlockEntity) world.getBlockEntity(pos)));
            }
        }
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalArrayBlockEntity(pos, state);
    }
}
