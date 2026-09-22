package dev.nitron.wayfinder.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderItems;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow @Final private ItemModelShaper itemModelShaper;
    @ModifyVariable(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private BakedModel wayfinder$selectModel(BakedModel original,ItemStack stack,ItemDisplayContext context,boolean leftHanded,
                                             PoseStack poseStack,MultiBufferSource buffer,int light,int overlay){
        if(!(stack.getItem() instanceof SignalscopeItem))return original;
        SignalscopeComponent c=stack.get(WayfinderDataComponents.SIGNALSCOPE.get());if(c==null)return original;
        String lens=c.lensItemstack().isEmpty()?"_empty":c.lensItemstack().is(WayfinderItems.PRIVACY_LENS.get())?"_privacy":c.lensItemstack().is(WayfinderItems.VANTAGE_LENS.get())?"_vantage":"";
        String upgrade=c.upgradeItemstack().is(WayfinderItems.SIGNAL_BOOSTER.get())?"_b":c.upgradeItemstack().is(WayfinderItems.SIGNAL_EXPANDER.get())?"_e":"";
        boolean gui=context==ItemDisplayContext.GUI||context==ItemDisplayContext.GROUND||context==ItemDisplayContext.FIXED;
        String path=gui?"item/signalscope/gui/gui_signalscope"+lens+upgrade:"item/signalscope/hand/signalscope"+lens+upgrade;
        return itemModelShaper.getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID,path)));
    }
}
