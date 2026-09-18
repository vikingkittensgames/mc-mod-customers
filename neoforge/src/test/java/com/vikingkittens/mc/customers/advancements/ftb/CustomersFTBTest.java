package com.vikingkittens.mc.customers.advancements.ftb;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersLocationPredicate;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCounterPlaced;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerCustomerSpawnerChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerLeaderboardChanged;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerSupplierSpawnerChanged;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;
import com.vikingkittens.mc.customers.supplier.SupplierInternalEvents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersFTBTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void enablesIntegrationOnlyWhenFtbQuestsIsLoaded() {
        assertTrue(CustomersFTB.isEnabled("ftbquests"::equals));
        assertFalse(CustomersFTB.isEnabled(modId -> false));
    }

    @Test
    void exposesTaskKindsForEachSupportedInternalEvent() {
        assertEquals(
                List.of(
                        CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED,
                        CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED,
                        CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED,
                        CustomersFTBTasks.CustomersTaskKind.LEADERBOARD_CHANGED,
                        CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SPAWNER_CHANGED,
                        CustomersFTBTasks.CustomersTaskKind.SUPPLIER_SPAWNER_CHANGED,
                        CustomersFTBTasks.CustomersTaskKind.COUNTER_PLACED
                ),
                List.of(CustomersFTBTasks.CustomersTaskKind.values())
        );
        assertEquals(
                "customers:textures/item/advancement_icon_customer.png",
                CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED.iconResource()
        );
        assertEquals(
                "customers:textures/item/advancement_icon_star.png",
                CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.iconResource()
        );
        assertEquals(
                "customers:textures/gui/leaderboard_icon.png",
                CustomersFTBTasks.CustomersTaskKind.LEADERBOARD_CHANGED.iconResource()
        );
        assertEquals(
                "customers:textures/block/customer_spawner_block_top.png",
                CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SPAWNER_CHANGED.iconResource()
        );
        assertEquals(
                "customers:textures/block/supplier_spawner_block_top.png",
                CustomersFTBTasks.CustomersTaskKind.SUPPLIER_SPAWNER_CHANGED.iconResource()
        );
        assertEquals(
                "customers:textures/item/advancement_icon_table.png",
                CustomersFTBTasks.CustomersTaskKind.COUNTER_PLACED.iconResource()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.item_served",
                CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED.displayNameKey()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.customer_served",
                CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED.displayNameKey()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.shift_finished",
                CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.displayNameKey()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.leaderboard_changed",
                CustomersFTBTasks.CustomersTaskKind.LEADERBOARD_CHANGED.displayNameKey()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.customer_spawner_changed",
                CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SPAWNER_CHANGED.displayNameKey()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.supplier_spawner_changed",
                CustomersFTBTasks.CustomersTaskKind.SUPPLIER_SPAWNER_CHANGED.displayNameKey()
        );
        assertEquals(
                "ftbquests.task.customers.customers_task.counter_placed",
                CustomersFTBTasks.CustomersTaskKind.COUNTER_PLACED.displayNameKey()
        );
    }

    @Test
    void routesEachTaskKindToItsInternalEvent() {
        CustomerInternalEvents.ItemServed itemServed = itemServedEvent();
        CustomerInternalEvents.CustomerServed customerServed = customerServedEvent();
        CustomerInternalEvents.ShiftFinished shiftFinished = shiftFinishedEvent();
        CustomerInternalEvents.LeaderboardChanged leaderboardChanged = leaderboardChangedEvent();
        CustomerInternalEvents.CustomerSpawnerConfigChanged customerSpawnerChanged = customerSpawnerChangedEvent();
        SupplierInternalEvents.SupplierSpawnerConfigChanged supplierSpawnerChanged = supplierSpawnerChangedEvent();
        CustomerInternalEvents.CounterBlockPlaced counterPlaced = counterPlacedEvent();

        assertTrue(CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED.handles(itemServed));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED.handles(itemServed));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED.handles(customerServed));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.handles(customerServed));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.handles(shiftFinished));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED.handles(shiftFinished));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.LEADERBOARD_CHANGED.handles(leaderboardChanged));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.handles(leaderboardChanged));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SPAWNER_CHANGED.handles(customerSpawnerChanged));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.SUPPLIER_SPAWNER_CHANGED.handles(customerSpawnerChanged));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.SUPPLIER_SPAWNER_CHANGED.handles(supplierSpawnerChanged));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SPAWNER_CHANGED.handles(supplierSpawnerChanged));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.COUNTER_PLACED.handles(counterPlaced));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED.handles(counterPlaced));
    }

    @Test
    void preservesTriggerSchemaOrderInFtbConfig() {
        assertEquals(
                List.of(
                        "spawner_location_enabled",
                        "spawner_location_dimension",
                        "spawner_location_x",
                        "spawner_location_y",
                        "spawner_location_z",
                        "spawner_mode",
                        "customer_profession",
                        "served_item",
                        "served_item_tag",
                        "served_count_min",
                        "served_count_max",
                        "cost_item",
                        "cost_item_tag",
                        "cost_count_min",
                        "cost_count_max",
                        "is_pet_item"
                ),
                CustomersFTBTriggerSchema.configPropertyOrder(CustomersTriggerItemServed.SCHEMA)
        );
    }

    @Test
    void exposesEveryLeaderboardTriggerPropertyInSchemaOrder() {
        assertEquals(
                List.of(
                        "spawner_location_enabled",
                        "spawner_location_dimension",
                        "spawner_location_x",
                        "spawner_location_y",
                        "spawner_location_z",
                        "spawner_mode",
                        "leaderboard_location_enabled",
                        "leaderboard_location_dimension",
                        "leaderboard_location_x",
                        "leaderboard_location_y",
                        "leaderboard_location_z",
                        "level_min",
                        "level_max",
                        "previous_score_min",
                        "previous_score_max",
                        "new_score_min",
                        "new_score_max",
                        "is_leader",
                        "was_leader",
                        "leader_changed"
                ),
                CustomersFTBTriggerSchema.configPropertyOrder(CustomersTriggerLeaderboardChanged.SCHEMA)
        );
    }

    @Test
    void exposesEverySpawnerConfigurationPropertyInSchemaOrder() {
        assertEquals(
                List.of(
                        "spawner_location_enabled",
                        "spawner_location_dimension",
                        "spawner_location_x",
                        "spawner_location_y",
                        "spawner_location_z",
                        "spawner_mode",
                        "level_min",
                        "level_max",
                        "required_stars_min",
                        "required_stars_max",
                        "max_customers_min",
                        "max_customers_max",
                        "pet_percentage_min",
                        "pet_percentage_max",
                        "pet_types_customized",
                        "auto_cost",
                        "num_sell_items_min",
                        "num_sell_items_max",
                        "num_cost_items_min",
                        "num_cost_items_max",
                        "num_appearances_min",
                        "num_appearances_max"
                ),
                CustomersFTBTriggerSchema.configPropertyOrder(CustomersTriggerCustomerSpawnerChanged.SCHEMA)
        );
        assertEquals(
                List.of(
                        "spawner_location_enabled",
                        "spawner_location_dimension",
                        "spawner_location_x",
                        "spawner_location_y",
                        "spawner_location_z",
                        "auto_cost",
                        "num_sell_items_min",
                        "num_sell_items_max",
                        "num_cost_items_min",
                        "num_cost_items_max",
                        "num_appearances_min",
                        "num_appearances_max"
                ),
                CustomersFTBTriggerSchema.configPropertyOrder(CustomersTriggerSupplierSpawnerChanged.SCHEMA)
        );
    }

    @Test
    void exposesEveryCounterPlacementPropertyInSchemaOrder() {
        assertEquals(
                List.of(
                        "spawner_location_enabled",
                        "spawner_location_dimension",
                        "spawner_location_x",
                        "spawner_location_y",
                        "spawner_location_z",
                        "spawner_mode",
                        "counter_location_enabled",
                        "counter_location_dimension",
                        "counter_location_x",
                        "counter_location_y",
                        "counter_location_z",
                        "counter_block"
                ),
                CustomersFTBTriggerSchema.configPropertyOrder(CustomersTriggerCounterPlaced.SCHEMA)
        );
    }

    @Test
    void createsOptionalLocationFromFtbFields() {
        assertEquals(
                Optional.of(Optional.empty()),
                CustomersFTBTriggerSchema.location(false, "not a resource location", 0, 0, 0)
        );

        Optional<CustomersLocationPredicate> location = CustomersFTBTriggerSchema
                .location(true, "minecraft:the_nether", -120, 0, 350)
                .orElseThrow();

        assertEquals(Level.NETHER, location.orElseThrow().dimension());
        assertEquals(new BlockPos(-120, 0, 350), location.orElseThrow().position());
        assertTrue(CustomersFTBTriggerSchema
                .location(true, "not a resource location", 0, 64, 0)
                .isEmpty());
    }

    @Test
    void createsItemPredicateFromNativeItemSelection() {
        Optional<ItemPredicate> predicate = CustomersFTBTriggerSchema
                .itemPredicate(new ItemStack(Items.APPLE), "")
                .orElseThrow();

        assertTrue(predicate.orElseThrow().test(new ItemStack(Items.APPLE)));
        assertFalse(predicate.orElseThrow().test(new ItemStack(Items.CARROT)));
        assertEquals(
                Items.APPLE,
                CustomersFTBTriggerSchema.selectedItem(predicate).getItem()
        );
        assertEquals("", CustomersFTBTriggerSchema.selectedTag(predicate));
    }

    @Test
    void createsTagPredicateWithPrecedenceOverItemSelection() {
        TagKey<Item> logs = TagKey.create(Registries.ITEM, ResourceLocation.parse("minecraft:logs"));
        Optional<ItemPredicate> predicate = CustomersFTBTriggerSchema
                .itemPredicate(new ItemStack(Items.APPLE), "#minecraft:logs")
                .orElseThrow();

        assertEquals(
                Optional.of(logs),
                predicate.orElseThrow().items().orElseThrow().unwrapKey()
        );
        assertTrue(CustomersFTBTriggerSchema.selectedItem(predicate).isEmpty());
        assertEquals("#minecraft:logs", CustomersFTBTriggerSchema.selectedTag(predicate));
    }

    @Test
    void supportsEmptyAndRejectsInvalidItemTagSelections() {
        assertEquals(
                Optional.of(Optional.empty()),
                CustomersFTBTriggerSchema.itemPredicate(ItemStack.EMPTY, "")
        );
        assertTrue(CustomersFTBTriggerSchema
                .itemPredicate(ItemStack.EMPTY, "not a resource location")
                .isEmpty());
    }

    private static CustomerInternalEvents.ItemServed itemServedEvent() {
        return new CustomerInternalEvents.ItemServed(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer"),
                new ItemStack(Items.APPLE),
                new ItemStack(Items.EMERALD),
                false
        );
    }

    private static CustomerInternalEvents.CustomerServed customerServedEvent() {
        return new CustomerInternalEvents.CustomerServed(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.parse("customers:customer"),
                new ItemStack(Items.APPLE),
                new ItemStack(Items.EMERALD),
                false
        );
    }

    private static CustomerInternalEvents.ShiftFinished shiftFinishedEvent() {
        return new CustomerInternalEvents.ShiftFinished(
                mock(ServerLevel.class),
                null,
                CustomerSpawnerMode.LUNCH,
                1,
                0.75F,
                10,
                8,
                2,
                20,
                Map.of(UUID.randomUUID(), 3),
                Map.of(UUID.randomUUID(), 5),
                0,
                0
        );
    }

    private static CustomerInternalEvents.LeaderboardChanged leaderboardChangedEvent() {
        return new CustomerInternalEvents.LeaderboardChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                new BlockPos(8, 64, 8),
                1,
                Map.of(UUID.randomUUID(), 0.75F),
                Map.of()
        );
    }

    private static CustomerInternalEvents.CustomerSpawnerConfigChanged customerSpawnerChangedEvent() {
        return new CustomerInternalEvents.CustomerSpawnerConfigChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                1,
                List.of(List.of(new ItemStack(Items.APPLE))),
                List.of(new ItemStack(Items.EMERALD)),
                3.0F,
                10,
                0.25F,
                true,
                new LinkedHashSet<>(List.of("minecraft:cat")),
                new LinkedHashMap<>(),
                false,
                List.of("customers:default")
        );
    }

    private static SupplierInternalEvents.SupplierSpawnerConfigChanged supplierSpawnerChangedEvent() {
        return new SupplierInternalEvents.SupplierSpawnerConfigChanged(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                UUID.randomUUID(),
                List.of(new SupplierInternalEvents.Offer(
                        new ItemStack(Items.APPLE),
                        new ItemStack(Items.EMERALD)
                )),
                false,
                List.of("customers:default")
        );
    }

    private static CustomerInternalEvents.CounterBlockPlaced counterPlacedEvent() {
        return new CustomerInternalEvents.CounterBlockPlaced(
                mock(ServerLevel.class),
                BlockPos.ZERO,
                CustomerSpawnerMode.LUNCH,
                UUID.randomUUID(),
                new BlockPos(1, 64, 1),
                Blocks.OAK_PLANKS.defaultBlockState()
        );
    }
}
