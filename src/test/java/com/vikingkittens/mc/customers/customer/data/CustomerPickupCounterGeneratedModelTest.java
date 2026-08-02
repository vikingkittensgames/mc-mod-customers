package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPickupCounterGeneratedModelTest {
    private static final Path GENERATED =
            Path.of("src/generated/resources");

    @Test
    void generatesLayeredModelsForEveryVariant() throws IOException {
        for (CustomerPickupCounterVariant variant
                : CustomerPickupCounterVariants.ALL) {
            String name = CustomerPickupCounter.getBlockName(variant);
            Path blockState = GENERATED.resolve(
                    "assets/customers/blockstates/" + name + ".json"
            );
            Path blockModel = GENERATED.resolve(
                    "assets/customers/models/block/" + name + ".json"
            );
            Path itemModel = GENERATED.resolve(
                    "assets/customers/models/item/" + name + ".json"
            );

            assertTrue(Files.exists(blockState));
            assertTrue(Files.exists(blockModel));
            assertTrue(Files.exists(itemModel));

            JsonObject model = JsonParser.parseString(
                    Files.readString(blockModel)
            ).getAsJsonObject();
            JsonObject item = JsonParser.parseString(
                    Files.readString(itemModel)
            ).getAsJsonObject();
            JsonObject gui = item.getAsJsonObject("display")
                    .getAsJsonObject("gui");
            assertEquals(
                    45.0F,
                    gui.getAsJsonArray("rotation").get(0).getAsFloat()
            );
            assertEquals(
                    225.0F,
                    gui.getAsJsonArray("rotation").get(1).getAsFloat()
            );
            assertEquals(
                    0.8F,
                    gui.getAsJsonArray("scale").get(0).getAsFloat()
            );
            assertEquals(
                    3.0F,
                    gui.getAsJsonArray("translation").get(1).getAsFloat()
            );
            assertEquals(
                    "neoforge:composite",
                    model.get("loader").getAsString()
            );
            assertEquals(
                    "minecraft:translucent",
                    model.get("render_type").getAsString()
            );
            JsonObject children = model.getAsJsonObject("children");
            assertEquals(
                    "minecraft:solid",
                    children.getAsJsonObject("base")
                            .get("render_type")
                            .getAsString()
            );
            assertEquals(
                    "minecraft:translucent",
                    children.getAsJsonObject("overlay")
                            .get("render_type")
                            .getAsString()
            );
            assertEquals(
                    variant.sideTexture().toString(),
                    children.getAsJsonObject("base")
                            .getAsJsonObject("textures")
                            .get("base")
                            .getAsString()
            );
            JsonObject overlay = children
                    .getAsJsonObject("overlay")
                    .getAsJsonObject("textures");
            assertEquals(
                    "customers:block/customer_pickup_counter_top_overlay",
                    overlay.get("top_overlay").getAsString()
            );
            assertEquals(
                    "customers:block/"
                            + "customer_pickup_counter_bottom_side_overlay",
                    overlay.get("bottom_side_overlay").getAsString()
            );
        }
    }
}
