package com.vikingkittens.mc.customers.supplier;

import com.google.common.collect.ImmutableSet;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.VillagerProfession;

import com.vikingkittens.mc.customers.Customers;

public class Supplier {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.ENTITY_TYPE);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Customers.MODID, Registries.VILLAGER_PROFESSION);

    public static void initialize() {
        ENTITY_TYPES.register();
        PROFESSIONS.register();
    }

    // -------------------- Entities --------------------
    public static final RegistrySupplier<EntityType<SupplierVillagerEntity>> SUPPLIER_VILLAGER = ENTITY_TYPES.register(
                    SupplierVillagerEntity.NAME,
            () -> EntityType.Builder.of(SupplierVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(SupplierVillagerEntity.NAME)
    );

    // -------------------- Professions --------------------
    public static final RegistrySupplier<VillagerProfession> SUPPLIER_PROFESSION = PROFESSIONS.register(
                    "supplier",
            () -> new VillagerProfession(
                    "supplier",
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
}
