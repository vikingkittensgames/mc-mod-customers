package com.vikingkittens.mc.customers.supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.CreativeModeTabs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import com.vikingkittens.mc.customers.common.BlockBreakConfirmation;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public final class SupplierForgeEvents {
    private SupplierForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(SupplierForgeEvents::addCreative);
        modEventBus.addListener(SupplierForgeEvents::registerAttributes);
        MinecraftForge.EVENT_BUS.addListener(SupplierForgeEvents::onEntityLeaveLevel);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, SupplierForgeEvents::onSupplierSpawnerBreak);
    }

    static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(Supplier.SUPPLIER_VILLAGER.get(), Villager.createAttributes().build());
    }

    static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!LevelCUtils.isClientSide(event.getLevel()) &&
                event.getEntity() instanceof SupplierVillagerEntity supplier &&
                supplier.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            supplier.discard();
        }
    }

    static void onSupplierSpawnerBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player &&
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

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get());
        }
    }
}
