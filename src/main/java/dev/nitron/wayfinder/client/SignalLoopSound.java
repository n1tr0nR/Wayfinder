package dev.nitron.wayfinder.client;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.network.ClientPlayerSignalStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SignalLoopSound extends AbstractTickableSoundInstance {
    private final LocalPlayer player; private final int type;
    public SignalLoopSound(LocalPlayer player,SoundEvent sound,int type){
        super(sound,SoundSource.AMBIENT,RandomSource.create());this.player=player;this.type=type;looping=true;delay=0;volume=0.01F;relative=true;
    }
    @Override public void tick(){
        if(player.isRemoved()||!player.isAlive()){stop();return;}
        boolean holding=player.getMainHandItem().getItem() instanceof SignalscopeItem;
        float target=holding?0.5F:0.0F;
        float partial=Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        volume=Mth.lerp(partial,volume,target)*ClientPlayerSignalStore.factor(type);
        if(volume<=0.01F&&!holding)volume=0.0F;
    }
    public void stopNow(){Minecraft.getInstance().getSoundManager().stop(this);stop();}
    @Override public boolean canStartSilent(){return true;}
}
