package dev.nitron.wayfinder.item.tooltip;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SignalscopeTooltipComponent implements TooltipComponent {
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int SLOT_PADDING = 1;
    private final SignalscopeComponent component;

    public SignalscopeTooltipComponent(SignalscopeComponent component) {
        this.component = component;
    }

    @Override
    public int getHeight() {
        return SLOT_HEIGHT + 2 + SLOT_PADDING;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return SLOT_WIDTH + 2 + SLOT_PADDING;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        ItemStack stack = component.item();

        drawSlot(context, x + 3, y, stack, textRenderer, -1);
    }

    private void drawSlot(DrawContext context, int x, int y, ItemStack stack, TextRenderer textRenderer, int index) {
        Identifier BACKGROUND_TEXTURE = Identifier.of(Wayfinder.MOD_ID, "signalscope_slot");
        context.drawGuiTexture(BACKGROUND_TEXTURE, x - 1, y - 1, 0, 18, 20);
        if (stack != null && !stack.isEmpty()){
            context.drawItem(stack, x, y, index);
            context.drawItemInSlot(textRenderer, stack, x, y);
        }
    }
}
