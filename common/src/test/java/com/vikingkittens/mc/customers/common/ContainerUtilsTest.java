package com.vikingkittens.mc.customers.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void atomicallyCombinesStacksBeforeUsingEmptySlots() {
        SimpleContainer container = new SimpleContainer(3);
        container.setItem(0, new ItemStack(Items.EMERALD, 60));
        container.setItem(1, new ItemStack(Items.DIRT, 64));

        boolean inserted = ContainerUtils.tryInsertStacked(container, new ItemStack(Items.EMERALD, 8));

        assertTrue(inserted);
        assertEquals(64, container.getItem(0).getCount());
        assertEquals(4, container.getItem(2).getCount());
    }

    @Test
    void rejectsTheEntireStackWhenOnlyPartFits() {
        SimpleContainer container = new SimpleContainer(2);
        container.setItem(0, new ItemStack(Items.EMERALD, 60));
        container.setItem(1, new ItemStack(Items.DIRT, 64));

        boolean inserted = ContainerUtils.tryInsertStacked(container, new ItemStack(Items.EMERALD, 8));

        assertFalse(inserted);
        assertEquals(60, container.getItem(0).getCount());
        assertEquals(64, container.getItem(1).getCount());
    }

    @Test
    void acceptsAnEmptyStackWithoutChangingTheContainer() {
        SimpleContainer container = new SimpleContainer(1);

        assertTrue(ContainerUtils.tryInsertStacked(container, ItemStack.EMPTY));
        assertTrue(container.isEmpty());
    }

    @Test
    void identifiesWhetherAnyContainerSlotHasItems() {
        SimpleContainer container = new SimpleContainer(2);

        assertFalse(ContainerUtils.hasItems(container));

        container.setItem(1, new ItemStack(Items.BREAD));

        assertTrue(ContainerUtils.hasItems(container));
    }
}
