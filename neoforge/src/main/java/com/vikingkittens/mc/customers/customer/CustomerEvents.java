package com.vikingkittens.mc.customers.customer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmation;

@EventBusSubscriber(modid = Customers.MODID)
public class CustomerEvents {
    /**
     * Routes sneaking pickup-counter interactions to the block instead of
     * allowing the held item to handle them.
     *
     * @param event block interaction event
     */
    @SubscribeEvent
    public static void onPickupCounterInteract(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (CustomerInteractions.shouldUsePickupCounter(event.getEntity(), event.getLevel(), event.getPos())) {
            event.setUseBlock(TriState.TRUE);
            event.setUseItem(TriState.FALSE);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCustomerSpawnerBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player &&
                event.getState().getBlock() instanceof CustomerSpawnerBlock &&
                event.getLevel().getBlockEntity(event.getPos()) instanceof CustomerSpawnerBlockEntity spawner &&
                spawner.shouldConfirmBreak()) {
            event.setCanceled(BlockBreakConfirmation.shouldCancelBreak(
                    player,
                    event.getPos(),
                    spawner,
                    "screen.customers.break_confirmation.customer_spawner_title",
                    "screen.customers.break_confirmation.message"
            ));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCustomerLeaderboardBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player &&
                event.getState().getBlock() instanceof CustomerLeaderboardBlock &&
                event.getLevel().getBlockEntity(event.getPos()) instanceof CustomerLeaderboardBlockEntity leaderboard &&
                leaderboard.shouldConfirmBreak()) {
            event.setCanceled(BlockBreakConfirmation.shouldCancelBreak(
                    player,
                    event.getPos(),
                    leaderboard,
                    "screen.customers.break_confirmation.customer_leaderboard_title",
                    "screen.customers.break_confirmation.customer_leaderboard_message"
            ));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Player player = event.getEntity() instanceof Player placingPlayer ? placingPlayer : null;
            CustomerSpawnerCache.onBlockPlaced(
                    level,
                    event.getPos(),
                    event.getPlacedBlock(),
                    player
            );
            CustomerLeaderboardCache.onBlockPlaced(
                    level,
                    event.getPos(),
                    event.getPlacedBlock()
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            CustomerSpawnerCache.onBlockBroken(
                    level,
                    event.getPos(),
                    event.getState()
            );
            CustomerLeaderboardCache.onBlockBroken(
                    level,
                    event.getPos(),
                    event.getState()
            );
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level &&
                event.getChunk() instanceof LevelChunk chunk) {
            CustomerSpawnerCache.onChunkLoaded(level, chunk);
            CustomerLeaderboardCache.onChunkLoaded(level, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level &&
                event.getChunk() instanceof LevelChunk chunk) {
            CustomerSpawnerCache.onChunkUnloaded(level, chunk);
            CustomerLeaderboardCache.onChunkUnloaded(level, chunk);
        }
    }
}
