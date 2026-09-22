package dev.nitron.wayfinder.signal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class SignalSavedData extends SavedData {
    private static final Factory<SignalSavedData> FACTORY=new Factory<>(SignalSavedData::new,SignalSavedData::load,DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    private final Map<Long,SignalRecord> signals=new LinkedHashMap<>();
    public static SignalSavedData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(FACTORY,"wayfinder_signals");}
    public static SignalSavedData load(CompoundTag tag,HolderLookup.Provider regs){
        SignalSavedData d=new SignalSavedData(); ListTag list=tag.getList("Signals",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){CompoundTag e=list.getCompound(i);BlockPos p=new BlockPos(e.getInt("x"),e.getInt("y"),e.getInt("z"));
            UUID o;try{o=UUID.fromString(e.getString("ownerUUID"));}catch(Exception ex){o=new UUID(0,0);}
            d.signals.put(p.asLong(),new SignalRecord(p,e.getString("name"),e.getInt("red"),e.getInt("green"),e.getInt("blue"),e.contains("freq")?e.getInt("freq"):3,o,e.getBoolean("powered"),e.getInt("type")));
        }return d;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider regs){
        ListTag list=new ListTag();for(SignalRecord s:signals.values()){CompoundTag e=new CompoundTag();
            e.putInt("x",s.pos().getX());e.putInt("y",s.pos().getY());e.putInt("z",s.pos().getZ());e.putString("name",s.name());
            e.putInt("red",s.red());e.putInt("green",s.green());e.putInt("blue",s.blue());e.putInt("type",s.type());e.putInt("freq",s.frequency());
            e.putString("ownerUUID",s.owner().toString());e.putBoolean("powered",s.powered());list.add(e);}tag.put("Signals",list);return tag;
    }
    public Collection<SignalRecord> all(){return new ArrayList<>(signals.values());} public SignalRecord get(BlockPos p){return signals.get(p.asLong());}
    public boolean put(SignalRecord s){var old=signals.put(s.pos().asLong(),s);boolean c=!s.equals(old);if(c)setDirty();return c;}
    public boolean remove(BlockPos p){boolean c=signals.remove(p.asLong())!=null;if(c)setDirty();return c;}
    public void clear(){if(!signals.isEmpty()){signals.clear();setDirty();}}
}
