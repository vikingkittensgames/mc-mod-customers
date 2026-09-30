package com.vikingkittens.mc.customers.customer;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.vikingkittens.mc.customers.Customers;

public final class CustomerSeat {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Customers.MODID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<CustomerSeatEntity>> ENTITY_TYPE =
            ENTITY_TYPES.register(CustomerSeatEntity.NAME,
                    () -> EntityType.Builder.of(CustomerSeatEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build(CustomerSeatEntity.NAME));

    private CustomerSeat() {}

    public static void initialize() {
        ENTITY_TYPES.register();
    }
}
