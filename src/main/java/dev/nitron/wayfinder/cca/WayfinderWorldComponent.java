package dev.nitron.wayfinder.cca;

import dev.nitron.wayfinder.registries.WayfinderComponents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.ArrayList;
import java.util.List;

public class WayfinderWorldComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final World world;

    public static class SignalData{
        public BlockPos pos;
        public String name;
        public Vec3i color;
        public int type;
        public String ownerUUID;

        public SignalData(BlockPos pos, String name, Vec3i color, int type, String ownerUUID){
            this.pos = pos;
            this.name = name;
            this.color = color;
            this.type = type;
            this.ownerUUID = ownerUUID;
        }
    }

    private final List<SignalData> signalPositions = new ArrayList<>();

    public WayfinderWorldComponent(World world) {
        this.world = world;
    }
    public void sync(){
        WayfinderComponents.WAYFINDER_W.sync(this.world);
    }

    public void addSignal(SignalData data) {
        signalPositions.add(data);
        sync();
    }

    public void removeSignal(BlockPos pos) {
        signalPositions.removeIf(signalData -> signalData.pos.equals(pos));
        sync();
    }

    public void updateSignal(BlockPos pos, String name, Vec3i color, int type, String ownerUuid) {
        for (SignalData s : signalPositions) {
            if (s.pos.equals(pos)) {
                s.name = name;
                s.color = color;
                s.type = type;
                s.ownerUUID = ownerUuid;
                sync();
                return;
            }
        }
        signalPositions.add(new SignalData(pos, name, color, type, ownerUuid));
        sync();
    }


    public List<SignalData> getSignalPositions() {
        return signalPositions;
    }

    @Override
    public void tick() {

    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        signalPositions.clear();
        NbtList list = nbt.getList("Signals", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : list){
            NbtCompound tag = (NbtCompound) element;
            BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            String name = tag.getString("name");
            Vec3i color = new Vec3i(tag.getInt("red"), tag.getInt("green"), tag.getInt("blue"));
            int type = tag.getInt("type");
            String ownerUUID = tag.getString("ownerUUID");
            signalPositions.add(new SignalData(pos, name, color, type, ownerUUID));
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        NbtList list = new NbtList();
        for (SignalData data : signalPositions) {
            NbtCompound tag = new NbtCompound();
            tag.putInt("x", data.pos.getX());
            tag.putInt("y", data.pos.getY());
            tag.putInt("z", data.pos.getZ());
            tag.putString("name", data.name);
            tag.putInt("red", data.color.getX());
            tag.putInt("green", data.color.getY());
            tag.putInt("blue", data.color.getZ());
            tag.putInt("type", data.type);
            tag.putString("ownerUUID", data.ownerUUID);
            list.add(tag);
        }
        nbt.put("Signals", list);
    }
}
