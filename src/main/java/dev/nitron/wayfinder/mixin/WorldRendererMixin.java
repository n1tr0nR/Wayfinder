package dev.nitron.wayfinder.mixin;


import dev.nitron.wayfinder.client.sound.SignalSoundInstance;
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

    @Inject(method = "tick", at = @At("HEAD"))
    public void solitude$playSoundAmbiances(CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player != null && SignalSoundInstance.shouldPlay(player) && (this.default_signal == null || !this.client.getSoundManager().isPlaying(this.default_signal))) {
            this.default_signal = new SignalSoundInstance(player);
            this.client.getSoundManager().play(this.default_signal);
        }
    }
}
