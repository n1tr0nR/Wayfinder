package dev.nitron.wayfinder;

import dev.nitron.wayfinder.client.render.hud.SignalscopeHud;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.item.tooltip.SignalscopeTooltipComponent;
import dev.nitron.wayfinder.item.tooltip.SignalscopeTooltipData;
import dev.nitron.wayfinder.registries.WayfinderBlocks;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.registries.WayfinderModelProvider;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import dev.nitron.wayfinder.util.ZoomHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WayfinderClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(WayfinderBlocks.SIGNAL_ARRAY, RenderLayer.getCutout());

        WayfinderModelProvider.loadModels();
        ModelLoadingPlugin.register(context -> context.addModels(WayfinderModelProvider.MODELS));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {

                boolean isScoping = client.player.isUsingItem()
                        && client.player.getActiveItem().getItem() instanceof SignalscopeItem
                        && client.options.getPerspective().isFirstPerson();

                ZoomHandler.shouldZoom = isScoping;
                ZoomHandler.tick();
            }
        });

        HudRenderCallback.EVENT.register(new SignalscopeHud());

        TooltipComponentCallback.EVENT.register(tooltipData -> {
            if (tooltipData instanceof SignalscopeTooltipData(SignalscopeComponent contents)){
                return new SignalscopeTooltipComponent(contents);
            }
            return null;
        });
    }
}
