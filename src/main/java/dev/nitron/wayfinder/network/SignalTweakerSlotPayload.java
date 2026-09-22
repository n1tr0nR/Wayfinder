package dev.nitron.wayfinder.network;
import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record SignalTweakerSlotPayload(int slot,int direction,boolean alt,boolean control) implements CustomPacketPayload {
    public static final Type<SignalTweakerSlotPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID,"signal_tweaker_slot_payload"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SignalTweakerSlotPayload> STREAM_CODEC=CustomPacketPayload.codec(SignalTweakerSlotPayload::write,SignalTweakerSlotPayload::new);
    private SignalTweakerSlotPayload(RegistryFriendlyByteBuf b){this(b.readInt(),b.readInt(),b.readBoolean(),b.readBoolean());}
    private void write(RegistryFriendlyByteBuf b){b.writeInt(slot);b.writeInt(direction);b.writeBoolean(alt);b.writeBoolean(control);}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
