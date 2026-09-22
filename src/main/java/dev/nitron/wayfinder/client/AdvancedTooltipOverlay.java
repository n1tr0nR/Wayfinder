package dev.nitron.wayfinder.client;

import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

/** Rich rows rendered by the same tooltip pass as vanilla text. */
public final class AdvancedTooltipOverlay implements ClientTooltipComponent {
    private static final int TEXT = 0xFFF7DB70;
    private static final int LABEL = 0xFF555555;
    private static final ResourceLocation MODULES = id("textures/gui/sprites/hud/signalscope_tooltip.png");
    private static final ResourceLocation TWEAKER = id("textures/gui/sprites/hud/signal_tweaker_tooltip.png");
    private static final ResourceLocation BARS = id("hud/bars");
    private static final ResourceLocation SELECTED = id("hud/bars_selected");
    private final WayfinderTooltip row;

    public AdvancedTooltipOverlay(WayfinderTooltip row) {
        this.row = row;
    }

    @Override
    public int getHeight() {
        return row instanceof WayfinderTooltip.Modules ? 30
                : row instanceof WayfinderTooltip.Data && wrapData(Minecraft.getInstance().font) ? 36 : 18;
    }

    @Override
    public int getWidth(Font font) {
        return switch (row) {
            case WayfinderTooltip.Modules ignored -> 70;
            case WayfinderTooltip.Colors ignored -> 3 * (font.width("000") + 10) + 22;
            case WayfinderTooltip.Network network -> 12 + font.width("Network: ")
                    + font.width(network.value().privateNetwork() ? "Private" : "Public") + 16;
            case WayfinderTooltip.Data data -> wrapData(font)
                    ? Math.max(frequencyWidth(font, data), typeWidth(font, data))
                    : frequencyWidth(font, data) + typeWidth(font, data) + 8;
        };
    }

    private int frequencyWidth(Font font, WayfinderTooltip.Data data) {
        return 12 + font.width("- Frequency: ") + font.width(Integer.toString(data.value().freq()));
    }

    private int typeWidth(Font font, WayfinderTooltip.Data data) {
        return 12 + font.width("- Type: ") + font.width(typeName(data.value().type()));
    }

    private boolean wrapData(Font font) {
        if (!(row instanceof WayfinderTooltip.Data data)) return false;
        return frequencyWidth(font, data) + typeWidth(font, data) + 8
                > Minecraft.getInstance().getWindow().getGuiScaledWidth() - 24;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        // Flush preceding text before drawing sprites/items with their own render states.
        graphics.flush();
        switch (row) {
            case WayfinderTooltip.Modules modules -> {
                var c = modules.value();
                graphics.blit(MODULES, x + 3, y + 1, 0, 0, 64, 26, 64, 64);
                if (!c.lensItemstack().isEmpty()) graphics.renderItem(c.lensItemstack(), x + 8, y + 6);
                if (!c.upgradeItemstack().isEmpty()) graphics.renderItem(c.upgradeItemstack(), x + 40, y + 6);
                if (c.selected() == 1 || c.selected() == 2) {
                    graphics.blit(MODULES, x + 6 + (c.selected() - 1) * 32, y + 4,
                            0, 26, 20, 20, 64, 64);
                }
            }
            case WayfinderTooltip.Colors colors -> {
                var c = colors.value();
                int[] values = {c.color().getX(), c.color().getY(), c.color().getZ()};
                int cursor = x + 6;
                for (int i = 0; i < values.length; i++) {
                    String value = String.format(java.util.Locale.ROOT, "%03d", values[i]);
                    field(graphics, font, value, cursor, y + 4, c.selected() == i);
                    cursor += font.width("000") + 10;
                }
                var box = TooltipLayout.background(cursor, y + 2, 14, 14);
                graphics.blitSprite(BARS, box.x(), box.y(), box.width(), box.height());
                graphics.setColor(values[0] / 255.0F, values[1] / 255.0F, values[2] / 255.0F, 1.0F);
                graphics.blit(TWEAKER, cursor, y + 2, 16, 15, 14, 14, 128, 128);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            case WayfinderTooltip.Network network -> {
                var c = network.value();
                graphics.drawString(font, "Network: ", x + 6, y + 4, LABEL, true);
                int valueX = x + 6 + font.width("Network: ");
                String value = c.privateNetwork() ? "Private" : "Public";
                field(graphics, font, value, valueX, y + 4, c.selected() == 3);
                graphics.blit(TWEAKER, valueX + font.width(value) + 4, y + 3,
                        c.privateNetwork() ? 40 : 31, 0, 9, 11, 128, 128);
            }
            case WayfinderTooltip.Data data -> {
                var c = data.value();
                int textY = y + 4;
                graphics.drawString(font, "- Frequency: ", x + 6, textY, LABEL, true);
                field(graphics, font, Integer.toString(c.freq()),
                        x + 6 + font.width("- Frequency: "), textY, c.selected() == 4);
                int typeX = wrapData(font) ? x + 6 : x + frequencyWidth(font, data) + 8;
                int typeY = wrapData(font) ? textY + 18 : textY;
                graphics.drawString(font, "- Type: ", typeX, typeY, LABEL, true);
                field(graphics, font, typeName(c.type()), typeX + font.width("- Type: "), typeY,
                        c.selected() == 5);
            }
        }
    }

    private static void field(GuiGraphics graphics, Font font, String text, int x, int y, boolean selected) {
        var box = TooltipLayout.background(x, y, font.width(text), font.lineHeight);
        graphics.blitSprite(selected ? SELECTED : BARS, box.x(), box.y(), box.width(), box.height());
        graphics.drawString(font, text, x, y, TEXT, true);
    }

    private static String typeName(int type) {
        return type == 1 ? "Alternate" : type == 2 ? "Final" : "Default";
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID, path);
    }
}
