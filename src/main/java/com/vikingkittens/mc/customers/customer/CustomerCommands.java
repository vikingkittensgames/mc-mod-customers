package com.vikingkittens.mc.customers.customer;

import com.vikingkittens.mc.customers.compatability.PlayerCUtils;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.SearchUtils;
import com.vikingkittens.mc.customers.config.Config;
import com.vikingkittens.mc.customers.customer.CustomerCounter;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

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
        ServerLevel level = PlayerCUtils.getServerLevel(player);
        List<BlockPos> spawnerPositions = SearchUtils.findBlocksInBox(
                level,
                player.blockPosition(),
                SEARCH_SIZE,
                (pos, state) -> state.is(CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.get())
        );
        Map<BlockPos, CustomerCounterMarker> markers = new LinkedHashMap<>();
        Set<BlockPos> surroundingPositions = new LinkedHashSet<>();

        source.sendSuccess(
                () -> formatHeading(includeCounters),
                false
        );

        for (BlockPos spawnerPos : spawnerPositions) {
            BlockState spawnerState = level.getBlockState(spawnerPos);
            CustomerSpawnerMode mode = spawnerState.getValue(CustomerSpawnerBlock.STATE_SPAWN_MODE);
            boolean enabled = !spawnerState.getValue(CustomerSpawnerBlock.STATE_DISABLED);
            source.sendSuccess(() -> formatSpawner(spawnerPos, enabled, mode), false);

            if (includeCounters) {
                sendCounters(source, level, spawnerPos, player, mode, markers, surroundingPositions);
            }
        }

        if (includeCounters) {
            PacketDistributor.sendToPlayer(
                    player,
                    new CustomerCounterMarkersPayload(
                            List.copyOf(markers.values()),
                            List.copyOf(surroundingPositions)
                    )
            );
        }

        return spawnerPositions.size();
    }

    private static void sendCounters(
            CommandSourceStack source,
            ServerLevel level,
            BlockPos spawnerPos,
            ServerPlayer player,
            CustomerSpawnerMode spawnerMode,
            Map<BlockPos, CustomerCounterMarker> markers,
            Set<BlockPos> surroundingPositions
    ) {
        BlockState counterBlockState = level.getBlockState(spawnerPos.above());
        if (counterBlockState.isAir()) {
            return;
        }

        BlockState avoidBlockState = level.getBlockState(spawnerPos.below());
        List<BlockPos> counterPositions = CustomerCounter.findCounterPositions(
                level,
                spawnerPos,
                counterBlockState
        );
        CustomerCounter.findValidSurroundingPositions(
                level,
                counterPositions,
                player,
                avoidBlockState
        ).stream().map(position -> CustomerCounter.getMarkerPosition(
                level,
                position,
                player,
                avoidBlockState
        ))
                .forEach(surroundingPositions::add);

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

    static Component formatHeading(boolean includeCounters) {
        return Component.literal(
                includeCounters
                        ? "Customer Spawners & Counters:"
                        : "Customer Spawners:"
        ).withStyle(ChatFormatting.GREEN);
    }

    static Component formatSpawner(
            BlockPos pos,
            boolean enabled,
            CustomerSpawnerMode mode
    ) {
        return Component.literal(
                "  (" + pos.toShortString() + "): " +
                        (enabled ? "Enabled" : "Disabled") + ", "
        ).append(mode.getTitle()).withStyle(ChatFormatting.YELLOW);
    }

    static Component formatCounter(BlockPos pos, Component blockName) {
        return Component.literal("      (" + pos.toShortString() + "): ")
                .append(blockName);
    }
}