package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPickupCounterGeneratedModelTest {
    private static final Path GENERATED =
            Path.of("src/generated/resources");

    /** Initializes Minecraft item registries used by pickup-counter variants. */
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

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
            Path itemDefinition = GENERATED.resolve(
                    "assets/customers/items/" + name + ".json"
            );

            assertTrue(Files.exists(blockState));
            assertTrue(Files.exists(blockModel));
            assertTrue(Files.exists(itemModel));
            assertTrue(Files.exists(itemDefinition));

            JsonObject model = JsonParser.parseString(
                    Files.readString(blockModel)
            ).getAsJsonObject();
            JsonObject item = JsonParser.parseString(
                    Files.readString(itemModel)
            ).getAsJsonObject();
            JsonObject itemDefinitionModel = JsonParser.parseString(
                    Files.readString(itemDefinition)
            ).getAsJsonObject().getAsJsonObject("model");
            assertEquals(
                    "minecraft:model",
                    itemDefinitionModel.get("type").getAsString()
            );
            assertEquals(
                    "customers:item/" + name,
                    itemDefinitionModel.get("model").getAsString()
            );
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
            assertTrue(!model.has("loader"));
            assertEquals(
                    "minecraft:translucent",
                    model.get("render_type").getAsString()
            );
            assertEquals(
                    variant.sideTexture().toString(),
                    model.getAsJsonObject("textures")
                            .get("base")
                            .getAsString()
            );
            assertEquals(2, model.getAsJsonArray("elements").size());
            JsonObject baseFaces = model.getAsJsonArray("elements")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("faces");
            assertEquals(6, baseFaces.size());
            assertEquals(
                    "#base",
                    baseFaces.getAsJsonObject("north")
                            .get("texture")
                            .getAsString()
            );
            JsonObject overlayFaces = model
                    .getAsJsonArray("elements")
                    .get(1)
                    .getAsJsonObject()
                    .getAsJsonObject("faces");
            assertEquals(1, overlayFaces.size());
            assertEquals(
                    "#top_overlay",
                    overlayFaces.getAsJsonObject("up")
                            .get("texture")
                            .getAsString()
            );
            assertEquals(
                    "customers:block/customer_pickup_counter_top_overlay",
                    model.getAsJsonObject("textures")
                            .get("top_overlay")
                            .getAsString()
            );
        }
    }
}
