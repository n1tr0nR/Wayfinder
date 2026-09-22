package dev.nitron.wayfinder.network;
import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record SignalscopeSlotPayload(int slot,int selected) implements CustomPacketPayload {
    public static final Type<SignalscopeSlotPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID,"signalscope_slot_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SignalscopeSlotPayload> STREAM_CODEC=CustomPacketPayload.codec(SignalscopeSlotPayload::write,SignalscopeSlotPayload::new);
    private SignalscopeSlotPayload(RegistryFriendlyByteBuf b){this(b.readInt(),b.readInt());}
    private void write(RegistryFriendlyByteBuf b){b.writeInt(slot);b.writeInt(selected);}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
