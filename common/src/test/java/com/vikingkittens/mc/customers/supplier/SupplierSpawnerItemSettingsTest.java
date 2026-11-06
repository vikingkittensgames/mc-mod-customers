package com.vikingkittens.mc.customers.supplier;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.common.SpawnerPresetDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplierSpawnerItemSettingsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void storesEachPublicSettingOnTheItem() {
        ItemStack stack = new ItemStack(Items.STONE);
        SupplierSpawnerItem.setCustomName(stack, Component.literal("Supplier"));
        SupplierSpawnerItem.setIcon(stack, ResourceLocation.parse("minecraft:textures/item/cookie.png"));
        SupplierSpawnerItem.setEnabledAppearances(stack, List.of(ResourceLocation.parse("customers:default")));
        SupplierSpawnerItem.setAutomaticCost(stack, true);
        SupplierSpawnerItem.setItems(stack, List.of(
                new SupplierSpawnerItem.Offer(new ItemStack(Items.COOKIE, 16), Optional.empty()),
                new SupplierSpawnerItem.Offer(new ItemStack(Items.MILK_BUCKET), Optional.of(new ItemStack(Items.EMERALD)))
        ), RegistryAccess.EMPTY);

        assertEquals("Supplier", SupplierSpawnerItem.getCustomName(stack).orElseThrow().getString());
        assertTrue(stack.has(DataComponents.CUSTOM_NAME));
        assertEquals("minecraft:textures/item/cookie.png", SupplierSpawnerItem.getIcon(stack).orElseThrow().toString());
        assertEquals(List.of(ResourceLocation.parse("customers:default")),
                SupplierSpawnerItem.getEnabledAppearances(stack).orElseThrow());
        assertTrue(SupplierSpawnerItem.getAutomaticCost(stack).orElseThrow());
        List<SupplierSpawnerItem.Offer> offers = SupplierSpawnerItem.getItems(stack, RegistryAccess.EMPTY).orElseThrow();
        assertEquals(2, offers.size());
        assertEquals(16, offers.get(0).item().getCount());
        assertFalse(offers.get(0).cost().isPresent());
        assertTrue(offers.get(1).cost().orElseThrow().is(Items.EMERALD));
    }

    @Test
    void appliesPresetThroughThePublicSettings() {
        CompoundTag settings = new CompoundTag();
        settings.putBoolean("automatic_cost", true);
        settings.putInt("Size", 1);
        settings.put("lockedInventory", new CompoundTag());
        ListTag appearances = new ListTag();
        appearances.add(StringTag.valueOf("customers:default"));
        settings.put("enabled_appearances", appearances);
        ListTag offers = new ListTag();
        CompoundTag offer = new CompoundTag();
        CompoundTag item = new CompoundTag();
        item.putString("id", "minecraft:cookie");
        item.putInt("count", 16);
        offer.put("item", item);
        offers.add(offer);
        settings.put("items", offers);

        ItemStack stack = new ItemStack(Items.STONE);
        SupplierSpawnerItem.applyPreset(stack, new SpawnerPresetDefinition("Supplier", Optional.empty(), settings),
                RegistryAccess.EMPTY);

        assertEquals("Supplier", SupplierSpawnerItem.getCustomName(stack).orElseThrow().getString());
        assertTrue(SupplierSpawnerItem.getAutomaticCost(stack).orElseThrow());
        assertEquals(16, SupplierSpawnerItem.getItems(stack, RegistryAccess.EMPTY).orElseThrow().getFirst().item().getCount());
        assertFalse(stack.get(DataComponents.CUSTOM_DATA).copyTag()
                .getCompound(SupplierSpawnerItem.TAG_SETTINGS).contains("lockedInventory"));
    }
}
