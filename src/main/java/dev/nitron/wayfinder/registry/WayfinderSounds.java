package dev.nitron.wayfinder.registry;

import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WayfinderSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,Wayfinder.MOD_ID);
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNALSCOPE_INCREASE=sound("signalscope_increase");
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNALSCOPE_DECREASE=sound("signalscope_decrease");
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNALSCOPE_AMBIENT=sound("signalscope_ambient");
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNALSCOPE=sound("signalscope");
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNAL_1=sound("signal_1");
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNAL_2=sound("signal_2");
    public static final DeferredHolder<SoundEvent,SoundEvent> SIGNAL_3=sound("signal_3");
    public static final DeferredHolder<SoundEvent,SoundEvent> PLACE_LENS=sound("place_lens");
    public static final DeferredHolder<SoundEvent,SoundEvent> TAKE_LENS=sound("take_lens");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID,id)));}
    private WayfinderSounds(){}
}
