package com.vikingkittens.mc.customers.supplier;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SupplierSpawnerBlockEntityTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void persistsCustomName() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        Component name = Component.literal("Farm Shop");
        TestSupplierSpawnerBlockEntity source = new TestSupplierSpawnerBlockEntity(type, state);
        source.setCustomName(name);
        CompoundTag tag = new CompoundTag();
        source.saveTo(tag);
        TestSupplierSpawnerBlockEntity restored = new TestSupplierSpawnerBlockEntity(type, state);
        restored.loadFrom(tag);

        assertEquals(name, restored.getCustomName());
        assertEquals(name, restored.getDisplayName());
    }

    @Test
    void restoresAppearanceAndAutomaticCostSettingsWithoutInventory() {
        BlockEntityType<?> type = mock(BlockEntityType.class);
        BlockState state = mock(BlockState.class);
        when(type.isValid(state)).thenReturn(true);
        List<ResourceLocation> appearances = List.of(
                ResourceLocation.fromNamespaceAndPath("customers", "farmer"),
                ResourceLocation.fromNamespaceAndPath("customers", "shopkeeper")
        );
        TestSupplierSpawnerBlockEntity source = new TestSupplierSpawnerBlockEntity(type, state);
        source.setEnabledAppearanceIds(appearances);
        source.setAutoCost(true);
        ItemStack stack = new ItemStack(Items.STONE);

        SupplierSpawnerItemSettings.write(stack, source, RegistryAccess.EMPTY);

        CustomData itemData = stack.get(DataComponents.CUSTOM_DATA);
        assertNotNull(itemData);
        assertFalse(itemData.copyTag().getCompound(SupplierSpawnerItemSettings.TAG_SETTINGS).contains("inventory"));
        TestSupplierSpawnerBlockEntity restored = new TestSupplierSpawnerBlockEntity(type, state);
        SupplierSpawnerItemSettings.apply(stack, restored, RegistryAccess.EMPTY);

        assertEquals(appearances, restored.getEnabledAppearanceIds());
        assertEquals(true, restored.isAutoCost());
    }

    private static final class TestSupplierSpawnerBlockEntity extends SupplierSpawnerBlockEntity {
        private TestSupplierSpawnerBlockEntity(BlockEntityType<?> type, BlockState state) {
            super(type, BlockPos.ZERO, state);
        }

        private void saveTo(CompoundTag tag) {
            saveAdditional(tag, RegistryAccess.EMPTY);
        }

        private void loadFrom(CompoundTag tag) {
            loadAdditional(tag, RegistryAccess.EMPTY);
        }
    }
}
