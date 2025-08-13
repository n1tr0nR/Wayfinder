package dev.nitron.wayfinder.mixin;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.SignalscopeItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Shadow @Final private ItemModels models;
    @Unique
    private static final List<ModelTransformationMode> MODES = List.of(
            ModelTransformationMode.GROUND,
            ModelTransformationMode.GUI
    );

    @ModifyVariable(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), argsOnly = true)
    private BakedModel renderItem(BakedModel model, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel modelAgain) {
        if (stack.getItem() instanceof SignalscopeItem){
            Identifier modelId = Identifier.of(Wayfinder.MOD_ID, "item/signalscope");
            Identifier guiModelId = Identifier.of(Wayfinder.MOD_ID, "item/signalscope_gui");

            return models.getModelManager().getModel(MODES.contains(renderMode) ? guiModelId : modelId);
        }
        return model;
    }
}
