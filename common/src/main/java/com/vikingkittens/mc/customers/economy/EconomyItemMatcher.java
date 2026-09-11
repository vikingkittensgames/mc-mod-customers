package com.vikingkittens.mc.customers.economy;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

record EconomyItemMatcher(
        @Nullable ResourceLocation itemId,
        @Nullable ResourceLocation tagId,
        int count
) {
    boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (itemId != null && !itemId.equals(EconomyJsonData.itemId(stack))) {
            return false;
        }
        if (tagId != null && !stack.is(TagKey.create(Registries.ITEM, tagId))) {
            return false;
        }
        return true;
    }
}
