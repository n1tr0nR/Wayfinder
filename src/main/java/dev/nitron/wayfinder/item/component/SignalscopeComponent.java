package dev.nitron.wayfinder.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public record SignalscopeComponent(ItemStack item) {
    public static final Codec<SignalscopeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.xmap(
                    stack -> stack.getItem() == Items.BARRIER ? ItemStack.EMPTY : stack,
                    stack -> stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack
            ).fieldOf("item").forGetter(SignalscopeComponent::item)
    ).apply(instance, SignalscopeComponent::new));
}