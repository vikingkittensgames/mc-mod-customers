package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerPaymentBoxNeoForgeAdapterTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesAllSlotsThroughTheNeoForgeAutomationAdapter() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();
        IItemHandler itemHandler = new InvWrapper(paymentBox);

        ItemStack remainder = itemHandler.insertItem(26, new ItemStack(Items.EMERALD, 12), false);

        assertTrue(remainder.isEmpty());
        assertEquals(27, itemHandler.getSlots());
        assertStack(Items.EMERALD, 12, paymentBox.getItem(26));

        ItemStack extracted = itemHandler.extractItem(26, 5, false);

        assertStack(Items.EMERALD, 5, extracted);
        assertStack(Items.EMERALD, 7, paymentBox.getItem(26));
    }

    @SuppressWarnings("unchecked")
    private static CustomerPaymentBoxBlockEntity createPaymentBox() {
        BlockEntityType<CustomerPaymentBoxBlockEntity> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomerPaymentBoxBlockEntity(type, BlockPos.ZERO, state);
    }

    private static void assertStack(Item item, int count, ItemStack actual) {
        assertTrue(ItemStack.isSameItemSameComponents(new ItemStack(item), actual));
        assertEquals(count, actual.getCount());
    }
}
