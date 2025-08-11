package dev.nitron.wayfinder.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.util.ZoomHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final
    MinecraftClient client;

    @Shadow private float zoom;

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double modifyFov(double original){
        float zoom = ZoomHandler.getZoomProgress(client.getRenderTickCounter().getTickDelta(false));
        float zoomFactor = MathHelper.lerp(zoom, 1.0F, 0.1F);

        return original * zoomFactor;
    }
}