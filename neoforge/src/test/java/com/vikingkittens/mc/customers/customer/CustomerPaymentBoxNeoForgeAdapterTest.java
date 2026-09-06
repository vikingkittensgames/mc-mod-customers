package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerPaymentBoxNeoForgeAdapterTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void exposesAllSlotsThroughTheNeoForgeAutomationAdapter() {
        CustomerPaymentBoxBlockEntity paymentBox = createPaymentBox();
        ResourceHandler<ItemResource> itemHandler = VanillaContainerWrapper.of(paymentBox);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(12, itemHandler.insert(26, ItemResource.of(Items.EMERALD), 12, transaction));
            transaction.commit();
        }

        assertEquals(27, itemHandler.size());
        assertStack(Items.EMERALD, 12, paymentBox.getItem(26));

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(5, itemHandler.extract(26, ItemResource.of(Items.EMERALD), 5, transaction));
            transaction.commit();
        }

        assertStack(Items.EMERALD, 7, paymentBox.getItem(26));
    }

    private static CustomerPaymentBoxBlockEntity createPaymentBox() {
        BlockState state = Blocks.BARREL.defaultBlockState();
        return new CustomerPaymentBoxBlockEntity(BlockEntityType.BARREL, BlockPos.ZERO, state);
    }

    private static void assertStack(Item item, int count, ItemStack actual) {
        assertTrue(ItemStack.isSameItemSameComponents(new ItemStack(item), actual));
        assertEquals(count, actual.getCount());
    }
}
