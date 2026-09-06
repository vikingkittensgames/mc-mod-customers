package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.vikingkittens.mc.customers.compatability.CustomersRegistryEntry;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class CustomerSeat {
    public static final CustomersRegistryEntry<EntityType<?>, EntityType<CustomerSeatEntity>> ENTITY_TYPE =
            CustomersServices.registration().register(
                    Registries.ENTITY_TYPE,
                    CustomerSeatEntity.NAME,
                    () -> EntityType.Builder.of(CustomerSeatEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .noSave()
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build(ResourceKey.create(
                                    Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath("customers", CustomerSeatEntity.NAME)
                            )));

    private CustomerSeat() {}

    public static void initialize() {}
}
