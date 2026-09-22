package dev.nitron.wayfinder.block;
import org.joml.Vector3f;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.Vec3i;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.item.SignalTweakerItem;
import dev.nitron.wayfinder.Wayfinder;

import dev.nitron.wayfinder.blockentity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.network.WayfinderNetwork;
import dev.nitron.wayfinder.signal.SignalRecord;
import dev.nitron.wayfinder.signal.SignalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public final class SignalArrayBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Shapes.box(0.125, 0.0, 0.125, 0.875, 0.5, 0.875);

    public SignalArrayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SignalArrayBlockEntity(pos, state); }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server) || !(server.getBlockEntity(pos) instanceof SignalArrayBlockEntity array)) return;
        if (placer instanceof Player player) array.setOwner(player.getUUID());
        UUID owner = array.owner() == null ? new UUID(0L, 0L) : array.owner();
        SignalSavedData.get(server).put(new SignalRecord(pos, array.signalName(), array.color().getX(), array.color().getY(),
                array.color().getZ(), array.frequency(), owner, level.hasNeighborSignal(pos), array.frequency()));
        WayfinderNetwork.syncLevel(server);
    }

    @SuppressWarnings("deprecation")
    @Override public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean piston) {
        if (level instanceof ServerLevel server) {
            SignalRecord s = SignalSavedData.get(server).get(pos);
            if (s != null) {
                SignalSavedData.get(server).put(new SignalRecord(s.pos(), s.name(), s.red(), s.green(), s.blue(),
                        s.frequency(), s.owner(), level.hasNeighborSignal(pos), s.type()));
                WayfinderNetwork.syncLevel(server);
            }
        }
        super.neighborChanged(state, level, pos, block, fromPos, piston);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean piston) {
        if (state.getBlock() != next.getBlock() && level instanceof ServerLevel server) {
            SignalSavedData.get(server).remove(pos); WayfinderNetwork.syncLevel(server);
        }
        super.onRemove(state, level, pos, next, piston);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        if (level instanceof ServerLevel server) {
            SignalSavedData.get(server).remove(pos);
            WayfinderNetwork.syncLevel(server);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult) {
        ItemStack mainHand = player.getMainHandItem();

        if (player.isShiftKeyDown()) {
            if (level instanceof ServerLevel server) {
                SignalRecord signal = SignalSavedData.get(server).get(pos);

                if (mainHand.getItem() instanceof SignalTweakerItem
                        && signal != null) {
                    mainHand.set(
                            WayfinderDataComponents.SIGNAL_TWEAKER.get(),
                            new SignalTweakerComponent(
                                    new Vec3i(
                                            signal.red(),
                                            signal.green(),
                                            signal.blue()),
                                    signal.name(),
                                    signal.powered(),
                                    signal.frequency(),
                                    signal.type(),
                                    0));
                }

                server.playSound(
                        null,
                        pos,
                        SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F);
            }

            return InteractionResult.SUCCESS;
        }

        if (mainHand.getItem() instanceof SignalTweakerItem
                && level.getBlockEntity(pos)
                instanceof SignalArrayBlockEntity array) {
            SignalTweakerComponent component =
                    mainHand.get(WayfinderDataComponents.SIGNAL_TWEAKER.get());

            if (component != null) {
                array.update(
                        component.name(),
                        component.color(),
                        component.type());

                if (component.privateNetwork()) {
                    Wayfinder.grantAdvancement(
                            player,
                            ResourceLocation.fromNamespaceAndPath(
                                    Wayfinder.MOD_ID,
                                    "personal_vpn"),
                            "incode");
                }

                if (level instanceof ServerLevel server) {
                    SignalSavedData data = SignalSavedData.get(server);
                    SignalRecord old = data.get(pos);

                    if (old != null) {
                        data.put(
                                new SignalRecord(
                                        pos,
                                        component.name(),
                                        component.color().getX(),
                                        component.color().getY(),
                                        component.color().getZ(),
                                        component.freq(),
                                        old.owner(),
                                        component.privateNetwork(),
                                        component.type()));
                    }

                    Vector3f particleColor =
                            new Vector3f(
                                    component.color().getX() / 255.0F,
                                    component.color().getY() / 255.0F,
                                    component.color().getZ() / 255.0F);

                    server.sendParticles(
                            new DustParticleOptions(particleColor, 2.0F),
                            pos.getCenter().x,
                            pos.getY(),
                            pos.getCenter().z,
                            10,
                            0.10000000149011612D,
                            0.25D,
                            0.10000000149011612D,
                            1.0D);

                    server.playSound(
                            null,
                            pos,
                            SoundEvents.VAULT_OPEN_SHUTTER,
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F);

                    WayfinderNetwork.syncLevel(server);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(POWERED); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
