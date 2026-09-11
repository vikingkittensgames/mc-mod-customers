package com.vikingkittens.mc.customers.economy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EconomyItemCostTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void defaultProviderReturnsOneEmeraldForEachInputItem() {
        EconomyItemCostProviderDefault provider = new EconomyItemCostProviderDefault(mock(Logger.class));

        ItemStack cost = provider.calculateItemStackCost(new ItemStack(Items.APPLE, 5));

        assertSame(Items.EMERALD, cost.getItem());
        assertEquals(5, cost.getCount());
    }

    @Test
    void defaultProviderLogsUniqueFallbackItemsAtTheNextServerDay() {
        Logger logger = mock(Logger.class);
        EconomyItemCostProviderDefault provider = new EconomyItemCostProviderDefault(logger);
        provider.serverStarted(10, 1_000L);
        provider.calculateItemStackCost(Items.APPLE.getDefaultInstance());
        provider.calculateItemStackCost(Items.APPLE.getDefaultInstance());
        provider.calculateItemStackCost(Items.CARROT.getDefaultInstance());

        provider.tick(10, 2_000L);
        verify(logger, never()).warn(any(String.class), any(), any());

        provider.tick(11, 3_000L);
        verify(logger).warn(
                eq("{}: {}"),
                eq(EconomyItemCostProviderDefault.WARNING_HEADING),
                eq(List.of(
                        ResourceLocation.parse("minecraft:apple"),
                        ResourceLocation.parse("minecraft:carrot")
                ))
        );
    }

    @Test
    void manualProviderUsesACeilingForConfiguredBatchRatios() {
        EconomyItemCostDefinition definition = new EconomyItemCostDefinition(
                new EconomyItemMatcher(ResourceLocation.parse("minecraft:apple"), null, 5),
                ResourceLocation.parse("minecraft:gold_nugget"),
                2
        );
        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(List.of(definition));

        ItemStack cost = provider.calculateItemStackCost(new ItemStack(Items.APPLE, 6));

        assertSame(Items.GOLD_NUGGET, cost.getItem());
        assertEquals(3, cost.getCount());
    }

    @Test
    void manualProviderReturnsNullWithoutAMatchingEntry() {
        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(List.of());

        assertNull(provider.calculateItemStackCost(Items.APPLE.getDefaultInstance()));
    }

    @Test
    void laterFilesOverrideEarlierFilesInAlphabeticalFileOrder(@TempDir Path directory) throws Exception {
        Files.writeString(directory.resolve("b.json"), """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:diamond"}]}
                """);
        Files.writeString(directory.resolve("a.json"), """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:gold_ingot"}]}
                """);

        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(
                EconomyJsonData.loadConfigSources(List.of(directory), mock(Logger.class))
        );
        ItemStack cost = provider.calculateItemStackCost(Items.APPLE.getDefaultInstance());

        assertSame(Items.DIAMOND, cost.getItem());
    }

    @Test
    void replaceClearsValuesFromEarlierFiles(@TempDir Path directory) throws Exception {
        Files.writeString(directory.resolve("a.json"), """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:gold_ingot"}]}
                """);
        Files.writeString(directory.resolve("b.json"), """
                {
                  "replace": true,
                  "values": [{"item":"minecraft:carrot","costItem":"minecraft:diamond"}]
                }
                """);

        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(
                EconomyJsonData.loadConfigSources(List.of(directory), mock(Logger.class))
        );

        assertNull(provider.calculateItemStackCost(Items.APPLE.getDefaultInstance()));
        assertSame(
                Items.DIAMOND,
                provider.calculateItemStackCost(Items.CARROT.getDefaultInstance()).getItem()
        );
    }

    @Test
    void invalidReplacingFileDoesNotClearEarlierValues(@TempDir Path directory) throws Exception {
        Files.writeString(directory.resolve("a.json"), """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:gold_ingot"}]}
                """);
        Files.writeString(directory.resolve("b.json"), """
                {"replace":true,"values":[{"item":"minecraft:apple"}]}
                """);

        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(
                EconomyJsonData.loadConfigSources(List.of(directory), mock(Logger.class))
        );

        assertSame(
                Items.GOLD_INGOT,
                provider.calculateItemStackCost(Items.APPLE.getDefaultInstance()).getItem()
        );
    }

    @Test
    void rejectsThePreviousRootArrayFormat(@TempDir Path directory) throws Exception {
        Files.writeString(directory.resolve("old.json"), """
                [{"item":"minecraft:apple","costItem":"minecraft:gold_ingot"}]
                """);

        List<EconomyItemCostDefinition> definitions =
                EconomyJsonData.loadConfigSources(List.of(directory), mock(Logger.class));

        assertEquals(List.of(), definitions);
    }

    @Test
    void higherPriorityDatapackReplaceClearsLowerPriorityValues() {
        ResourceManager resourceManager = mock(ResourceManager.class);
        PackResources lowerPack = mock(PackResources.class);
        PackResources higherPack = mock(PackResources.class);
        when(lowerPack.packId()).thenReturn("lower pack");
        when(higherPack.packId()).thenReturn("higher pack");
        when(resourceManager.listPacks()).thenAnswer(invocation -> Stream.of(lowerPack, higherPack));
        ResourceLocation resourceId = ResourceLocation.parse("example:customers/economy/items/food.json");
        Resource lowerResource = resource(lowerPack, """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:gold_ingot"}]}
                """);
        Resource higherResource = resource(higherPack, """
                {
                  "replace": true,
                  "values": [{"item":"minecraft:carrot","costItem":"minecraft:diamond"}]
                }
                """);
        when(resourceManager.listResourceStacks(eq("customers/economy/items"), any()))
                .thenReturn(Map.of(resourceId, List.of(lowerResource, higherResource)));

        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(EconomyJsonData.load(
                resourceManager,
                "customers/economy/items",
                List.of(),
                mock(Logger.class)
        ));

        assertNull(provider.calculateItemStackCost(Items.APPLE.getDefaultInstance()));
        assertSame(
                Items.DIAMOND,
                provider.calculateItemStackCost(Items.CARROT.getDefaultInstance()).getItem()
        );
    }

    @Test
    void configValuesOverrideDatapackValues(@TempDir Path configDirectory) throws Exception {
        ResourceManager resourceManager = mock(ResourceManager.class);
        PackResources datapack = mock(PackResources.class);
        when(datapack.packId()).thenReturn("test datapack");
        when(resourceManager.listPacks()).thenAnswer(invocation -> Stream.of(datapack));
        ResourceLocation resourceId = ResourceLocation.parse("example:customers/economy/items/food.json");
        Resource resource = resource(datapack, """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:diamond"}]}
                """);
        when(resourceManager.listResourceStacks(eq("customers/economy/items"), any()))
                .thenReturn(Map.of(resourceId, List.of(resource)));
        Files.writeString(configDirectory.resolve("food.json"), """
                {"values":[{"item":"minecraft:apple","costItem":"minecraft:gold_nugget"}]}
                """);

        EconomyItemCostProviderManual provider = new EconomyItemCostProviderManual(EconomyJsonData.load(
                resourceManager,
                "customers/economy/items",
                List.of(configDirectory),
                mock(Logger.class)
        ));

        assertSame(
                Items.GOLD_NUGGET,
                provider.calculateItemStackCost(Items.APPLE.getDefaultInstance()).getItem()
        );
    }

    @Test
    void currencyUsesTheLastExactItemAndCountMatch() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                ),
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:diamond"),
                        1
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 5));

        assertSame(Items.DIAMOND, converted.getItem());
        assertEquals(1, converted.getCount());
    }

    @Test
    void currencyUsesTheLargestSourceBatchWhenSeveralConversionsAreWhole() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                ),
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 1),
                        ResourceLocation.parse("minecraft:gold_nugget"),
                        3
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 10));

        assertSame(Items.GOLD_INGOT, converted.getItem());
        assertEquals(2, converted.getCount());
    }

    @Test
    void currencyPrefersAWholeResultOverALargerSourceBatch() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                ),
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 1),
                        ResourceLocation.parse("minecraft:gold_nugget"),
                        3
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 6));

        assertSame(Items.GOLD_NUGGET, converted.getItem());
        assertEquals(18, converted.getCount());
    }

    @Test
    void currencyPrefersTheResultClosestToAWholeNumber() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        2
                ),
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 3),
                        ResourceLocation.parse("minecraft:diamond"),
                        1
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 4));

        assertSame(Items.DIAMOND, converted.getItem());
        assertEquals(1, converted.getCount());
    }

    @Test
    void currencyUsesTheLargerSourceBatchWhenDistancesAreEqual() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 10),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        2
                ),
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:diamond"),
                        1
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 7));

        assertSame(Items.GOLD_INGOT, converted.getItem());
        assertEquals(1, converted.getCount());
    }

    @Test
    void currencyRoundsFractionalResultsDown() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 7));

        assertSame(Items.GOLD_INGOT, converted.getItem());
        assertEquals(1, converted.getCount());
    }

    @Test
    void currencyRoundsFractionalResultsUp() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                )
        ));

        ItemStack converted = currency.convert(new ItemStack(Items.EMERALD, 8));

        assertSame(Items.GOLD_INGOT, converted.getItem());
        assertEquals(2, converted.getCount());
    }

    @Test
    void currencyKeepsARoundedNonemptyCostAtOneItem() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                )
        ));

        ItemStack converted = currency.convert(Items.EMERALD.getDefaultInstance());

        assertSame(Items.GOLD_INGOT, converted.getItem());
        assertEquals(1, converted.getCount());
    }

    @Test
    void currencyLeavesItemsWithoutAMatchingSourceUnchanged() {
        EconomyCurrency currency = new EconomyCurrency(List.of(
                new EconomyItemCostDefinition(
                        new EconomyItemMatcher(ResourceLocation.parse("minecraft:emerald"), null, 5),
                        ResourceLocation.parse("minecraft:gold_ingot"),
                        1
                )
        ));
        ItemStack original = new ItemStack(Items.APPLE, 4);

        assertSame(original, currency.convert(original));
    }

    @Test
    void customerCostIsScaledFromTheFullConfiguredStacks() {
        ItemStack scaled = EconomyCost.scale(
                new ItemStack(Items.APPLE, 5),
                3,
                new ItemStack(Items.EMERALD, 2)
        );

        assertSame(Items.EMERALD, scaled.getItem());
        assertEquals(1, scaled.getCount());
    }

    @Test
    void scaledCustomerCostHasAMinimumOfOne() {
        ItemStack scaled = EconomyCost.scale(
                new ItemStack(Items.APPLE, 64),
                1,
                Items.EMERALD.getDefaultInstance()
        );

        assertEquals(1, scaled.getCount());
    }

    @Test
    void customerCostCannotBeScaledWhenNoProviderReturnedACost() {
        ItemStack scaled = EconomyCost.scale(new ItemStack(Items.APPLE, 5), 3, null);

        assertSame(ItemStack.EMPTY, scaled);
    }

    @Test
    void economyUsesTheFirstProviderThatReturnsACost() {
        EconomyItemCostProvider first = mock(EconomyItemCostProvider.class);
        EconomyItemCostProvider second = mock(EconomyItemCostProvider.class);
        EconomyItemCostProvider third = mock(EconomyItemCostProvider.class);
        ItemStack item = Items.APPLE.getDefaultInstance();
        when(second.calculateItemStackCost(item)).thenReturn(new ItemStack(Items.DIAMOND, 2));

        ItemStack cost = Economy.calculateItemStackCost(
                item,
                true,
                List.of(first, second, third),
                new EconomyCurrency(List.of())
        );

        assertSame(Items.DIAMOND, cost.getItem());
        assertEquals(2, cost.getCount());
        verify(third, never()).calculateItemStackCost(item);
    }

    @Test
    void disabledEconomyDoesNotCallProviders() {
        EconomyItemCostProvider provider = mock(EconomyItemCostProvider.class);

        assertNull(Economy.calculateItemStackCost(
                Items.APPLE.getDefaultInstance(),
                false,
                List.of(provider),
                new EconomyCurrency(List.of())
        ));
        verify(provider, never()).calculateItemStackCost(any());
    }

    private static Resource resource(PackResources pack, String json) {
        return new Resource(
                pack,
                () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );
    }
}
