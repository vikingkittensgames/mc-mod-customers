package com.vikingkittens.mc.customers.customer;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.VillagerProfession;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.IRegistrationHelper;

/***
 * Customer main feature class that covers registering the pieces
 * of this feature as part of the mod.
 *
 * It also provides type references for the registered pieces like
 * blocks, items, entities, etc to be used by this feature or other
 * features.
 */
public class Customer {
    private static final IRegistrationHelper REGISTRATIONS =
            CustomersServices.registration();

    public static void initialize() {}

    // -------------------- Entities --------------------
    public static final CustomersRegistryEntry<EntityType<?>, EntityType<CustomerVillagerEntity>>
            CUSTOMER_VILLAGER = REGISTRATIONS.register(
                    Registries.ENTITY_TYPE,
                    CustomerVillagerEntity.NAME,
            () -> EntityType.Builder.of(CustomerVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(CustomerVillagerEntity.NAME)
    );
    public static final CustomersRegistryEntry<EntityType<?>, EntityType<CustomerSeatEntity>>
            CUSTOMER_SEAT = CustomerSeat.ENTITY_TYPE;


    // -------------------- Professions --------------------
    public static final CustomersRegistryEntry<VillagerProfession, VillagerProfession>
            CUSTOMER_PROFESSION = REGISTRATIONS.register(
                    Registries.VILLAGER_PROFESSION,
                    "customer",
            () -> new VillagerProfession(
                    "customer",
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
    public static final CustomersRegistryEntry<VillagerProfession, VillagerProfession>
            CUSTOMER_CASUAL_PROFESSION = REGISTRATIONS.register(
                    Registries.VILLAGER_PROFESSION,
                    "customer_casual",
            () -> new VillagerProfession(
                    "customer_casual",
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
    public static final CustomersRegistryEntry<VillagerProfession, VillagerProfession>
            CUSTOMER_IMPATIENT_PROFESSION = REGISTRATIONS.register(
                    Registries.VILLAGER_PROFESSION,
                    "customer_impatient",
            () -> new VillagerProfession(
                    "customer_impatient",
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
}
