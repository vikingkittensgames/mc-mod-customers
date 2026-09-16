package com.vikingkittens.mc.customers.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersLocationPredicateTest {
    private static final BlockPos SHOP_POSITION = new BlockPos(-120, 64, 350);

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesDimensionAndSpawnerPositionTogether() {
        CustomersLocationPredicate predicate = new CustomersLocationPredicate(Level.OVERWORLD, SHOP_POSITION);

        assertTrue(predicate.matches(event(Level.OVERWORLD, SHOP_POSITION)));
        assertFalse(predicate.matches(event(Level.OVERWORLD, SHOP_POSITION.above())));
        assertFalse(predicate.matches(event(Level.NETHER, SHOP_POSITION)));
        assertFalse(predicate.matches(event(Level.OVERWORLD, null)));
    }

    @Test
    void decodesDimensionAndSignedBlockPosition() {
        JsonObject json = JsonParser.parseString("""
                {
                  "dimension": "minecraft:overworld",
                  "position": [-120, 64, 350]
                }
                """).getAsJsonObject();

        CustomersLocationPredicate predicate = CustomersLocationPredicate.CODEC
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow();

        assertEquals(Level.OVERWORLD, predicate.dimension());
        assertEquals(SHOP_POSITION, predicate.position());
    }

    private static CustomerInternalEvents.CustomerEvent event(
            net.minecraft.resources.ResourceKey<Level> dimension,
            BlockPos position
    ) {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(dimension);
        return new CustomerInternalEvents.CustomerEvent(level, position, null);
    }
}
