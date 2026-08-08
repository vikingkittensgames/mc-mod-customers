package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.registries.RegistryBuilder;

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
        cost.set(DataComponents.CUSTOM_NAME, Component.literal("Requested"));
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
        RegistryFriendlyByteBuf buffer = createBuffer();

        CustomerSpawnerSnapshotPayload.STREAM_CODEC.encode(buffer, original);
        CustomerSpawnerSnapshotPayload decoded =
                CustomerSpawnerSnapshotPayload.STREAM_CODEC.decode(buffer);

        CustomerSpawnerSnapshot decodedSnapshot =
                decoded.snapshot().orElseThrow();
        CustomerSpawnerSnapshot.Customer decodedCustomer =
                decodedSnapshot.customers().getFirst();
        ItemStack decodedCost = decodedCustomer.offerCostItems().getFirst();
        assertEquals(spawnerPos, decoded.spawnerPos());
        assertEquals(CustomerSpawnerMode.DINNER, decodedSnapshot.spawnerMode());
        assertEquals(Optional.of(bossEventId), decodedSnapshot.bossEventId());
        assertEquals(customerId, decodedCustomer.customerId());
        assertEquals(
                CustomerSpawnerSnapshot.Customer.Type.CASUAL,
                decodedCustomer.type()
        );
        assertEquals(7, decodedCost.getCount());
        assertTrue(ItemStack.isSameItemSameComponents(cost, decodedCost));
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
        RegistryFriendlyByteBuf buffer = createBuffer();

        CustomerSpawnerSnapshotPayload.STREAM_CODEC.encode(buffer, original);
        CustomerSpawnerSnapshotPayload decoded =
                CustomerSpawnerSnapshotPayload.STREAM_CODEC.decode(buffer);

        assertEquals(spawnerPos, decoded.spawnerPos());
        assertEquals(Optional.empty(), decoded.snapshot());
    }

    private static RegistryFriendlyByteBuf createBuffer() {
        Registry<Item> itemRegistry = copyRegistryValue(
                Registries.ITEM,
                BuiltInRegistries.ITEM,
                Items.DIAMOND
        );
        Registry<DataComponentType<?>> componentRegistry = copyRegistryValue(
                Registries.DATA_COMPONENT_TYPE,
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                DataComponents.CUSTOM_NAME
        );
        return new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                new RegistryAccess.ImmutableRegistryAccess(List.of(
                        itemRegistry,
                        componentRegistry
                ))
        );
    }

    private static <T> Registry<T> copyRegistryValue(
            ResourceKey<? extends Registry<T>> registryKey,
            Registry<T> source,
            T value
    ) {
        Registry<T> copy = new RegistryBuilder<T>(registryKey)
                .sync(true)
                .disableRegistrationCheck()
                .create();
        Registry.register(
                copy,
                source.getResourceKey(value).orElseThrow(),
                value
        );
        return copy.freeze();
    }
}
