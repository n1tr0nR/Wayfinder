package dev.nitron.wayfinder.blockentity;

import dev.nitron.wayfinder.registry.WayfinderBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public final class SignalArrayBlockEntity extends BlockEntity {
    private String signalName = "Signal";
    private Vec3i color = new Vec3i(76, 255, 135);
    private int type = 0;
    private int frequency = 3;
    private UUID owner;

    public SignalArrayBlockEntity(BlockPos pos, BlockState state) { super(WayfinderBlockEntities.SIGNAL_ARRAY.get(), pos, state); }
    public void setOwner(UUID owner) { this.owner = owner; sync(); }
    public void update(String name, Vec3i color, int type) { this.signalName = name; this.color = color; this.type = type; sync(); }
    public String signalName() { return signalName; }
    public Vec3i color() { return color; }
    public int type() { return type; }
    public int frequency() { return frequency; }
    public UUID owner() { return owner; }

    private void sync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("name", signalName);
        if (owner != null) tag.putString("owner_uuid", owner.toString());
        tag.putInt("red", color.getX()); tag.putInt("green", color.getY()); tag.putInt("blue", color.getZ());
        tag.putInt("type", type);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        signalName = tag.getString("name");
        try { owner = UUID.fromString(tag.getString("owner_uuid")); } catch (Exception ignored) { owner = null; }
        color = new Vec3i(tag.getInt("red"), tag.getInt("green"), tag.getInt("blue"));
        type = tag.getInt("type"); frequency = 3;
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
}
