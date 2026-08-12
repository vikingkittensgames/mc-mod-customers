package com.vikingkittens.mc.customers.client.supplier;

import net.minecraft.client.renderer.entity.VillagerRenderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.appearance.CustomersVillagerAppearanceEntityRenderer;
import com.vikingkittens.mc.customers.supplier.Supplier;

@Mod.EventBusSubscriber(
        modid = Customers.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class SupplierForgeClientEvents {
    private SupplierForgeClientEvents() {}

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                Supplier.SUPPLIER_VILLAGER.get(),
                context -> new CustomersVillagerAppearanceEntityRenderer<>(
                        context,
                        new VillagerRenderer(context)
                )
        );
    }
}
