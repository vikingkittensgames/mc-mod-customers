package com.vikingkittens.mc.customers.client.customer;

import com.vikingkittens.mc.customers.customer.CustomerCounterMarker;

import java.util.List;

final class CustomerCounterMarkerManager {
    static final long DURATION_MILLIS = 90_000L;

    private static List<CustomerCounterMarker> markers = List.of();
    private static long expiresAt;

    private CustomerCounterMarkerManager() {
    }

    static void show(List<CustomerCounterMarker> newMarkers, long currentTime) {
        markers = List.copyOf(newMarkers);
        expiresAt = currentTime + DURATION_MILLIS;
    }

    static List<CustomerCounterMarker> get(long currentTime) {
        if (currentTime >= expiresAt) {
            clear();
        }
        return markers;
    }

    static void clear() {
        markers = List.of();
        expiresAt = 0L;
    }
}
