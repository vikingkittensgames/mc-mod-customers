package com.vikingkittens.mc.customers.customer.ai;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Verifies customers taking complete offers from pickup counters. */
class CustomerTakePickupItemGoalTest {
    /** Initializes Minecraft item registries used by item-stack tests. */
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    /** Takes at most one complete offer and credits its depositing player. */
    @Test
    void takesOneOfferFromPickupCounter() {
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        CustomerMoveToCounterGoal moveGoal =
                mock(CustomerMoveToCounterGoal.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        Level level = mock(Level.class);
        MerchantOffer firstOffer = mock(MerchantOffer.class);
        MerchantOffer secondOffer = mock(MerchantOffer.class);
        MerchantOffers offers = new MerchantOffers();
        ItemStack firstCost = new ItemStack(Items.BREAD, 5);
        UUID crafterId = UUID.randomUUID();
        BlockPos counterPosition = new BlockPos(2, 64, 3);

        offers.add(firstOffer);
        offers.add(secondOffer);
        moveGoal.counterPosition = counterPosition;
        when(customer.level()).thenReturn(level);
        when(customer.getState()).thenReturn(CustomerState.BUYING);
        when(customer.getOffers()).thenReturn(offers);
        when(level.getGameTime()).thenReturn(100L);
        when(level.getBlockEntity(counterPosition)).thenReturn(counter);
        when(firstOffer.isOutOfStock()).thenReturn(false);
        when(secondOffer.isOutOfStock()).thenReturn(false);
        when(counter.takeMatchingStoredStack(firstOffer))
                .thenReturn(new CustomerPickupCounterBlockEntity.StoredStack(
                        firstCost.copy(),
                        true,
                        crafterId
                ));

        CustomerTakePickupItemGoal goal =
                new CustomerTakePickupItemGoal(customer, moveGoal);

        assertTrue(goal.canUse());
        goal.start();

        verify(customer).completePickupCounterOffer(
                firstOffer,
                crafterId,
                counterPosition
        );
        verify(secondOffer, never()).getCostA();
    }

    /** Waits one second after each attempt before trying the counter again. */
    @Test
    void waitsOneSecondBetweenAttempts() {
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        CustomerMoveToCounterGoal moveGoal =
                mock(CustomerMoveToCounterGoal.class);
        CustomerPickupCounterBlockEntity counter =
                mock(CustomerPickupCounterBlockEntity.class);
        Level level = mock(Level.class);
        MerchantOffer offer = mock(MerchantOffer.class);
        MerchantOffers offers = new MerchantOffers();
        ItemStack cost = new ItemStack(Items.BREAD, 5);
        BlockPos counterPosition = new BlockPos(2, 64, 3);

        offers.add(offer);
        moveGoal.counterPosition = counterPosition;
        when(customer.level()).thenReturn(level);
        when(customer.getState()).thenReturn(CustomerState.BUYING);
        when(customer.getOffers()).thenReturn(offers);
        when(level.getBlockEntity(counterPosition)).thenReturn(counter);
        when(offer.isOutOfStock()).thenReturn(false);
        when(offer.getCostA()).thenReturn(cost);
        when(counter.takeMatchingStoredStack(offer))
                .thenReturn(new CustomerPickupCounterBlockEntity.StoredStack(
                        ItemStack.EMPTY,
                        false,
                        null
                ));
        when(level.getGameTime()).thenReturn(100L, 119L, 120L);

        CustomerTakePickupItemGoal goal =
                new CustomerTakePickupItemGoal(customer, moveGoal);

        assertTrue(goal.canUse());
        goal.start();
        assertFalse(goal.canUse());
        assertTrue(goal.canUse());
    }

    /** Rejects customers that are not actively buying. */
    @Test
    void requiresBuyingState() {
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        CustomerMoveToCounterGoal moveGoal =
                mock(CustomerMoveToCounterGoal.class);

        when(customer.getState()).thenReturn(CustomerState.IN_LINE);

        CustomerTakePickupItemGoal goal =
                new CustomerTakePickupItemGoal(customer, moveGoal);

        assertFalse(goal.canUse());
    }
}
