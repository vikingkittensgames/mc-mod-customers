package com.vikingkittens.mc.customers.customer.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

public final class CustomerOverlayBlockModelProvider implements DataProvider {
    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider blockModels;
    private final PackOutput.PathProvider itemModels;
    private final PackOutput.PathProvider itemDefinitions;

    public CustomerOverlayBlockModelProvider(PackOutput output) {
        blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        itemDefinitions = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> generated = new ArrayList<>();
        addStandaloneModels(output, generated);
        for (CustomerOverlayBlockVariant variant : CustomerOverlayBlockVariants.ALL) {
            addPickupCounter(output, generated, variant);
            addPaymentBox(output, generated, variant);
        }
        return CompletableFuture.allOf(generated.toArray(CompletableFuture[]::new));
    }

    private void addStandaloneModels(CachedOutput output, List<CompletableFuture<?>> generated) {
        generated.add(save(output, blockStates, "customer_leaderboard", leaderboardBlockState()));
        generated.add(save(output, itemModels, "customer_leaderboard", leaderboardItemModel()));
        generated.add(save(output, itemDefinitions, "customer_leaderboard", itemDefinition("customer_leaderboard")));
        generated.add(save(output, itemDefinitions, "customer_spawner_block", itemDefinition("customer_spawner_block")));
        generated.add(save(output, itemDefinitions, "supplier_spawner_block", itemDefinition("supplier_spawner_block")));
    }

    private void addPickupCounter(
            CachedOutput output,
            List<CompletableFuture<?>> generated,
            CustomerOverlayBlockVariant variant
    ) {
        String name = CustomerPickupCounter.getBlockName(variant);
        generated.add(save(output, blockStates, name, blockState(name, false)));
        generated.add(save(output, blockModels, name, pickupCounterModel(variant)));
        generated.add(save(output, itemModels, name, itemModel(name, 3.0F, 0.8F)));
        generated.add(save(output, itemDefinitions, name, itemDefinition(name)));
    }

    private void addPaymentBox(
            CachedOutput output,
            List<CompletableFuture<?>> generated,
            CustomerOverlayBlockVariant variant
    ) {
        String name = CustomerPaymentBox.getBlockName(variant);
        generated.add(save(output, blockStates, name, blockState(name, true)));
        generated.add(save(output, blockModels, name, paymentBoxModel(variant)));
        generated.add(save(output, itemModels, name, itemModel(name, 1.0F, 0.6F)));
        generated.add(save(output, itemDefinitions, name, itemDefinition(name)));
    }

    private static CompletableFuture<?> save(
            CachedOutput output,
            PackOutput.PathProvider paths,
            String name,
            JsonObject json
    ) {
        return DataProvider.saveStable(
                output,
                json,
                paths.json(Identifier.fromNamespaceAndPath(Customers.MODID, name))
        );
    }

    private static JsonObject blockState(String name, boolean facing) {
        JsonObject variants = new JsonObject();
        if (facing) {
            variants.add("facing=north", modelReference(name, 0));
            variants.add("facing=east", modelReference(name, 90));
            variants.add("facing=south", modelReference(name, 180));
            variants.add("facing=west", modelReference(name, 270));
        } else {
            variants.add("", modelReference(name, 0));
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private static JsonObject leaderboardBlockState() {
        JsonObject variants = new JsonObject();
        variants.add("facing=north", blockModelReference("leaderboard_block", 0));
        variants.add("facing=east", blockModelReference("leaderboard_block", 90));
        variants.add("facing=south", blockModelReference("leaderboard_block", 180));
        variants.add("facing=west", blockModelReference("leaderboard_block", 270));
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private static JsonObject modelReference(String name, int rotation) {
        return blockModelReference(name, rotation);
    }

    private static JsonObject blockModelReference(String name, int rotation) {
        JsonObject model = new JsonObject();
        model.addProperty("model", Customers.MODID + ":block/" + name);
        if (rotation != 0) {
            model.addProperty("y", rotation);
        }
        return model;
    }

    private static JsonObject pickupCounterModel(CustomerOverlayBlockVariant variant) {
        JsonObject root = modelRoot(variant);
        root.getAsJsonObject("textures").addProperty(
                "top_overlay",
                Customers.MODID + ":block/customer_pickup_counter_top_overlay"
        );
        JsonArray elements = new JsonArray();
        elements.add(element(
                vector(0.0F, 0.0F, 0.0F),
                vector(16.0F, 1.0F, 16.0F),
                allFaces("#base", false)
        ));
        JsonObject overlayFaces = new JsonObject();
        overlayFaces.add("up", face("#top_overlay", false));
        elements.add(element(
                vector(-0.01F, -0.01F, -0.01F),
                vector(16.01F, 1.01F, 16.01F),
                overlayFaces
        ));
        root.add("elements", elements);
        return root;
    }

    private static JsonObject paymentBoxModel(CustomerOverlayBlockVariant variant) {
        JsonObject root = modelRoot(variant);
        JsonObject textures = root.getAsJsonObject("textures");
        textures.addProperty("top", Customers.MODID + ":block/customer_payment_box_block_overlay_top");
        textures.addProperty("bottom", Customers.MODID + ":block/customer_payment_box_block_overlay_bottom");
        textures.addProperty("side", Customers.MODID + ":block/customer_payment_box_block_overlay_side");
        textures.addProperty("front", Customers.MODID + ":block/customer_payment_box_block_overlay_front");
        JsonArray elements = new JsonArray();
        elements.add(element(
                vector(1.0F, 0.0F, 1.0F),
                vector(15.0F, 14.0F, 15.0F),
                allFaces("#base", false)
        ));
        JsonObject overlayFaces = new JsonObject();
        overlayFaces.add("up", face("#top", true));
        overlayFaces.add("down", face("#bottom", true));
        overlayFaces.add("north", face("#front", true));
        overlayFaces.add("south", face("#side", true));
        overlayFaces.add("east", face("#side", true));
        overlayFaces.add("west", face("#side", true));
        elements.add(element(
                vector(0.99F, -0.01F, 0.99F),
                vector(15.01F, 14.01F, 15.01F),
                overlayFaces
        ));
        root.add("elements", elements);
        return root;
    }

    private static JsonObject modelRoot(CustomerOverlayBlockVariant variant) {
        JsonObject root = new JsonObject();
        root.addProperty("render_type", "minecraft:translucent");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", variant.baseTexture().toString());
        textures.addProperty("base", variant.baseTexture().toString());
        root.add("textures", textures);
        return root;
    }

    private static JsonObject element(JsonArray from, JsonArray to, JsonObject faces) {
        JsonObject element = new JsonObject();
        element.add("from", from);
        element.add("to", to);
        element.add("faces", faces);
        return element;
    }

    private static JsonObject allFaces(String texture, boolean includeUvs) {
        JsonObject faces = new JsonObject();
        for (String direction : List.of("down", "up", "north", "south", "west", "east")) {
            faces.add(direction, face(texture, includeUvs));
        }
        return faces;
    }

    private static JsonObject face(String texture, boolean includeUvs) {
        JsonObject face = new JsonObject();
        face.addProperty("texture", texture);
        if (includeUvs) {
            face.add("uv", vector(0.0F, 0.0F, 16.0F, 16.0F));
        }
        return face;
    }

    private static JsonObject itemModel(String name, float translationY, float scale) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", Customers.MODID + ":block/" + name);
        JsonObject display = new JsonObject();
        display.add("gui", transform(vector(45.0F, 225.0F, 0.0F), vector(0.0F, translationY, 0.0F), scale));
        display.add("ground", transform(null, vector(0.0F, 3.0F, 0.0F), 0.5F));
        root.add("display", display);
        return root;
    }

    private static JsonObject leaderboardItemModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", Customers.MODID + ":block/leaderboard_block");
        JsonObject display = new JsonObject();
        for (String context : List.of(
                "firstperson_lefthand",
                "firstperson_righthand",
                "fixed",
                "ground",
                "gui",
                "thirdperson_lefthand",
                "thirdperson_righthand"
        )) {
            JsonObject transform = new JsonObject();
            transform.add("rotation", vector(0.0F, 180.0F, 0.0F));
            display.add(context, transform);
        }
        root.add("display", display);
        return root;
    }

    private static JsonObject transform(JsonArray rotation, JsonArray translation, float scale) {
        JsonObject transform = new JsonObject();
        if (rotation != null) {
            transform.add("rotation", rotation);
        }
        transform.add("translation", translation);
        transform.add("scale", vector(scale, scale, scale));
        return transform;
    }

    private static JsonObject itemDefinition(String name) {
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", Customers.MODID + ":item/" + name);
        JsonObject root = new JsonObject();
        root.add("model", model);
        return root;
    }

    private static JsonArray vector(float... values) {
        JsonArray vector = new JsonArray();
        for (float value : values) {
            vector.add(value);
        }
        return vector;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
