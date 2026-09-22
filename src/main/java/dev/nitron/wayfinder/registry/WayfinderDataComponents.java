package dev.nitron.wayfinder.registry;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class WayfinderDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Wayfinder.MOD_ID);
    public static final Supplier<DataComponentType<SignalscopeComponent>> SIGNALSCOPE =
            DATA_COMPONENTS.registerComponentType("signalscope", builder -> builder.persistent(SignalscopeComponent.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SignalscopeComponent.CODEC)));
    public static final Supplier<DataComponentType<SignalTweakerComponent>> SIGNAL_TWEAKER =
            DATA_COMPONENTS.registerComponentType("signal_tweaker", builder -> builder.persistent(SignalTweakerComponent.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(SignalTweakerComponent.CODEC)));
    private WayfinderDataComponents() {}
}
