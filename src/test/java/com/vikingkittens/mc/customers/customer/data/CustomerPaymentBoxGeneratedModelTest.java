package com.vikingkittens.mc.customers.customer.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPaymentBoxGeneratedModelTest {
    private static final Path GENERATED =
            Path.of("src/generated/resources");

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void generatesFacingLayeredModelsForEveryVariant()
            throws IOException {
        for (CustomerOverlayBlockVariant variant
                : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPaymentBox.getBlockName(variant);
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
                    "assets/customers/models/block/"
                            + name
                            + "_overlay.json"
            );
            Path itemBaseModel = GENERATED.resolve(
                    "assets/customers/models/item/"
                            + name
                            + "_base.json"
            );
            Path itemOverlayModel = GENERATED.resolve(
                    "assets/customers/models/item/"
                            + name
                            + "_overlay.json"
            );
            Path itemModel = GENERATED.resolve(
                    "assets/customers/models/item/" + name + ".json"
            );

            assertTrue(Files.exists(blockState));
            assertTrue(Files.exists(blockModel));
            assertTrue(Files.exists(baseModel));
            assertTrue(Files.exists(overlayModel));
            assertTrue(Files.exists(itemBaseModel));
            assertTrue(Files.exists(itemOverlayModel));
            assertTrue(Files.exists(itemModel));

            JsonObject states = read(blockState)
                    .getAsJsonObject("variants");
            assertEquals(4, states.size());
            assertFacing(states, "north", name, 0);
            assertFacing(states, "east", name, 90);
            assertFacing(states, "south", name, 180);
            assertFacing(states, "west", name, 270);

            JsonObject model = read(blockModel);
            assertEquals(
                    "neoforge:composite",
                    model.get("loader").getAsString()
            );
            assertEquals(2, model.getAsJsonObject("children").size());

            JsonObject base = read(baseModel);
            assertEquals(
                    variant.baseTexture().toString(),
                    base.getAsJsonObject("textures")
                            .get("base")
                            .getAsString()
            );
            assertTrue(!base.has("render_type"));
            JsonObject baseElement = base.getAsJsonArray("elements")
                    .get(0)
                    .getAsJsonObject();
            assertVector(baseElement, "from", 1.0F, 0.0F, 1.0F);
            assertVector(baseElement, "to", 15.0F, 14.0F, 15.0F);
            assertEquals(
                    6,
                    baseElement.getAsJsonObject("faces").size()
            );

            JsonObject overlay = read(overlayModel);
            assertEquals(
                    "minecraft:translucent",
                    overlay.get("render_type").getAsString()
            );
            JsonObject textures = overlay.getAsJsonObject("textures");
            assertEquals(
                    "customers:block/customer_payment_box_block_overlay_top",
                    textures.get("top").getAsString()
            );
            assertEquals(
                    "customers:block/customer_payment_box_block_overlay_bottom",
                    textures.get("bottom").getAsString()
            );
            assertEquals(
                    "customers:block/customer_payment_box_block_overlay_side",
                    textures.get("side").getAsString()
            );
            assertEquals(
                    "customers:block/customer_payment_box_block_overlay_front",
                    textures.get("front").getAsString()
            );
            JsonObject faces = overlay.getAsJsonArray("elements")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("faces");
            assertEquals("#top", faceTexture(faces, "up"));
            assertEquals("#bottom", faceTexture(faces, "down"));
            assertEquals("#front", faceTexture(faces, "north"));
            assertEquals("#side", faceTexture(faces, "south"));
            assertEquals("#side", faceTexture(faces, "east"));
            assertEquals("#side", faceTexture(faces, "west"));

            JsonObject item = read(itemModel);
            assertEquals(
                    "neoforge:composite",
                    item.get("loader").getAsString()
            );
            assertEquals(
                    2,
                    item.getAsJsonObject("children").size()
            );
            assertEquals(
                    "minecraft:translucent",
                    read(itemBaseModel)
                            .get("render_type")
                            .getAsString()
            );
            assertEquals(
                    "minecraft:translucent",
                    read(itemOverlayModel)
                            .get("render_type")
                            .getAsString()
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
                    1.0F,
                    gui.getAsJsonArray("translation").get(1).getAsFloat()
            );
            assertEquals(
                    0.6F,
                    gui.getAsJsonArray("scale").get(0).getAsFloat()
            );
        }
    }

    private static void assertFacing(
            JsonObject states,
            String facing,
            String name,
            int rotation
    ) {
        JsonObject state = states.getAsJsonObject(
                "facing=" + facing
        );
        assertEquals(
                "customers:block/" + name,
                state.get("model").getAsString()
        );
        if (rotation == 0) {
            assertTrue(!state.has("y"));
        } else {
            assertEquals(rotation, state.get("y").getAsInt());
        }
    }

    private static void assertVector(
            JsonObject element,
            String key,
            float x,
            float y,
            float z
    ) {
        assertEquals(x, element.getAsJsonArray(key).get(0).getAsFloat());
        assertEquals(y, element.getAsJsonArray(key).get(1).getAsFloat());
        assertEquals(z, element.getAsJsonArray(key).get(2).getAsFloat());
    }

    private static String faceTexture(
            JsonObject faces,
            String direction
    ) {
        return faces.getAsJsonObject(direction)
                .get("texture")
                .getAsString();
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path))
                .getAsJsonObject();
    }
}
