package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomersDisplayCaseBlockMenuTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void mapsTopShelfSlotsToTheLeftGroup() {
        assertEquals(25, CustomersDisplayCaseBlockMenu.getDisplaySlotX(0));
        assertEquals(61, CustomersDisplayCaseBlockMenu.getDisplaySlotX(2));
        assertEquals(25, CustomersDisplayCaseBlockMenu.getDisplaySlotX(3));
        assertEquals(61, CustomersDisplayCaseBlockMenu.getDisplaySlotX(5));
        assertEquals(7, CustomersDisplayCaseBlockMenu.getDisplaySlotY(0));
        assertEquals(25, CustomersDisplayCaseBlockMenu.getDisplaySlotY(3));
    }

    @Test
    void mapsBottomShelfSlotsToTheRightGroup() {
        assertEquals(97, CustomersDisplayCaseBlockMenu.getDisplaySlotX(6));
        assertEquals(133, CustomersDisplayCaseBlockMenu.getDisplaySlotX(8));
        assertEquals(97, CustomersDisplayCaseBlockMenu.getDisplaySlotX(9));
        assertEquals(133, CustomersDisplayCaseBlockMenu.getDisplaySlotX(11));
        assertEquals(7, CustomersDisplayCaseBlockMenu.getDisplaySlotY(6));
        assertEquals(25, CustomersDisplayCaseBlockMenu.getDisplaySlotY(9));
    }

    @Test
    void shiftClicksDisplayCaseStacksIntoPlayerInventory() {
        Player player = mock(Player.class);
        Inventory inventory = new Inventory(player);
        SimpleContainer displayCase = new SimpleContainer(12);
        displayCase.setItem(0, new ItemStack(Items.COOKIE, 8));
        CustomersDisplayCaseBlockMenu menu =
                new CustomersDisplayCaseBlockMenu(
                        mock(MenuType.class),
                        1,
                        inventory,
                        displayCase
                );

        ItemStack moved = menu.quickMoveStack(player, 0);

        assertTrue(moved.is(Items.COOKIE));
        assertEquals(8, moved.getCount());
        assertTrue(displayCase.getItem(0).isEmpty());
        assertEquals(8, countItems(inventory, Items.COOKIE));
    }

    @Test
    void shiftClicksPlayerStacksIntoTheDisplayCase() {
        Player player = mock(Player.class);
        Inventory inventory = new Inventory(player);
        inventory.setItem(0, new ItemStack(Items.APPLE, 6));
        SimpleContainer displayCase = new SimpleContainer(12);
        CustomersDisplayCaseBlockMenu menu =
                new CustomersDisplayCaseBlockMenu(
                        mock(MenuType.class),
                        1,
                        inventory,
                        displayCase
                );

        ItemStack moved = menu.quickMoveStack(player, 39);

        assertTrue(moved.is(Items.APPLE));
        assertEquals(6, moved.getCount());
        assertTrue(inventory.getItem(0).isEmpty());
        assertEquals(6, displayCase.getItem(0).getCount());
    }

    @Test
    void leavesPlayerStackUntouchedWhenDisplayCaseIsFull() {
        Player player = mock(Player.class);
        Inventory inventory = new Inventory(player);
        inventory.setItem(0, new ItemStack(Items.APPLE, 6));
        SimpleContainer displayCase = new SimpleContainer(12);
        for (int slot = 0; slot < 12; slot++) {
            displayCase.setItem(
                    slot,
                    new ItemStack(Items.COOKIE, 64)
            );
        }
        CustomersDisplayCaseBlockMenu menu =
                new CustomersDisplayCaseBlockMenu(
                        mock(MenuType.class),
                        1,
                        inventory,
                        displayCase
                );

        assertTrue(menu.quickMoveStack(player, 39).isEmpty());
        assertEquals(6, inventory.getItem(0).getCount());
    }

    private static int countItems(Inventory inventory, Item item) {
        int count = 0;
        for (int slot = 0;
                slot < inventory.getContainerSize();
                slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
