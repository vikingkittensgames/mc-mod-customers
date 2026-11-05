package com.vikingkittens.mc.customers.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnerInventoryLocksTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void restoresOnlyLockedInventoryItemsAndTheirLocks() {
        SimpleContainer source = new SimpleContainer(3);
        source.setItem(0, new ItemStack(Items.BREAD, 2));
        source.setItem(1, new ItemStack(Items.CARROT, 3));
        SpawnerInventoryLocks sourceLocks = new SpawnerInventoryLocks(3);
        sourceLocks.setLocked(0, true);
        CompoundTag tag = new CompoundTag();
        sourceLocks.write(PersistenceCUtils.writer(tag, RegistryAccess.EMPTY));
        sourceLocks.writeLockedInventory(PersistenceCUtils.writer(tag, RegistryAccess.EMPTY), source);

        SimpleContainer restored = new SimpleContainer(3);
        SpawnerInventoryLocks restoredLocks = new SpawnerInventoryLocks(3);
        restoredLocks.read(PersistenceCUtils.reader(tag, RegistryAccess.EMPTY));
        restoredLocks.readLockedInventory(PersistenceCUtils.reader(tag, RegistryAccess.EMPTY), restored);

        assertTrue(restored.getItem(0).is(Items.BREAD));
        assertEquals(2, restored.getItem(0).getCount());
        assertTrue(restoredLocks.isLocked(0));
        assertTrue(restored.getItem(1).isEmpty());
        assertFalse(restoredLocks.isLocked(1));
        assertFalse(restoredLocks.isLocked(2));
    }
}
