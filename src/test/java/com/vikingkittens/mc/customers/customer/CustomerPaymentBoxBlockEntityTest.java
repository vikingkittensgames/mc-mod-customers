package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.IItemHandler;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerPaymentBoxBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void providesTwentySevenContainerSlots() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();

        assertInstanceOf(Container.class, paymentBox);
        assertEquals(27, paymentBox.getContainerSize());
    }

    @Test
    void storesItemsThroughTheContainerInterface() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();
        ItemStack emeralds = new ItemStack(Items.EMERALD, 12);

        paymentBox.setItem(26, emeralds);

        assertEquals(emeralds, paymentBox.getItem(26));
    }

    @Test
    void createsTheStandardThreeRowChestMenu() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();
        Inventory inventory = new Inventory(mock(Player.class));

        AbstractContainerMenu menu =
                paymentBox.createContainerMenu(7, inventory);

        ChestMenu chestMenu = assertInstanceOf(ChestMenu.class, menu);
        assertEquals(3, chestMenu.getRowCount());
        assertEquals(paymentBox, chestMenu.getContainer());
    }

    @Test
    void exposesAllSlotsThroughTheAutomationItemHandler() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();
        IItemHandler itemHandler = paymentBox.getItemHandler();

        ItemStack remainder = itemHandler.insertItem(
                26,
                new ItemStack(Items.EMERALD, 12),
                false
        );

        assertEquals(ItemStack.EMPTY, remainder);
        assertEquals(27, itemHandler.getSlots());
        assertStack(Items.EMERALD, 12, paymentBox.getItem(26));

        ItemStack extracted = itemHandler.extractItem(26, 5, false);

        assertStack(Items.EMERALD, 5, extracted);
        assertStack(Items.EMERALD, 7, paymentBox.getItem(26));
    }

    @SuppressWarnings("unchecked")
    private static CustomerPaymentBoxBlockEntity createPaymentBox() {
        BlockEntityType<CustomerPaymentBoxBlockEntity> type =
                mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomerPaymentBoxBlockEntity(
                type,
                BlockPos.ZERO,
                state
        );
    }

    private static void assertStack(
            net.minecraft.world.item.Item item,
            int count,
            ItemStack actual
    ) {
        assertTrue(ItemStack.isSameItemSameComponents(
                new ItemStack(item),
                actual
        ));
        assertEquals(count, actual.getCount());
    }
}
