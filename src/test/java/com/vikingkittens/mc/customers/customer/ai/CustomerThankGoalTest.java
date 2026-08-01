package com.vikingkittens.mc.customers.customer.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerState;
import com.vikingkittens.mc.customers.customer.CustomerVillagerEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerThankGoalTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void remembersFinalRemainingItemForThankYouMessage() {
        CustomerVillagerEntity customer = mock(CustomerVillagerEntity.class);
        MerchantOffers offers = new MerchantOffers();
        MerchantOffer offer = mock(MerchantOffer.class);
        ItemStack stack = mock(ItemStack.class);
        RandomSource random = mock(RandomSource.class);
        Level level = mock(Level.class);

        offers.add(offer);
        when(customer.getOffers()).thenReturn(offers);
        when(customer.getState()).thenReturn(CustomerState.BUYING);
        when(customer.getRandom()).thenReturn(random);
        when(customer.level()).thenReturn(level);
        when(customer.getSpawnerPos()).thenReturn(BlockPos.ZERO);
        when(offer.isOutOfStock()).thenReturn(false);
        when(offer.getBaseCostA()).thenReturn(stack);
        when(stack.getHoverName()).thenReturn(Component.literal("Apple"));
        when(random.nextInt(15)).thenReturn(1);

        CustomerThankGoal goal = new CustomerThankGoal(customer);

        assertFalse(goal.canUse());
        offers.clear();
        assertTrue(goal.canUse());

        goal.start();
        for (int tick = 0; tick < 20; tick++) {
            goal.tick();
        }

        ArgumentCaptor<Component> message =
                ArgumentCaptor.forClass(Component.class);
        verify(customer).sentPlayersMessage(message.capture());
        TranslatableContents contents =
                (TranslatableContents)message.getValue().getContents();

        assertEquals(
                "messages.customers.thank_you2",
                contents.getKey()
        );
        assertEquals(
                Component.literal("Apple"),
                contents.getArgs()[0]
        );
    }

    @Test
    void randomlySelectsThankYouMessage() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(15)).thenReturn(14);

        Component message = CustomerThankGoal.createThankYouMessage(
                random,
                Component.literal("Carrot")
        );
        TranslatableContents contents =
                (TranslatableContents)message.getContents();

        assertEquals(
                "messages.customers.thank_you15",
                contents.getKey()
        );
        assertEquals(
                Component.literal("Carrot"),
                contents.getArgs()[0]
        );
    }
}
