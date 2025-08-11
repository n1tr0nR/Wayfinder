package dev.nitron.wayfinder.client.screen;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.client.screen.widgets.TransparentButtonWidget;
import dev.nitron.wayfinder.networking.c2s.SignalArrayC2SPayload;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class SignalArrayScreen extends Screen {
    private final SignalArrayBlockEntity blockEntity;

    private TextFieldWidget nameWidget;
    private TextFieldWidget redWidget;
    private TextFieldWidget greenWidget;
    private TextFieldWidget blueWidget;
    private TransparentButtonWidget buttonWidget;
    private TextWidget typeField;

    public SignalArrayScreen(SignalArrayBlockEntity entity) {
        this(Text.literal("Signal Array"), entity);
    }

    public SignalArrayScreen(Text title, SignalArrayBlockEntity entity){
        super(title);
        this.blockEntity = entity;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawText(
                client.textRenderer,
                this.title,
                context.getScaledWindowWidth() / 2 - (client.textRenderer.getWidth(this.title) / 2),
                context.getScaledWindowHeight() / 2 - 50,
                0xFFFFFFFF,
                true
        );
        context.drawTexture(
                Identifier.of(Wayfinder.MOD_ID, "textures/gui/signal_array.png"),
                context.getScaledWindowWidth() / 2 - (176 / 2),
                context.getScaledWindowHeight() / 2 - (65 / 2),
                0,
                0,
                176,
                65,
                256,
                256
        );
    }

    @Override
    protected void init() {
        super.init();
        int x = this.client.getWindow().getScaledWidth() / 2;
        int y = this.client.getWindow().getScaledHeight() / 2;

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.finishEditing()).dimensions(x - 50, y + 40, 100, 20).build());

        this.buttonWidget = (TransparentButtonWidget) TransparentButtonWidget.transparentBuilder(ScreenTexts.SPACE, (button) -> this.cycleType()).dimensions(x - 77, y - 13, 43, 34).build();
        this.addDrawableChild(buttonWidget);

        typeField = new TextWidget(x - 65, y - 5, 20, 20, Text.literal(String.valueOf(blockEntity.type)), client.textRenderer);
        this.addDrawableChild(typeField);

        x += 2;
        y += 5;

        this.nameWidget = new TextFieldWidget(client.textRenderer, x - 23, y - 14, 100, 17, Text.literal("Signal"));
        this.nameWidget.setDrawsBackground(false);
        this.nameWidget.setMaxLength(15);
        this.nameWidget.setText(this.blockEntity.name);
        this.addDrawableChild(this.nameWidget);

        this.redWidget = new TextFieldWidget(client.textRenderer, x - 23, y + 4, 26, 17, Text.literal("Signal"));
        this.redWidget.setDrawsBackground(false);
        this.redWidget.setMaxLength(3);
        this.redWidget.setText(String.valueOf(this.blockEntity.color.getX()));

        this.addDrawableChild(this.redWidget);

        this.greenWidget = new TextFieldWidget(client.textRenderer, x - 23 + 37, y + 4, 26, 17, Text.literal("Signal"));
        this.greenWidget.setDrawsBackground(false);
        this.greenWidget.setMaxLength(3);
        this.greenWidget.setText(String.valueOf(this.blockEntity.color.getY()));
        this.addDrawableChild(this.greenWidget);

        this.blueWidget = new TextFieldWidget(client.textRenderer, x - 23 + 37 + 37, y + 4, 26, 17, Text.literal("Signal"));
        this.blueWidget.setDrawsBackground(false);
        this.blueWidget.setMaxLength(3);
        this.blueWidget.setText(String.valueOf(this.blockEntity.color.getZ()));
        this.addDrawableChild(this.blueWidget);

    }

    private void cycleType() {
        int value = Integer.parseInt(this.typeField.getMessage().getString());
        value++;
        if (value > 3){
            value = 0;
        }
        this.typeField.setMessage(Text.literal(String.valueOf(value)));
    }

    @Override
    public void removed() {
        int value = Integer.parseInt(this.typeField.getMessage().getString());
        super.removed();
        Vec3i intFromValues = new Vec3i(
                parseIntSafe(this.redWidget.getText(), 255),
                parseIntSafe(this.greenWidget.getText(), 255),
                parseIntSafe(this.blueWidget.getText(), 255)
        );
        SignalArrayC2SPayload.send(this.nameWidget.getText(), intFromValues, this.blockEntity.getPos(), value);
    }

    private void finishEditing() {
        int value = Integer.parseInt(this.typeField.getMessage().getString());
        Vec3i intFromValues = new Vec3i(
                parseIntSafe(this.redWidget.getText(), 255),
                parseIntSafe(this.greenWidget.getText(), 255),
                parseIntSafe(this.blueWidget.getText(), 255)
        );
        SignalArrayC2SPayload.send(this.nameWidget.getText(), intFromValues, this.blockEntity.getPos(), value);

        this.client.setScreen(null);
    }

    private int parseIntSafe(String text, int fallback){
        try {
            return Math.clamp(Integer.parseInt(text), 0, 255);
        } catch (NumberFormatException e){
            return fallback;
        }
    }
}
