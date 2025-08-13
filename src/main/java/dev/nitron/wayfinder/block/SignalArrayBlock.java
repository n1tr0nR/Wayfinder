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
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SignalArrayBlock extends BlockWithEntity {
    public static final BooleanProperty POWERED = Properties.POWERED;

    public SignalArrayBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(SignalArrayBlock::new);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof SignalArrayBlockEntity signalArrayBlockEntity){
            if (placer instanceof PlayerEntity player){
                signalArrayBlockEntity.setUuid(player.getUuidAsString());
            }
        }
        if (!world.isClient) {
            WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);

            if (world.getBlockEntity(pos) instanceof SignalArrayBlockEntity be) {
                comp.addSignal(new WayfinderWorldComponent.SignalData(
                        pos,
                        be.name,
                        be.color,
                        be.type,
                        be.owner_uuid
                ));
            }
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
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isReceivingRedstonePower(pos)){
            WayfinderWorldComponent component = WayfinderComponents.WAYFINDER_W.get(world);
            WayfinderWorldComponent.SignalData signal = component.getSignalPositions().stream()
                    .filter(signalData -> signalData.pos.equals(pos))
                    .findFirst()
                    .orElse(null);
            PlayerEntity player = world.getPlayerByUuid(UUID.fromString(signal.ownerUUID));
            if (player != null){
                Wayfinder.grantAdvancement(player, Identifier.of(Wayfinder.MOD_ID, "personal_vpn"), "incode");
            }
        }
        world.setBlockState(pos, state.with(POWERED, world.isReceivingRedstonePower(pos)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient){
            if (player instanceof ClientPlayerEntity player1){
                if (state.get(POWERED) && !player.getUuidAsString().equals(world.getBlockEntity(pos, WayfinderBlocks.SIGNAL_ARRAY_BE).get().owner_uuid)){
                    player.sendMessage(Text.literal("You cannot edit a private network!").formatted(Formatting.RED));
                    return ActionResult.FAIL;
                }
                MinecraftClient.getInstance().setScreen(new SignalArrayScreen((SignalArrayBlockEntity) world.getBlockEntity(pos)));
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
        super.onDestroyedByExplosion(world, pos, explosion);
        if (!world.isClient) {
            WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(world);
            comp.removeSignal(pos);
        }
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
