package com.vikingkittens.mc.customers.compatability;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import com.vikingkittens.mc.customers.Customers;

public final class ItemCUtils {
    private ItemCUtils() {}

    public static Item.Properties setId(Item.Properties properties, String name) {
        return properties.setId(ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(Customers.MODID, name)
        ));
    }
}
