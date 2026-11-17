package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;

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
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING,
                RegistryAccess.EMPTY
        );
        output.putInt("Size", size);
        ValueOutput item = output.childrenList("Items").addChild();
        item.putInt("Slot", 8);
        item.store(
                ItemStack.MAP_CODEC,
                new ItemStack(Items.DIAMOND, 2)
        );
        return output.buildResult();
    }
}
