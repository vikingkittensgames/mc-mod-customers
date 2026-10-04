package com.vikingkittens.mc.customers.appearance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

public final class CustomersVillagerAppearancePersistence {
    static final String TAG_APPEARANCE = "CustomersAppearance";
    static final String TAG_VARIATION_SEED = "CustomersVariationSeed";
    static final String TAG_ADDITIONAL_PROPERTIES = "CustomersAppearanceAdditionalProperties";
    static final String TAG_PROPERTY_KEY = "Key";
    static final String TAG_PROPERTY_VALUE = "Value";

    private CustomersVillagerAppearancePersistence() {}

    public static void read(
            DataReader input,
            CustomersVillager villager
    ) {
        input.getString(TAG_APPEARANCE)
                .map(ResourceLocation::tryParse)
                .ifPresent(villager::setAppearanceId);
        input.getFloat(TAG_VARIATION_SEED)
                .ifPresent(villager::setVariationSeed);
        List<DataReader> storedProperties = input.getChildren(TAG_ADDITIONAL_PROPERTIES);
        Map<String, String> additionalProperties = new LinkedHashMap<>();
        for (DataReader storedProperty : storedProperties) {
            storedProperty.getString(TAG_PROPERTY_KEY).ifPresent(key ->
                    storedProperty.getString(TAG_PROPERTY_VALUE)
                            .ifPresent(value -> additionalProperties.put(key, value))
            );
        }
        villager.setAdditionalProperties(additionalProperties);
    }

    public static void write(
            DataWriter output,
            CustomersVillager villager
    ) {
        output.putString(
                TAG_APPEARANCE,
                villager.getAppearanceId().toString()
        );
        output.putFloat(
                TAG_VARIATION_SEED,
                villager.getVariationSeed()
        );
        villager.getAdditionalProperties().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    DataWriter property = output.addChild(TAG_ADDITIONAL_PROPERTIES);
                    property.putString(TAG_PROPERTY_KEY, entry.getKey());
                    property.putString(TAG_PROPERTY_VALUE, entry.getValue());
                });
    }

    public static CompoundTag toSynchedData(Map<String, String> additionalProperties) {
        CompoundTag tag = new CompoundTag();
        additionalProperties.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> tag.putString(entry.getKey(), entry.getValue()));
        return tag;
    }

    public static Map<String, String> fromSynchedData(CompoundTag tag) {
        Map<String, String> additionalProperties = new LinkedHashMap<>();
        tag.getAllKeys().stream()
                .sorted()
                .filter(key -> tag.contains(key, Tag.TAG_STRING))
                .forEach(key -> additionalProperties.put(key, tag.getString(key)));
        return Map.copyOf(additionalProperties);
    }
}
