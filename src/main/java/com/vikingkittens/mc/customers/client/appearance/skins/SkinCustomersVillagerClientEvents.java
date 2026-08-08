package com.vikingkittens.mc.customers.client.appearance.skins;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public final class SkinCustomersVillagerClientEvents {
    private SkinCustomersVillagerClientEvents() {
    }

    @SubscribeEvent
    public static void registerAppearances(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        CustomersVillagerClientAppearances.registerProvider(
                SkinCustomersVillagerClientAppearanceProvider.INSTANCE
        );
    }
}
