package dev.nitron.wayfinder.networking.c2s;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

public record SignalArrayC2SPayload(String name, Vector3f color, int type, BlockPos pos) implements CustomPayload {
    public static final Identifier SIGNAL_ARRAY_PAYLOAD = Identifier.of(Wayfinder.MOD_ID, "signal_array_payload");
    public static final Id<SignalArrayC2SPayload> ID = new Id<>(SIGNAL_ARRAY_PAYLOAD);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final PacketCodec<PacketByteBuf, SignalArrayC2SPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, SignalArrayC2SPayload::name,
                    PacketCodecs.VECTOR3F, SignalArrayC2SPayload::color,
                    PacketCodecs.INTEGER, SignalArrayC2SPayload::type,
                    BlockPos.PACKET_CODEC, SignalArrayC2SPayload::pos, SignalArrayC2SPayload::new);

    public static void send(String name, Vec3i var, BlockPos pos, int type){
        Vector3f converted = new Vector3f(var.getX(), var.getY(), var.getZ());
        ClientPlayNetworking.send(new SignalArrayC2SPayload(name, converted, type, pos));
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<SignalArrayC2SPayload> {
        @Override
        public void receive(SignalArrayC2SPayload signalArrayC2SPayload, ServerPlayNetworking.Context context) {
            BlockEntity blockEntity = context.player().getWorld().getBlockEntity(signalArrayC2SPayload.pos);
            if (blockEntity instanceof SignalArrayBlockEntity signalArrayBlockEntity){
                signalArrayBlockEntity.update(signalArrayC2SPayload.name, new Vec3i((int) signalArrayC2SPayload.color.x, (int) signalArrayC2SPayload.color.y, (int) signalArrayC2SPayload.color.z), signalArrayC2SPayload.type);
            }
        }
    }
}
