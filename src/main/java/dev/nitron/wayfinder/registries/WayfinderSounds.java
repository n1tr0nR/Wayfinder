package dev.nitron.wayfinder.registries;

import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class WayfinderSounds {
    public static void init(){}

    public static final SoundEvent DEFAULT_SIGNAL = registerSound("default_signal");
    public static final SoundEvent SIGNALSCOPE = registerSound("signalscope");

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(Wayfinder.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
}
