package com.vikingkittens.mc.customers.supplier;

import java.util.Optional;

import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import com.vikingkittens.mc.customers.compatability.ItemStackCUtils;

public final class SupplierGameTestAssertions {
    private SupplierGameTestAssertions() {}

    public static void assertTradesSurviveSaveAndLoad(GameTestHelper helper) {
        SimpleContainer inventory = new SimpleContainer(9);
        inventory.setItem(0, new ItemStack(Items.APPLE, 3));
        inventory.setItem(1, new ItemStack(Items.EMERALD, 2));
        MerchantOffers saved = SupplierSpawnerBlockEntity.getOffersFromInventory(
                RandomSource.create(),
                inventory
        );
        MerchantOffers loaded = saveAndLoad(helper, saved);

        helper.assertTrue(
                loaded.getFirst().satisfiedBy(
                        new ItemStack(Items.EMERALD, 2),
                        ItemStack.EMPTY
                ),
                Component.literal("Reloaded supplier offer rejected its configured cost")
        );

        ItemStack legacyCostStack = new ItemStack(Items.EMERALD, 2);
        MerchantOffers legacySaved = new MerchantOffers();
        legacySaved.add(new MerchantOffer(
                new ItemCost(
                        legacyCostStack.getItem().builtInRegistryHolder(),
                        legacyCostStack.getCount(),
                        DataComponentExactPredicate.allOf(
                                legacyCostStack.getComponents()
                        )
                ),
                Optional.empty(),
                new ItemStack(Items.APPLE, 3),
                1,
                10,
                0,
                0.0F
        ));
        MerchantOffers normalized = ItemStackCUtils.normalizeOfferCosts(
                saveAndLoad(helper, legacySaved)
        );

        helper.assertTrue(
                normalized.getFirst().satisfiedBy(
                        new ItemStack(Items.EMERALD, 2),
                        ItemStack.EMPTY
                ),
                Component.literal("Existing supplier offer was not repaired")
        );
        helper.assertValueEqual(
                1,
                normalized.getFirst().getUses(),
                "repaired supplier offer uses"
        );
    }

    private static MerchantOffers saveAndLoad(
            GameTestHelper helper,
            MerchantOffers offers
    ) {
        var ops = helper.getLevel().registryAccess()
                .createSerializationContext(NbtOps.INSTANCE);
        return MerchantOffers.CODEC.parse(
                ops,
                MerchantOffers.CODEC.encodeStart(ops, offers).getOrThrow()
        ).getOrThrow();
    }
}
