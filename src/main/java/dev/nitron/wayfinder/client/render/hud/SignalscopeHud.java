package dev.nitron.wayfinder.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.cca.WayfinderWorldComponent;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import dev.nitron.wayfinder.util.ZoomHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SignalscopeHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        RenderSystem.enableBlend();
        float alpha = ZoomHandler.getZoomProgress(renderTickCounter.getTickDelta(false)) * 2;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        drawContext.drawTexture(Identifier.of(Wayfinder.MOD_ID, "textures/gui/vignette.png"), 0, 0, 0, 0, drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight(), drawContext.getScaledWindowWidth(), drawContext.getScaledWindowHeight());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();


        if (client.player == null || client.world == null) return;

        if (client.options.hudHidden || !(client.player.getActiveItem().getItem() instanceof SignalscopeItem)) return;

        int x = drawContext.getScaledWindowWidth() / 2;
        int y = drawContext.getScaledWindowHeight() / 2;

        Identifier leftTexture1 = Identifier.of(Wayfinder.MOD_ID, "textures/gui/half_circle_left1.png");
        Identifier leftTexture2 = Identifier.of(Wayfinder.MOD_ID, "textures/gui/half_circle_left2.png");
        Identifier leftTexture3 = Identifier.of(Wayfinder.MOD_ID, "textures/gui/half_circle_left3.png");
        Identifier rightTexture1 = Identifier.of(Wayfinder.MOD_ID, "textures/gui/half_circle_right1.png");
        Identifier rightTexture2 = Identifier.of(Wayfinder.MOD_ID, "textures/gui/half_circle_right2.png");
        Identifier rightTexture3 = Identifier.of(Wayfinder.MOD_ID, "textures/gui/half_circle_right3.png");

        RenderSystem.enableBlend();

        WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(client.world);
        BlockPos lookedAtSignal = SignalscopeHelper.getLookedAtSignal(client.player, comp.getSignalPositions(), 15.0F, 1000F);
        for (BlockPos signalPos : comp.getSignalPositions()) {
            float factor = (float) (1.0 - SignalscopeHelper.getLookFactor(
                    client.player,
                    signalPos,
                    75F,
                    renderTickCounter.getTickDelta(false)
            ));

            int maxDistance = (int) (130 * factor);

            Vec3d targetColor = new Vec3d(1.0F, 1.0F, 1.0F);
            if (factor == 0) {
                targetColor = new Vec3d(0.3F, 1.0F, 0.6F);
                if (lookedAtSignal != null){
                    BlockEntity blockEntity = client.world.getBlockEntity(lookedAtSignal);

                    if (blockEntity instanceof SignalArrayBlockEntity signalArrayBlockEntity){
                        targetColor = new Vec3d((double) signalArrayBlockEntity.color.getX() / 255, (double) signalArrayBlockEntity.color.getY() / 255, (double) signalArrayBlockEntity.color.getZ() / 255);
                    }
                }
            }
            if (factor > 0.66) {
                targetColor = new Vec3d(1.0F, 0.0F, 0.25F);
            }

            RenderSystem.setShaderColor((float) targetColor.x, (float) targetColor.y, (float) targetColor.z, 1.0F - factor);

            Identifier targetLeftTexture;
            if (factor > 0.66) {
                targetLeftTexture = leftTexture3;
            } else if (factor > 0.33 && factor < 0.66) {
                targetLeftTexture = leftTexture2;
            } else {
                targetLeftTexture = leftTexture1;
            }

            Identifier targetRightTexture;
            if (factor > 0.66) {
                targetRightTexture = rightTexture3;
            } else if (factor > 0.33 && factor < 0.66) {
                targetRightTexture = rightTexture2;
            } else {
                targetRightTexture = rightTexture1;
            }

            drawContext.drawTexture(
                    targetLeftTexture,
                    x - 29 - maxDistance, y - 29,
                    0, 0,
                    29, 58,
                    29, 58
            );

            drawContext.drawTexture(
                    targetRightTexture,
                    x + maxDistance, y - 29,
                    0, 0,
                    29, 58,
                    29, 58
            );
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        //text



        if (lookedAtSignal != null){
            BlockEntity blockEntity = client.world.getBlockEntity(lookedAtSignal);
            double distance = client.player.getPos().distanceTo(Vec3d.ofCenter(lookedAtSignal));
            Text text = Text.literal("Signal: " + String.format("%.0f", distance) + "m").formatted(Formatting.BOLD);
            int color = 0xFF4cff99;

            if (blockEntity instanceof SignalArrayBlockEntity signalArrayBlockEntity){
                text = Text.literal(signalArrayBlockEntity.name + ": " + String.format("%.0f", distance) + "m").formatted(Formatting.BOLD);
                Vec3i vec3i = signalArrayBlockEntity.color;
                int r  = vec3i.getX();
                int g  = vec3i.getY();
                int b  = vec3i.getZ();
                int a = 255;

                color = (a << 24) | (r << 16) | (g << 8) | b;
            }

            drawContext.drawText(
                    client.textRenderer,
                    text,
                    x - (client.textRenderer.getWidth(text) / 2), y + 60, color, true
            );
        }
    }
}