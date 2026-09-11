package com.vikingkittens.mc.customers.economy;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

final class EconomyJsonData {
    private EconomyJsonData() {}

    static List<EconomyItemCostDefinition> load(
            ResourceManager resourceManager,
            String datapackDirectory,
            List<Path> configSources,
            Logger logger
    ) {
        List<EconomyItemCostDefinition> definitions = new ArrayList<>();
        loadDatapacks(resourceManager, datapackDirectory, definitions, logger);
        for (Path configSource : configSources) {
            loadConfigSource(configSource, definitions, logger);
        }
        return List.copyOf(definitions);
    }

    static List<EconomyItemCostDefinition> loadConfigSources(List<Path> sources, Logger logger) {
        List<EconomyItemCostDefinition> definitions = new ArrayList<>();
        for (Path source : sources) {
            loadConfigSource(source, definitions, logger);
        }
        return List.copyOf(definitions);
    }

    private static void loadDatapacks(
            ResourceManager resourceManager,
            String directory,
            List<EconomyItemCostDefinition> definitions,
            Logger logger
    ) {
        Map<String, Integer> packOrder = new HashMap<>();
        AtomicInteger index = new AtomicInteger();
        resourceManager.listPacks().forEach(pack -> packOrder.put(pack.packId(), index.getAndIncrement()));

        resourceManager.listResourceStacks(directory, id -> id.getPath().endsWith(".json"))
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .stream()
                        .map(resource -> new EconomyResource(entry.getKey(), resource)))
                .sorted(Comparator.comparingInt((EconomyResource resource) ->
                                packOrder.getOrDefault(resource.resource().sourcePackId(), -1))
                        .thenComparing(resource -> resource.id().toString()))
                .forEach(resource -> loadResource(resource, definitions, logger));
    }

    private static void loadResource(
            EconomyResource resource,
            List<EconomyItemCostDefinition> definitions,
            Logger logger
    ) {
        try (Reader reader = resource.resource().openAsReader()) {
            applyFile(reader, resource.id().toString(), definitions);
        } catch (IOException | RuntimeException exception) {
            logger.warn("Unable to read Customers economy data from {}", resource.id(), exception);
        }
    }

    private static void loadConfigSource(
            Path source,
            List<EconomyItemCostDefinition> definitions,
            Logger logger
    ) {
        if (Files.isRegularFile(source)) {
            loadFile(source, definitions, logger);
        } else if (Files.isDirectory(source)) {
            loadDirectory(source, definitions, logger);
        }
    }

    private static void loadDirectory(
            Path directory,
            List<EconomyItemCostDefinition> definitions,
            Logger logger
    ) {
        try (Stream<Path> paths = Files.list(directory)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> loadFile(path, definitions, logger));
        } catch (IOException exception) {
            logger.warn("Unable to read Customers economy item costs from {}", directory, exception);
        }
    }

    private static void loadFile(
            Path file,
            List<EconomyItemCostDefinition> definitions,
            Logger logger
    ) {
        try (Reader reader = Files.newBufferedReader(file)) {
            applyFile(reader, file.toString(), definitions);
        } catch (IOException | RuntimeException exception) {
            logger.warn("Unable to read Customers economy data from {}", file, exception);
        }
    }

    private static void applyFile(
            Reader reader,
            String source,
            List<EconomyItemCostDefinition> accumulated
    ) {
        EconomyFile file = parseFile(reader, source);
        if (file.replace()) {
            accumulated.clear();
        }
        accumulated.addAll(file.values());
    }

    private static EconomyFile parseFile(Reader reader, String source) {
        JsonElement root = JsonParser.parseReader(reader);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException(source + ": root value must be an object");
        }
        JsonObject object = root.getAsJsonObject();
        boolean replace = false;
        if (object.has("replace")) {
            if (!object.get("replace").isJsonPrimitive()
                    || !object.getAsJsonPrimitive("replace").isBoolean()) {
                throw new IllegalArgumentException(source + ": replace must be a boolean");
            }
            replace = object.get("replace").getAsBoolean();
        }
        if (!object.has("values") || !object.get("values").isJsonArray()) {
            throw new IllegalArgumentException(source + ": values must be an array");
        }
        JsonArray array = object.getAsJsonArray("values");
        List<EconomyItemCostDefinition> definitions = new ArrayList<>(array.size());
        for (int index = 0; index < array.size(); index++) {
            definitions.add(parseDefinition(array.get(index), source, index));
        }
        return new EconomyFile(replace, List.copyOf(definitions));
    }

    private static EconomyItemCostDefinition parseDefinition(JsonElement element, String source, int index) {
        if (!element.isJsonObject()) {
            throw invalid(source, index, "entry must be an object");
        }
        JsonObject object = element.getAsJsonObject();
        boolean hasItem = object.has("item");
        boolean hasTag = object.has("tag");
        if (hasItem == hasTag) {
            throw invalid(source, index, "entry must have exactly one of item or tag");
        }
        ResourceLocation itemId = hasItem ? parseId(object, "item", source, index) : null;
        ResourceLocation tagId = hasTag ? parseId(object, "tag", source, index) : null;
        ResourceLocation costItemId = parseId(object, "costItem", source, index);
        int itemCount = positiveCount(object, "itemCount", source, index);
        int costCount = positiveCount(object, "costCount", source, index);
        if (itemId != null && !BuiltInRegistries.ITEM.containsKey(itemId)) {
            throw invalid(source, index, "unknown item " + itemId);
        }
        if (!BuiltInRegistries.ITEM.containsKey(costItemId)) {
            throw invalid(source, index, "unknown cost item " + costItemId);
        }
        return new EconomyItemCostDefinition(
                new EconomyItemMatcher(itemId, tagId, itemCount),
                costItemId,
                costCount
        );
    }

    private static ResourceLocation parseId(JsonObject object, String name, String source, int index) {
        if (!object.has(name) || !object.get(name).isJsonPrimitive()) {
            throw invalid(source, index, name + " must be an item or tag ID string");
        }
        return ResourceLocation.tryParse(object.get(name).getAsString()) == null
                ? throwInvalidId(source, index, name)
                : ResourceLocation.parse(object.get(name).getAsString());
    }

    private static ResourceLocation throwInvalidId(String source, int index, String name) {
        throw invalid(source, index, name + " is not a valid resource ID");
    }

    private static int positiveCount(JsonObject object, String name, String source, int index) {
        if (!object.has(name)) {
            return 1;
        }
        int count = object.get(name).getAsInt();
        if (count <= 0) {
            throw invalid(source, index, name + " must be greater than zero");
        }
        return count;
    }

    private static IllegalArgumentException invalid(String source, int index, String message) {
        return new IllegalArgumentException(source + " entry " + index + ": " + message);
    }

    static ResourceLocation itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    static ItemStack costStack(ResourceLocation itemId, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(itemId), count);
    }

    private record EconomyFile(boolean replace, List<EconomyItemCostDefinition> values) {}

    private record EconomyResource(ResourceLocation id, Resource resource) {}
}
