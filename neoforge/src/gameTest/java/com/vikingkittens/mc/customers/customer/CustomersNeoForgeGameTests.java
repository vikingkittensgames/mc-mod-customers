package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.supplier.SupplierGameTestAssertions;

@EventBusSubscriber(modid = Customers.MODID)
public final class CustomersNeoForgeGameTests {
    private static final Identifier ENVIRONMENT =
            id("default_test_environment");
    private static final Identifier REGISTERS_CUSTOMERS_CONTENT =
            id("registers_customers_content");
    private static final Identifier CREATES_TRADE_REMAINDERS =
            id("creates_trade_remainders");
    private static final Identifier CUSTOMERS_CAN_SIT =
            id("customers_can_sit");
    private static final Identifier SUPPLIER_TRADES_SURVIVE_SAVE_AND_LOAD =
            id("supplier_trades_survive_save_and_load");
    private static final Identifier STRUCTURE =
            Identifier.withDefaultNamespace(
                    "woodland_mansion/wall_window"
            );

    private CustomersNeoForgeGameTests() {}

    @SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        event.register(
                Registries.TEST_FUNCTION,
                REGISTERS_CUSTOMERS_CONTENT,
                () -> CustomersNeoForgeGameTests::registersCustomersContent
        );
        event.register(
                Registries.TEST_FUNCTION,
                CREATES_TRADE_REMAINDERS,
                () -> CustomersNeoForgeGameTests::createsTradeRemainders
        );
        event.register(
                Registries.TEST_FUNCTION,
                CUSTOMERS_CAN_SIT,
                () -> CustomersNeoForgeGameTests::customersCanSit
        );
        event.register(
                Registries.TEST_FUNCTION,
                SUPPLIER_TRADES_SURVIVE_SAVE_AND_LOAD,
                () -> CustomersNeoForgeGameTests::supplierTradesSurviveSaveAndLoad
        );
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment =
                event.registerEnvironment(ENVIRONMENT);
        registerTest(
                event,
                environment,
                REGISTERS_CUSTOMERS_CONTENT
        );
        registerTest(
                event,
                environment,
                CREATES_TRADE_REMAINDERS
        );
        registerTest(
                event,
                environment,
                CUSTOMERS_CAN_SIT
        );
        registerTest(
                event,
                environment,
                SUPPLIER_TRADES_SURVIVE_SAVE_AND_LOAD
        );
    }

    public static void registersCustomersContent(GameTestHelper helper) {
        CustomersGameTestAssertions.assertCustomersContentRegistered(helper);
        helper.succeed();
    }

    public static void createsTradeRemainders(GameTestHelper helper) {
        CustomersGameTestAssertions.assertTradeRemainders(helper);
        helper.succeed();
    }

    public static void customersCanSit(GameTestHelper helper) {
        CustomersGameTestAssertions.assertCustomersCanSit(helper);
        helper.succeed();
    }

    public static void supplierTradesSurviveSaveAndLoad(GameTestHelper helper) {
        SupplierGameTestAssertions.assertTradesSurviveSaveAndLoad(helper);
        helper.succeed();
    }

    private static void registerTest(
            RegisterGameTestsEvent event,
            Holder<TestEnvironmentDefinition> environment,
            Identifier id
    ) {
        event.registerTest(
                id,
                new FunctionGameTestInstance(
                        ResourceKey.create(Registries.TEST_FUNCTION, id),
                        new TestData<>(
                                environment,
                                STRUCTURE,
                                100,
                                0,
                                true
                        )
                )
        );
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Customers.MODID, path);
    }
}
