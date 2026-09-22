package com.vikingkittens.mc.customers.fabric;

import dev.architectury.event.events.common.InteractionEvent;

import net.minecraft.world.InteractionResult;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import com.vikingkittens.mc.customers.customer.CustomerInteractions;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerCache;

public final class CustomersFabricEvents {
    private CustomersFabricEvents() {}

    public static void initialize() {
        UseEntityCallback.EVENT.register((player, level, hand, target, hitResult) ->
                InteractionEvent.INTERACT_ENTITY.invoker().interact(player, target, hand).asMinecraft());
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                CustomerInteractions.shouldUsePickupCounter(player, level, hitResult.getBlockPos())
                        ? CustomerPickupCounterBlock.handleSecondaryUse(
                                level,
                                hitResult.getBlockPos(),
                                player,
                                hand
                        )
                        : InteractionResult.PASS);
        PlayerBlockBreakEvents.AFTER.register((level, player, position, state, blockEntity) ->
                CustomerSpawnerCache.onBlockBroken(level, position, state));
        ServerChunkEvents.CHUNK_LOAD.register(CustomerSpawnerCache::onChunkLoaded);
        ServerChunkEvents.CHUNK_UNLOAD.register(CustomerSpawnerCache::onChunkUnloaded);
    }
}
