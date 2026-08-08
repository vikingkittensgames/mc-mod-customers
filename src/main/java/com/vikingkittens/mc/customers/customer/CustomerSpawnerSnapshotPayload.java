package com.vikingkittens.mc.customers.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.client.customer.CustomerClientEvents;

public record CustomerSpawnerSnapshotPayload(
        BlockPos spawnerPos,
        Optional<CustomerSpawnerSnapshot> snapshot
) implements CustomPacketPayload {
    public static final Type<CustomerSpawnerSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "customer_spawner_snapshot"
            )
    );
    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            CustomerSpawnerSnapshotPayload
    > STREAM_CODEC = StreamCodec.of(
            CustomerSpawnerSnapshotPayload::write,
            CustomerSpawnerSnapshotPayload::read
    );

    private static void write(
            RegistryFriendlyByteBuf buffer,
            CustomerSpawnerSnapshotPayload payload
    ) {
        buffer.writeBlockPos(payload.spawnerPos());
        buffer.writeBoolean(payload.snapshot().isPresent());
        payload.snapshot().ifPresent(snapshot -> {
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
            RegistryFriendlyByteBuf buffer,
            CustomerSpawnerSnapshot.Customer customer
    ) {
        buffer.writeUUID(customer.customerId());
        buffer.writeEnum(customer.type());
        buffer.writeVarInt(customer.offerCostItems().size());
        for (ItemStack offerCostItem : customer.offerCostItems()) {
            ItemStack.STREAM_CODEC.encode(buffer, offerCostItem);
        }
        buffer.writeLong(customer.ticksSinceTrade());
        buffer.writeLong(customer.giveUpTicks());
    }

    private static CustomerSpawnerSnapshotPayload read(
            RegistryFriendlyByteBuf buffer
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
            RegistryFriendlyByteBuf buffer
    ) {
        UUID customerId = buffer.readUUID();
        CustomerSpawnerSnapshot.Customer.Type type =
                buffer.readEnum(CustomerSpawnerSnapshot.Customer.Type.class);
        int offerCount = buffer.readVarInt();
        List<ItemStack> offerCostItems = new java.util.ArrayList<>(offerCount);
        for (int index = 0; index < offerCount; index++) {
            offerCostItems.add(ItemStack.STREAM_CODEC.decode(buffer));
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

    public static void handle(
            CustomerSpawnerSnapshotPayload payload,
            IPayloadContext context
    ) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CustomerClientEvents.updateCustomerSpawnerSnapshot(payload);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
