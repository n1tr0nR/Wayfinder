package dev.nitron.wayfinder.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.network.ClientSignalStore;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderItems;
import dev.nitron.wayfinder.signal.SignalRecord;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SignalscopeHud {
    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(
                    Wayfinder.MOD_ID,
                    "textures/gui/sprites/hud/signalscope.png");

    private static final ResourceLocation AUDIO_WAVES =
            ResourceLocation.fromNamespaceAndPath(
                    Wayfinder.MOD_ID,
                    "textures/gui/sprites/hud/audio_waves.png");

    public static void render(
            GuiGraphics graphics,
            float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        ItemStack scope = mc.player.getMainHandItem();

        if (!(scope.getItem() instanceof SignalscopeItem)) {
            return;
        }

        SignalscopeComponent component =
                scope.get(WayfinderDataComponents.SIGNALSCOPE.get());

        if (component == null) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int centerX = width / 2;

        int hotbarOffset = mc.player.isCreative() ? 0 : 20;

        graphics.blit(
                PANEL,
                centerX - 52,
                height - 47 - hotbarOffset,
                0,
                0,
                104,
                26,
                128,
                64);

        graphics.drawString(
                mc.font,
                component.frequency() + "hz",
                centerX - 45,
                height - 34 - hotbarOffset,
                0xFFF4E063,
                true);

        graphics.drawString(
                mc.font,
                component.gain() + "mm",
                centerX + 5,
                height - 34 - hotbarOffset,
                0xFFF4E063,
                true);

        renderAudioWaves(
                graphics,
                component,
                partialTick,
                centerX,
                height);

        if (component.lensItemstack().isEmpty()) {
            return;
        }

        int gainIndex =
                ((int) ((component.gain() - 30.0D) * 2.0D)) / 2;

        ResourceLocation gainTexture =
                ResourceLocation.fromNamespaceAndPath(
                        Wayfinder.MOD_ID,
                        "textures/gui/sprites/hud/gain/gain"
                                + (gainIndex + 1)
                                + ".png");

        blitTinted(
                graphics,
                gainTexture,
                centerX - 40,
                height / 2 - 40,
                0,
                0,
                80,
                80,
                80,
                80,
                0x50FAECA4);

        double range = SignalscopeHelper.maxDistance(component);

        String rangeText = "Range: " + (int) range + "m";

        graphics.drawString(
                mc.font,
                rangeText,
                centerX - mc.font.width(rangeText) / 2,
                height / 2 + 50,
                0xFFFAECA4,
                true);

        List<SignalRecord> signals =
                ClientSignalStore.signals();

        BlockPos lookedAt =
                SignalscopeHelper.lookedAtSignal(
                        mc.player,
                        signals,
                        SignalscopeHelper.IDENTIFY_ANGLE,
                        range,
                        component);

        int randomBound =
                (int) ((component.gain() - 30.0D) / 2.0D);

        BlockPos lookedAtFinal = lookedAt;

        SignalRecord lookedSignal =
                signals.stream()
                        .filter(signal ->
                                signal.pos().equals(lookedAtFinal))
                        .findFirst()
                        .orElse(null);

        for (SignalRecord signal : SignalIndicator.targetLast(signals, s -> s.pos().equals(lookedAtFinal))) {
            BlockPos displayPos = signal.pos();

            if (randomBound >= 1) {
                RandomSource stableRandom =
                        RandomSource.create(
                                signal.pos().asLong()
                                        ^ Double.doubleToLongBits(component.gain()));
                int span = randomBound * 2 + 1;

                displayPos =
                        displayPos.offset(
                                stableRandom.nextInt(span) - randomBound,
                                stableRandom.nextInt(span) - randomBound,
                                stableRandom.nextInt(span) - randomBound);
            }

            if (!SignalscopeHelper.accepts(
                    mc.player,
                    signal,
                    scope)) {
                continue;
            }

            float inverseLook =
                    1.0F
                            - SignalscopeHelper.lookFactor(
                                    mc.player,
                                    displayPos,
                                    SignalscopeHelper.TRACKING_ANGLE,
                                    partialTick);

            int signalRgb = (signal.red() << 16) | (signal.green() << 8) | signal.blue();
            SignalIndicator indicator = SignalIndicator.resolve(
                    signal.pos().equals(lookedAt), 1.0F - inverseLook, signalRgb);
            int displacement = indicator.displacement();

            double distance =
                    mc.player.position()
                            .distanceTo(
                                    net.minecraft.world.phys.Vec3
                                            .atLowerCornerOf(signal.pos()));

            float distanceAlpha =
                    distanceAlpha(
                            distance,
                            range);

            float alpha = indicator.lookFactor() * distanceAlpha;
            int argb = argb(alpha, (indicator.rgb() >>> 16) & 255,
                    (indicator.rgb() >>> 8) & 255, indicator.rgb() & 255);

            blitTinted(
                    graphics,
                    gainTexture,
                    centerX - 40 - displacement,
                    height / 2 - 40,
                    0,
                    0,
                    40,
                    80,
                    80,
                    80,
                    argb);

            blitTinted(
                    graphics,
                    gainTexture,
                    centerX + displacement,
                    height / 2 - 40,
                    40,
                    0,
                    40,
                    80,
                    80,
                    80,
                    argb);
        }

        if (lookedSignal == null) {
            return;
        }

        double lookedDistance =
                SignalscopeHelper.distance(
                        mc.player,
                        lookedSignal);

        if (lookedDistance >= range) {
            return;
        }

        float labelAlpha =
                distanceAlpha(
                        lookedDistance,
                        range);

        int labelColor =
                argb(
                        labelAlpha,
                        lookedSignal.red(),
                        lookedSignal.green(),
                        lookedSignal.blue());

        String label =
                lookedSignal.name()
                        + ": "
                        + String.format("%.0f", lookedDistance)
                        + "m";

        int labelY =
                (int) (
                        height / 2.0D
                                - 50.0D
                                + component.gain() * 2.0D
                                - 60.0D);

        graphics.drawString(
                mc.font,
                Component.literal(label),
                centerX - mc.font.width(label) / 2,
                labelY,
                labelColor,
                true);
    }

    private static void renderAudioWaves(
            GuiGraphics graphics,
            SignalscopeComponent component,
            float partialTick,
            int centerX,
            int height) {
        int band =
                component.frequency() / 3 - 1;

        int waveWidth = 64;
        int waveHeight = 32;

        float speed =
                band == 0 ? 0.15F : 0.1F;

        float time =
                (Minecraft.getInstance()
                        .level
                        .getDayTime()
                        % 24000L)
                        + partialTick;

        float first =
                (time * speed * waveWidth)
                        % waveWidth;

        float second =
                (time * speed * 0.5F * waveWidth)
                        % waveWidth;

        int y = height - 100;
        int v = band * 48;

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX,
                y,
                (int) first,
                v,
                waveWidth,
                waveHeight,
                256,
                256,
                0x90FFFFFF);

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX - waveWidth,
                y,
                (int) first,
                v,
                waveWidth,
                waveHeight,
                256,
                256,
                0x90FFFFFF);

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX,
                y,
                (int) second,
                v,
                waveWidth,
                waveHeight,
                256,
                256,
                0x50FFFFFF);

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX - waveWidth,
                y,
                (int) second,
                v,
                waveWidth,
                waveHeight,
                256,
                256,
                0x50FFFFFF);

        int reverse =
                -(int) second + waveWidth;

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX,
                y,
                reverse,
                v,
                waveWidth,
                waveHeight,
                256,
                256,
                0x30FFFFFF);

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX - waveWidth,
                y,
                reverse,
                v,
                waveWidth,
                waveHeight,
                256,
                256,
                0x30FFFFFF);

        blitTinted(
                graphics,
                AUDIO_WAVES,
                centerX - waveWidth - 2,
                height - 103,
                0,
                192,
                132,
                48,
                256,
                256,
                0xFFFFFFFF);
    }

    private static float distanceAlpha(
            double distance,
            double range) {
        float alpha = 1.0F;

        float fadeStart =
                (float) (range - range / 10.0D);

        float fadeEnd =
                (float) range;

        if (distance > fadeStart) {
            if (distance >= fadeEnd) {
                alpha = 0.0F;
            } else {
                alpha =
                        1.0F
                                - (float) (
                                (distance - fadeStart)
                                        / (fadeEnd - fadeStart));
            }
        }

        return alpha;
    }

    private static int argb(
            float alpha,
            int red,
            int green,
            int blue) {
        int a =
                Math.max(
                        0,
                        Math.min(
                                255,
                                (int) (255.0F * alpha)));

        return (a << 24)
                | ((red & 255) << 16)
                | ((green & 255) << 8)
                | (blue & 255);
    }

    private static void blitTinted(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int textureWidth,
            int textureHeight,
            int argb) {
        float a =
                ((argb >>> 24) & 255) / 255.0F;

        float r =
                ((argb >>> 16) & 255) / 255.0F;

        float g =
                ((argb >>> 8) & 255) / 255.0F;

        float b =
                (argb & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, a);

        graphics.blit(
                texture,
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight);

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F);
    }

    private SignalscopeHud() {}
}
