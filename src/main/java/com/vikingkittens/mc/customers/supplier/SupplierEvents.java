package com.vikingkittens.mc.customers.supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

@EventBusSubscriber(modid = Customers.MODID)
public class SupplierEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

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
}
