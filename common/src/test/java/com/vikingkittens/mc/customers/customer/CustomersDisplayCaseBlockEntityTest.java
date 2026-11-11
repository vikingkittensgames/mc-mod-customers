package com.vikingkittens.mc.customers.customer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomersDisplayCaseBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void providesTwelvePersistentSlots() {
        CustomersDisplayCaseBlockEntity original = createDisplayCase();
        original.setItem(0, new ItemStack(Items.APPLE, 3));
        original.setItem(11, new ItemStack(Items.BREAD, 5));
        CompoundTag tag = new CompoundTag();

        original.saveAdditional(tag, RegistryAccess.EMPTY);
        CustomersDisplayCaseBlockEntity restored =
                createDisplayCase();
        restored.loadAdditional(tag, RegistryAccess.EMPTY);

        assertEquals(12, restored.getContainerSize());
        assertTrue(restored.getItem(0).is(Items.APPLE));
        assertEquals(3, restored.getItem(0).getCount());
        assertTrue(restored.getItem(11).is(Items.BREAD));
        assertEquals(5, restored.getItem(11).getCount());
    }

    @Test
    void extractsOnlyTheRequestedCountFromAMatchingSlot() {
        CustomersDisplayCaseBlockEntity displayCase = createDisplayCase();
        displayCase.setItem(0, new ItemStack(Items.COOKIE, 8));

        ItemStack extracted = displayCase.extract(
                0,
                new ItemStack(Items.COOKIE),
                3
        );

        assertTrue(extracted.is(Items.COOKIE));
        assertEquals(3, extracted.getCount());
        assertEquals(5, displayCase.getItem(0).getCount());
    }

    @Test
    void doesNotExtractWhenTheExpectedStackDoesNotMatch() {
        CustomersDisplayCaseBlockEntity displayCase = createDisplayCase();
        displayCase.setItem(0, new ItemStack(Items.COOKIE, 8));

        ItemStack extracted = displayCase.extract(
                0,
                new ItemStack(Items.APPLE),
                3
        );

        assertTrue(extracted.isEmpty());
        assertEquals(8, displayCase.getItem(0).getCount());
    }

    @SuppressWarnings("unchecked")
    private static CustomersDisplayCaseBlockEntity createDisplayCase() {
        BlockEntityType<CustomersDisplayCaseBlockEntity> type =
                mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        return new CustomersDisplayCaseBlockEntity(
                type,
                BlockPos.ZERO,
                state
        );
    }
}
