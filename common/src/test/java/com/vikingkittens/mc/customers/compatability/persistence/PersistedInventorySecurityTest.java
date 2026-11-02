package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PersistedInventorySecurityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void corruptedSerializedSizeCannotResizeAFixedContainer() {
        CompoundTag serialized = serializedInventory(10_000);
        PersistedContainer container =
                new PersistedContainer(9, () -> {});

        container.deserializeNBT(RegistryAccess.EMPTY, serialized);

        assertEquals(9, container.getContainerSize());
        assertSame(Items.DIAMOND, container.getItem(8).getItem());
    }

    @Test
    void boundedItemStackReadDoesNotAllocateTheSerializedSize() {
        CompoundTag root = new CompoundTag();
        root.put("inventory", serializedInventory(10_000));

        List<ItemStack> stacks = PersistenceCUtils
                .reader(root, RegistryAccess.EMPTY)
                .getItemStacks("inventory", 55);

        assertEquals(55, stacks.size());
        assertSame(Items.DIAMOND, stacks.get(8).getItem());
    }

    private static CompoundTag serializedInventory(int size) {
        CompoundTag itemPrefix = new CompoundTag();
        itemPrefix.putInt("Slot", 8);
        CompoundTag item = (CompoundTag) new ItemStack(Items.DIAMOND, 2)
                .save(RegistryAccess.EMPTY, itemPrefix);
        ListTag items = new ListTag();
        items.add(item);
        CompoundTag serialized = new CompoundTag();
        serialized.putInt("Size", size);
        serialized.put("Items", items);
        return serialized;
    }
}
