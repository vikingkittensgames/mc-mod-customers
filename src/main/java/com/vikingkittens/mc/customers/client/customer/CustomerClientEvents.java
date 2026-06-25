package com.vikingkittens.mc.customers.client.customer;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.Customer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class CustomerClientEvents {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Customer.CUSTOMER_VILLAGER.get(), VillagerRenderer::new);
    }
}

