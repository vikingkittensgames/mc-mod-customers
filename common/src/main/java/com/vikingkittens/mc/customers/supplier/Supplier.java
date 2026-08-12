package com.vikingkittens.mc.customers.supplier;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.VillagerProfession;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;

public class Supplier {
    private static final IRegistrationHelper REGISTRATIONS =
            CustomersServices.registration();

    public static void initialize() {}

    // -------------------- Entities --------------------
    public static final CustomersRegistryEntry<EntityType<?>, EntityType<SupplierVillagerEntity>>
            SUPPLIER_VILLAGER = REGISTRATIONS.register(
                    Registries.ENTITY_TYPE,
                    SupplierVillagerEntity.NAME,
            () -> EntityType.Builder.of(SupplierVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(SupplierVillagerEntity.NAME)
    );

    // -------------------- Professions --------------------
    public static final CustomersRegistryEntry<VillagerProfession, VillagerProfession>
            SUPPLIER_PROFESSION = REGISTRATIONS.register(
                    Registries.VILLAGER_PROFESSION,
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
