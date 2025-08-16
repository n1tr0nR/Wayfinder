package dev.nitron.wayfinder.block;

import com.mojang.serialization.MapCodec;
import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.cca.WayfinderWorldComponent;
import dev.nitron.wayfinder.client.screen.SignalArrayScreen;
import dev.nitron.wayfinder.registries.WayfinderBlocks;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SignalBeaconBlock extends Block {
    public SignalBeaconBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);

            comp.addSignal(new WayfinderWorldComponent.SignalData(
                    pos,
                    "???",
                    new Vec3i(170, 91, 249),
                    4,
                    "beacon"
            ));
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);

        WayfinderWorldComponent.SignalData signal = comp.getSignalPositions().stream()
                .filter(signalData -> signalData.pos.equals(pos))
                .findFirst()
                .orElse(null);

        if (signal == null){
            comp.addSignal(new WayfinderWorldComponent.SignalData(
                    pos,
                    "???",
                    new Vec3i(170, 91, 249),
                    4,
                    "beacon"
            ));
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape((double)6.0F, (double)0.0F, (double)6.0F, (double)10.0F, (double)16.0F, (double)10.0F);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);

        WayfinderWorldComponent.SignalData signal = comp.getSignalPositions().stream()
                .filter(signalData -> signalData.pos.equals(pos))
                .findFirst()
                .orElse(null);

        if (signal == null){
            comp.addSignal(new WayfinderWorldComponent.SignalData(
                    pos,
                    "???",
                    new Vec3i(170, 91, 249),
                    4,
                    "beacon"
            ));
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
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        super.onDestroyedByExplosion(world, pos, explosion);
        if (!world.isClient) {
            WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);
            comp.removeSignal(pos);
        }
    }
}
