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
    private static final Path GENERATED = Path.of("../neoforge/src/generated/resources");

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void generatesFacingTranslucentModelsForEveryVariant() throws IOException {
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPaymentBox.getBlockName(variant);
            JsonObject states = read("assets/customers/blockstates/" + name + ".json").getAsJsonObject("variants");
            JsonObject blockModel = read("assets/customers/models/block/" + name + ".json");
            JsonObject itemModel = read("assets/customers/models/item/" + name + ".json");
            JsonObject itemDefinition = read("assets/customers/items/" + name + ".json");

            assertEquals(4, states.size());
            assertFacing(states, "north", name, 0);
            assertFacing(states, "east", name, 90);
            assertFacing(states, "south", name, 180);
            assertFacing(states, "west", name, 270);
            assertEquals("minecraft:translucent", blockModel.get("render_type").getAsString());
            assertEquals(variant.baseTexture().toString(), blockModel.getAsJsonObject("textures").get("base").getAsString());
            assertEquals(2, blockModel.getAsJsonArray("elements").size());
            assertVector(blockModel.getAsJsonArray("elements").get(0).getAsJsonObject(), "from", 1.0F, 0.0F, 1.0F);
            assertVector(blockModel.getAsJsonArray("elements").get(0).getAsJsonObject(), "to", 15.0F, 14.0F, 15.0F);
            JsonObject overlayFaces = blockModel.getAsJsonArray("elements")
                    .get(1).getAsJsonObject().getAsJsonObject("faces");
            assertEquals("#top", texture(overlayFaces, "up"));
            assertEquals("#bottom", texture(overlayFaces, "down"));
            assertEquals("#front", texture(overlayFaces, "north"));
            assertEquals("#side", texture(overlayFaces, "south"));
            assertEquals("customers:block/" + name, itemModel.get("parent").getAsString());
            assertEquals(
                    0.6F,
                    itemModel.getAsJsonObject("display").getAsJsonObject("gui").getAsJsonArray("scale").get(0).getAsFloat()
            );
            assertEquals(
                    "customers:item/" + name,
                    itemDefinition.getAsJsonObject("model").get("model").getAsString()
            );
        }
    }

    private static void assertFacing(JsonObject states, String direction, String name, int rotation) {
        JsonObject state = states.getAsJsonObject("facing=" + direction);
        assertEquals("customers:block/" + name, state.get("model").getAsString());
        if (rotation == 0) {
            assertTrue(!state.has("y"));
        } else {
            assertEquals(rotation, state.get("y").getAsInt());
        }
    }

    private static void assertVector(JsonObject element, String name, float x, float y, float z) {
        assertEquals(x, element.getAsJsonArray(name).get(0).getAsFloat());
        assertEquals(y, element.getAsJsonArray(name).get(1).getAsFloat());
        assertEquals(z, element.getAsJsonArray(name).get(2).getAsFloat());
    }

    private static String texture(JsonObject faces, String direction) {
        return faces.getAsJsonObject(direction).get("texture").getAsString();
    }

    private static JsonObject read(String relativePath) throws IOException {
        Path path = GENERATED.resolve(relativePath);
        assertTrue(Files.exists(path));
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
