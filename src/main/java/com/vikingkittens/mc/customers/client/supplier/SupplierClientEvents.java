package com.vikingkittens.mc.customers.client.supplier;

import net.minecraft.client.renderer.entity.VillagerRenderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.supplier.Supplier;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public class SupplierClientEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Supplier.SUPPLIER_VILLAGER.get(), VillagerRenderer::new);
    }
}
