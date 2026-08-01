package com.vikingkittens.mc.customers.supplier;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

public class Supplier {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String modid = Customers.MODID;

    private static final DeferredRegister<EntityType<?>> entities = DeferredRegister.create(Registries.ENTITY_TYPE, modid);
    private static final DeferredRegister<VillagerProfession> professions = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, modid);

    public static void register(IEventBus modEventBus) {
        LOGGER.info("Registering components");

        entities.register(modEventBus);
        professions.register(modEventBus);
    }

    public static final DeferredHolder<EntityType<?>, EntityType<SupplierVillagerEntity>> SUPPLIER_VILLAGER = entities.register(SupplierVillagerEntity.NAME,
            key -> EntityType.Builder.of(SupplierVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // Villager size
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );

    public static final DeferredHolder<VillagerProfession, VillagerProfession> SUPPLIER_PROFESSION = professions.register("supplier",
            () -> new VillagerProfession(
                    Component.literal("supplier"),
                    holder -> false, // No POI
                    holder -> false, // No POI
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
}
