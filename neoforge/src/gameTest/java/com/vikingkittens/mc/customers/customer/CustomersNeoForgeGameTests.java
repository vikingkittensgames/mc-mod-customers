package com.vikingkittens.mc.customers.customer;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.advancements.CustomersStatistics;
import com.vikingkittens.mc.customers.advancements.CustomersTriggers;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.appearance.monsters.MonsterCustomersVillagerAppearanceEvents;
import com.vikingkittens.mc.customers.supplier.Supplier;
import com.vikingkittens.mc.customers.supplier.SupplierSpawner;

@GameTestHolder(Customers.MODID)
@PrefixGameTestTemplate(false)
public final class CustomersNeoForgeGameTests {
    @GameTest(templateNamespace = "minecraft", template = "woodland_mansion/wall_window")
    public static void registersCustomersContent(GameTestHelper helper) {
        CustomersGameTestAssertions.assertCustomersContentRegistered(helper);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "woodland_mansion/wall_window")
    public static void createsTradeRemainders(GameTestHelper helper) {
        CustomersGameTestAssertions.assertTradeRemainders(helper);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "woodland_mansion/wall_window")
    public static void customersCanSit(GameTestHelper helper) {
        CustomersGameTestAssertions.assertCustomersCanSit(helper);
        helper.succeed();
    }
}
