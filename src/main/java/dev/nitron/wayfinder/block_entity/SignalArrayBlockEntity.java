package dev.nitron.wayfinder.block_entity;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.registries.WayfinderBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

public class SignalArrayBlockEntity extends BlockEntity {
    public String name;
    public Vec3i color;
    public int type;
    public String owner_uuid;

    public SignalArrayBlockEntity(BlockPos pos, BlockState state) {
        super(WayfinderBlocks.SIGNAL_ARRAY_BE, pos, state);
        this.name = "Signal";
        this.color = new Vec3i(76, 255, 135);
        this.type = 0;
    }

    public void setUuid(String uuid){
        this.owner_uuid = uuid;
        if (this.world != null) {
            this.markDirty();
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public void update(String name, Vec3i color, int type){
        this.name = name;
        this.color = color;
        this.type = type;
        if (this.world != null) {
            this.markDirty();
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("name", this.name);
        nbt.putString("owner_uuid", this.owner_uuid);
        nbt.putInt("red", this.color.getX());
        nbt.putInt("green", this.color.getY());
        nbt.putInt("blue", this.color.getZ());
        nbt.putInt("type", this.type);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.name = nbt.getString("name");
        this.owner_uuid = nbt.getString("owner_uuid");
        this.color = new Vec3i(nbt.getInt("red"), nbt.getInt("green"), nbt.getInt("blue"));
        this.type = nbt.getInt("type");
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
