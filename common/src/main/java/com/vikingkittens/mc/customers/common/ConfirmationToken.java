package com.vikingkittens.mc.customers.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

record ConfirmationToken(
        UUID playerId,
        UUID token,
        long expiresAtMillis
) {
    private static final Map<UUID, ConfirmationToken> KNOWN_TOKENS = new HashMap<>();

    static ConfirmationToken create(UUID playerId, long nowMillis, long lifetimeMillis) {
        removeExpired(nowMillis);
        ConfirmationToken confirmation = new ConfirmationToken(playerId, UUID.randomUUID(), nowMillis + lifetimeMillis);
        KNOWN_TOKENS.put(confirmation.token(), confirmation);
        return confirmation;
    }

    static boolean isKnownFor(UUID playerId, UUID token, long nowMillis) {
        removeExpired(nowMillis);
        ConfirmationToken confirmation = KNOWN_TOKENS.get(token);
        return confirmation != null && confirmation.matches(playerId, token, nowMillis);
    }

    static void remove(UUID token) {
        KNOWN_TOKENS.remove(token);
    }

    private boolean matches(UUID playerId, UUID token, long nowMillis) {
        return this.playerId.equals(playerId)
                && this.token.equals(token)
                && nowMillis < expiresAtMillis;
    }

    private static void removeExpired(long nowMillis) {
        KNOWN_TOKENS.entrySet().removeIf(entry -> entry.getValue().expiresAtMillis() <= nowMillis);
    }
}
