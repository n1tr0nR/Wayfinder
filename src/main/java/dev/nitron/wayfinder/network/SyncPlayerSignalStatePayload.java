package dev.nitron.wayfinder.network;
import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.signal.PlayerSignalState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record SyncPlayerSignalStatePayload(PlayerSignalState state) implements CustomPacketPayload {
    public static final Type<SyncPlayerSignalStatePayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID,"sync_player_signal_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SyncPlayerSignalStatePayload> STREAM_CODEC=CustomPacketPayload.codec(SyncPlayerSignalStatePayload::write,SyncPlayerSignalStatePayload::new);
    private SyncPlayerSignalStatePayload(RegistryFriendlyByteBuf b){this(new PlayerSignalState(b.readFloat(),b.readFloat(),b.readFloat()));}
    private void write(RegistryFriendlyByteBuf b){b.writeFloat(state.factor0());b.writeFloat(state.factor1());b.writeFloat(state.factor2());}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
