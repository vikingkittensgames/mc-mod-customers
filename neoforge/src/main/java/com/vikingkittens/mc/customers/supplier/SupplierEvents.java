package com.vikingkittens.mc.customers.supplier;

import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmation;

@EventBusSubscriber(modid = Customers.MODID)
public class SupplierEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSupplierSpawnerBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player &&
                event.getState().getBlock() instanceof SupplierSpawnerBlock &&
                event.getLevel().getBlockEntity(event.getPos()) instanceof SupplierSpawnerBlockEntity spawner &&
                spawner.shouldConfirmBreak()) {
            event.setCanceled(BlockBreakConfirmation.shouldCancelBreak(
                    player,
                    event.getPos(),
                    spawner,
                    "screen.customers.break_confirmation.supplier_spawner_title",
                    "screen.customers.break_confirmation.message"
            ));
        }
    }
}
