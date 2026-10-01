package com.vikingkittens.mc.customers.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.world.level.storage.loot.LootDataManager;

import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersCriterionTriggerTest {
    @Test
    void decodesAndEncodesThroughTheAdvancementCriterionAdapter() {
        CustomersTriggerItemServed trigger = new CustomersTriggerItemServed();
        JsonObject json = JsonParser.parseString("""
                {
                  "served_count": { "min": 3 }
                }
                """).getAsJsonObject();
        DeserializationContext context = new DeserializationContext(
                ResourceLocationCUtils.create("customers", "adapter_test"),
                mock(LootDataManager.class)
        );

        CustomersCriterionTriggerInstance<CustomersTriggerItemServed.Instance> instance =
                trigger.createInstance(json, context);
        JsonObject encoded = instance.serializeToJson(SerializationContext.INSTANCE);

        assertEquals(ResourceLocationCUtils.create("customers", "item_served"), trigger.getId());
        assertTrue(instance.value().servedCount().orElseThrow().matches(3));
        assertEquals(3, encoded.getAsJsonObject("served_count").get("min").getAsInt());
    }
}
