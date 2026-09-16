package com.vikingkittens.mc.customers.advancements.ftb;

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

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersLocationPredicate;
import com.vikingkittens.mc.customers.advancements.triggers.CustomersTriggerItemServed;
import com.vikingkittens.mc.customers.customer.CustomerInternalEvents;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerMode;

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
                        CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED
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
    }

    @Test
    void routesEachTaskKindToItsInternalEvent() {
        CustomerInternalEvents.ItemServed itemServed = itemServedEvent();
        CustomerInternalEvents.CustomerServed customerServed = customerServedEvent();
        CustomerInternalEvents.ShiftFinished shiftFinished = shiftFinishedEvent();

        assertTrue(CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED.handles(itemServed));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED.handles(itemServed));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.CUSTOMER_SERVED.handles(customerServed));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.handles(customerServed));
        assertTrue(CustomersFTBTasks.CustomersTaskKind.SHIFT_FINISHED.handles(shiftFinished));
        assertFalse(CustomersFTBTasks.CustomersTaskKind.ITEM_SERVED.handles(shiftFinished));
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
}
