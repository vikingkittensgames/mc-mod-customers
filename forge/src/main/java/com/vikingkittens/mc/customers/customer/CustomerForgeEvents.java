package com.vikingkittens.mc.customers.customer;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.CreativeModeTabs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public final class CustomerForgeEvents {
    private CustomerForgeEvents() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CustomerForgeEvents::addCreative);
        modEventBus.addListener(CustomerForgeEvents::registerAttributes);
        MinecraftForge.EVENT_BUS.addListener(CustomerForgeEvents::onEntityLeaveLevel);
        MinecraftForge.EVENT_BUS.addListener(CustomerForgeEvents::onPickupCounterInteract);
        MinecraftForge.EVENT_BUS.addListener(CustomerForgeEvents::onCustomerInteract);
    }

    static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(Customer.CUSTOMER_VILLAGER.get(), Villager.createAttributes().build());
    }

    static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!LevelCUtils.isClientSide(event.getLevel()) &&
                event.getEntity() instanceof CustomerVillagerEntity customer &&
                customer.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION) {
            customer.discard();
        }
    }

    static void onPickupCounterInteract(PlayerInteractEvent.RightClickBlock event) {
        if (CustomerInteractions.shouldUsePickupCounter(event.getEntity(), event.getLevel(), event.getPos())) {
            event.setUseBlock(Event.Result.ALLOW);
            event.setUseItem(Event.Result.DENY);
        }
    }

    static void onCustomerInteract(PlayerInteractEvent.EntityInteract event) {
        if (CustomerInteractions.tryQuickSell(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get());
            CustomerPickupCounter.ITEMS.values().forEach(item -> event.accept(item.get()));
        }
    }
}
