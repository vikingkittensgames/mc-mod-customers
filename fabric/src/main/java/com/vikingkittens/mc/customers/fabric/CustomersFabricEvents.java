package com.vikingkittens.mc.customers.fabric;

import net.minecraft.world.InteractionResult;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import com.vikingkittens.mc.customers.customer.CustomerInteractions;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardCache;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerCache;

public final class CustomersFabricEvents {
    private CustomersFabricEvents() {}

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                CustomerInteractions.shouldUsePickupCounter(player, level, hitResult.getBlockPos())
                        ? CustomerPickupCounterBlock.handleSecondaryUse(
                                level,
                                hitResult.getBlockPos(),
                                player,
                                hand
                        )
                        : InteractionResult.PASS);
        PlayerBlockBreakEvents.AFTER.register((level, player, position, state, blockEntity) -> {
            CustomerSpawnerCache.onBlockBroken(level, position, state);
            CustomerLeaderboardCache.onBlockBroken(
                    level,
                    position,
                    state
            );
        });
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            CustomerSpawnerCache.onChunkLoaded(level, chunk);
            CustomerLeaderboardCache.onChunkLoaded(level, chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            CustomerSpawnerCache.onChunkUnloaded(level, chunk);
            CustomerLeaderboardCache.onChunkUnloaded(level, chunk);
        });
    }
}
