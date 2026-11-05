package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

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
    void disablesShiftClickTransfers() {
        Player player = mock(Player.class);
        Inventory inventory = new Inventory(player);
        CustomersDisplayCaseBlockMenu menu =
                new CustomersDisplayCaseBlockMenu(
                        mock(MenuType.class),
                        1,
                        inventory,
                        new SimpleContainer(12)
                );

        assertTrue(menu.quickMoveStack(player, 0).isEmpty());
        assertTrue(menu.quickMoveStack(player, 12).isEmpty());
    }
}
