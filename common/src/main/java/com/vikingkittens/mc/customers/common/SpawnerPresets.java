package com.vikingkittens.mc.customers.common;

import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.CustomersServices;

public final class SpawnerPresets {
    @FunctionalInterface
    public interface Applier {
        void apply(ItemStack stack, SpawnerPresetDefinition preset, HolderLookup.Provider registries);
    }
    public static final String TAG_PRESET = "customers:preset";
    public static final String TAG_APPLIED = "customers:preset_applied";
    public static final ResourceKey<Registry<SpawnerPresetDefinition>> CUSTOMER = registry("customer_spawner_presets");
    public static final ResourceKey<Registry<SpawnerPresetDefinition>> SUPPLIER = registry("supplier_spawner_presets");

    private SpawnerPresets() {}

    private static ResourceKey<Registry<SpawnerPresetDefinition>> registry(String path) {
        return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Customers.MODID, path));
    }

    public static void initialize() {
        CustomersServices.registration().registerDataPackRegistry(CUSTOMER, SpawnerPresetDefinition.CODEC);
        CustomersServices.registration().registerDataPackRegistry(SUPPLIER, SpawnerPresetDefinition.CODEC);
    }

    public static Optional<ResourceLocation> getId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return Optional.empty();
        var tag = data.copyTag();
        return tag.contains(TAG_PRESET, net.minecraft.nbt.Tag.TAG_STRING)
                ? Optional.ofNullable(ResourceLocation.tryParse(tag.getString(TAG_PRESET))) : Optional.empty();
    }

    public static Optional<SpawnerPresetDefinition> find(
            HolderLookup.Provider registries, ResourceKey<Registry<SpawnerPresetDefinition>> type, ResourceLocation id) {
        return registries.lookup(type).flatMap(registry -> registry.get(ResourceKey.create(type, id)))
                .map(holder -> holder.value());
    }

    public static boolean apply(ItemStack stack, HolderLookup.Provider registries,
            ResourceKey<Registry<SpawnerPresetDefinition>> type, Applier applier) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData.copyTag().getBoolean(TAG_APPLIED)) return false;
        Optional<ResourceLocation> id = getId(stack);
        if (id.isEmpty()) return false;
        Optional<SpawnerPresetDefinition> preset = find(registries, type, id.get());
        if (preset.isEmpty()) return false;
        applier.apply(stack, preset.get(), registries);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putBoolean(TAG_APPLIED, true));
        return true;
    }

    public static ItemStack create(ResourceLocation id, SpawnerPresetDefinition preset, net.minecraft.world.item.Item item,
            HolderLookup.Provider registries, Applier applier) {
        ItemStack stack = new ItemStack(item);
        applier.apply(stack, preset, registries);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putString(TAG_PRESET, id.toString());
            data.putBoolean(TAG_APPLIED, true);
        });
        return stack;
    }

    public static void preserve(ItemStack stack, ResourceLocation id) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putString(TAG_PRESET, id.toString());
            data.putBoolean(TAG_APPLIED, true);
        });
    }
}
