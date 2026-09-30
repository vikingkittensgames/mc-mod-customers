package com.vikingkittens.mc.customers.customer;

import com.google.common.collect.ImmutableSet;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import com.vikingkittens.mc.customers.Customers;

/***
 * Customer main feature class that covers registering the pieces
 * of this feature as part of the mod.
 *
 * It also provides type references for the registered pieces like
 * blocks, items, entities, etc to be used by this feature or other
 * features.
 */
public class Customer {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.ENTITY_TYPE);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Customers.MODID, Registries.VILLAGER_PROFESSION);

    public static void initialize() {
        ENTITY_TYPES.register();
        PROFESSIONS.register();
    }

    // -------------------- Entities --------------------
    public static final RegistrySupplier<EntityType<CustomerVillagerEntity>> CUSTOMER_VILLAGER =
            ENTITY_TYPES.register(CustomerVillagerEntity.NAME,
                    () -> EntityType.Builder.of(CustomerVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Customers.MODID, CustomerVillagerEntity.NAME)
                    ))
            );
    public static final RegistrySupplier<EntityType<CustomerSeatEntity>> CUSTOMER_SEAT = CustomerSeat.ENTITY_TYPE;


    // -------------------- Professions --------------------
    public static final RegistrySupplier<VillagerProfession> CUSTOMER_PROFESSION =
            PROFESSIONS.register("customer", () -> new VillagerProfession(
                    Component.literal("customer"),
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
            );
    public static final RegistrySupplier<VillagerProfession> CUSTOMER_CASUAL_PROFESSION =
            PROFESSIONS.register("customer_casual", () -> new VillagerProfession(
                    Component.literal("customer_casual"),
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
            );
    public static final RegistrySupplier<VillagerProfession> CUSTOMER_IMPATIENT_PROFESSION =
            PROFESSIONS.register("customer_impatient", () -> new VillagerProfession(
                    Component.literal("customer_impatient"),
                    holder -> false,
                    holder -> false,
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
            );
}
