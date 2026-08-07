package com.vikingkittens.mc.customers.client.customer;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerBossBarRendererTest {
    @Test
    void usesAnOpaqueTitleColor() {
        assertEquals(0xFFFFFFFF, CustomerBossBarRenderer.TITLE_COLOR);
    }

    @Test
    void includesPlayersWithinTheSpawnerViewRange() {
        BlockPos spawnerPos = BlockPos.ZERO;

        assertTrue(CustomerBossBarRenderer.isInRange(spawnerPos, 0.5D, 0.5D, 64.5D));
        assertFalse(CustomerBossBarRenderer.isInRange(spawnerPos, 0.5D, 0.5D, 64.6D));
    }

    @Test
    void addsTheItemRowsAndBarGapToTheVanillaIncrement() {
        assertTrue(CustomerBossBarRenderer.calculateIncrement(19, 16) == 38);
        assertTrue(CustomerBossBarRenderer.calculateIncrement(19, 34) == 56);
    }

    @Test
    void preservesTheVanillaIncrementWhenThereAreNoItemRows() {
        assertTrue(CustomerBossBarRenderer.calculateIncrement(19, 0) == 19);
    }

    @Test
    void usesAlmostTheFullScreenWidthForCustomerGroups() {
        assertEquals(300, CustomerBossBarRenderer.calculateLayoutWidth(320));
    }

    @Test
    void keepsTheLayoutWidthPositiveOnVeryNarrowScreens() {
        assertEquals(1, CustomerBossBarRenderer.calculateLayoutWidth(15));
    }
    @Test
    void blinksWarningsEveryHalfSecond() {
        assertTrue(CustomerBossBarRenderer.isWarningBlinkOn(0));
        assertTrue(CustomerBossBarRenderer.isWarningBlinkOn(499));
        assertFalse(CustomerBossBarRenderer.isWarningBlinkOn(500));
        assertFalse(CustomerBossBarRenderer.isWarningBlinkOn(999));
        assertTrue(CustomerBossBarRenderer.isWarningBlinkOn(1000));
    }
}
