package com.vikingkittens.mc.customers.customer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.common.CustomersNetworkPayload;
import com.vikingkittens.mc.customers.compatability.ResourceLocationCUtils;
import com.vikingkittens.mc.customers.customer.pets.CustomerPet;

public record CustomerSpawnerPetDataPayload(int containerId, List<Entry> entries)
        implements CustomersNetworkPayload {
    public static final ResourceLocation ID = ResourceLocationCUtils.create(Customers.MODID, "customer_spawner_pets");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(entries.size());
        for (Entry entry : entries) {
            CustomerPet.PetType type = entry.type();
            buffer.writeUtf(type.entityId());
            buffer.writeComponent(type.name());
            buffer.writeVarInt(type.foods().size());
            for (ItemStack food : type.foods()) {
                buffer.writeItem(food);
            }
            buffer.writeBoolean(entry.enabled());
            buffer.writeItem(entry.selectedFood());
        }
    }

    public static CustomerSpawnerPetDataPayload read(FriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        int count = buffer.readVarInt();
        List<Entry> entries = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            String entityId = buffer.readUtf();
            var name = buffer.readComponent();
            int foodCount = buffer.readVarInt();
            List<ItemStack> foods = new ArrayList<>(foodCount);
            for (int foodIndex = 0; foodIndex < foodCount; foodIndex++) {
                foods.add(buffer.readItem());
            }
            boolean enabled = buffer.readBoolean();
            ItemStack selectedFood = buffer.readItem();
            entries.add(new Entry(new CustomerPet.PetType(entityId, name, foods), enabled, selectedFood));
        }
        return new CustomerSpawnerPetDataPayload(containerId, List.copyOf(entries));
    }

    public record Entry(CustomerPet.PetType type, boolean enabled, ItemStack selectedFood) {}
}
