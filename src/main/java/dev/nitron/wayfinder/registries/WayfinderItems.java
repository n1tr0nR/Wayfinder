package dev.nitron.wayfinder.registries;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.item.SignalscopeItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WayfinderItems {
    public static void init(){}

    public static final Item SIGNALSCOPE = register("signalscope", new SignalscopeItem(new Item.Settings().maxCount(1)));

    public static Item register(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of(Wayfinder.MOD_ID, name), item);
    }
}
