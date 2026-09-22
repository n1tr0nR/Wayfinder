package dev.nitron.wayfinder.network;
import dev.nitron.wayfinder.signal.PlayerSignalState;
public final class ClientPlayerSignalStore {
    private static volatile PlayerSignalState state=new PlayerSignalState(0,0,0);
    public static void replace(PlayerSignalState next){state=next;}
    public static float factor(int type){return state.factor(type);}
    public static void clear(){state=new PlayerSignalState(0,0,0);}
    private ClientPlayerSignalStore(){}
}
