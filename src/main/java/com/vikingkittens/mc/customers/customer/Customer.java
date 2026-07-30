package com.vikingkittens.mc.customers.customer;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.special.CustomerWitchEntity;
import com.vikingkittens.mc.customers.customer.special.CustomerStrayEntity;
import com.vikingkittens.mc.customers.customer.special.CustomerSkeletonEntity;
import com.vikingkittens.mc.customers.customer.special.CustomerHuskEntity;
import com.vikingkittens.mc.customers.customer.special.CustomerDrownedEntity;
import com.vikingkittens.mc.customers.customer.special.CustomerZombieEntity;
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
            key -> EntityType.Builder.of(CustomerVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // Villager size
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<CustomerSeatEntity>> CUSTOMER_SEAT = entities.register(CustomerSeatEntity.NAME,
            key -> EntityType.Builder.of(CustomerSeatEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );


    public static final DeferredHolder<EntityType<?>, EntityType<CustomerZombieEntity>> CUSTOMER_ZOMBIE = entities.register(CustomerZombieEntity.NAME,
            key -> EntityType.Builder.of(CustomerZombieEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<CustomerSkeletonEntity>> CUSTOMER_SKELETON = entities.register(CustomerSkeletonEntity.NAME,
            key -> EntityType.Builder.of(CustomerSkeletonEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<CustomerWitchEntity>> CUSTOMER_WITCH = entities.register(CustomerWitchEntity.NAME,
            key -> EntityType.Builder.of(CustomerWitchEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<CustomerHuskEntity>> CUSTOMER_HUSK = entities.register(CustomerHuskEntity.NAME,
            key -> EntityType.Builder.of(CustomerHuskEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<CustomerDrownedEntity>> CUSTOMER_DROWNED = entities.register(CustomerDrownedEntity.NAME,
            key -> EntityType.Builder.of(CustomerDrownedEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );

    public static final DeferredHolder<EntityType<?>, EntityType<CustomerStrayEntity>> CUSTOMER_STRAY = entities.register(CustomerStrayEntity.NAME,
            key -> EntityType.Builder.of(CustomerStrayEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key))
    );
    // -------------------- Professions --------------------
    public static final DeferredHolder<VillagerProfession, VillagerProfession> CUSTOMER_PROFESSION = professions.register("customer",
            () -> new VillagerProfession(
                    Component.literal("customer"),
                    holder -> false, // No POI
                    holder -> false, // No POI
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
    public static final DeferredHolder<VillagerProfession, VillagerProfession> CUSTOMER_CASUAL_PROFESSION = professions.register("customer_casual",
            () -> new VillagerProfession(
                    Component.literal("customer_casual"),
                    holder -> false, // No POI
                    holder -> false, // No POI
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
    public static final DeferredHolder<VillagerProfession, VillagerProfession> CUSTOMER_IMPATIENT_PROFESSION = professions.register("customer_impatient",
            () -> new VillagerProfession(
                    Component.literal("customer_impatient"),
                    holder -> false, // No POI
                    holder -> false, // No POI
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    null
            )
    );
}


