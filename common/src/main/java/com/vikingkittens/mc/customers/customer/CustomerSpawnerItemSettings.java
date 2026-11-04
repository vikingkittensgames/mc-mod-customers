package com.vikingkittens.mc.customers.customer;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class CustomerSpawnerItemSettings {
    static final String TAG_SETTINGS = "customers:customer_spawner_settings";
    static final String TAG_LEVEL_SETTINGS = "level_settings";
    private static final String TAG_MODE = "mode";

    private CustomerSpawnerItemSettings() {}

    static Optional<CustomerSpawnerMode> getMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return Optional.empty();
        }

        CompoundTag itemData = customData.copyTag();
        if (!itemData.contains(TAG_SETTINGS, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        String modeName = itemData.getCompound(TAG_SETTINGS).getString(TAG_MODE);
        return Arrays.stream(CustomerSpawnerMode.values())
                .filter(mode -> mode.getSerializedName().equals(modeName))
                .findFirst();
    }

    static void apply(ItemStack stack, CustomerSpawnerBlockEntity spawner, HolderLookup.Provider registries) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag itemData = customData.copyTag();
        if (itemData.contains(TAG_SETTINGS, Tag.TAG_COMPOUND)) {
            spawner.readItemSettings(itemData.getCompound(TAG_SETTINGS), registries);
        }
    }

    static void write(
            ItemStack stack,
            CustomerSpawnerMode mode,
            CustomerSpawnerBlockEntity spawner,
            HolderLookup.Provider registries
    ) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, itemData -> {
            CompoundTag settings = new CompoundTag();
            settings.putString(TAG_MODE, mode.getSerializedName());
            spawner.writeItemSettings(settings, registries);
            itemData.put(TAG_SETTINGS, settings);
        });
    }
}
