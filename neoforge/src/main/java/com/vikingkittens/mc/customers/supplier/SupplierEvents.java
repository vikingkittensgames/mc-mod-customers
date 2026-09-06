package com.vikingkittens.mc.customers.supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.BlockBreakConfirmation;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

@EventBusSubscriber(modid = Customers.MODID)
public class SupplierEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(Supplier.SUPPLIER_VILLAGER.get(), Villager.createAttributes().build());
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!LevelCUtils.isClientSide(event.getLevel())) {
            if (event.getEntity() instanceof SupplierVillagerEntity supplier) {
                Entity.RemovalReason reason = supplier.getRemovalReason();
                if (reason == Entity.RemovalReason.CHANGED_DIMENSION) {
                    supplier.discard();
                }
            }
        }
    }

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
