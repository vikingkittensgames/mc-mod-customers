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
            Path baseModel = GENERATED.resolve(
                    "assets/customers/models/block/" + name + "_base.json"
            );
            Path overlayModel = GENERATED.resolve(
                    "assets/customers/models/block/" + name
                            + "_overlay.json"
            );
            Path itemModel = GENERATED.resolve(
                    "assets/customers/models/item/" + name + ".json"
            );

            assertTrue(Files.exists(blockState));
            assertTrue(Files.exists(blockModel));
            assertTrue(Files.exists(baseModel));
            assertTrue(Files.exists(overlayModel));
            assertTrue(Files.exists(itemModel));

            JsonObject model = JsonParser.parseString(
                    Files.readString(blockModel)
            ).getAsJsonObject();
            JsonObject base = JsonParser.parseString(
                    Files.readString(baseModel)
            ).getAsJsonObject();
            JsonObject overlay = JsonParser.parseString(
                    Files.readString(overlayModel)
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
            assertEquals(2, model.getAsJsonObject("children").size());
            assertEquals(
                    variant.sideTexture().toString(),
                    base.getAsJsonObject("textures")
                            .get("base")
                            .getAsString()
            );
            assertTrue(!base.has("render_type"));
            assertEquals(1, base.getAsJsonArray("elements").size());
            JsonObject baseFaces = base.getAsJsonArray("elements")
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
            assertEquals(
                    "minecraft:translucent",
                    overlay.get("render_type").getAsString()
            );
            JsonObject overlayFaces = overlay
                    .getAsJsonArray("elements")
                    .get(0)
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
                    overlay.getAsJsonObject("textures")
                            .get("top_overlay")
                            .getAsString()
            );
        }
    }
}
