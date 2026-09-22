package dev.nitron.wayfinder.signal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import java.util.UUID;
public record SignalRecord(BlockPos pos,String name,int red,int green,int blue,int frequency,UUID owner,boolean powered,int type){
    public void write(RegistryFriendlyByteBuf b){b.writeBlockPos(pos);b.writeUtf(name);b.writeInt(red);b.writeInt(green);b.writeInt(blue);b.writeInt(frequency);b.writeUUID(owner);b.writeBoolean(powered);b.writeInt(type);}
    public static SignalRecord read(RegistryFriendlyByteBuf b){return new SignalRecord(b.readBlockPos(),b.readUtf(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readUUID(),b.readBoolean(),b.readInt());}
}
