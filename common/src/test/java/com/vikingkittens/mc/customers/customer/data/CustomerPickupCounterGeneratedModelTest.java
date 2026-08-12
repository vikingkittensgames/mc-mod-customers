package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPickupCounterGeneratedModelTest {
    private static final Map<Path, String> GENERATED = Map.of(
            Path.of("../forge/src/generated/resources"), "forge:composite",
            Path.of("../neoforge/src/generated/resources"), "neoforge:composite"
    );

    /** Initializes Minecraft item registries used by pickup-counter variants. */
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void generatesLayeredModelsForEveryVariant() throws IOException {
        for (Map.Entry<Path, String> generation : GENERATED.entrySet()) {
        for (CustomerOverlayBlockVariant variant
                : CustomerOverlayBlockVariants.ALL) {
            Path generated = generation.getKey();
            String name = CustomerPickupCounter.getBlockName(variant);
            Path blockState = generated.resolve(
                    "assets/customers/blockstates/" + name + ".json"
            );
            Path blockModel = generated.resolve(
                    "assets/customers/models/block/" + name + ".json"
            );
            Path baseModel = generated.resolve(
                    "assets/customers/models/block/" + name + "_base.json"
            );
            Path overlayModel = generated.resolve(
                    "assets/customers/models/block/" + name
                            + "_overlay.json"
            );
            Path itemModel = generated.resolve(
                    "assets/customers/models/item/" + name + ".json"
            );
            Path itemBaseModel = generated.resolve(
                    "assets/customers/models/item/"
                            + name
                            + "_base.json"
            );
            Path itemOverlayModel = generated.resolve(
                    "assets/customers/models/item/"
                            + name
                            + "_overlay.json"
            );

            assertTrue(Files.exists(blockState));
            assertTrue(Files.exists(blockModel));
            assertTrue(Files.exists(baseModel));
            assertTrue(Files.exists(overlayModel));
            assertTrue(Files.exists(itemModel));
            assertTrue(Files.exists(itemBaseModel));
            assertTrue(Files.exists(itemOverlayModel));

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
            JsonObject itemBase = JsonParser.parseString(
                    Files.readString(itemBaseModel)
            ).getAsJsonObject();
            JsonObject itemOverlay = JsonParser.parseString(
                    Files.readString(itemOverlayModel)
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
                    generation.getValue(),
                    item.get("loader").getAsString()
            );
            assertEquals(2, item.getAsJsonObject("children").size());
            assertEquals(
                    "minecraft:translucent",
                    itemBase.get("render_type").getAsString()
            );
            assertEquals(
                    "minecraft:translucent",
                    itemOverlay.get("render_type").getAsString()
            );
            assertEquals(
                    generation.getValue(),
                    model.get("loader").getAsString()
            );
            assertEquals(2, model.getAsJsonObject("children").size());
            assertEquals(
                    variant.baseTexture().toString(),
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
            assertEquals(
                    1.01F,
                    overlay.getAsJsonArray("elements")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonArray("to")
                            .get(1)
                            .getAsFloat()
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
}
