package dev.nitron.wayfinder.client.sound;

import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.registries.WayfinderSounds;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class SignalSoundInstance extends MovingSoundInstance {
    private final ClientPlayerEntity player;

    public SignalSoundInstance(ClientPlayerEntity player) {
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
            float raw = WayfinderComponents.WAYFINDER.get(player).getLookFactor();

            if (raw < 0.25F){
                this.volume = 0;
            } else {
                this.volume = MathHelper.lerp((raw - 0.25F) / 0.75F, 0F, 1F) * WayfinderComponents.WAYFINDER.get(this.player).getSIgnalscopeVol(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
            }
        } else {
            this.setDone();
        }
    }
}
