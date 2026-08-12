package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerQuickSellOfferSelectorTest {
    @Test
    void findsFirstAvailableOfferSatisfiedByHeldStackAndCount() {
        TestOffer outOfStock = new TestOffer(false, true);
        TestOffer insufficientOrDifferent = new TestOffer(true, false);
        TestOffer matching = new TestOffer(true, true);

        TestOffer result = CustomerQuickSellOfferSelector.find(
                List.of(outOfStock, insufficientOrDifferent, matching),
                TestOffer::available,
                TestOffer::satisfied
        );

        assertEquals(matching, result);
    }

    @Test
    void returnsNullWhenNoOfferAcceptsHeldStack() {
        TestOffer offer = new TestOffer(true, false);

        assertNull(CustomerQuickSellOfferSelector.find(
                List.of(offer),
                TestOffer::available,
                TestOffer::satisfied
        ));
    }

    private record TestOffer(boolean available, boolean satisfied) {
    }
}
