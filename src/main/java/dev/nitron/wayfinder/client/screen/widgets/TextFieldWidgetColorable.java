package dev.nitron.wayfinder.client.screen.widgets;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TextFieldWidgetColorable extends TextFieldWidget {
    private final int hexCode;

    public TextFieldWidgetColorable(TextRenderer textRenderer, int width, int height, Text text, int hexCode) {
        this(textRenderer, 0, 0, width, height, text, hexCode);
    }

    public TextFieldWidgetColorable(TextRenderer textRenderer, int x, int y, int width, int height, Text text, int hexCode) {
        super(textRenderer, x, y, width, height, text);
        this.hexCode = hexCode;
    }

    @Override
    public Text getMessage() {
        return Text.literal(super.getMessage().getString()).withColor(hexCode);
    }
}
