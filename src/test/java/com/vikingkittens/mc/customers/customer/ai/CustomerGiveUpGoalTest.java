package com.vikingkittens.mc.customers.customer.ai;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerGiveUpGoalTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void randomlySelectsGiveUpMessageAndRemainingItem() {
        RandomSource random = mock(RandomSource.class);
        MerchantOffer appleOffer = mockOffer("Apple");
        MerchantOffer carrotOffer = mockOffer("Carrot");

        when(random.nextInt(15)).thenReturn(14);
        when(random.nextInt(2)).thenReturn(1);

        Component message = CustomerGiveUpGoal.createGiveUpMessage(
                random,
                List.of(appleOffer, carrotOffer)
        );
        TranslatableContents contents =
                (TranslatableContents)message.getContents();

        assertEquals("messages.customers.give_up15", contents.getKey());
        assertEquals(Component.literal("Carrot"), contents.getArgs()[0]);
    }

    @Test
    void excludesCompletedOffersFromItemSelection() {
        RandomSource random = mock(RandomSource.class);
        MerchantOffer completedOffer = mockOffer("Apple");
        MerchantOffer remainingOffer = mockOffer("Carrot");

        when(completedOffer.isOutOfStock()).thenReturn(true);
        when(random.nextInt(15)).thenReturn(5);
        when(random.nextInt(1)).thenReturn(0);

        Component message = CustomerGiveUpGoal.createGiveUpMessage(
                random,
                List.of(completedOffer, remainingOffer)
        );
        TranslatableContents contents =
                (TranslatableContents)message.getContents();

        assertEquals("messages.customers.give_up6", contents.getKey());
        assertEquals(Component.literal("Carrot"), contents.getArgs()[0]);
        verify(completedOffer).isOutOfStock();
    }

    @Test
    void safelyBuildsMessageWithoutRemainingOffers() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(15)).thenReturn(0);

        Component message = CustomerGiveUpGoal.createGiveUpMessage(
                random,
                List.of()
        );
        TranslatableContents contents =
                (TranslatableContents)message.getContents();

        assertEquals("messages.customers.give_up1", contents.getKey());
        assertEquals(Component.empty(), contents.getArgs()[0]);
    }

    private static MerchantOffer mockOffer(String itemName) {
        MerchantOffer offer = mock(MerchantOffer.class);
        ItemStack stack = mock(ItemStack.class);
        when(offer.getBaseCostA()).thenReturn(stack);
        when(stack.getHoverName()).thenReturn(Component.literal(itemName));
        return offer;
    }
}
