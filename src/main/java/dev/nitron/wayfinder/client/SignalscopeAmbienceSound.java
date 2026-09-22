package dev.nitron.wayfinder.client;

import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SignalscopeAmbienceSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private float targetVolume = 0.0F;
    private float targetPitch = 1.0F;

    public SignalscopeAmbienceSound(LocalPlayer player) {
        super(WayfinderSounds.SIGNALSCOPE_AMBIENT.get(), SoundSource.AMBIENT, RandomSource.create());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (player.isRemoved() || !player.isAlive()) {
            stop();
            return;
        }

        var stack = player.getMainHandItem();
        boolean holding = stack.getItem() instanceof SignalscopeItem;

        // Exact 3.5 JAR quirk: targetVolume is zero in both branches.
        targetVolume = 0.0F;

        if (holding) {
            SignalscopeComponent component = stack.get(WayfinderDataComponents.SIGNALSCOPE.get());
            if (component != null) {
                int band = component.frequency() / 3;
                targetPitch = 1.0F + (band - 1) / 3.0F;
            }
        } else {
            targetPitch = 1.0F;
        }

        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        volume = Mth.lerp(partial, volume, targetVolume);
        pitch = Mth.lerp(partial, pitch, targetPitch);

        if (volume < 0.01F && !holding) volume = 0.0F;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void stopNow() {
        Minecraft.getInstance().getSoundManager().stop(this);
        stop();
    }
}
