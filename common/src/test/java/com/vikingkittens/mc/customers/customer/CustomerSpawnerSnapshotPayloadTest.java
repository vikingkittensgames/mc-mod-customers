package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerSpawnerSnapshotPayloadTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void roundTripsCompleteSpawnerSnapshot() {
        ItemStack cost = new ItemStack(Items.DIAMOND, 7);
        BlockPos spawnerPos = new BlockPos(10, 64, -20);
        UUID bossEventId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        CustomerSpawnerSnapshot snapshot = new CustomerSpawnerSnapshot(
                spawnerPos,
                CustomerSpawnerMode.DINNER,
                Optional.of(bossEventId),
                List.of(new CustomerSpawnerSnapshot.Customer(
                        customerId,
                        CustomerSpawnerSnapshot.Customer.Type.CASUAL,
                        List.of(cost),
                        60,
                        120
                ))
        );
        CustomerSpawnerSnapshotPayload original =
                new CustomerSpawnerSnapshotPayload(
                        spawnerPos,
                        Optional.of(snapshot)
                );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        CustomerSpawnerSnapshotPayload.write(buffer, original);
        CustomerSpawnerSnapshotPayload decoded = CustomerSpawnerSnapshotPayload.read(buffer);

        CustomerSpawnerSnapshot decodedSnapshot =
                decoded.snapshot().orElseThrow();
        CustomerSpawnerSnapshot.Customer decodedCustomer =
                decodedSnapshot.customers().get(0);
        ItemStack decodedCost = decodedCustomer.offerCostItems().get(0);
        assertEquals(spawnerPos, decoded.spawnerPos());
        assertEquals(CustomerSpawnerMode.DINNER, decodedSnapshot.spawnerMode());
        assertEquals(Optional.of(bossEventId), decodedSnapshot.bossEventId());
        assertEquals(customerId, decodedCustomer.customerId());
        assertEquals(
                CustomerSpawnerSnapshot.Customer.Type.CASUAL,
                decodedCustomer.type()
        );
        assertEquals(7, decodedCost.getCount());
        assertTrue(ItemStack.isSameItemSameTags(cost, decodedCost));
        assertEquals(60, decodedCustomer.ticksSinceTrade());
        assertEquals(120, decodedCustomer.giveUpTicks());
    }

    @Test
    void roundTripsSnapshotRemoval() {
        BlockPos spawnerPos = new BlockPos(-5, 70, 30);
        CustomerSpawnerSnapshotPayload original =
                new CustomerSpawnerSnapshotPayload(
                        spawnerPos,
                        Optional.empty()
                );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        CustomerSpawnerSnapshotPayload.write(buffer, original);
        CustomerSpawnerSnapshotPayload decoded = CustomerSpawnerSnapshotPayload.read(buffer);

        assertEquals(spawnerPos, decoded.spawnerPos());
        assertEquals(Optional.empty(), decoded.snapshot());
    }

}
