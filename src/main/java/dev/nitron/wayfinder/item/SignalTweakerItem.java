package dev.nitron.wayfinder.item;

import dev.nitron.wayfinder.Wayfinder;
import dev.nitron.wayfinder.blockentity.SignalArrayBlockEntity;
import dev.nitron.wayfinder.item.component.SignalTweakerComponent;
import dev.nitron.wayfinder.network.WayfinderNetwork;
import dev.nitron.wayfinder.registry.WayfinderDataComponents;
import dev.nitron.wayfinder.signal.SignalRecord;
import dev.nitron.wayfinder.signal.SignalSavedData;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.joml.Vector3f;

public final class SignalTweakerItem extends Item {
    public SignalTweakerItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack)
                .copy()
                .withStyle(style -> style.withColor(16243568));
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            ItemStack tweaker,
            ItemStack carried,
            Slot slot,
            ClickAction action,
            Player player,
            SlotAccess access) {
        if (action == ClickAction.SECONDARY
                && carried.is(Items.NAME_TAG)
                && carried.has(DataComponents.CUSTOM_NAME)) {
            if (player.getCooldowns().isOnCooldown(this)) {
                if (player.level().isClientSide) {
                    player.playNotifySound(
                            SoundEvents.VAULT_INSERT_ITEM_FAIL,
                            SoundSource.PLAYERS,
                            0.1F,
                            1.0F);
                }
                return true;
            }

            SignalTweakerComponent component =
                    tweaker.get(WayfinderDataComponents.SIGNAL_TWEAKER.get());

            Component customName = carried.get(DataComponents.CUSTOM_NAME);

            if (component != null && customName != null) {
                tweaker.set(
                        WayfinderDataComponents.SIGNAL_TWEAKER.get(),
                        new SignalTweakerComponent(
                                component.color(),
                                customName.getString(),
                                component.privateNetwork(),
                                component.freq(),
                                component.type(),
                                component.selected()));

                if (player.level().isClientSide) {
                    player.playNotifySound(
                            SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                }

                player.getCooldowns().addCooldown(this, 10);
                return true;
            }
        }

        return super.overrideOtherStackedOnMe(
                tweaker,
                carried,
                slot,
                action,
                player,
                access);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return super.useOn(context);
        }

        var level = context.getLevel();
        var pos = context.getClickedPos();
        ItemStack mainHandTweaker = player.getMainHandItem();
        boolean hasMainHandTweaker =
                mainHandTweaker.getItem() instanceof SignalTweakerItem;

        if (player.isShiftKeyDown()) {
            if (level instanceof ServerLevel server) {
                SignalRecord signal =
                        SignalSavedData.get(server).get(pos);

                if (signal != null && hasMainHandTweaker) {
                    mainHandTweaker.set(
                            WayfinderDataComponents.SIGNAL_TWEAKER.get(),
                            new SignalTweakerComponent(
                                    new Vec3i(
                                            signal.red(),
                                            signal.green(),
                                            signal.blue()),
                                    signal.name(),
                                    signal.powered(),
                                    signal.frequency(),
                                    signal.type(),
                                    0));
                }
            }

            player.displayClientMessage(
                    Component.literal("Successfully copied Signal Data")
                            .withStyle(style -> style.withColor(0x55FF55)),
                    true);

            level.playSound(
                    null,
                    pos.getCenter().x,
                    pos.getCenter().y,
                    pos.getCenter().z,
                    SoundEvents.TRIAL_SPAWNER_DETECT_PLAYER,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F);

            return InteractionResult.SUCCESS;
        }

        if (hasMainHandTweaker
                && level.getBlockEntity(pos)
                instanceof SignalArrayBlockEntity array) {
            SignalTweakerComponent component =
                    mainHandTweaker.get(WayfinderDataComponents.SIGNAL_TWEAKER.get());

            if (component != null) {
                array.update(
                        component.name(),
                        component.color(),
                        component.type());

                if (component.privateNetwork()) {
                    Wayfinder.grantAdvancement(
                            player,
                            ResourceLocation.fromNamespaceAndPath(
                                    Wayfinder.MOD_ID,
                                    "personal_vpn"),
                            "incode");
                }

                if (level instanceof ServerLevel server) {
                    SignalSavedData data = SignalSavedData.get(server);
                    SignalRecord old = data.get(pos);

                    if (old != null) {
                        data.put(
                                new SignalRecord(
                                        pos,
                                        component.name(),
                                        component.color().getX(),
                                        component.color().getY(),
                                        component.color().getZ(),
                                        component.freq(),
                                        old.owner(),
                                        component.privateNetwork(),
                                        component.type()));
                    }

                    Vector3f particleColor =
                            new Vector3f(
                                    component.color().getX() / 255.0F,
                                    component.color().getY() / 255.0F,
                                    component.color().getZ() / 255.0F);

                    server.sendParticles(
                            new DustParticleOptions(particleColor, 2.0F),
                            pos.getCenter().x,
                            pos.getY(),
                            pos.getCenter().z,
                            10,
                            0.10000000149011612D,
                            0.25D,
                            0.10000000149011612D,
                            1.0D);

                    WayfinderNetwork.syncLevel(server);
                }

                level.playSound(
                        null,
                        pos.getCenter().x,
                        pos.getCenter().y,
                        pos.getCenter().z,
                        SoundEvents.VAULT_OPEN_SHUTTER,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F);

                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }
}
