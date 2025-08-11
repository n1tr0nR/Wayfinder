package dev.nitron.wayfinder.util;

import dev.nitron.wayfinder.item.SignalscopeItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.math.MathHelper;

public class ZoomHandler {
    public static float currentZoomProgress = 0.0f;
    public static float previousZoomProgress = 0.0f;
    public static boolean shouldZoom = false;

    public static void tick() {
        previousZoomProgress = currentZoomProgress;

        float speed = 0.5f;
        float target = shouldZoom ? 0.5f : 0.0f;

        currentZoomProgress += (target - currentZoomProgress) * speed;

        if (Math.abs(currentZoomProgress - target) < 0.001f) {
            currentZoomProgress = target;
        }

        updateSensitivity();
    }

    public static float getZoomProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, previousZoomProgress, currentZoomProgress);
    }

    private static final double NORMAL_SENSITIVITY = MinecraftClient.getInstance().options.getMouseSensitivity().getValue();

    public static void updateSensitivity() {
        var options = MinecraftClient.getInstance().options;
        if (MinecraftClient.getInstance().player.isUsingItem() && MinecraftClient.getInstance().player.getStackInHand(MinecraftClient.getInstance().player.getActiveHand()).getItem() instanceof SignalscopeItem
        && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            options.getMouseSensitivity().setValue((double) (NORMAL_SENSITIVITY * 0.4));
        } else {
            options.getMouseSensitivity().setValue((double) NORMAL_SENSITIVITY);
        }
    }
}

