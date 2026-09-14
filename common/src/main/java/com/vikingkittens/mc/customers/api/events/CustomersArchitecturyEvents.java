package com.vikingkittens.mc.customers.api.events;

import java.util.function.Predicate;

import com.vikingkittens.mc.customers.compatability.CustomersServices;

/**
 * Planned public bridge from Customers internal events to Architectury events.
 *
 * <p>Define each public callback as a functional interface and expose an
 * {@code Event<Callback>} created with {@code EventFactory.createLoop()} when
 * every listener should be notified. Other mods register listeners through
 * {@code EVENT.register(listener)}. Customers publishes an event by invoking
 * {@code EVENT.invoker().callback(...)} after receiving the corresponding
 * internal event.
 *
 * <p>Use an event-result factory only when listeners genuinely need to
 * interrupt or alter an operation. Customer completion notifications occur
 * after the transaction commits, so they should be non-cancellable loop events.
 * Keep callbacks server-side unless their contract explicitly identifies a
 * client event, use immutable values or defensive copies for mutable values
 * such as {@code ItemStack}, and document the thread and event timing.
 *
 * <p>Do not reference Architectury API classes during Customers startup unless
 * {@link #isEnabled()} is true. Consumers must declare Architectury API and
 * Customers as dependencies, then register their callback during their own
 * initialization. Customers remains usable without Architectury installed;
 * only this public event bridge is disabled.
 */
public final class CustomersArchitecturyEvents {
    public static final String MOD_ID = "architectury";

    private CustomersArchitecturyEvents() {}

    public static void initialize() {
        if (!isEnabled()) {
            return;
        }
    }

    public static boolean isEnabled() {
        return isEnabled(CustomersServices.platform()::isModLoaded);
    }

    static boolean isEnabled(Predicate<String> loadedMods) {
        return loadedMods.test(MOD_ID);
    }
}
