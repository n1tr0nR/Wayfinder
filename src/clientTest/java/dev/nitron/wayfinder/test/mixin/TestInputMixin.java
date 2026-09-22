package dev.nitron.wayfinder.test.mixin;

import dev.nitron.wayfinder.test.ClientValidation;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces only the keyboard boundary for reproducible modifier-key scenarios. */
@Mixin(Screen.class)
public class TestInputMixin {
    @Inject(method = "hasShiftDown", at = @At("HEAD"), cancellable = true)
    private static void shift(CallbackInfoReturnable<Boolean> result) {
        if (!ClientValidation.simulateKeys) result.setReturnValue(ClientValidation.shift);
    }
    @Inject(method = "hasControlDown", at = @At("HEAD"), cancellable = true)
    private static void control(CallbackInfoReturnable<Boolean> result) {
        if (!ClientValidation.simulateKeys) result.setReturnValue(ClientValidation.control);
    }
}
