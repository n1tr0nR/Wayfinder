package dev.nitron.wayfinder.client.screen.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class TransparentButtonWidget extends ButtonWidget {
    protected TransparentButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message, onPress, narrationSupplier);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Environment(EnvType.CLIENT)
    public static class TransparentBuilder {
        private final Text message;
        private final PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private NarrationSupplier narrationSupplier;

        public TransparentBuilder(Text message, PressAction onPress) {
            this.narrationSupplier = TransparentButtonWidget.DEFAULT_NARRATION_SUPPLIER;
            this.message = message;
            this.onPress = onPress;
        }

        public TransparentBuilder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public TransparentBuilder width(int width) {
            this.width = width;
            return this;
        }

        public TransparentBuilder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public TransparentBuilder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public TransparentBuilder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public TransparentBuilder narrationSupplier(NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public TransparentButtonWidget build() {
            TransparentButtonWidget buttonWidget = new TransparentButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }

    public static TransparentBuilder transparentBuilder(Text message, PressAction onPress) {
        return new TransparentBuilder(message, onPress);
    }
}
