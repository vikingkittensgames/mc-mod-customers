package com.vikingkittens.mc.customers.client.appearance.mca;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McaCustomersVillagerWeightedSelectorTest {
    @Test
    void selectsEntriesUsingTheirWeights() {
        List<Entry> entries = List.of(
                new Entry("first", 1.0F),
                new Entry("second", 3.0F)
        );

        assertEquals(
                "first",
                select(entries, 0.0F).name()
        );
        assertEquals(
                "first",
                select(entries, 0.249F).name()
        );
        assertEquals(
                "second",
                select(entries, 0.25F).name()
        );
        assertEquals(
                "second",
                select(entries, 0.999F).name()
        );
    }

    @Test
    void returnsTheFallbackForAnEmptyPool() {
        Entry fallback = new Entry("fallback", 1.0F);

        assertEquals(
                fallback,
                McaCustomersVillagerWeightedSelector.select(
                        List.of(),
                        Entry::weight,
                        0.5F,
                        fallback
                )
        );
    }

    private static Entry select(List<Entry> entries, float choice) {
        return McaCustomersVillagerWeightedSelector.select(
                entries,
                Entry::weight,
                choice,
                null
        );
    }

    private record Entry(String name, float weight) {}
}
