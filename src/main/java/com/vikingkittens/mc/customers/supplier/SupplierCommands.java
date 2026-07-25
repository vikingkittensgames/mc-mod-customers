package com.vikingkittens.mc.customers.supplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@EventBusSubscriber(modid = Customers.MODID)
public class SupplierCommands {
    private static final int SEARCH_SIZE = 64;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("suppliers")
                        .requires(source -> Config.ENABLE_BUILD_COMMANDS.get())
                        .then(Commands.literal("spawners")
                                .executes(context -> listSpawners(context.getSource())))
        );
    }

    private static int listSpawners(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
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