package com.vikingkittens.mc.customers.customer;

import java.util.Arrays;
import java.util.Optional;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

public final class CustomerSpawnerItem {
    static final String TAG_SETTINGS = "customers:customer_spawner_settings";
    static final String TAG_LEVEL_SETTINGS = "level_settings";
    private static final String TAG_ICON = "icon";
    private static final String TAG_MODE = "mode";

    private CustomerSpawnerItem() {}

    public static void register() {
        ItemProperties.register(
                CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, "spawner_mode"),
                (stack, level, entity, seed) -> predicateValueSpawnerMode(stack)
        );
        ItemProperties.register(
                CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, "has_spawner_icon"),
                (stack, level, entity, seed) -> getIcon(stack).isPresent() ? 1.0F : 0.0F
        );
    }

    static float predicateValueSpawnerMode(ItemStack stack) {
        return getMode(stack)
                .map(CustomerSpawnerMode::ordinal)
                .orElse(0);
    }

    public static Optional<CustomerSpawnerMode> getMode(ItemStack stack) {
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

    public static Optional<ResourceLocation> getIcon(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return Optional.empty();
        CompoundTag data = customData.copyTag();
        if (!data.contains(TAG_SETTINGS, Tag.TAG_COMPOUND)) return Optional.empty();
        return Optional.ofNullable(ResourceLocation.tryParse(data.getCompound(TAG_SETTINGS).getString(TAG_ICON)));
    }

    static void applySettings(ItemStack stack, CustomerSpawnerBlockEntity spawner, HolderLookup.Provider registries) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag itemData = customData.copyTag();
        if (itemData.contains(TAG_SETTINGS, Tag.TAG_COMPOUND)) {
            CompoundTag settings = itemData.getCompound(TAG_SETTINGS);
            spawner.setIcon(ResourceLocation.tryParse(settings.getString(TAG_ICON)));
            spawner.readItemSettings(settings, registries);
            ListTag levels = settings.getList(TAG_LEVEL_SETTINGS, Tag.TAG_COMPOUND);
            for (int level = 0; level < levels.size() && level < spawner.getLevelSettingsCount(); level++) {
                spawner.getLevelSettings(level).readLockedInventory(
                        PersistenceCUtils.reader(levels.getCompound(level), registries)
                );
            }
        }
    }

    static void writeSettings(
            ItemStack stack,
            CustomerSpawnerMode mode,
            CustomerSpawnerBlockEntity spawner,
            HolderLookup.Provider registries
    ) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, itemData -> {
            CompoundTag settings = new CompoundTag();
            settings.putString(TAG_MODE, mode.getSerializedName());
            if (spawner.getIcon() != null) settings.putString(TAG_ICON, spawner.getIcon().toString());
            spawner.writeItemSettings(settings, registries);
            ListTag levels = settings.getList(TAG_LEVEL_SETTINGS, Tag.TAG_COMPOUND);
            for (int level = 0; level < levels.size() && level < spawner.getLevelSettingsCount(); level++) {
                spawner.getLevelSettings(level).writeLockedInventory(
                        PersistenceCUtils.writer(levels.getCompound(level), registries)
                );
            }
            itemData.put(TAG_SETTINGS, settings);
        });
    }
}
