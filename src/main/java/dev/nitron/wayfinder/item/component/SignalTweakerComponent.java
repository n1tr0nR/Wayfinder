package dev.nitron.wayfinder.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;

public record SignalTweakerComponent(Vec3i color, String name, boolean privateNetwork, int freq, int type, int selected) {
    public static final Codec<SignalTweakerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3i.CODEC.fieldOf("color").forGetter(SignalTweakerComponent::color),
            Codec.STRING.fieldOf("name").forGetter(SignalTweakerComponent::name),
            Codec.BOOL.fieldOf("privateNetwork").forGetter(SignalTweakerComponent::privateNetwork),
            Codec.INT.fieldOf("freq").forGetter(SignalTweakerComponent::freq),
            Codec.INT.fieldOf("type").forGetter(SignalTweakerComponent::type),
            Codec.INT.fieldOf("selected").forGetter(SignalTweakerComponent::selected)
    ).apply(instance, SignalTweakerComponent::new));
}
