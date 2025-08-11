package dev.nitron.wayfinder.cca;

import dev.nitron.wayfinder.item.SignalscopeItem;
import dev.nitron.wayfinder.registries.WayfinderComponents;
import dev.nitron.wayfinder.util.SignalscopeHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.List;

public class WayfinderEntityComponent implements AutoSyncedComponent, CommonTickingComponent {
    private final PlayerEntity player;

    private float lookFactor;

    private float prevSignalscopeVolume = 1F;
    private float signalscopeVolume = 1F;

    public WayfinderEntityComponent(PlayerEntity player) {
        this.player = player;
    }
    public void sync(){
        WayfinderComponents.WAYFINDER.sync(this.player);
    }

    public float getLookFactor() {
        return lookFactor;
    }

    private void setLookFactor(float factor) {
        this.lookFactor = factor;
        sync();
    }

    @Override
    public void tick() {
        if (player.getWorld().isClient) return;

        WayfinderWorldComponent comp = WayfinderComponents.WAYFINDER_W.get(player.getWorld());
        List<BlockPos> signals = comp.getSignalPositions();

        if (signals.isEmpty()) {
            setLookFactor(0f);
            return;
        }

        float maxFactor = 0f;

        for (BlockPos pos : signals) {
            float factor = (float)(SignalscopeHelper.getLookFactor(
                    player,
                    pos,
                    75F,
                    1.0f
            ));

            if (factor > maxFactor) {
                maxFactor = factor;
            }
        }

        setLookFactor(maxFactor);

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
        this.lookFactor = nbt.getFloat("LookFactor");
        this.prevSignalscopeVolume = nbt.getFloat("prevSignalscopeVolume");
        this.signalscopeVolume = nbt.getFloat("signalscopeVolume");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbt.putFloat("LookFactor", this.lookFactor);
        nbt.putFloat("prevSignalscopeVolume", this.prevSignalscopeVolume);
        nbt.putFloat("signalscopeVolume", this.signalscopeVolume);
    }

    public float getSignalscopeVolume() {
        return signalscopeVolume;
    }

    public void setSignalscopeVolume(float signalscopeVolume) {
        this.signalscopeVolume = signalscopeVolume;
        this.sync();
    }
}
