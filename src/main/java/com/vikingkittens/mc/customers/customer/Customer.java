package com.vikingkittens.mc.customers.customer;

import com.mojang.logging.LogUtils;
import com.vikingkittens.mc.customers.Customers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
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
    private static final DeferredRegister<PoiType> pois = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, modid);
    private static final DeferredRegister<VillagerProfession> professions = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, modid);

    // -------------------- Registries --------------------
    public static void register(IEventBus modEventBus) {
        LOGGER.info("Registering components");

        entities.register(modEventBus);
        pois.register(modEventBus);
        professions.register(modEventBus);
    }
}
