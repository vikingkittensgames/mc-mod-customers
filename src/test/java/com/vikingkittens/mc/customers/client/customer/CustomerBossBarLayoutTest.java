package com.vikingkittens.mc.customers.client.customer;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot.Customer;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerSnapshot.Customer.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CustomerBossBarLayoutTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void createsCenteredRowsWithoutSplittingCustomerGroups() {
        Customer first = customer(Type.NORMAL, 2);
        Customer second = customer(Type.CASUAL, 1);

        CustomerBossBarLayout.Layout layout = CustomerBossBarLayout.create(List.of(first, second), 100, 20, 45);

        assertEquals(2, layout.groups().size());
        assertEquals(new CustomerBossBarLayout.Bounds(86, 20, 28, 16), layout.groups().get(0).bounds());
        assertEquals(new CustomerBossBarLayout.Bounds(92, 38, 16, 16), layout.groups().get(1).bounds());
        assertEquals(34, layout.height());
    }

    @Test
    void providesAColorForEachCustomerType() {
        assertEquals(0x80FFD54F, CustomerBossBarLayout.backgroundColor(Type.NORMAL));
        assertEquals(0x80EF5350, CustomerBossBarLayout.backgroundColor(Type.IMPATIENT));
        assertEquals(0x8066BB6A, CustomerBossBarLayout.backgroundColor(Type.CASUAL));
    }

    @Test
    void skipsCustomersWithNoRemainingItems() {
        Customer completed = customer(Type.NORMAL, 0);
        Customer active = customer(Type.CASUAL, 1);

        CustomerBossBarLayout.Layout layout =
                CustomerBossBarLayout.create(
                        List.of(completed, active),
                        100,
                        20,
                        182
                );

        assertEquals(1, layout.groups().size());
        assertEquals(active, layout.groups().getFirst().customer());
        assertEquals(
                new CustomerBossBarLayout.Bounds(92, 20, 16, 16),
                layout.groups().getFirst().bounds()
        );
        assertEquals(16, layout.height());
    }

    @Test
    void includesWarningAndExtraWidthDuringTheFinalFifteenSeconds() {
        Customer warning = customer(Type.NORMAL, 1, 2100, 2400);
        Customer notYetWarning = customer(
                Type.NORMAL,
                1,
                2099,
                2400
        );
        Customer unlimited = customer(Type.CASUAL, 1, 2400, 0);

        CustomerBossBarLayout.Layout warningLayout =
                CustomerBossBarLayout.create(
                        List.of(warning),
                        100,
                        20,
                        182
                );
        CustomerBossBarLayout.Layout notYetWarningLayout =
                CustomerBossBarLayout.create(
                        List.of(notYetWarning),
                        100,
                        20,
                        182
                );
        CustomerBossBarLayout.Layout unlimitedLayout =
                CustomerBossBarLayout.create(
                        List.of(unlimited),
                        100,
                        20,
                        182
                );

        assertTrue(warningLayout.groups().getFirst().includeWarning());
        assertEquals(
                new CustomerBossBarLayout.Bounds(90, 20, 20, 16),
                warningLayout.groups().getFirst().bounds()
        );
        assertFalse(
                notYetWarningLayout.groups().getFirst().includeWarning()
        );
        assertEquals(
                new CustomerBossBarLayout.Bounds(92, 20, 16, 16),
                notYetWarningLayout.groups().getFirst().bounds()
        );
        assertFalse(
                unlimitedLayout.groups().getFirst().includeWarning()
        );
    }
    private static Customer customer(Type type, int itemCount, long ticksSinceTrade, long giveUpTicks) {
        List<ItemStack> items = IntStream.range(0, itemCount).mapToObj(ignored -> mock(ItemStack.class)).toList();
        return new Customer(UUID.randomUUID(), type, items, ticksSinceTrade, giveUpTicks);
    }

    private static Customer customer(Type type, int itemCount) {
        return customer(type, itemCount, 0, 0);
    }
}
