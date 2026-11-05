package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.customer.pets.CustomerPet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerSpawnerItemTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void providesTheMatchingModePredicateOrContinuousForAnUnconfiguredItem() {
        assertEquals(0.0F, CustomerSpawnerItem.predicateValueSpawnerMode(new ItemStack(Items.STONE)));

        for (CustomerSpawnerMode mode : CustomerSpawnerMode.values()) {
            ItemStack stack = new ItemStack(Items.STONE);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, itemData -> {
                CompoundTag settings = new CompoundTag();
                settings.putString("mode", mode.getSerializedName());
                itemData.put(CustomerSpawnerItem.TAG_SETTINGS, settings);
            });

            assertEquals((float) mode.ordinal(), CustomerSpawnerItem.predicateValueSpawnerMode(stack));
        }
    }

    @Test
    void preservesModeAndLevelSettingsWithoutAnyInventory() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        List<String> petTypes = List.of("minecraft:cat", "minecraft:wolf");
        CustomerPet.PetType cat = new CustomerPet.PetType(
                "minecraft:cat",
                Component.literal("Cat"),
                List.of(Items.COD.getDefaultInstance(), Items.SALMON.getDefaultInstance())
        );
        CustomerSpawnerBlockEntity source = new CustomerSpawnerBlockEntity(type, BlockPos.ZERO, state);
        CustomerSpawnerLevelSettings firstLevel = source.getLevelSettings(0);
        firstLevel.setRequiredStars(4.5F);
        firstLevel.setMaxCustomers(8);
        firstLevel.setPetPercentage(0.35F);
        firstLevel.setAutoCost(true);
        firstLevel.setEnabledAppearanceIds(List.of(ResourceLocation.fromNamespaceAndPath("customers", "chef")));
        firstLevel.setPetTypeEnabled("minecraft:cat", false, petTypes);
        firstLevel.initializePetFoods(List.of(cat));
        firstLevel.cyclePetFood(cat);
        firstLevel.getInventory().setItem(0, new ItemStack(Items.BREAD, 3));
        firstLevel.getInventory().setItem(CustomerSpawnerLevelSettings.PET_FOOD_COST_SLOT, new ItemStack(Items.EMERALD));
        CustomerSpawnerLevelSettings secondLevel = source.getLevelSettings(1);
        secondLevel.setRequiredStars(1.5F);
        secondLevel.setMaxCustomers(3);
        secondLevel.setPetPercentage(0.75F);
        secondLevel.setEnabledAppearanceIds(List.of(ResourceLocation.fromNamespaceAndPath("customers", "baker")));
        secondLevel.getInventory().setItem(0, new ItemStack(Items.CARROT, 2));
        ItemStack stack = new ItemStack(Items.STONE);

        CustomerSpawnerItem.writeSettings(stack, CustomerSpawnerMode.BREAKFAST, source, RegistryAccess.EMPTY);

        CustomData itemData = stack.get(DataComponents.CUSTOM_DATA);
        assertNotNull(itemData);
        CompoundTag settings = itemData.copyTag().getCompound(CustomerSpawnerItem.TAG_SETTINGS);
        assertFalse(settings.getList(CustomerSpawnerItem.TAG_LEVEL_SETTINGS, Tag.TAG_COMPOUND)
                .stream().anyMatch(serializedLevel -> ((CompoundTag) serializedLevel).contains("inventory")));
        CustomerSpawnerBlockEntity restored = new CustomerSpawnerBlockEntity(type, BlockPos.ZERO, state);
        CustomerSpawnerItem.applySettings(stack, restored, RegistryAccess.EMPTY);

        assertEquals(CustomerSpawnerMode.BREAKFAST, CustomerSpawnerItem.getMode(stack).orElseThrow());
        assertEquals(2, restored.getLevelSettingsCount());
        assertEquals(4.5F, restored.getLevelSettings(0).getRequiredStars());
        assertEquals(8, restored.getLevelSettings(0).getMaxCustomers());
        assertEquals(0.35F, restored.getLevelSettings(0).getPetPercentage());
        assertEquals(true, restored.getLevelSettings(0).isAutoCost());
        assertEquals(
                List.of(ResourceLocation.fromNamespaceAndPath("customers", "chef")),
                restored.getLevelSettings(0).getEnabledAppearanceIds()
        );
        assertEquals(List.of("minecraft:wolf"), restored.getLevelSettings(0).getEnabledPetTypes(petTypes));
        assertEquals(Items.SALMON, restored.getLevelSettings(0).getPetFood(cat).getItem());
        assertTrue(restored.getLevelSettings(0).getInventory().getItem(0).isEmpty());
        assertTrue(restored.getLevelSettings(0).getPetFoodCost().isEmpty());
        assertEquals(1.5F, restored.getLevelSettings(1).getRequiredStars());
        assertEquals(3, restored.getLevelSettings(1).getMaxCustomers());
        assertEquals(0.75F, restored.getLevelSettings(1).getPetPercentage());
        assertEquals(
                List.of(ResourceLocation.fromNamespaceAndPath("customers", "baker")),
                restored.getLevelSettings(1).getEnabledAppearanceIds()
        );
        assertTrue(restored.getLevelSettings(1).getInventory().getItem(0).isEmpty());
    }
}
