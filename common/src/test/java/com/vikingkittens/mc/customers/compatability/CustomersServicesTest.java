package com.vikingkittens.mc.customers.compatability;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomersServicesTest {
    @Test
    void loadsVanillaConfigDefaults() {
        IConfigHelper config = CustomersServices.config();

        assertEquals(VanillaConfigHelper.class, config.getClass());
        assertEquals(true, config.customerSpawnerRecipeEnabled());
        assertEquals(true, config.supplierSpawnerRecipeEnabled());
        assertEquals(64, config.maxCounterDistance());
        assertEquals(4, config.defaultMaxCustomers());
        assertEquals(120, config.customerGiveUpSeconds());
        assertEquals(false, config.buildCommandsEnabled());
        assertEquals(false, config.quickSellEnabled());
    }

    @Test
    void loadsVanillaNetworkProvider() {
        assertEquals(VanillaNetworkHelper.class, CustomersServices.network().getClass());
    }

    @Test
    void requiresExactlyOnePlatformImplementation() {
        IPlatformHelper helper = new TestPlatformHelper();

        assertEquals(helper, CustomersServices.requireSingle(IPlatformHelper.class, List.of(helper)));
        assertThrows(
                IllegalStateException.class,
                () -> CustomersServices.requireSingle(IPlatformHelper.class, List.of())
        );
        assertThrows(
                IllegalStateException.class,
                () -> CustomersServices.requireSingle(
                        IPlatformHelper.class,
                        List.of(helper, new TestPlatformHelper())
                )
        );
    }

    private static final class TestPlatformHelper implements IPlatformHelper {
        @Override
        public String platformName() {
            return "Test";
        }

        @Override
        public boolean isModLoaded(String modId) {
            return false;
        }

        @Override
        public void closeContainer(net.minecraft.world.entity.player.Player player) {}
    }
}
