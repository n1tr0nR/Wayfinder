package dev.nitron.wayfinder.test.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nitron.wayfinder.test.ClientValidation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public class TestKeyboardMixin {
    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    private static void key(long window, int code, CallbackInfoReturnable<Boolean> result) {
        if (ClientValidation.simulateKeys) result.setReturnValue(ClientValidation.pressedKeys.contains(code));
    }
}
