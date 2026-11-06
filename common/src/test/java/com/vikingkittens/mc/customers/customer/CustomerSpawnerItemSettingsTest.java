package com.vikingkittens.mc.customers.customer;

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

class CustomerSpawnerItemSettingsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void storesEachPublicSettingOnTheItem() {
        ItemStack stack = new ItemStack(Items.STONE);
        CustomerSpawnerItem.setCustomName(stack, Component.literal("Customers"));
        CustomerSpawnerItem.setIcon(stack, ResourceLocation.parse("minecraft:textures/item/cookie.png"));
        CustomerSpawnerItem.setMode(stack, CustomerSpawnerMode.NIGHT);
        CustomerSpawnerItem.setLevelCount(stack, 1);
        CustomerSpawnerItem.setRequiredStars(stack, 0, 2.5F);
        CustomerSpawnerItem.setMaxCustomers(stack, 0, 12);
        CustomerSpawnerItem.setPetPercentage(stack, 0, 0.25F);
        CustomerSpawnerItem.setEnabledPets(stack, 0, List.of(
                new CustomerSpawnerItem.EnabledPet("minecraft:cat",
                        Optional.of(ResourceLocation.parse("minecraft:cod"))),
                new CustomerSpawnerItem.EnabledPet("minecraft:wolf", Optional.empty())));
        CustomerSpawnerItem.setEnabledAppearances(stack, 0, List.of(ResourceLocation.parse("customers:default")));
        CustomerSpawnerItem.setAutomaticCost(stack, 0, true);
        CustomerSpawnerItem.setRows(stack, 0,
                List.of(new CustomerSpawnerItem.Row(List.of(new ItemStack(Items.COOKIE, 3)),
                        Optional.of(new ItemStack(Items.EMERALD)))), RegistryAccess.EMPTY);

        assertEquals("Customers", CustomerSpawnerItem.getCustomName(stack).orElseThrow().getString());
        assertEquals(CustomerSpawnerMode.NIGHT, CustomerSpawnerItem.getMode(stack).orElseThrow());
        assertEquals(1, CustomerSpawnerItem.getLevelCount(stack));
        assertEquals(2.5F, CustomerSpawnerItem.getRequiredStars(stack, 0).orElseThrow());
        assertEquals(12, CustomerSpawnerItem.getMaxCustomers(stack, 0).orElseThrow());
        assertEquals(0.25F, CustomerSpawnerItem.getPetPercentage(stack, 0).orElseThrow());
        assertEquals(List.of(new CustomerSpawnerItem.EnabledPet("minecraft:cat",
                        Optional.of(ResourceLocation.parse("minecraft:cod"))),
                        new CustomerSpawnerItem.EnabledPet("minecraft:wolf", Optional.empty())),
                CustomerSpawnerItem.getEnabledPets(stack, 0).orElseThrow());
        assertEquals(List.of(ResourceLocation.parse("customers:default")),
                CustomerSpawnerItem.getEnabledAppearances(stack, 0).orElseThrow());
        assertTrue(CustomerSpawnerItem.getAutomaticCost(stack, 0).orElseThrow());
        assertEquals(3, CustomerSpawnerItem.getRows(stack, 0, RegistryAccess.EMPTY)
                .orElseThrow().getFirst().items().getFirst().getCount());
    }

    @Test
    void appliesPresetWithDirectAppearanceIds() {
        CompoundTag level = new CompoundTag();
        level.putFloat("required_stars", 2.5F);
        level.put("lockedInventory", new CompoundTag());
        ListTag appearances = new ListTag();
        appearances.add(StringTag.valueOf("customers:default"));
        level.put("enabled_appearances", appearances);
        ListTag levels = new ListTag();
        levels.add(level);
        CompoundTag settings = new CompoundTag();
        settings.putString("mode", "night");
        settings.put("level_settings", levels);

        ItemStack stack = new ItemStack(Items.STONE);
        CustomerSpawnerItem.applyPreset(stack, new SpawnerPresetDefinition("Customers", Optional.empty(), settings),
                RegistryAccess.EMPTY);

        assertEquals(CustomerSpawnerMode.NIGHT, CustomerSpawnerItem.getMode(stack).orElseThrow());
        assertEquals(2.5F, CustomerSpawnerItem.getRequiredStars(stack, 0).orElseThrow());
        assertEquals(List.of(ResourceLocation.parse("customers:default")),
                CustomerSpawnerItem.getEnabledAppearances(stack, 0).orElseThrow());
        assertFalse(stack.get(DataComponents.CUSTOM_DATA).copyTag()
                .getCompound(CustomerSpawnerItem.TAG_SETTINGS).getList("level_settings", 10)
                .getCompound(0).contains("lockedInventory"));
    }
}
