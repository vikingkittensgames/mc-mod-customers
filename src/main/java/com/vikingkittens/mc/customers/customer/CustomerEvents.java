package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = Customers.MODID)
public class CustomerEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();

        AtomicInteger numRemovedCustomers = new AtomicInteger();
        for (ServerLevel serverLevel : server.getAllLevels()) {
            serverLevel.getAllEntities().forEach(entity -> {
                if (entity instanceof CustomerVillagerEntity) {
                    entity.discard();
                    numRemovedCustomers.getAndIncrement();
                }
            });
        }
        if (numRemovedCustomers.get() > 0) {
            LOGGER.info("Removed " + numRemovedCustomers.get() + " customers.");
        }
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Add the default Villager attributes to the customer
        event.put(Customer.CUSTOMER_VILLAGER.get(), Villager.createAttributes().build());
    }
}
