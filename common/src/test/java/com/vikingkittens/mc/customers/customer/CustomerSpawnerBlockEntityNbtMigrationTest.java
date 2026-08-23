package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomerSpawnerBlockEntityNbtMigrationTest {
    private static final ResourceLocation TEST_APPEARANCE = new ResourceLocation("customers", "test_appearance");

    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void migratesVersionOneDataThroughVersionTwoIntoLevelOne() {
        assertLegacyDataWasMigrated(loadLegacySettings(1));
    }

    @Test
    void migratesVersionTwoDataIntoLevelOne() {
        assertLegacyDataWasMigrated(loadLegacySettings(2));
    }

    @Test
    void migratesRootInventoryEvenWhenItHasCurrentDataVersion() {
        assertLegacyDataWasMigrated(loadLegacySettings(3));
    }

    @Test
    void loadsRootInventoryIntoLevelOneBeforeMigratingLegacyData() {
        CustomerSpawnerBlockEntity spawner = new CustomerSpawnerBlockEntity(
                mock(BlockEntityType.class),
                BlockPos.ZERO,
                Blocks.AIR.defaultBlockState()
        );

        spawner.load(loadLegacyTag(2));

        assertLegacyDataWasMigrated(spawner.getLevelSettings(0));
    }

    private static CustomerSpawnerLevelSettings loadLegacySettings(int dataVersion) {
        CompoundTag tag = loadLegacyTag(dataVersion);
        CustomerSpawnerLevelSettings settings = new CustomerSpawnerLevelSettings(4, () -> {}, player -> true);
        settings.getPersistedInventory().deserializeNBT(tag.getCompound("inventory"));
        CustomerSpawnerBlockEntity.readLegacyLevelSettings(
                PersistenceCUtils.reader(tag),
                dataVersion,
                settings,
                4
        );
        return settings;
    }

    private static CompoundTag loadLegacyTag(int dataVersion) {
        PersistedContainer inventory = new PersistedContainer(54, () -> {});
        inventory.setItem(0, new ItemStack(Items.BREAD, 2));
        inventory.setItem(43, new ItemStack(Items.CARROT, 3));

        CompoundTag tag = new CompoundTag();
        tag.put("inventory", inventory.serializeNBT());
        PersistenceCUtils.writer(tag).putInt(CustomerSpawnerBlockEntity.TAG_DATA_VERSION, dataVersion);
        PersistenceCUtils.writer(tag).putInt(CustomerSpawnerBlockEntity.TAG_MAX_CUSTOMERS, 7);
        PersistenceCUtils.writer(tag).putStrings(
                "CustomersEnabledAppearances",
                List.of(CustomersVillagerAppearances.DEFAULT.toString(), TEST_APPEARANCE.toString())
        );
        return tag;
    }

    private static void assertLegacyDataWasMigrated(CustomerSpawnerLevelSettings settings) {
        assertEquals(7, settings.getMaxCustomers());
        assertEquals(Items.BREAD, settings.getInventory().getItem(0).getItem());
        assertEquals(2, settings.getInventory().getItem(0).getCount());
        assertEquals(Items.CARROT, settings.getInventory().getItem(43).getItem());
        assertEquals(3, settings.getInventory().getItem(43).getCount());
        assertEquals(
                List.of(CustomersVillagerAppearances.DEFAULT, TEST_APPEARANCE),
                settings.getEnabledAppearanceIds()
        );
    }
}
