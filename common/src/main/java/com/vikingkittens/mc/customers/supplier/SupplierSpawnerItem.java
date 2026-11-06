package com.vikingkittens.mc.customers.supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.client.renderer.item.ItemProperties;
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

public final class SupplierSpawnerItem {
    public static final String TAG_SETTINGS = "customers:supplier_spawner_settings";
    private static final String TAG_ICON = "icon";
    private static final String TAG_APPEARANCES = "enabled_appearances";
    private static final String TAG_AUTOMATIC_COST = "automatic_cost";
    private static final String TAG_ITEMS = "items";

    public record Offer(ItemStack item, Optional<ItemStack> cost) {}

    private SupplierSpawnerItem() {}

    public static void register() {
        ItemProperties.register(SupplierSpawner.SUPPLIER_SPAWNER_ITEM.get(),
                ResourceLocation.fromNamespaceAndPath(Customers.MODID, "has_spawner_icon"),
                (stack, level, entity, seed) -> getIcon(stack).isPresent() ? 1.0F : 0.0F);
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

    public static Optional<List<ResourceLocation>> getEnabledAppearances(ItemStack stack) {
        CompoundTag settings = settings(stack);
        if (!settings.contains(TAG_APPEARANCES, Tag.TAG_LIST)) return Optional.empty();
        return Optional.of(settings.getList(TAG_APPEARANCES, Tag.TAG_STRING).stream()
                .map(Tag::getAsString).map(ResourceLocation::tryParse)
                .filter(java.util.Objects::nonNull).distinct().toList());
    }

    public static void setEnabledAppearances(ItemStack stack, List<ResourceLocation> appearances) {
        update(stack, settings -> {
            ListTag list = new ListTag();
            appearances.forEach(id -> list.add(StringTag.valueOf(id.toString())));
            settings.put(TAG_APPEARANCES, list);
        });
    }

    public static Optional<Boolean> getAutomaticCost(ItemStack stack) {
        CompoundTag settings = settings(stack);
        return settings.contains(TAG_AUTOMATIC_COST, Tag.TAG_BYTE)
                ? Optional.of(settings.getBoolean(TAG_AUTOMATIC_COST)) : Optional.empty();
    }

    public static void setAutomaticCost(ItemStack stack, boolean automaticCost) {
        update(stack, settings -> settings.putBoolean(TAG_AUTOMATIC_COST, automaticCost));
    }

    public static Optional<List<Offer>> getItems(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag settings = settings(stack);
        if (!settings.contains(TAG_ITEMS, Tag.TAG_LIST)) return Optional.empty();
        List<Offer> offers = new ArrayList<>();
        ListTag list = settings.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag pair = list.getCompound(index);
            ItemStack.parse(registries, pair.getCompound("item")).ifPresent(item ->
                    offers.add(new Offer(item, ItemStack.parse(registries, pair.getCompound("cost")))));
        }
        return Optional.of(List.copyOf(offers));
    }

    public static void setItems(ItemStack stack, List<Offer> offers, HolderLookup.Provider registries) {
        update(stack, settings -> {
            ListTag list = new ListTag();
            offers.forEach(offer -> {
                CompoundTag pair = new CompoundTag();
                pair.put("item", offer.item().save(registries, new CompoundTag()));
                offer.cost().ifPresent(cost -> pair.put("cost", cost.save(registries, new CompoundTag())));
                list.add(pair);
            });
            settings.put(TAG_ITEMS, list);
        });
    }

    public static void applyPreset(ItemStack stack, SpawnerPresetDefinition preset, HolderLookup.Provider registries) {
        CompoundTag source = preset.settings();
        if (!stack.has(DataComponents.CUSTOM_NAME)) setCustomName(stack, Component.literal(preset.name()));
        preset.icon().ifPresent(icon -> setIcon(stack, icon));
        if (source.contains(TAG_APPEARANCES, Tag.TAG_LIST)) {
            List<ResourceLocation> appearances = source.getList(TAG_APPEARANCES, Tag.TAG_STRING).stream()
                    .map(Tag::getAsString).map(ResourceLocation::tryParse)
                    .filter(java.util.Objects::nonNull).toList();
            setEnabledAppearances(stack, appearances);
        }
        if (source.contains(TAG_AUTOMATIC_COST, Tag.TAG_BYTE)) {
            setAutomaticCost(stack, source.getBoolean(TAG_AUTOMATIC_COST));
        }
        if (source.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            ItemStack sourceStack = new ItemStack(stack.getItem());
            update(sourceStack, settings ->
                    settings.put(TAG_ITEMS, source.getList(TAG_ITEMS, Tag.TAG_COMPOUND).copy()));
            getItems(sourceStack, registries).ifPresent(offers -> setItems(stack, offers, registries));
        }
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
