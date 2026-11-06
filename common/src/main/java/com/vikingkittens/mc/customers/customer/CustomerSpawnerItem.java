package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import dev.architectury.registry.item.ItemPropertiesRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.SpawnerPresetDefinition;

public final class CustomerSpawnerItem {
    public static final String TAG_SETTINGS = "customers:customer_spawner_settings";
    static final String TAG_LEVEL_SETTINGS = "level_settings";
    private static final String TAG_LEVELS = TAG_LEVEL_SETTINGS;
    private static final String TAG_ICON = "icon";
    private static final String TAG_MODE = "mode";
    private static final double MODE_PREDICATE_STEP = 0.01D;

    public record Row(List<ItemStack> items, Optional<ItemStack> cost) {}
    public record EnabledPet(String id, Optional<ResourceLocation> food) {}

    private CustomerSpawnerItem() {}

    public static void register() {
        ItemPropertiesRegistry.register(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, "spawner_mode"),
                (stack, level, entity, seed) -> predicateValueSpawnerMode(stack));
        ItemPropertiesRegistry.register(CustomerSpawner.CUSTOMER_SPAWNER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, "has_spawner_icon"),
                (stack, level, entity, seed) -> getIcon(stack).isPresent() ? 1.0F : 0.0F);
    }

    static float predicateValueSpawnerMode(ItemStack stack) {
        return getMode(stack).map(mode -> (float)(mode.ordinal() * MODE_PREDICATE_STEP)).orElse(0.0F);
    }

    public static Optional<Component> getCustomName(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.CUSTOM_NAME));
    }

    public static void setCustomName(ItemStack stack, Component name) {
        stack.set(DataComponents.CUSTOM_NAME, name);
    }

    public static Optional<ResourceLocation> getIcon(ItemStack stack) {
        CompoundTag settings = settings(stack);
        return settings.contains(TAG_ICON, Tag.TAG_STRING)
                ? Optional.ofNullable(ResourceLocation.tryParse(settings.getString(TAG_ICON))) : Optional.empty();
    }

    public static void setIcon(ItemStack stack, ResourceLocation icon) {
        update(stack, settings -> settings.putString(TAG_ICON, icon.toString()));
    }

    public static Optional<CustomerSpawnerMode> getMode(ItemStack stack) {
        String mode = settings(stack).getString(TAG_MODE);
        return Arrays.stream(CustomerSpawnerMode.values())
                .filter(value -> value.getSerializedName().equals(mode)).findFirst();
    }

    public static void setMode(ItemStack stack, CustomerSpawnerMode mode) {
        update(stack, settings -> settings.putString(TAG_MODE, mode.getSerializedName()));
    }

    public static int getLevelCount(ItemStack stack) {
        return settings(stack).getList(TAG_LEVELS, Tag.TAG_COMPOUND).size();
    }

    public static void setLevelCount(ItemStack stack, int count) {
        update(stack, settings -> {
            ListTag levels = settings.getList(TAG_LEVELS, Tag.TAG_COMPOUND);
            while (levels.size() < count && levels.size() < CustomerSpawnerBlockEntity.MAX_LEVELS) {
                levels.add(new CompoundTag());
            }
            while (levels.size() > count) levels.remove(levels.size() - 1);
            settings.put(TAG_LEVELS, levels);
        });
    }

    public static Optional<Float> getRequiredStars(ItemStack stack, int level) {
        return getFloat(stack, level, "required_stars");
    }

    public static void setRequiredStars(ItemStack stack, int level, float value) {
        updateLevel(stack, level, settings -> settings.putFloat("required_stars", value));
    }

    public static Optional<Integer> getMaxCustomers(ItemStack stack, int level) {
        CompoundTag settings = level(stack, level);
        return settings.contains("max_customers", Tag.TAG_INT)
                ? Optional.of(settings.getInt("max_customers")) : Optional.empty();
    }

    public static void setMaxCustomers(ItemStack stack, int level, int value) {
        updateLevel(stack, level, settings -> settings.putInt("max_customers", value));
    }

    public static Optional<Float> getPetPercentage(ItemStack stack, int level) {
        return getFloat(stack, level, "pet_percentage");
    }

    public static void setPetPercentage(ItemStack stack, int level, float value) {
        updateLevel(stack, level, settings -> settings.putFloat("pet_percentage", value));
    }

    public static Optional<List<EnabledPet>> getEnabledPets(ItemStack stack, int level) {
        CompoundTag settings = level(stack, level);
        if (!settings.contains("enabled_pets", Tag.TAG_LIST)) return Optional.empty();
        List<EnabledPet> pets = new ArrayList<>();
        ListTag values = settings.getList("enabled_pets", Tag.TAG_COMPOUND);
        for (int index = 0; index < values.size(); index++) {
            CompoundTag entry = values.getCompound(index);
            if (!entry.contains("id", Tag.TAG_STRING)) continue;
            Optional<ResourceLocation> food = entry.contains("food", Tag.TAG_STRING)
                    ? Optional.ofNullable(ResourceLocation.tryParse(entry.getString("food"))) : Optional.empty();
            pets.add(new EnabledPet(entry.getString("id"), food));
        }
        return Optional.of(List.copyOf(pets));
    }

    public static void setEnabledPets(ItemStack stack, int level, List<EnabledPet> pets) {
        updateLevel(stack, level, settings -> {
            ListTag values = new ListTag();
            pets.forEach(pet -> {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", pet.id());
                pet.food().ifPresent(food -> entry.putString("food", food.toString()));
                values.add(entry);
            });
            settings.put("enabled_pets", values);
        });
    }

    public static Optional<List<ResourceLocation>> getEnabledAppearances(ItemStack stack, int level) {
        return getStrings(stack, level, "enabled_appearances").map(values -> values.stream()
                .map(ResourceLocation::tryParse).filter(java.util.Objects::nonNull).distinct().toList());
    }

    public static void setEnabledAppearances(ItemStack stack, int level, List<ResourceLocation> values) {
        setStrings(stack, level, "enabled_appearances", values.stream().map(ResourceLocation::toString).toList());
    }

    public static Optional<Boolean> getAutomaticCost(ItemStack stack, int level) {
        return getBoolean(stack, level, "automatic_cost");
    }

    public static void setAutomaticCost(ItemStack stack, int level, boolean value) {
        updateLevel(stack, level, settings -> settings.putBoolean("automatic_cost", value));
    }

    public static Optional<List<Row>> getRows(ItemStack stack, int level, HolderLookup.Provider registries) {
        CompoundTag settings = level(stack, level);
        if (!settings.contains("rows", Tag.TAG_LIST)) return Optional.empty();
        List<Row> rows = new ArrayList<>();
        ListTag values = settings.getList("rows", Tag.TAG_COMPOUND);
        for (int index = 0; index < values.size(); index++) {
            CompoundTag entry = values.getCompound(index);
            List<ItemStack> items = new ArrayList<>();
            ListTag itemTags = entry.getList("items", Tag.TAG_COMPOUND);
            for (int item = 0; item < itemTags.size(); item++) {
                ItemStack.parse(registries, itemTags.getCompound(item)).ifPresent(items::add);
            }
            Optional<ItemStack> cost = entry.contains("cost", Tag.TAG_COMPOUND)
                    ? ItemStack.parse(registries, entry.getCompound("cost")) : Optional.empty();
            rows.add(new Row(List.copyOf(items), cost));
        }
        return Optional.of(List.copyOf(rows));
    }

    public static void setRows(ItemStack stack, int level, List<Row> rows, HolderLookup.Provider registries) {
        updateLevel(stack, level, settings -> {
            ListTag values = new ListTag();
            rows.forEach(row -> {
                CompoundTag entry = new CompoundTag();
                ListTag items = new ListTag();
                row.items().forEach(item -> items.add(item.save(registries, new CompoundTag())));
                entry.put("items", items);
                row.cost().ifPresent(cost -> entry.put("cost", cost.save(registries, new CompoundTag())));
                values.add(entry);
            });
            settings.put("rows", values);
        });
    }

    public static void applyPreset(ItemStack stack, SpawnerPresetDefinition preset, HolderLookup.Provider registries) {
        if (!stack.has(DataComponents.CUSTOM_NAME)) setCustomName(stack, Component.literal(preset.name()));
        preset.icon().ifPresent(icon -> setIcon(stack, icon));
        ItemStack source = new ItemStack(stack.getItem());
        update(source, settings -> settings.merge(preset.settings()));
        getMode(source).ifPresent(mode -> setMode(stack, mode));
        int levelCount = Math.min(getLevelCount(source), CustomerSpawnerBlockEntity.MAX_LEVELS);
        setLevelCount(stack, levelCount);
        for (int level = 0; level < levelCount; level++) {
            int current = level;
            getRequiredStars(source, level).ifPresent(value -> setRequiredStars(stack, current, value));
            getMaxCustomers(source, level).ifPresent(value -> setMaxCustomers(stack, current, value));
            getPetPercentage(source, level).ifPresent(value -> setPetPercentage(stack, current, value));
            getEnabledPets(source, level).ifPresent(value -> setEnabledPets(stack, current, value));
            getEnabledAppearances(source, level).ifPresent(value -> setEnabledAppearances(stack, current, value));
            getAutomaticCost(source, level).ifPresent(value -> setAutomaticCost(stack, current, value));
            getRows(source, level, registries).ifPresent(value -> setRows(stack, current, value, registries));
        }
    }

    private static Optional<Float> getFloat(ItemStack stack, int level, String key) {
        CompoundTag settings = level(stack, level);
        return settings.contains(key, Tag.TAG_ANY_NUMERIC) ? Optional.of(settings.getFloat(key)) : Optional.empty();
    }

    private static Optional<Boolean> getBoolean(ItemStack stack, int level, String key) {
        CompoundTag settings = level(stack, level);
        return settings.contains(key, Tag.TAG_BYTE) ? Optional.of(settings.getBoolean(key)) : Optional.empty();
    }

    private static Optional<List<String>> getStrings(ItemStack stack, int level, String key) {
        CompoundTag settings = level(stack, level);
        if (!settings.contains(key, Tag.TAG_LIST)) return Optional.empty();
        return Optional.of(settings.getList(key, Tag.TAG_STRING).stream().map(Tag::getAsString).toList());
    }

    private static void setStrings(ItemStack stack, int level, String key, List<String> values) {
        updateLevel(stack, level, settings -> {
            ListTag list = new ListTag();
            values.forEach(value -> list.add(StringTag.valueOf(value)));
            settings.put(key, list);
        });
    }

    private static CompoundTag level(ItemStack stack, int index) {
        ListTag levels = settings(stack).getList(TAG_LEVELS, Tag.TAG_COMPOUND);
        return index >= 0 && index < levels.size() ? levels.getCompound(index) : new CompoundTag();
    }

    private static void updateLevel(ItemStack stack, int index, Consumer<CompoundTag> change) {
        if (index < 0 || index >= CustomerSpawnerBlockEntity.MAX_LEVELS) {
            throw new IllegalArgumentException("Invalid customer spawner level: " + index);
        }
        update(stack, settings -> {
            ListTag levels = settings.getList(TAG_LEVELS, Tag.TAG_COMPOUND);
            while (levels.size() <= index) levels.add(new CompoundTag());
            change.accept(levels.getCompound(index));
            settings.put(TAG_LEVELS, levels);
        });
    }

    private static CompoundTag settings(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag().getCompound(TAG_SETTINGS).copy();
    }

    private static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            CompoundTag settings = data.getCompound(TAG_SETTINGS).copy();
            change.accept(settings);
            data.put(TAG_SETTINGS, settings);
        });
    }
}
