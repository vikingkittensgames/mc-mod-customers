package com.vikingkittens.mc.customers.advancements.triggers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersTriggerCounterPlacedTest {
    private static final BlockPos SPAWNER_POSITION = new BlockPos(4, 64, 8);
    private static final BlockPos COUNTER_POSITION = new BlockPos(10, 65, 8);

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void matchesSpawnerCounterLocationModeAndBlock() {
        CustomersTriggerCounterPlaced.Instance instance = new CustomersTriggerCounterPlaced.Instance(
                Optional.empty(),
                Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, SPAWNER_POSITION)),
                Optional.of(CustomerSpawnerMode.LUNCH),
                Optional.of(new CustomersLocationPredicate(Level.OVERWORLD, COUNTER_POSITION)),
                Optional.of(ResourceLocation.parse("minecraft:oak_planks"))
        );

        assertTrue(instance.matchesEvent(event()));
        assertFalse(CustomersTriggerCounterPlaced.SCHEMA.with(
                        instance,
                        CustomersTriggerCounterPlaced.SCHEMA.property("counter_block").orElseThrow(),
                        Optional.of(ResourceLocation.parse("minecraft:bricks"))
                )
                .matchesEvent(event()));
    }

    @Test
    void schemaExposesEveryEventConditionExceptTheAdvancementPlayerPredicate() {
        assertEquals(
                List.of("player", "spawner_location", "spawner_mode", "counter_location", "counter_block"),
                CustomersTriggerCounterPlaced.SCHEMA.properties().stream()
                        .map(CustomersTriggerSchema.Property::serializedName)
                        .toList()
        );
        assertFalse(CustomersTriggerCounterPlaced.SCHEMA.property("player").orElseThrow().taskEditable());
        assertTrue(CustomersTriggerCounterPlaced.SCHEMA.properties().stream()
                .filter(property -> !property.serializedName().equals("player"))
                .allMatch(CustomersTriggerSchema.Property::taskEditable));
    }

    @Test
    void decodesDataDrivenCounterPlacementConditions() {
        String json = """
                {
                  "spawner_mode": "lunch",
                  "counter_block": "minecraft:oak_planks"
                }
                """;

        CustomersTriggerCounterPlaced.Instance instance = CustomersTriggerCounterPlaced.Instance.CODEC
                .parse(
                        RegistryOps.create(
                                JsonOps.INSTANCE,
                                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                        ),
                        JsonParser.parseString(json)
                )
                .getOrThrow();

        assertTrue(instance.matchesEvent(event()));
    }

    private static CustomerInternalEvents.CounterBlockPlaced event() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return new CustomerInternalEvents.CounterBlockPlaced(
                level,
                SPAWNER_POSITION,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                COUNTER_POSITION,
                Blocks.OAK_PLANKS.defaultBlockState()
        );
    }
}
