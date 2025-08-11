package dev.nitron.wayfinder.block;

import com.mojang.serialization.MapCodec;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.cca.WayfinderWorldComponent;
import dev.nitron.wayfinder.client.screen.SignalArrayScreen;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
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

    @Environment(EnvType.CLIENT)
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient){
            if (player instanceof ClientPlayerEntity player1){
                MinecraftClient.getInstance().setScreen(new SignalArrayScreen((SignalArrayBlockEntity) world.getBlockEntity(pos)));
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignalArrayBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(0.125, 0, 0.125, 0.875, 0.5, 0.875);
    }
}
