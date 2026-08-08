package com.vikingkittens.mc.customers.customer;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.compatability.persistence.DataReader;
import com.vikingkittens.mc.customers.compatability.persistence.DataWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerVillagerEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void ignoresFallDamage() {
        CustomerVillagerEntity customer = mock(
                CustomerVillagerEntity.class,
                CALLS_REAL_METHODS
        );

        assertFalse(customer.causeFallDamage(
                100.0F,
                1.0F,
                mock(DamageSource.class)
        ));
    }
    /** Returns one empty container for each consumed item. */
    @Test
    void createsPickupCounterTradeRemainders() {
        ItemStack water =
                PotionContents.createItemStack(Items.POTION, Potions.WATER);

        assertEquals(
                Items.GLASS_BOTTLE,
                CustomerVillagerEntity
                        .getTradeRemainderStack(water)
                        .getItem()
        );
        assertEquals(
                Items.BUCKET,
                CustomerVillagerEntity
                        .getTradeRemainderStack(
                                new ItemStack(Items.MILK_BUCKET)
                        )
                        .getItem()
        );
        ItemStack bowls = CustomerVillagerEntity.getTradeRemainderStack(
                new ItemStack(Items.MUSHROOM_STEW, 3)
        );
        assertEquals(Items.BOWL, bowls.getItem());
        assertEquals(3, bowls.getCount());
    }
    @Test
    void roundTripsCounterTargetBlockPosition() {
        BlockPos targetPosition = new BlockPos(10, 64, -20);
        DataWriter output = mock(DataWriter.class);
        CustomerVillagerEntity.saveCounterTargetBlockPos(output, targetPosition);
        verify(output).putBlockPos("CounterTargetBlockPos", targetPosition);

        DataReader input = mock(DataReader.class);
        when(input.getBlockPos("CounterTargetBlockPos"))
                .thenReturn(java.util.Optional.of(targetPosition));

        assertEquals(
                targetPosition,
                CustomerVillagerEntity.readCounterTargetBlockPos(input).orElseThrow()
        );
    }

    @Test
    void readsPersistedAppearanceContext() {
        CustomerVillagerEntity customer = mock(
                CustomerVillagerEntity.class,
                CALLS_REAL_METHODS
        );
        DataReader input = mock(DataReader.class);
        ResourceLocation appearance =
                ResourceLocation.parse("customers:monsters");
        doNothing().when(customer).setAppearanceId(appearance);
        doNothing().when(customer).setVariationSeed(0.75F);
        doNothing().when(customer).setSpawnerMode(CustomerSpawnerMode.NIGHT);
        doNothing().when(customer).setSpecial(true);
        when(input.getString("CustomersAppearance"))
                .thenReturn(Optional.of("customers:monsters"));
        when(input.getFloat("CustomersVariationSeed"))
                .thenReturn(Optional.of(0.75F));
        when(input.getString("CustomersAppearanceSpawnerMode"))
                .thenReturn(Optional.of(CustomerSpawnerMode.NIGHT.name()));
        when(input.getBoolean("CustomersAppearanceSpecial"))
                .thenReturn(true);

        customer.readAppearanceData(input);

        verify(customer).setAppearanceId(appearance);
        verify(customer).setVariationSeed(0.75F);
        verify(customer).setSpawnerMode(CustomerSpawnerMode.NIGHT);
        verify(customer).setSpecial(true);
    }
}
