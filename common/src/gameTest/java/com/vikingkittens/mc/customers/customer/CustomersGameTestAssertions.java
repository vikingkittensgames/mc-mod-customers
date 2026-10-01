package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.vikingkittens.mc.customers.advancements.CustomersStatistics;
import com.vikingkittens.mc.customers.advancements.CustomersTriggers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

public final class CustomersGameTestAssertions {
    private CustomersGameTestAssertions() {}

    public static void assertCustomersContentRegistered(GameTestHelper helper) {
        assertId(helper, "customers:item_served", CustomersTriggers.ITEM_SERVED.getId());
        assertId(helper, "customers:leaderboard_changed", CustomersTriggers.LEADERBOARD_CHANGED.getId());
        assertId(helper, "customers:customer_spawner_changed", CustomersTriggers.CUSTOMER_SPAWNER_CHANGED.getId());
        assertId(helper, "customers:supplier_spawner_changed", CustomersTriggers.SUPPLIER_SPAWNER_CHANGED.getId());
        assertId(helper, "customers:counter_placed", CustomersTriggers.COUNTER_PLACED.getId());
        assertId(helper, "customers:item_served", CustomersStatistics.ITEM_SERVED.getId());
        assertId(helper, "customers:pet_item_served", CustomersStatistics.PET_ITEM_SERVED.getId());
        assertId(helper, "customers:default", CustomersVillagerAppearances.DEFAULT_APPEARANCE.getId());
        assertId(helper, "customers:monsters", MonsterCustomersVillagerAppearanceEvents.APPEARANCE.getId());
        assertId(helper, "customers:customer_payment_box_block_entity", CustomerPaymentBox.BLOCK_ENTITY.getId());
        assertId(helper, "customers:customer_seat", CustomerSeat.ENTITY_TYPE.getId());
        assertId(helper, "customers:customer_villager", Customer.CUSTOMER_VILLAGER.getId());
        assertId(helper, "customers:customer", Customer.CUSTOMER_PROFESSION.getId());
        assertId(helper, "customers:customer_casual", Customer.CUSTOMER_CASUAL_PROFESSION.getId());
        assertId(helper, "customers:customer_impatient", Customer.CUSTOMER_IMPATIENT_PROFESSION.getId());
        assertId(helper, "customers:customer_spawner_block", CustomerSpawner.CUSTOMER_SPAWNER_BLOCK.getId());
        assertId(helper, "customers:customer_pickup_counter", CustomerPickupCounter.BLOCK_ENTITY.getId());
        assertId(helper, "customers:supplier_villager", Supplier.SUPPLIER_VILLAGER.getId());
        assertId(helper, "customers:supplier_spawner_block", SupplierSpawner.SUPPLIER_SPAWNER_BLOCK.getId());
    }

    public static void assertTradeRemainders(GameTestHelper helper) {
        ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        assertItem(helper, Items.GLASS_BOTTLE, CustomerVillagerEntity.getTradeRemainderStack(water));
        assertItem(helper, Items.BUCKET, CustomerVillagerEntity.getTradeRemainderStack(new ItemStack(Items.MILK_BUCKET)));
        ItemStack bowls = CustomerVillagerEntity.getTradeRemainderStack(new ItemStack(Items.MUSHROOM_STEW, 3));
        assertItem(helper, Items.BOWL, bowls);
        helper.assertTrue(bowls.getCount() == 3, "mushroom stew remainder count");
    }

    public static void assertCustomersCanSit(GameTestHelper helper) {
        assertCustomerCanSit(helper, new BlockPos(1, 1, 1), Blocks.OAK_SLAB);
        assertCustomerCanSit(helper, new BlockPos(3, 1, 1), Blocks.OAK_STAIRS);
    }

    private static void assertCustomerCanSit(
            GameTestHelper helper,
            BlockPos seatPosition,
            Block seatBlock
    ) {
        helper.setBlock(seatPosition, seatBlock);
        CustomerVillagerEntity customer = helper.spawn(
                Customer.CUSTOMER_VILLAGER.get(),
                seatPosition.above()
        );
        boolean startedSitting = CustomerSeatEntity.trySit(
                helper.getLevel(),
                helper.absolutePos(seatPosition),
                customer
        );
        helper.assertTrue(
                startedSitting,
                "Customer did not mount " + seatBlock
        );
        helper.assertTrue(
                customer.getVehicle() != null
                        && customer.getVehicle().getType() == CustomerSeat.ENTITY_TYPE.get(),
                "customer seat vehicle"
        );
    }

    private static void assertId(GameTestHelper helper, String expected, ResourceLocation actual) {
        helper.assertTrue(new ResourceLocation(expected).equals(actual), expected);
    }

    private static void assertItem(GameTestHelper helper, Item expected, ItemStack actual) {
        helper.assertTrue(expected == actual.getItem(), expected.toString());
    }
}
