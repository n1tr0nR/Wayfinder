package dev.nitron.wayfinder.util;

import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public record SignalscopeLocation(BlockPos location, SoundEvent signalEvent, SignalscopeType type) {
    public enum SignalscopeType {
        DEFAULT,
        WHIMSEY,
        GLITCHED,
        DISTANT
    }

    public String getHudText(){
        return switch (this.type){
            case DEFAULT -> "Default Tuning: ";
            case WHIMSEY -> "Whimsey Tuning: ";
            case GLITCHED -> "??? Tuning: ";
            case DISTANT -> "Distant Tuning: ";
        };
    }
}
