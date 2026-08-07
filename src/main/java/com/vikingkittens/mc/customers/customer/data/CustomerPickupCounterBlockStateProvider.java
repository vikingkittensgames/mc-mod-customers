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
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;

public class CustomerPickupCounterBlockStateProvider implements DataProvider {
    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider blockModels;
    private final PackOutput.PathProvider itemModels;
    private final PackOutput.PathProvider itemDefinitions;

    public CustomerPickupCounterBlockStateProvider(PackOutput output) {
        blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        itemDefinitions = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> generated = new ArrayList<>();
        CustomerPickupCounter.BLOCKS.forEach((variant, block) -> {
            String name = CustomerPickupCounter.getBlockName(variant);
            generated.add(save(output, blockStates, name, createBlockState(name)));
            generated.add(save(output, blockModels, name, createBlockModel(variant)));
            generated.add(save(output, itemModels, name, createItemModel(name)));
            generated.add(save(output, itemDefinitions, name, createItemDefinition(name)));
        });
        return CompletableFuture.allOf(generated.toArray(CompletableFuture[]::new));
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

    private static JsonObject createBlockState(String name) {
        JsonObject model = new JsonObject();
        model.addProperty("model", Customers.MODID + ":block/" + name);
        JsonObject variants = new JsonObject();
        variants.add("", model);
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private static JsonObject createBlockModel(CustomerPickupCounterVariant variant) {
        JsonObject root = new JsonObject();
        root.addProperty("render_type", "minecraft:translucent");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", variant.sideTexture().toString());
        textures.addProperty("base", variant.sideTexture().toString());
        textures.addProperty(
                "top_overlay",
                Customers.MODID + ":block/customer_pickup_counter_top_overlay"
        );
        root.add("textures", textures);
        JsonArray elements = new JsonArray();
        elements.add(createBaseElement());
        elements.add(createOverlayElement());
        root.add("elements", elements);
        return root;
    }

    private static JsonObject createBaseElement() {
        JsonObject element = createElement(1.0D);
        JsonObject faces = new JsonObject();
        for (String direction : List.of("down", "up", "north", "south", "west", "east")) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#base");
            faces.add(direction, face);
        }
        element.add("faces", faces);
        return element;
    }

    private static JsonObject createOverlayElement() {
        JsonObject element = createElement(1.001D);
        JsonObject face = new JsonObject();
        face.addProperty("texture", "#top_overlay");
        JsonObject faces = new JsonObject();
        faces.add("up", face);
        element.add("faces", faces);
        return element;
    }

    private static JsonObject createElement(double height) {
        JsonObject element = new JsonObject();
        JsonArray from = new JsonArray();
        from.add(0);
        from.add(0);
        from.add(0);
        JsonArray to = new JsonArray();
        to.add(16);
        to.add(height);
        to.add(16);
        element.add("from", from);
        element.add("to", to);
        return element;
    }

    private static JsonObject createItemModel(String name) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", Customers.MODID + ":block/" + name);
        JsonObject gui = new JsonObject();
        gui.add("rotation", vector(45.0D, 225.0D, 0.0D));
        gui.add("translation", vector(0.0D, 3.0D, 0.0D));
        gui.add("scale", vector(0.8D, 0.8D, 0.8D));
        JsonObject display = new JsonObject();
        display.add("gui", gui);
        root.add("display", display);
        return root;
    }

    private static JsonObject createItemDefinition(String name) {
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", Customers.MODID + ":item/" + name);
        JsonObject root = new JsonObject();
        root.add("model", model);
        return root;
    }

    private static JsonArray vector(double x, double y, double z) {
        JsonArray vector = new JsonArray();
        vector.add(x);
        vector.add(y);
        vector.add(z);
        return vector;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
