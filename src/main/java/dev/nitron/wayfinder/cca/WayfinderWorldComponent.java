package dev.nitron.wayfinder.cca;

import dev.nitron.wayfinder.registries.WayfinderComponents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.ArrayList;
import java.util.List;

public class WayfinderWorldComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final World world;

    private final List<BlockPos> signalPositions = new ArrayList<>();

    public WayfinderWorldComponent(World world) {
        this.world = world;
    }
    public void sync(){
        WayfinderComponents.WAYFINDER_W.sync(this.world);
    }

    public void addSignal(BlockPos pos) {
        signalPositions.add(pos);
        sync();
    }

    public void removeSignal(BlockPos pos) {
        signalPositions.remove(pos);
        sync();
    }

    public List<BlockPos> getSignalPositions() {
        return signalPositions;
    }

    @Override
    public void tick() {

    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        signalPositions.clear();
        NbtList list = nbt.getList("Signals", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : list) {
            NbtCompound posTag = (NbtCompound) element;
            BlockPos pos = new BlockPos(
                    posTag.getInt("x"),
                    posTag.getInt("y"),
                    posTag.getInt("z")
            );
            signalPositions.add(pos);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        NbtList list = new NbtList();
        for (BlockPos pos : signalPositions) {
            NbtCompound posTag = new NbtCompound();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            list.add(posTag);
        }
        nbt.put("Signals", list);
    }
}
