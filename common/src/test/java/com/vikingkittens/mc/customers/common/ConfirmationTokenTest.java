package com.vikingkittens.mc.customers.common;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmationTokenTest {
    @Test
    void acceptsOnlyItsInitiatingPlayerAndTokenBeforeExpiry() {
        UUID playerId = UUID.randomUUID();
        ConfirmationToken confirmation = ConfirmationToken.create(playerId, 0L, 60_000L);

        assertTrue(ConfirmationToken.isKnownFor(playerId, confirmation.token(), 59_999L));
        assertFalse(ConfirmationToken.isKnownFor(UUID.randomUUID(), confirmation.token(), 59_999L));
        assertFalse(ConfirmationToken.isKnownFor(playerId, UUID.randomUUID(), 59_999L));
    }

    @Test
    void rejectsConfirmationAtOrAfterExpiry() {
        UUID playerId = UUID.randomUUID();
        ConfirmationToken confirmation = ConfirmationToken.create(playerId, 0L, 60_000L);

        assertFalse(ConfirmationToken.isKnownFor(playerId, confirmation.token(), 60_000L));
        assertFalse(ConfirmationToken.isKnownFor(playerId, confirmation.token(), 60_001L));
    }

    @Test
    void removesTokensExplicitly() {
        UUID playerId = UUID.randomUUID();
        ConfirmationToken confirmation = ConfirmationToken.create(playerId, 0L, 60_000L);

        ConfirmationToken.remove(confirmation.token());

        assertFalse(ConfirmationToken.isKnownFor(playerId, confirmation.token(), 1L));
    }
}
