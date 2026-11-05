package com.vikingkittens.mc.customers.supplier;

import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

final class SupplierSpawnerItemSettings {
    static final String TAG_SETTINGS = "customers:supplier_spawner_settings";
    private static final String TAG_ENABLED_APPEARANCES = "enabled_appearances";
    private static final String TAG_AUTOMATIC_COST = "automatic_cost";

    private SupplierSpawnerItemSettings() {}

    static void apply(ItemStack stack, SupplierSpawnerBlockEntity spawner, HolderLookup.Provider registries) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag itemData = customData.copyTag();
        if (!itemData.contains(TAG_SETTINGS, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag settings = itemData.getCompound(TAG_SETTINGS);
        if (settings.contains(TAG_ENABLED_APPEARANCES, Tag.TAG_LIST)) {
            List<ResourceLocation> appearances = settings.getList(TAG_ENABLED_APPEARANCES, Tag.TAG_STRING).stream()
                    .map(Tag::getAsString)
                    .map(ResourceLocation::tryParse)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            if (!appearances.isEmpty()) {
                spawner.setEnabledAppearanceIds(appearances);
            }
        }
        if (settings.contains(TAG_AUTOMATIC_COST, Tag.TAG_BYTE)) {
            spawner.setAutoCost(settings.getBoolean(TAG_AUTOMATIC_COST));
        }
        spawner.readLockedInventory(PersistenceCUtils.reader(settings, registries));
    }

    static void write(ItemStack stack, SupplierSpawnerBlockEntity spawner, HolderLookup.Provider registries) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, itemData -> {
            CompoundTag settings = new CompoundTag();
            ListTag appearances = new ListTag();
            spawner.getEnabledAppearanceIds().forEach(appearance ->
                    appearances.add(StringTag.valueOf(appearance.toString())));
            settings.put(TAG_ENABLED_APPEARANCES, appearances);
            settings.putBoolean(TAG_AUTOMATIC_COST, spawner.isAutoCost());
            spawner.writeLockedInventory(PersistenceCUtils.writer(settings, registries));
            itemData.put(TAG_SETTINGS, settings);
        });
    }
}
