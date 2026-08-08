package com.vikingkittens.mc.customers.client.appearance.mca;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.mca.McaCustomersVillagerMod;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;

@EventBusSubscriber(modid = Customers.MODID, value = Dist.CLIENT)
public final class McaCustomersVillagerClientEvents {
    private McaCustomersVillagerClientEvents() {}

    @SubscribeEvent
    public static void registerAppearances(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        if (!McaCustomersVillagerMod.isLoaded()) {
            return;
        }
        CustomersVillagerClientAppearances.register(
                CustomersVillagerAppearances.MCA,
                McaCustomersVillagerClientAppearance::new
        );
    }
}
