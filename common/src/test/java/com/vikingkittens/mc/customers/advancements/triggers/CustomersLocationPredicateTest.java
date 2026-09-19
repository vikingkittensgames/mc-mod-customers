package com.vikingkittens.mc.customers.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

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

        Level overworld = level(Level.OVERWORLD);
        Level nether = level(Level.NETHER);

        assertTrue(predicate.matches(overworld, SHOP_POSITION));
        assertFalse(predicate.matches(overworld, SHOP_POSITION.above()));
        assertFalse(predicate.matches(nether, SHOP_POSITION));
        assertFalse(predicate.matches(overworld, null));
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

    private static Level level(net.minecraft.resources.ResourceKey<Level> dimension) {
        Level level = mock(Level.class);
        when(level.dimension()).thenReturn(dimension);
        return level;
    }
}
