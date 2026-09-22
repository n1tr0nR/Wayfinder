package dev.nitron.wayfinder.util;

import dev.nitron.wayfinder.item.component.SignalscopeComponent;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.registry.WayfinderItems;
import dev.nitron.wayfinder.signal.SignalRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class SignalscopeHelper {
    public static final float TRACKING_ANGLE = 75.0F, IDENTIFY_ANGLE = 15.65F;
    public static float lookFactor(Player p, BlockPos target, float max, float partial) {
        Vec3 eye=p.getEyePosition(), look=p.getViewVector(partial).normalize(), direction=Vec3.atLowerCornerOf(target).subtract(eye).normalize();
        double dot=look.dot(direction); if(dot<=0) return 0;
        double min=Math.cos(Math.toRadians(max));
        float f=(float)Mth.clamp((dot-min)/(1-min),0.0D,1.0D);
        return f>0.95F?1.0F:f;
    }
    public static SignalscopeComponent component(ItemStack s){ return s.get(WayfinderDataComponents.SIGNALSCOPE.get()); }
    public static boolean canShowSignal(Player p, SignalRecord s, SignalscopeComponent c){
        if(c==null||c.frequency()!=s.frequency()) return false;
        if(c.lensItemstack().is(WayfinderItems.VANTAGE_LENS.get())&&s.owner().equals(p.getUUID())) return false;
        if(c.lensItemstack().is(WayfinderItems.PRIVACY_LENS.get())&&!s.owner().equals(p.getUUID())) return false;
        return true;
    }
    public static boolean accepts(Player p, SignalRecord s, ItemStack scope){
        SignalscopeComponent c=component(scope);
        return c!=null&&!c.lensItemstack().isEmpty()&&(!s.powered()||p.getUUID().equals(s.owner()))&&canShowSignal(p,s,c);
    }
    public static double maxDistance(SignalscopeComponent c){
        int base=c.upgradeItemstack().is(WayfinderItems.SIGNAL_EXPANDER.get())?500:c.upgradeItemstack().is(WayfinderItems.SIGNAL_BOOSTER.get())?300:200;
        return base*((c.gain()-20.0D)/2.0D);
    }
    public static double maxDistance(ItemStack s){ SignalscopeComponent c=component(s); return c==null?0:maxDistance(c); }
    public static double distance(Player p, SignalRecord s){ return p.position().distanceTo(Vec3.atLowerCornerOf(s.pos())); }
    public static BlockPos lookedAtSignal(Player p,List<SignalRecord> signals,float maxAngle,double maxDistance,SignalscopeComponent c){
        Vec3 eye=p.getEyePosition(),look=p.getViewVector(1.0F).normalize(); BlockPos closest=null; double best=Double.MAX_VALUE;
        for(SignalRecord s:signals){
            Vec3 target=Vec3.atLowerCornerOf(s.pos()).subtract(eye); double distance=target.length();
            if(distance>maxDistance||s.powered()&&!p.getUUID().equals(s.owner())||!canShowSignal(p,s,c)||c.lensItemstack().isEmpty())continue;
            double angle=Math.acos(Mth.clamp(look.dot(target.normalize()),-1.0D,1.0D))*57.29577951308232D;
            if(angle<=maxAngle&&angle<best){best=angle;closest=s.pos();}
        }
        return closest;
    }
    private SignalscopeHelper(){}
}
