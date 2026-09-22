package dev.nitron.wayfinder.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record SignalscopeComponent(int frequency, double gain, ItemStack lensItemstack, ItemStack upgradeItemstack, int selected) {
    private static final Codec<ItemStack> STACK_CODEC = ItemStack.CODEC.xmap(
            stack -> stack.is(Items.BARRIER) ? ItemStack.EMPTY : stack,
            stack -> stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack
    );
    public static final Codec<SignalscopeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("frequency").forGetter(SignalscopeComponent::frequency),
            Codec.DOUBLE.fieldOf("gain").forGetter(SignalscopeComponent::gain),
            STACK_CODEC.fieldOf("lensItemstack").forGetter(SignalscopeComponent::lensItemstack),
            STACK_CODEC.fieldOf("upgradeItemstack").forGetter(SignalscopeComponent::upgradeItemstack),
            Codec.INT.fieldOf("selected").forGetter(SignalscopeComponent::selected)
    ).apply(instance, SignalscopeComponent::new));
}
