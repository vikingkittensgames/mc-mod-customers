package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.function.Predicate;

final class CustomerQuickSellOfferSelector {
    private CustomerQuickSellOfferSelector() {
    }

    static <T> T find(
            List<T> offers,
            Predicate<T> isAvailable,
            Predicate<T> isSatisfied
    ) {
        return offers.stream()
                .filter(isAvailable)
                .filter(isSatisfied)
                .findFirst()
                .orElse(null);
    }
}