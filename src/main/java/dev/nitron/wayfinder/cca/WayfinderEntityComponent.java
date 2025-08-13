package dev.nitron.wayfinder.cca;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.block.SignalArrayBlock;
import dev.nitron.wayfinder.block_entity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.registries.WayfinderDataComponents;
import dev.nitron.wayfinder.registries.WayfinderItems;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.List;

public class WayfinderEntityComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;

    private float factor0;
    private float factor1;
    private float factor2;
    private float factor3;

    private float prevSignalscopeVolume = 1F;
    private float signalscopeVolume = 1F;

    public WayfinderEntityComponent(PlayerEntity player) {
        this.player = player;
    }
    public void sync(){
        WayfinderComponents.WAYFINDER.sync(this.player);
    }

    public float getFactor0(){
        return this.factor0;
    }

    public float getFactor1(){
        return this.factor1;
    }

    public float getFactor2(){
        return this.factor2;
    }

    public float getFactor3(){
        return this.factor3;
    }

    @Override
    public void tick() {
        if (player.getWorld().isClient) return;

        WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(player.getWorld());
        List<WayfinderWorldComponent.SignalData> signals = comp.getSignalPositions();

        float newFactor0 = 0f;
        float newFactor1 = 0f;
        float newFactor2 = 0f;
        float newFactor3 = 0f;

        if (!signals.isEmpty()){
            for (WayfinderWorldComponent.SignalData signalData : signals){
                BlockPos pos = signalData.pos;
                double distance = player.getPos().distanceTo(Vec3d.ofCenter(pos));
                BlockEntity be = player.getWorld().getBlockEntity(pos);
                boolean privacy = false;
                if (player.getActiveItem().contains(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE)){
                    privacy = player.getActiveItem().get(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE).item().isOf(WayfinderItems.PRIVACY_LENS);
                }
                if ((!(be instanceof SignalArrayBlockEntity signalArrayBlockEntity)) || (this.player.getWorld().getBlockState(pos).get(SignalArrayBlock.POWERED) && !this.player.getUuidAsString().equals(signalArrayBlockEntity.owner_uuid))) continue;
                if (privacy &&  !this.player.getUuidAsString().equals(signalData.ownerUUID)) continue;
                int type = signalArrayBlockEntity.type;
                float factor = (float) SignalscopeHelper.getLookFactor(
                        player,
                        pos,
                        75F,
                        1.0F
                );
                boolean concave = false;
                if (player.getActiveItem().contains(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE)){
                    concave = player.getActiveItem().get(WayfinderDataComponents.SIGNALSCOPE_COMPONENT_COMPONENT_TYPE).item().isOf(WayfinderItems.CONCAVE_LENS);
                }
                if (distance < (concave ? 3000 : 1000)){
                    switch (type) {
                        case 0 -> newFactor0 = Math.max(newFactor0, factor);
                        case 1 -> newFactor1 = Math.max(newFactor1, factor);
                        case 2 -> newFactor2 = Math.max(newFactor2, factor);
                        case 3 -> newFactor3 = Math.max(newFactor3, factor);
                        default -> {}
                    }
                }
            }
        }

        this.factor0 = newFactor0;
        this.factor1 = newFactor1;
        this.factor2 = newFactor2;
        this.factor3 = newFactor3;
        sync();

        //TODO: Add an achievement for this!!
        if (this.factor0 == 1 && this.factor1 == 1 && this.factor2 == 1 && this.factor3 == 1 && this.signalscopeVolume >= 0.9){
            Wayfinder.grantAdvancement(this.player, Identifier.of(Wayfinder.MOD_ID, "let_the_choir_sing"), "incode");
        }

        if (player.getActiveItem().getItem() instanceof SignalscopeItem){
            this.prevSignalscopeVolume = this.signalscopeVolume;
            this.signalscopeVolume += 0.3F;
            if (this.signalscopeVolume > 1){
                this.signalscopeVolume = 1;
            }
            if (this.prevSignalscopeVolume != this.signalscopeVolume){
                this.sync();
            }
        } else {
            this.prevSignalscopeVolume = this.signalscopeVolume;
            this.signalscopeVolume -= 0.05F;
            if (this.signalscopeVolume < 0){
                this.signalscopeVolume = 0;
            }
            if (this.prevSignalscopeVolume != this.signalscopeVolume){
                this.sync();
            }
        }
    }

    public float getSIgnalscopeVol(float tickDelta){
        return MathHelper.lerp(tickDelta, this.prevSignalscopeVolume, this.signalscopeVolume);
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.factor0 = nbt.getFloat("factor0");
        this.factor1 = nbt.getFloat("factor1");
        this.factor2 = nbt.getFloat("factor2");
        this.factor3 = nbt.getFloat("factor3");
        this.prevSignalscopeVolume = nbt.getFloat("prevSignalscopeVolume");
        this.signalscopeVolume = nbt.getFloat("signalscopeVolume");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbt.putFloat("factor0", this.factor0);
        nbt.putFloat("factor1", this.factor1);
        nbt.putFloat("factor2", this.factor2);
        nbt.putFloat("factor3", this.factor3);
        nbt.putFloat("prevSignalscopeVolume", this.prevSignalscopeVolume);
        nbt.putFloat("signalscopeVolume", this.signalscopeVolume);
    }
}
