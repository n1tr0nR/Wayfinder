package dev.nitron.wayfinder.client;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.SignalTweakerItem;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.network.ClientPlayerSignalStore;
import dev.nitron.wayfinder.network.ClientSignalStore;
import dev.nitron.wayfinder.network.SignalTweakerSlotPayload;
import dev.nitron.wayfinder.network.SignalscopeChangePayload;
import dev.nitron.wayfinder.network.SignalscopeSlotPayload;
import dev.nitron.wayfinder.registry.WayfinderBlocks;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderItems;
import dev.nitron.wayfinder.registry.WayfinderSounds;
import com.mojang.datafixers.util.Either;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientEvents {
    private static final ResourceLocation TOOLTIP_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(
                    Wayfinder.MOD_ID,
                    "tooltip/signal_background");
    private static final ResourceLocation TOOLTIP_FRAME =
            ResourceLocation.fromNamespaceAndPath(
                    Wayfinder.MOD_ID,
                    "tooltip/signal_frame");

    private static final SignalLoopSound[] SIGNALS = new SignalLoopSound[3];
    private static SignalscopeAmbienceSound ambience;

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(
                        WayfinderBlocks.SIGNAL_ARRAY.get(),
                        RenderType.cutout()));
    }

    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        String[] lens = {"", "_empty", "_privacy", "_vantage"};
        String[] upgrade = {"", "_b", "_e"};

        for (String l : lens) {
            for (String u : upgrade) {
                event.register(ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                Wayfinder.MOD_ID,
                                "item/signalscope/gui/gui_signalscope" + l + u)));

                event.register(ModelResourceLocation.standalone(
                        ResourceLocation.fromNamespaceAndPath(
                                Wayfinder.MOD_ID,
                                "item/signalscope/hand/signalscope" + l + u)));
            }
        }
    }

    public static void onScreenMouseScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        double delta = event.getScrollDeltaY();
        if (delta == 0.0D) return;

        var slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        boolean alt = Screen.hasAltDown();
        // Upstream uses physical Ctrl; Screen.hasControlDown maps to Command on macOS.
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean control = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);

        if (stack.getItem() instanceof SignalscopeItem) {
            SignalscopeComponent component =
                    stack.get(WayfinderDataComponents.SIGNALSCOPE.get());
            if (component == null) return;

            int selected = component.selected();

            if (delta < 0.0D) {
                selected++;
                if (selected > 2) selected = 1;
            } else {
                selected--;
                if (selected < 1) selected = 2;
            }

            PacketDistributor.sendToServer(
                    new SignalscopeSlotPayload(slot.index, selected));

            event.setCanceled(true);
            return;
        }

        if (stack.getItem() instanceof SignalTweakerItem) {
            int direction = delta < 0.0D ? -1 : 1;

            PacketDistributor.sendToServer(
                    new SignalTweakerSlotPayload(
                            slot.index,
                            direction,
                            alt,
                            control));

            event.setCanceled(true);
        }
    }

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        double delta = event.getScrollDeltaY();
        boolean alt = Screen.hasAltDown();

        if (mc.player == null || !alt) return;

        ItemStack scope = mc.player.getMainHandItem();
        if (!(scope.getItem() instanceof SignalscopeItem)) return;

        event.setCanceled(true);

        SignalscopeComponent component =
                scope.get(WayfinderDataComponents.SIGNALSCOPE.get());

        if (component == null) return;

        int oldFrequency = component.frequency();
        double oldGain = component.gain();

        int frequency = oldFrequency;
        double gain = oldGain;

        if (!mc.player.isCrouching()) {
            if (delta > 0.0D) frequency += 3;
            else if (delta < 0.0D) frequency -= 3;

            frequency = Math.max(3, Math.min(12, frequency));
        } else {
            if (delta > 0.0D) gain += 1.0D;
            else if (delta < 0.0D) gain -= 1.0D;

            gain = Math.max(30.0D, Math.min(40.0D, gain));
        }

        PacketDistributor.sendToServer(
                new SignalscopeChangePayload(
                        oldFrequency,
                        oldGain,
                        frequency,
                        gain));
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ambience == null || !mc.getSoundManager().isActive(ambience)) {
            ambience = new SignalscopeAmbienceSound(mc.player);
            mc.getSoundManager().play(ambience);
        }

        for (int i = 0; i < SIGNALS.length; i++) {
            if (SIGNALS[i] == null
                    || !mc.getSoundManager().isActive(SIGNALS[i])) {
                var sound = switch (i) {
                    case 1 -> WayfinderSounds.SIGNAL_2.get();
                    case 2 -> WayfinderSounds.SIGNAL_3.get();
                    default -> WayfinderSounds.SIGNAL_1.get();
                };

                SIGNALS[i] =
                        new SignalLoopSound(
                                mc.player,
                                sound,
                                i);

                mc.getSoundManager().play(SIGNALS[i]);
            }
        }
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(Wayfinder.MOD_ID, "signalscope_hand"),
                (graphics, partialTick) -> SignalscopeHud.render(
                        graphics,
                        partialTick.getGameTimeDeltaPartialTick(false)));
    }

    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        List<Either<FormattedText, TooltipComponent>> lines = event.getTooltipElements();

        if (!(stack.getItem() instanceof SignalscopeItem)
                && !(stack.getItem() instanceof SignalTweakerItem)) return;

        lines.removeIf(line -> line.left()
                .map(FormattedText::getString)
                .map("Tools & Utilities"::equals)
                .orElse(false));

        if (stack.getItem() instanceof SignalscopeItem) {
            SignalscopeComponent component =
                    stack.get(WayfinderDataComponents.SIGNALSCOPE.get());

            if (component != null) {
                addSignalscopeTooltip(lines, component);
            }
        }

        if (stack.getItem() instanceof SignalTweakerItem) {
            SignalTweakerComponent component =
                    stack.get(WayfinderDataComponents.SIGNAL_TWEAKER.get());

            if (component != null) {
                addTweakerTooltip(lines, component);
            }
        }
    }

    private static void addSignalscopeTooltip(
            List<Either<FormattedText, TooltipComponent>> lines,
            SignalscopeComponent component) {
        boolean shift = Screen.hasShiftDown();
        boolean control = Screen.hasControlDown();

        lines.add(1, Either.right(new WayfinderTooltip.Modules(component)));

        if (!component.upgradeItemstack().isEmpty()) {
            Component name = component.upgradeItemstack().getHoverName().copy()
                    .withStyle(style -> style.withColor(
                            component.selected() == 2 ? 0xF7DB70 : 0xA8A8A8));

            if (component.selected() == 2) {
                name = name.copy().append(
                        Component.literal("  <")
                                .withStyle(style -> style.withColor(0xF7DB70)));
            }

            line(lines, Component.literal("- ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(name));
        }

        if (!component.lensItemstack().isEmpty()) {
            Component name = component.lensItemstack().getHoverName().copy()
                    .withStyle(style -> style.withColor(
                            component.selected() == 1 ? 0xF7DB70 : 0xA8A8A8));

            if (component.selected() == 1) {
                name = name.copy().append(
                        Component.literal("  <")
                                .withStyle(style -> style.withColor(0xF7DB70)));
            }

            line(lines, Component.literal("- ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(name));
        }

        line(lines, Component.empty());
        line(lines, Component.empty());

        if (!shift) {
            line(lines, Component.literal("Hold down [")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("SHIFT")
                            .withStyle(style -> style.withColor(0xF7DB70)))
                    .append(Component.literal("] for details.")
                            .withStyle(ChatFormatting.GRAY)));
        } else {
            line(lines, Component.literal("Hold down [")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("SHIFT")
                            .withStyle(style -> style.withColor(0xF7DB70)))
                    .append(Component.literal("] for details.")
                            .withStyle(ChatFormatting.GRAY)));

            line(lines, Component.empty());

            line(lines, Component.literal("Performing ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("ALT + SCROLL-WHEEL")
                            .withStyle(style -> style
                                    .withColor(0xF7DB70)
                                    .withItalic(true))));

            line(lines, Component.literal("- Scrolls through the Signalscope's")
                    .withStyle(ChatFormatting.GRAY));
            line(lines, Component.literal("  frequency.")
                    .withStyle(ChatFormatting.GRAY));

            line(lines, Component.empty());

            line(lines, Component.literal("Performing ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("SHIFT + ALT + SCROLL-WHEEL")
                            .withStyle(style -> style
                                    .withColor(0xF7DB70)
                                    .withItalic(true))));

            line(lines, Component.literal("- Scrolls through the Signalscope's")
                    .withStyle(ChatFormatting.GRAY));
            line(lines, Component.literal("  gain.")
                    .withStyle(ChatFormatting.GRAY));
            line(lines, Component.literal("- Increasing the gain also lowers the")
                    .withStyle(ChatFormatting.RED));
            line(lines, Component.literal("  Signalscope's accuracy.")
                    .withStyle(ChatFormatting.RED));
            line(lines, Component.empty());
        }

        boolean hasSelectedModule =
                component.selected() != 0
                        && (!component.lensItemstack().isEmpty()
                        || !component.upgradeItemstack().isEmpty());

        if (!control && hasSelectedModule) {
            line(lines, Component.literal("Hold down [")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("CTRL")
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("] for upgrade info.")
                            .withStyle(ChatFormatting.GRAY)));
        }

        if (!shift && control && hasSelectedModule) {
            line(lines, Component.literal("Hold down [")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("CTRL")
                            .withStyle(style -> style.withColor(0xF7DB70)))
                    .append(Component.literal("] for upgrade info.")
                            .withStyle(ChatFormatting.GRAY)));

            line(lines, Component.empty());

            if (component.selected() == 1) {
                line(lines, Component.literal("- ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("Lens:")
                                .withStyle(style -> style.withColor(0xF7DB70))));

                if (component.lensItemstack().isEmpty()) {
                    line(lines, Component.literal("- ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("No Lens Added")
                                    .withStyle(ChatFormatting.DARK_GRAY)));
                } else {
                    line(lines, Component.literal(component.lensItemstack()
                                    .getHoverName().getString() + ":")
                            .withStyle(ChatFormatting.DARK_GRAY));

                    if (component.lensItemstack().is(WayfinderItems.VANTAGE_LENS.get())) {
                        line(lines, Component.literal(
                                        "  Shows only signals that are placed")
                                .withStyle(ChatFormatting.GRAY));
                        line(lines, Component.literal(
                                        "  by OTHER players.")
                                .withStyle(ChatFormatting.GRAY));
                    }

                    if (component.lensItemstack().is(WayfinderItems.PRIVACY_LENS.get())) {
                        line(lines, Component.literal(
                                        "  Shows only signals that are placed")
                                .withStyle(ChatFormatting.GRAY));
                        line(lines, Component.literal(
                                        "  by YOU.")
                                .withStyle(ChatFormatting.GRAY));
                    }
                }
            }

            if (component.selected() == 2) {
                line(lines, Component.literal("- ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("Upgrade:")
                                .withStyle(style -> style.withColor(0xF7DB70))));

                if (component.upgradeItemstack().isEmpty()) {
                    line(lines, Component.literal("- ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal("No Upgrade Added")
                                    .withStyle(ChatFormatting.DARK_GRAY)));
                } else {
                    line(lines, Component.literal(component.upgradeItemstack()
                                    .getHoverName().getString() + ":")
                            .withStyle(ChatFormatting.DARK_GRAY));

                    if (component.upgradeItemstack().is(WayfinderItems.SIGNAL_BOOSTER.get())) {
                        line(lines, Component.literal(
                                        "  Boosts the Signalscope's range")
                                .withStyle(ChatFormatting.GRAY));
                        line(lines, Component.literal(
                                        "  to 1500m-3000m")
                                .withStyle(ChatFormatting.GRAY));
                    }

                    if (component.upgradeItemstack().is(WayfinderItems.SIGNAL_EXPANDER.get())) {
                        line(lines, Component.literal(
                                        "  Boosts the Signalscope's range")
                                .withStyle(ChatFormatting.GRAY));
                        line(lines, Component.literal(
                                        "  to 2500m-5000m")
                                .withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }
    }

    private static void addTweakerTooltip(
            List<Either<FormattedText, TooltipComponent>> lines,
            SignalTweakerComponent component) {
        line(lines, Component.literal("Hold down ALT to switch data.")
                .withStyle(ChatFormatting.GRAY));
        line(lines, Component.empty());
        lines.add(Either.right(new WayfinderTooltip.Colors(component)));
        line(lines, Component.empty());
        line(lines, Component.literal("  Name: ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(component.name())
                        .withStyle(style -> style.withColor(0xF7DB70))));
        line(lines, Component.empty());
        lines.add(Either.right(new WayfinderTooltip.Network(component)));
        line(lines, Component.empty());
        line(lines, Component.literal("  Data:").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Either.right(new WayfinderTooltip.Data(component)));
    }

    public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WayfinderTooltip.Modules.class, AdvancedTooltipOverlay::new);
        event.register(WayfinderTooltip.Colors.class, AdvancedTooltipOverlay::new);
        event.register(WayfinderTooltip.Network.class, AdvancedTooltipOverlay::new);
        event.register(WayfinderTooltip.Data.class, AdvancedTooltipOverlay::new);
    }

    private static void line(
            List<Either<FormattedText, TooltipComponent>> lines,
            Component component) {
        lines.add(Either.left(component));
    }

    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof SignalscopeItem)
                && !(stack.getItem() instanceof SignalTweakerItem)) {
            return;
        }

        int tooltipWidth = 0;
        int tooltipHeight =
                event.getComponents().size() == 1 ? -2 : 0;

        for (var component : event.getComponents()) {
            tooltipWidth = Math.max(
                    tooltipWidth,
                    component.getWidth(event.getFont()));
            tooltipHeight += component.getHeight();
        }

        GuiGraphics graphics = event.getGraphics();

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        var bounds = TooltipLayout.background(event.getX(), event.getY(), tooltipWidth, tooltipHeight);
        graphics.blitSprite(TOOLTIP_BACKGROUND, bounds.x(), bounds.y(), bounds.width(), bounds.height());
        graphics.blitSprite(TOOLTIP_FRAME, bounds.x(), bounds.y(), bounds.width(), bounds.height());

        graphics.pose().popPose();

        event.setBackgroundStart(0x00000000);
        event.setBackgroundEnd(0x00000000);
        event.setBorderStart(0x00000000);
        event.setBorderEnd(0x00000000);
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSignalStore.clear();
        ClientPlayerSignalStore.clear();

        if (ambience != null) {
            ambience.stopNow();
            ambience = null;
        }

        for (int i = 0; i < SIGNALS.length; i++) {
            if (SIGNALS[i] != null) {
                SIGNALS[i].stopNow();
                SIGNALS[i] = null;
            }
        }
    }

    private ClientEvents() {}
}
