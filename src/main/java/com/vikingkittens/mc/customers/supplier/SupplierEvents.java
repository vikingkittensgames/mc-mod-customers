package com.vikingkittens.mc.customers.supplier;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.Customer;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = Customers.MODID)
public class SupplierEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Add the default Villager attributes to the supplier
        event.put(Supplier.SUPPLIER_VILLAGER.get(), Villager.createAttributes().build());
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof SupplierVillagerEntity supplier) {
                Entity.RemovalReason reason = supplier.getRemovalReason();
                if (reason == Entity.RemovalReason.CHANGED_DIMENSION) {
                    supplier.discard();
                }
            }
        }
    }
}
