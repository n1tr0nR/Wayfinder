package dev.nitron.wayfinder.util;

import dev.nitron.wayfinder.cca.WayfinderWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SignalscopeHelper {
    public static float getLookFactor(PlayerEntity player, BlockPos target, float maxAngle, float tickDelta){
        Vec3d eyePosition = player.getEyePos();
        Vec3d lookDirection = player.getRotationVec(tickDelta).normalize();
        Vec3d targetDir = Vec3d.ofCenter(target).subtract(eyePosition).normalize();
        double dot = lookDirection.dotProduct(targetDir);
        if (dot <= 0){
            return 0.0F; //return because its behind the player.
        }

        double maxAngleRad = Math.toRadians(maxAngle);
        double minDot = Math.cos(maxAngleRad);

        double factor = (dot - minDot) / (1.0F - minDot);
        float clamped = (float) MathHelper.clamp(factor, 0.0, 1.0);
        if (clamped > 0.95){
            return 1.0F;
        }
        return clamped;
    }

    @Nullable
    public static BlockPos getLookedAtSignal(PlayerEntity player, List<WayfinderWorldComponent.SignalData> signals, float maxAngle, double maxDistance, boolean privacy) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F).normalize();

        BlockPos closestSignal = null;
        double closestAngle = Double.MAX_VALUE;

        for (WayfinderWorldComponent.SignalData signalData : signals) {
            BlockPos signalPos = signalData.pos;

            Vec3d targetVec = Vec3d.ofCenter(signalPos).subtract(eyePos);
            double distance = targetVec.length();

            if (distance > maxDistance) continue;
            if (privacy && !player.getUuidAsString().equals(signalData.ownerUUID)) continue;

            targetVec = targetVec.normalize();

            double dot = lookVec.dotProduct(targetVec);
            dot = MathHelper.clamp(dot, -1.0F, 1.0F);
            double angle = Math.acos(dot) * (180.0 / Math.PI);

            if (angle <= maxAngle && angle < closestAngle) {
                closestAngle = angle;
                closestSignal = signalPos;
            }
        }

        return closestSignal;
    }
}
