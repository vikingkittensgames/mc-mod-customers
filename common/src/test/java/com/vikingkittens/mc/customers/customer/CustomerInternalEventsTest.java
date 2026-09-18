package com.vikingkittens.mc.customers.customer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomerInternalEventsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void testItemServedCreation() {
        ItemStack servedItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        CustomerInternalEvents.ItemServed event = new CustomerInternalEvents.ItemServed(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer"),
                servedItem,
                costItem,
                false
        );

        servedItem.setCount(1);
        costItem.setCount(1);
        event.servedItem().setCount(64);
        event.costItem().setCount(64);

        assertEquals(3, event.servedItem().getCount());
        assertEquals(2, event.costItem().getCount());
        assertFalse(event.isPetItem());
    }

    @Test
    void testCustomerServedCreation() {
        ServerLevel level = mock(ServerLevel.class);
        UUID playerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        ResourceLocation profession = ResourceLocation.parse("customers:customer");
        ItemStack servedItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        CustomerInternalEvents.CustomerServed event = new CustomerInternalEvents.CustomerServed(
                level,
                null,
                CustomerSpawnerMode.LUNCH,
                playerId,
                customerId,
                profession,
                servedItem,
                costItem,
                false
        );

        assertInstanceOf(CustomerInternalEvents.ServedEvent.class, event);
        assertEquals(level, event.level());
        assertEquals(CustomerSpawnerMode.LUNCH, event.spawnerMode());
        assertEquals(playerId, event.playerId());
        assertEquals(customerId, event.customerId());
        assertEquals(profession, event.customerProfession());
        assertTrue(ItemStack.isSameItemSameComponents(servedItem, event.servedItem()));
        assertEquals(servedItem.getCount(), event.servedItem().getCount());
        assertTrue(ItemStack.isSameItemSameComponents(costItem, event.costItem()));
        assertEquals(costItem.getCount(), event.costItem().getCount());
        assertFalse(event.isPetItem());
    }

    @Test
    void testShiftFinishedCreation() {
        CustomerInternalEvents.ShiftFinished event = new CustomerInternalEvents.ShiftFinished(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                1,
                0.75F,
                3,
                2,
                1,
                10,
                Map.of(
                        UUID.randomUUID(), 3,
                        UUID.randomUUID(), 2
                ),
                Map.of(
                        UUID.randomUUID(), 5,
                        UUID.randomUUID(), 3
                ),
                0,
                0
        );
    }

    @Test
    void leaderboardRequiresMultiplePlayersToHaveALeader() {
        UUID playerId = UUID.randomUUID();
        CustomerInternalEvents.LeaderboardChanged event = new CustomerInternalEvents.LeaderboardChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                BlockPos.ZERO,
                0,
                Map.of(playerId, 0.75F),
                Map.of(playerId, 0.5F)
        );

        assertNull(event.leader());
        assertNull(event.previousLeader());
        assertFalse(event.leaderChanged());
    }

    @Test
    void leaderboardAffectedPlayersIncludeAnUnchangedLeader() {
        UUID leader = UUID.randomUUID();
        UUID newPlayer = UUID.randomUUID();
        CustomerInternalEvents.LeaderboardChanged event = new CustomerInternalEvents.LeaderboardChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                BlockPos.ZERO,
                0,
                Map.of(leader, 0.75F, newPlayer, 0.5F),
                Map.of(leader, 0.75F)
        );

        assertEquals(Set.of(newPlayer), event.changedPlayerIds());
        assertEquals(Set.of(leader, newPlayer), event.affectedPlayerIds());
        assertEquals(leader, event.leader());
    }

    @Test
    void customerSpawnerConfigurationIsCountedCopiedAndComparedByValue() {
        ItemStack sellItem = new ItemStack(Items.APPLE, 3);
        ItemStack costItem = new ItemStack(Items.EMERALD, 2);
        LinkedHashMap<String, ItemStack> petFoods = new LinkedHashMap<>();
        petFoods.put("minecraft:cat", new ItemStack(Items.COD));
        CustomerInternalEvents.CustomerSpawnerConfigChanged event = customerSpawnerConfigChanged(
                List.of(List.of(sellItem, ItemStack.EMPTY)),
                List.of(costItem),
                petFoods
        );
        CustomerInternalEvents.CustomerSpawnerConfigChanged equivalent = customerSpawnerConfigChanged(
                List.of(List.of(sellItem.copy(), ItemStack.EMPTY)),
                List.of(costItem.copy()),
                new LinkedHashMap<>(petFoods)
        );

        sellItem.setCount(1);
        costItem.setCount(1);
        petFoods.get("minecraft:cat").setCount(12);

        assertEquals(1, event.numSellItems());
        assertEquals(1, event.numCostItems());
        assertEquals(2, event.numAppearances());
        assertEquals(3, event.rowSellItems().getFirst().getFirst().getCount());
        assertEquals(2, event.rowCostItems().getFirst().getCount());
        assertTrue(event.hasSameConfiguration(equivalent));

        equivalent.rowSellItems().getFirst().getFirst().setCount(64);
        assertTrue(event.hasSameConfiguration(equivalent));
    }

    @Test
    void customerSpawnerConfigurationDetectsAChangedValue() {
        CustomerInternalEvents.CustomerSpawnerConfigChanged event = customerSpawnerConfigChanged(
                List.of(List.of(new ItemStack(Items.APPLE))),
                List.of(new ItemStack(Items.EMERALD)),
                new LinkedHashMap<>()
        );
        CustomerInternalEvents.CustomerSpawnerConfigChanged changed = new CustomerInternalEvents.CustomerSpawnerConfigChanged(
                event.level(),
                event.spawnerPosition(),
                event.spawnerMode(),
                event.playerId(),
                event.activeLevel(),
                event.rowSellItems(),
                event.rowCostItems(),
                event.requiredStars(),
                event.maxCustomers() + 1,
                event.petPercentage(),
                event.petTypesCustomized(),
                event.enabledPetTypes(),
                event.petFoods(),
                event.autoCost(),
                event.enabledAppearances()
        );

        assertFalse(event.hasSameConfiguration(changed));
    }

    private static CustomerInternalEvents.CustomerSpawnerConfigChanged customerSpawnerConfigChanged(
            List<List<ItemStack>> sellItems,
            List<ItemStack> costItems,
            LinkedHashMap<String, ItemStack> petFoods
    ) {
        return new CustomerInternalEvents.CustomerSpawnerConfigChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                2,
                sellItems,
                costItems,
                4.5F,
                12,
                0.25F,
                true,
                new LinkedHashSet<>(List.of("minecraft:cat")),
                petFoods,
                false,
                List.of("default", "customers:first", "customers:second")
        );
    }
}
