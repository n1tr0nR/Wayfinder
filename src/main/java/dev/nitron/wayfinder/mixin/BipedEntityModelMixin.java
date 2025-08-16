package dev.nitron.wayfinder.mixin;

import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {
    @Shadow public BipedEntityModel.ArmPose rightArmPose;

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart head;

    @Shadow public BipedEntityModel.ArmPose leftArmPose;

    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "positionRightArm", at = @At("TAIL"))
    private void wawa(T entity, CallbackInfo ci){
        if (this.rightArmPose.ordinal() == 7){
            SignalscopeHelper.positionRightArm(this.head, entity, this.rightArm);
        }
    }

    @Inject(method = "positionLeftArm", at = @At("TAIL"))
    private void wawa2(T entity, CallbackInfo ci){
        if (this.leftArmPose.ordinal() == 7){
            SignalscopeHelper.positionRightArm(this.head, entity, this.leftArm);
        }
    }
}
