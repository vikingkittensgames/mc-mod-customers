package com.vikingkittens.mc.customers.customer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.customer.ai.CustomerMoveToCounterGoal;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Customers.MODID)
public class CustomerCommands {
    private static final int SEARCH_SIZE = 64;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("customers")
                        .requires(source -> Config.ENABLE_BUILD_COMMANDS.get())
                        .then(Commands.literal("spawners")
                                .executes(context -> listSpawners(context.getSource(), false))
                                .then(Commands.literal("counters")
                                        .executes(context -> listSpawners(context.getSource(), true))))
        );
    }

    private static int listSpawners(
            CommandSourceStack source,
            boolean includeCounters
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        List<BlockPos> spawnerPositions = SearchUtils.findBlocksInBox(
                level,
                player.blockPosition(),
                SEARCH_SIZE,
                (pos, state) -> state.is(CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.get())
        );
        Map<BlockPos, CustomerCounterMarker> markers = new LinkedHashMap<>();

        source.sendSuccess(
                () -> Component.literal(
                        includeCounters
                                ? "Customer Spawners & Counters:"
                                : "Customer Spawners:"
                ),
                false
        );

        for (BlockPos spawnerPos : spawnerPositions) {
            BlockState spawnerState = level.getBlockState(spawnerPos);
            CustomerSpawnerMode mode = spawnerState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
            boolean enabled = !spawnerState.getValue(CustomerSpawnerBlock.STATE_DISABLED);
            source.sendSuccess(() -> formatSpawner(spawnerPos, enabled, mode), false);

            if (includeCounters) {
                sendCounters(source, level, spawnerPos, mode, markers);
            }
        }

        if (includeCounters) {
            PacketDistributor.sendToPlayer(
                    player,
                    new CustomerCounterMarkersPayload(List.copyOf(markers.values()))
            );
        }

        return spawnerPositions.size();
    }

    private static void sendCounters(
            CommandSourceStack source,
            ServerLevel level,
            BlockPos spawnerPos,
            CustomerSpawnerMode spawnerMode,
            Map<BlockPos, CustomerCounterMarker> markers
    ) {
        BlockState counterBlockState = level.getBlockState(spawnerPos.above());
        if (counterBlockState.isAir()) {
            return;
        }

        List<BlockPos> counterPositions = CustomerMoveToCounterGoal.findCounterPositions(
                level,
                spawnerPos,
                counterBlockState
        );
        for (BlockPos counterPos : counterPositions) {
            BlockState state = level.getBlockState(counterPos);
            addCounterMarker(markers, counterPos, spawnerMode);
            source.sendSuccess(
                    () -> formatCounter(
                            counterPos,
                            Component.translatable(state.getBlock().getDescriptionId())
                    ),
                    false
            );
        }
    }

    static void addCounterMarker(
            Map<BlockPos, CustomerCounterMarker> markers,
            BlockPos position,
            CustomerSpawnerMode spawnerMode
    ) {
        markers.putIfAbsent(
                position,
                new CustomerCounterMarker(position.immutable(), spawnerMode)
        );
    }

    static Component formatSpawner(
            BlockPos pos,
            boolean enabled,
            CustomerSpawnerMode mode
    ) {
        return Component.literal(
                "  (" + pos.toShortString() + "): " +
                        (enabled ? "Enabled" : "Disabled") + ", "
        ).append(mode.getTitle());
    }

    static Component formatCounter(BlockPos pos, Component blockName) {
        return Component.literal("      (" + pos.toShortString() + "): ")
                .append(blockName);
    }
}