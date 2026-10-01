package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.CustomersNetworkPayload;

public record CustomerSpawnerSnapshotPayload(
        BlockPos spawnerPos,
        Optional<CustomerSpawnerSnapshot> snapshot
) implements CustomersNetworkPayload {
    public static final ResourceLocation ID = new ResourceLocation(Customers.MODID, "customer_spawner_snapshot");

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(spawnerPos());
        buffer.writeBoolean(snapshot().isPresent());
        snapshot().ifPresent(snapshot -> {
            buffer.writeEnum(snapshot.spawnerMode());
            buffer.writeBoolean(snapshot.bossEventId().isPresent());
            snapshot.bossEventId().ifPresent(buffer::writeUUID);
            buffer.writeVarInt(snapshot.customers().size());
            for (CustomerSpawnerSnapshot.Customer customer
                    : snapshot.customers()) {
                writeCustomer(buffer, customer);
            }
        });
    }

    private static void writeCustomer(
            FriendlyByteBuf buffer,
            CustomerSpawnerSnapshot.Customer customer
    ) {
        buffer.writeUUID(customer.customerId());
        buffer.writeEnum(customer.type());
        buffer.writeVarInt(customer.offerCostItems().size());
        for (ItemStack offerCostItem : customer.offerCostItems()) {
            buffer.writeItem(offerCostItem);
        }
        buffer.writeLong(customer.ticksSinceTrade());
        buffer.writeLong(customer.giveUpTicks());
    }

    public static CustomerSpawnerSnapshotPayload read(
            FriendlyByteBuf buffer
    ) {
        BlockPos spawnerPos = buffer.readBlockPos();
        if (!buffer.readBoolean()) {
            return new CustomerSpawnerSnapshotPayload(
                    spawnerPos,
                    Optional.empty()
            );
        }
        CustomerSpawnerMode spawnerMode =
                buffer.readEnum(CustomerSpawnerMode.class);
        Optional<UUID> bossEventId = buffer.readBoolean()
                ? Optional.of(buffer.readUUID())
                : Optional.empty();
        int customerCount = buffer.readVarInt();
        List<CustomerSpawnerSnapshot.Customer> customers =
                new java.util.ArrayList<>(customerCount);
        for (int index = 0; index < customerCount; index++) {
            customers.add(readCustomer(buffer));
        }
        return new CustomerSpawnerSnapshotPayload(
                spawnerPos,
                Optional.of(new CustomerSpawnerSnapshot(
                        spawnerPos,
                        spawnerMode,
                        bossEventId,
                        customers
                ))
        );
    }

    private static CustomerSpawnerSnapshot.Customer readCustomer(
            FriendlyByteBuf buffer
    ) {
        UUID customerId = buffer.readUUID();
        CustomerSpawnerSnapshot.Customer.Type type =
                buffer.readEnum(CustomerSpawnerSnapshot.Customer.Type.class);
        int offerCount = buffer.readVarInt();
        List<ItemStack> offerCostItems = new java.util.ArrayList<>(offerCount);
        for (int index = 0; index < offerCount; index++) {
            offerCostItems.add(buffer.readItem());
        }
        long ticksSinceTrade = buffer.readLong();
        long giveUpTicks = buffer.readLong();
        return new CustomerSpawnerSnapshot.Customer(
                customerId,
                type,
                offerCostItems,
                ticksSinceTrade,
                giveUpTicks
        );
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
