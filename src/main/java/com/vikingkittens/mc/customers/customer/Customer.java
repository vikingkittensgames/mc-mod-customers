package com.vikingkittens.mc.customers.customer;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

/***
 * Customer main feature class that covers registering the pieces
 * of this feature as part of the mod.
 *
 * It also provides type references for the registered pieces like
 * blocks, items, entities, etc to be used by this feature or other
 * features.
 */
public class Customer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String modid = Customers.MODID;

    // -------------------- Registries --------------------
    private static final DeferredRegister<EntityType<?>> entities = DeferredRegister.create(Registries.ENTITY_TYPE, modid);
    private static final DeferredRegister<VillagerProfession> professions = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, modid);

    // -------------------- Registries --------------------
    public static void register(IEventBus modEventBus) {
        LOGGER.info("Registering components");

        entities.register(modEventBus);
        professions.register(modEventBus);
    }

    // -------------------- Entities --------------------
    public static final DeferredHolder<EntityType<?>, EntityType<CustomerVillagerEntity>> CUSTOMER_VILLAGER = entities.register(CustomerVillagerEntity.NAME,
            () -> EntityType.Builder.of(CustomerVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // Villager size
                    .build(CustomerVillagerEntity.NAME)
    );

    // -------------------- Professions --------------------
    public static final DeferredHolder<VillagerProfession, VillagerProfession> CUSTOMER_PROFESSION = professions.register("customer",
            () -> new VillagerProfession(
                    "customer",
                    holder -> false, // No POI
                    holder -> false, // No POI
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
}
