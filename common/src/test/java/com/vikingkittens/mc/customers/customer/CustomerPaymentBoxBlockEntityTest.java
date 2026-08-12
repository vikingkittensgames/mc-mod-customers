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

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerPaymentBoxBlockEntityTest {
    @Test
    void atomicallyAcceptsACompletePayment() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();

        boolean inserted = paymentBox.tryInsertPayment(
                new ItemStack(Items.EMERALD, 20)
        );

        assertTrue(inserted);
        assertEquals(
                20,
                paymentBox.getItem(0).getCount()
        );
    }

    @Test
    void rejectsAPaymentWhenOnlyPartOfItFits() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();
        for (int slot = 0;
                slot < paymentBox.getContainerSize();
                slot++) {
            paymentBox.setItem(
                    slot,
                    new ItemStack(Items.EMERALD, 64)
            );
        }
        paymentBox.setItem(
                paymentBox.getContainerSize() - 1,
                new ItemStack(Items.EMERALD, 60)
        );

        boolean inserted = paymentBox.tryInsertPayment(
                new ItemStack(Items.EMERALD, 8)
        );

        assertFalse(inserted);
        assertEquals(
                60,
                paymentBox.getItem(
                        paymentBox.getContainerSize() - 1
                ).getCount()
        );
    }

    @Test
    void treatsAnEmptyPaymentAsSuccessfullyInserted() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();

        assertTrue(paymentBox.tryInsertPayment(ItemStack.EMPTY));
        assertTrue(paymentBox.isEmpty());
    }

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
