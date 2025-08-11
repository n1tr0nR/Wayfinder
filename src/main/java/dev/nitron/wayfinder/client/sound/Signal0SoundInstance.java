package dev.nitron.wayfinder.client.sound;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.registries.WayfinderSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class Signal0SoundInstance extends MovingSoundInstance {
    private final ClientPlayerEntity player;

    public Signal0SoundInstance(ClientPlayerEntity player) {
        super(WayfinderSounds.DEFAULT_SIGNAL, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.01F;
        this.relative = true;
    }

    public static boolean shouldPlay(ClientPlayerEntity player){
        return player.getWorld() != null && WayfinderComponents.WAYFINDER.get(player).getSIgnalscopeVol(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)) > 0;
    }

    @Override
    public void tick() {
        if (shouldPlay(this.player)){
            float raw = WayfinderComponents.WAYFINDER.get(player).getFactor0();

            if (raw < 0.9F){
                this.volume = 0;
            } else {
                this.volume = MathHelper.lerp((raw - 0.9F) / 0.1F, 0F, 1F) * WayfinderComponents.WAYFINDER.get(this.player).getSIgnalscopeVol(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
            }
        } else {
            this.setDone();
        }
    }
}
