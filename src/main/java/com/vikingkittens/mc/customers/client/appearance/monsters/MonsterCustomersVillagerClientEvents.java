package com.vikingkittens.mc.customers.client.appearance.monsters;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;

@EventBusSubscriber(
        modid = Customers.MODID,
        value = Dist.CLIENT
)
public final class MonsterCustomersVillagerClientEvents {
    private MonsterCustomersVillagerClientEvents() {}

    @SubscribeEvent
    public static void registerAppearances(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        CustomersVillagerClientAppearances.register(
                CustomersVillagerAppearances.MONSTERS,
                MonsterCustomersVillagerClientAppearance::new
        );
    }
}
