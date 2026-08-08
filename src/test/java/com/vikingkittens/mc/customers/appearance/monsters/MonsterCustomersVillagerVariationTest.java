package com.vikingkittens.mc.customers.appearance.monsters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonsterCustomersVillagerVariationTest {
    @Test
    void dividesTheSeedRangeEvenlyAcrossMonsterVariations() {
        assertEquals(MonsterCustomersVillagerVariation.ZOMBIE, MonsterCustomersVillagerVariation.fromSeed(0.0F));
        assertEquals(MonsterCustomersVillagerVariation.SKELETON, MonsterCustomersVillagerVariation.fromSeed(1.0F / 6.0F));
        assertEquals(MonsterCustomersVillagerVariation.WITCH, MonsterCustomersVillagerVariation.fromSeed(2.0F / 6.0F));
        assertEquals(MonsterCustomersVillagerVariation.HUSK, MonsterCustomersVillagerVariation.fromSeed(3.0F / 6.0F));
        assertEquals(MonsterCustomersVillagerVariation.DROWNED, MonsterCustomersVillagerVariation.fromSeed(4.0F / 6.0F));
        assertEquals(MonsterCustomersVillagerVariation.STRAY, MonsterCustomersVillagerVariation.fromSeed(5.0F / 6.0F));
        assertEquals(MonsterCustomersVillagerVariation.STRAY, MonsterCustomersVillagerVariation.fromSeed(1.0F));
    }
}
