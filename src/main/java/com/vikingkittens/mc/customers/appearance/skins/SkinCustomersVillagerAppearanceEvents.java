package com.vikingkittens.mc.customers.appearance.skins;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import com.vikingkittens.mc.customers.Customers;

@EventBusSubscriber(modid = Customers.MODID)
public final class SkinCustomersVillagerAppearanceEvents {
    private SkinCustomersVillagerAppearanceEvents() {}

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SkinCustomersVillagerRegistries.SKINS, SkinCustomersVillagerDefinition.CODEC, SkinCustomersVillagerDefinition.CODEC);
        event.dataPackRegistry(SkinCustomersVillagerRegistries.SKIN_PACKS, SkinPackCustomersVillagerDefinition.CODEC, SkinPackCustomersVillagerDefinition.CODEC);
    }
}
