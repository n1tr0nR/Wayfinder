package dev.nitron.wayfinder.network;
import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record SignalscopeChangePayload(int oldFrequency,double oldGain,int newFrequency,double newGain) implements CustomPacketPayload {
    public static final Type<SignalscopeChangePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID,"signalscope_change_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SignalscopeChangePayload> STREAM_CODEC=CustomPacketPayload.codec(SignalscopeChangePayload::write,SignalscopeChangePayload::new);
    private SignalscopeChangePayload(RegistryFriendlyByteBuf b){this(b.readInt(),b.readDouble(),b.readInt(),b.readDouble());}
    private void write(RegistryFriendlyByteBuf b){b.writeInt(oldFrequency);b.writeDouble(oldGain);b.writeInt(newFrequency);b.writeDouble(newGain);}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
