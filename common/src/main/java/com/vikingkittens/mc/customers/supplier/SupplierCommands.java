package com.vikingkittens.mc.customers.supplier;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.PlayerCUtils;

public class SupplierCommands {
    private static final int SEARCH_SIZE = 64;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("suppliers")
                        .requires(source -> CustomersServices.config().buildCommandsEnabled())
                        .then(Commands.literal("spawners")
                                .executes(context -> listSpawners(context.getSource())))
        );
    }

    private static int listSpawners(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = PlayerCUtils.getServerLevel(player);
        List<BlockPos> spawnerPositions = SearchUtils.findBlocksInBox(
                level,
                player.blockPosition(),
                SEARCH_SIZE,
                (pos, state) -> state.is(SupplierSpawner.SUPPLIER_SPAWNER_BLOCK.get())
        );

        source.sendSuccess(SupplierCommands::formatHeading, false);
        for (BlockPos spawnerPos : spawnerPositions) {
            BlockState state = level.getBlockState(spawnerPos);
            source.sendSuccess(
                    () -> formatSpawner(
                            spawnerPos,
                            !state.getValue(SupplierSpawnerBlock.STATE_DISABLED)
                    ),
                    false
            );
        }
        return spawnerPositions.size();
    }

    static Component formatHeading() {
        return Component.literal("Supplier Spawners:")
                .withStyle(ChatFormatting.GREEN);
    }

    static Component formatSpawner(BlockPos pos, boolean enabled) {
        return Component.literal(
                "  (" + pos.toShortString() + "): " + (enabled ? "Enabled" : "Disabled")
        ).withStyle(ChatFormatting.YELLOW);
    }
}
