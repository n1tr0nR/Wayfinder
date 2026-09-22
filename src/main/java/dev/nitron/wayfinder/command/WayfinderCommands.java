package dev.nitron.wayfinder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.nitron.wayfinder.network.WayfinderNetwork;
import dev.nitron.wayfinder.signal.SignalRecord;
import dev.nitron.wayfinder.signal.SignalSavedData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public final class WayfinderCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext buildContext,
                                Commands.CommandSelection selection) {
        dispatcher.register(
                Commands.literal("signals")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.literal("add").executes(WayfinderCommands::add))
                                .then(Commands.literal("remove").executes(WayfinderCommands::remove))
                                .then(Commands.literal("clear").executes(WayfinderCommands::clear)))
        );
    }

    private static int add(CommandContext<CommandSourceStack> context) {
        var level = context.getSource().getLevel();
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

        UUID owner = context.getSource().getPlayer() != null
                ? context.getSource().getPlayer().getUUID()
                : UUID.fromString("");

        SignalSavedData.get(level).put(new SignalRecord(
                pos, "New Signal", 255, 255, 255, 3, owner, false, 1));
        WayfinderNetwork.syncLevel(level);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        var level = context.getSource().getLevel();
        SignalSavedData.get(level).remove(BlockPosArgument.getBlockPos(context, "pos"));
        WayfinderNetwork.syncLevel(level);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        var level = context.getSource().getLevel();
        SignalSavedData.get(level).clear();
        WayfinderNetwork.syncLevel(level);
        return 1;
    }

    private WayfinderCommands() {}
}
