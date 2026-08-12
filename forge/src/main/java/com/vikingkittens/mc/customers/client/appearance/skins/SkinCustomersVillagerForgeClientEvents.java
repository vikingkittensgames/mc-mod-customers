package com.vikingkittens.mc.customers.client.appearance.skins;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerClientAppearances;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class SkinCustomersVillagerForgeClientEvents {
    private SkinCustomersVillagerForgeClientEvents() {}

    @SubscribeEvent
    public static void registerAppearances(EntityRenderersEvent.RegisterRenderers event) {
        CustomersVillagerClientAppearances.registerProvider(SkinCustomersVillagerClientAppearanceProvider.INSTANCE);
    }
}
