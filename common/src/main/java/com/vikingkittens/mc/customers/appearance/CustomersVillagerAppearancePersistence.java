package com.vikingkittens.mc.customers.appearance;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

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
                .map(Identifier::tryParse)
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

    public static String toSynchedData(Map<String, String> additionalProperties) {
        return additionalProperties.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> encode(entry.getKey()) + ":" + encode(entry.getValue()))
                .collect(Collectors.joining(","));
    }

    public static Map<String, String> fromSynchedData(String data) {
        Map<String, String> additionalProperties = new LinkedHashMap<>();
        if (data.isEmpty()) {
            return Map.of();
        }
        for (String property : data.split(",", -1)) {
            String[] parts = property.split(":", 2);
            if (parts.length == 2) {
                additionalProperties.put(decode(parts[0]), decode(parts[1]));
            }
        }
        return Map.copyOf(additionalProperties);
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
