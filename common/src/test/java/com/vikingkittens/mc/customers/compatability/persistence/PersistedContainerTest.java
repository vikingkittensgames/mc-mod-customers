package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.concurrent.atomic.AtomicInteger;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistedContainerTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void behavesAsAVanillaContainerAndReportsChanges() {
        AtomicInteger changes = new AtomicInteger();
        PersistedContainer container = new PersistedContainer(3, changes::incrementAndGet);

        container.setItem(1, new ItemStack(Items.BEEF, 8));
        ItemStack removed = container.removeItem(1, 3);

        assertSame(Items.BEEF, removed.getItem());
        assertEquals(3, removed.getCount());
        assertEquals(5, container.getItem(1).getCount());
        assertEquals(2, changes.get());
    }

    @Test
    void preservesAssignedStackCountLikeTheExistingItemStackHandler() {
        PersistedContainer container = new PersistedContainer(1, () -> {});

        container.setItem(0, new ItemStack(Items.EMERALD, 99));

        assertEquals(99, container.getItem(0).getCount());
    }

    @Test
    void insertsPartOfAStackIntoASpecificSlot() {
        PersistedContainer container = new PersistedContainer(1, () -> {});
        container.setItem(0, new ItemStack(Items.EMERALD, 60));

        ItemStack remainder = container.insertItem(0, new ItemStack(Items.EMERALD, 8), false);

        assertEquals(64, container.getItem(0).getCount());
        assertEquals(4, remainder.getCount());
    }

    @Test
    void simulatesSlotInsertionWithoutChangingTheContainer() {
        PersistedContainer container = new PersistedContainer(1, () -> {});
        container.setItem(0, new ItemStack(Items.EMERALD, 60));

        ItemStack remainder = container.insertItem(0, new ItemStack(Items.EMERALD, 8), true);

        assertEquals(60, container.getItem(0).getCount());
        assertEquals(4, remainder.getCount());
    }

    @Test
    void serializesUsingTheExistingItemStackHandlerLayout() {
        PersistedContainer container = new PersistedContainer(3, () -> {});
        container.setItem(1, new ItemStack(Items.DIAMOND, 2));

        CompoundTag serialized = container.serializeNBT();
        ListTag items = serialized.getList("Items", CompoundTag.TAG_COMPOUND);

        assertEquals(3, serialized.getInt("Size"));
        assertEquals(1, items.size());
        assertEquals(1, items.getCompound(0).getInt("Slot"));
    }

    @Test
    void loadsExistingItemStackHandlerDataWithoutReportingAChange() {
        CompoundTag itemPrefix = new CompoundTag();
        itemPrefix.putInt("Slot", 2);
        CompoundTag item = new ItemStack(Items.EMERALD, 7).save(itemPrefix);
        ListTag items = new ListTag();
        items.add(item);
        CompoundTag serialized = new CompoundTag();
        serialized.putInt("Size", 4);
        serialized.put("Items", items);
        AtomicInteger changes = new AtomicInteger();
        PersistedContainer container = new PersistedContainer(1, changes::incrementAndGet);

        container.deserializeNBT(serialized);

        assertEquals(4, container.getContainerSize());
        assertTrue(container.getItem(0).isEmpty());
        assertSame(Items.EMERALD, container.getItem(2).getItem());
        assertEquals(7, container.getItem(2).getCount());
        assertEquals(0, changes.get());
    }
}
