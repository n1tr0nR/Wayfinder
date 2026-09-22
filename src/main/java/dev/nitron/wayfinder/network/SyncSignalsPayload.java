package dev.nitron.wayfinder.network;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.signal.SignalRecord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncSignalsPayload(List<SignalRecord> signals) implements CustomPacketPayload {
    public static final Type<SyncSignalsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID, "sync_signals"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSignalsPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncSignalsPayload::write, SyncSignalsPayload::new);

    public SyncSignalsPayload {
        signals = List.copyOf(signals);
    }

    private SyncSignalsPayload(RegistryFriendlyByteBuf buf) {
        this(readSignals(buf));
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(signals.size());
        for (SignalRecord signal : signals) signal.write(buf);
    }

    private static List<SignalRecord> readSignals(RegistryFriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 4096);
        List<SignalRecord> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) result.add(SignalRecord.read(buf));
        return result;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
