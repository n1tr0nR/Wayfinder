package dev.nitron.wayfinder.registry;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.SignalTweakerItem;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WayfinderItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Wayfinder.MOD_ID);
    public static final DeferredItem<BlockItem> SIGNAL_ARRAY = ITEMS.registerSimpleBlockItem(WayfinderBlocks.SIGNAL_ARRAY);
    public static final DeferredItem<Item> SIGNAL_BOOSTER = simple("signal_booster");
    public static final DeferredItem<Item> SIGNAL_EXPANDER = simple("signal_expander");
    public static final DeferredItem<Item> LENS = simple("lens");
    public static final DeferredItem<Item> PRIVACY_LENS = simple("privacy_lens");
    public static final DeferredItem<Item> VANTAGE_LENS = simple("vantage_lens");
    public static final DeferredItem<SignalscopeItem> SIGNALSCOPE = ITEMS.registerItem(
            "signalscope",
            properties -> new SignalscopeItem(properties.component(
                    WayfinderDataComponents.SIGNALSCOPE.get(),
                    new SignalscopeComponent(3, 35.0D, new ItemStack(LENS.get()), ItemStack.EMPTY, 0))),
            new Item.Properties());
    public static final DeferredItem<SignalTweakerItem> SIGNAL_TWEAKER = ITEMS.registerItem(
            "signal_tweaker",
            properties -> new SignalTweakerItem(properties.component(
                    WayfinderDataComponents.SIGNAL_TWEAKER.get(),
                    new SignalTweakerComponent(new Vec3i(255,255,255), "Signal", false, 3, 0, 0))),
            new Item.Properties());
    private static DeferredItem<Item> simple(String name) { return ITEMS.registerSimpleItem(name, new Item.Properties().stacksTo(1)); }
    private WayfinderItems() {}
}
