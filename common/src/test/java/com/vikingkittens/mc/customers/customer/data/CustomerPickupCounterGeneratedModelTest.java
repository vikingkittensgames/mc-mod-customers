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
    private static final Path GENERATED = Path.of("../neoforge/src/generated/resources");

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void generatesTranslucentLayeredModelsForEveryVariant() throws IOException {
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            String name = CustomerPickupCounter.getBlockName(variant);
            JsonObject blockState = read("assets/customers/blockstates/" + name + ".json");
            JsonObject blockModel = read("assets/customers/models/block/" + name + ".json");
            JsonObject itemModel = read("assets/customers/models/item/" + name + ".json");
            JsonObject itemDefinition = read("assets/customers/items/" + name + ".json");

            assertEquals(
                    "customers:block/" + name,
                    blockState.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString()
            );
            assertEquals("minecraft:translucent", blockModel.get("render_type").getAsString());
            assertEquals(variant.baseTexture().toString(), blockModel.getAsJsonObject("textures").get("base").getAsString());
            assertEquals(2, blockModel.getAsJsonArray("elements").size());
            assertEquals(
                    6,
                    blockModel.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces").size()
            );
            JsonObject overlay = blockModel.getAsJsonArray("elements").get(1).getAsJsonObject();
            assertEquals(1.01F, overlay.getAsJsonArray("to").get(1).getAsFloat());
            assertEquals(1, overlay.getAsJsonObject("faces").size());
            assertEquals(
                    "#top_overlay",
                    overlay.getAsJsonObject("faces").getAsJsonObject("up").get("texture").getAsString()
            );
            assertEquals("customers:block/" + name, itemModel.get("parent").getAsString());
            assertEquals(
                    0.8F,
                    itemModel.getAsJsonObject("display").getAsJsonObject("gui").getAsJsonArray("scale").get(0).getAsFloat()
            );
            assertEquals(
                    "customers:item/" + name,
                    itemDefinition.getAsJsonObject("model").get("model").getAsString()
            );
        }
    }

    private static JsonObject read(String relativePath) throws IOException {
        Path path = GENERATED.resolve(relativePath);
        assertTrue(Files.exists(path));
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
