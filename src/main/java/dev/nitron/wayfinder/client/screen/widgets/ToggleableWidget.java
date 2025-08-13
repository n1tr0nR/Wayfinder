package dev.nitron.wayfinder.client.screen.widgets;

import net.minecraft.client.gui.widget.ToggleButtonWidget;

public class ToggleableWidget extends ToggleButtonWidget {
    public ToggleableWidget(int x, int y, int width, int height, boolean toggled) {
        super(x, y, width, height, toggled);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        setToggled(!isToggled());
    }
}
