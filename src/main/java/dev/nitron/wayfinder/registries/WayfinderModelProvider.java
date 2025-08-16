package dev.nitron.wayfinder.registries;

import dev.nitron.wayfinder.Wayfinder;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class WayfinderModelProvider {
    public static final  List<Identifier> MODELS = new ArrayList<>();

    public static void loadModels(){
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_gui"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_privacy_gui"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_privacy"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_vantage"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_vantage_gui"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_twisted"));
        MODELS.add(Identifier.of(Wayfinder.MOD_ID, "item/signalscope_twisted_gui"));
    }
}
