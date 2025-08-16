package dev.nitron.wayfinder.mixin;


import dev.nitron.wayfinder.client.sound.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.SoundInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;
    @Unique
    SoundInstance default_signal;

    @Unique
    SoundInstance alt_1;

    @Unique
    SoundInstance alt_2;

    @Unique
    SoundInstance alt_3;

    @Unique
    SoundInstance alt_4;


    @Inject(method = "tick", at = @At("HEAD"))
    public void solitude$playSoundAmbiances(CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player != null && Signal0SoundInstance.shouldPlay(player) && (this.default_signal == null || !this.client.getSoundManager().isPlaying(this.default_signal))) {
            this.default_signal = new Signal0SoundInstance(player);
            this.client.getSoundManager().play(this.default_signal);
        }

        if (player != null && Signal1SoundInstance.shouldPlay(player) && (this.alt_1 == null || !this.client.getSoundManager().isPlaying(this.alt_1))) {
            this.alt_1 = new Signal1SoundInstance(player);
            this.client.getSoundManager().play(this.alt_1);
        }

        if (player != null && Signal2SoundInstance.shouldPlay(player) && (this.alt_2 == null || !this.client.getSoundManager().isPlaying(this.alt_2))) {
            this.alt_2 = new Signal2SoundInstance(player);
            this.client.getSoundManager().play(this.alt_2);
        }

        if (player != null && Signal3SoundInstance.shouldPlay(player) && (this.alt_3 == null || !this.client.getSoundManager().isPlaying(this.alt_3))) {
            this.alt_3 = new Signal3SoundInstance(player);
            this.client.getSoundManager().play(this.alt_3);
        }

        if (player != null && Signal4SoundInstance.shouldPlay(player) && (this.alt_4 == null || !this.client.getSoundManager().isPlaying(this.alt_4))) {
            this.alt_4 = new Signal4SoundInstance(player);
            this.client.getSoundManager().play(this.alt_4);
        }
    }
}
