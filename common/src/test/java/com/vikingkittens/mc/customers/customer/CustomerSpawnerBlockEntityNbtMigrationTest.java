package com.vikingkittens.mc.customers.customer;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;
import com.vikingkittens.mc.customers.appearance.CustomersVillagerAppearances;
import com.vikingkittens.mc.customers.compatability.persistence.PersistedContainer;
import com.vikingkittens.mc.customers.compatability.persistence.PersistenceCUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerSpawnerBlockEntityNbtMigrationTest {
    private static final Identifier TEST_APPEARANCE =
            Identifier.fromNamespaceAndPath("customers", "test_appearance");

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

    private static CustomerSpawnerLevelSettings loadLegacySettings(int dataVersion) {
        PersistedContainer inventory = new PersistedContainer(54, () -> {});
        inventory.setItem(0, new ItemStack(Items.BREAD, 2));
        inventory.setItem(43, new ItemStack(Items.CARROT, 3));

        CompoundTag tag = new CompoundTag();
        tag.put("inventory", inventory.serializeNBT(RegistryAccess.EMPTY));
        PersistenceCUtils.writer(tag)
                .putInt(CustomerSpawnerBlockEntity.TAG_DATA_VERSION, dataVersion);
        PersistenceCUtils.writer(tag)
                .putInt(CustomerSpawnerBlockEntity.TAG_MAX_CUSTOMERS, 7);
        PersistenceCUtils.writer(tag).putStrings(
                "CustomersEnabledAppearances",
                List.of(
                        CustomersVillagerAppearances.DEFAULT.toString(),
                        TEST_APPEARANCE.toString()
                )
        );

        CustomerSpawnerLevelSettings settings = new CustomerSpawnerLevelSettings(
                4,
                () -> {},
                player -> true
        );
        settings.getPersistedInventory().deserializeNBT(
                RegistryAccess.EMPTY,
                tag.getCompound("inventory").orElseThrow()
        );
        CustomerSpawnerBlockEntity.readLegacyLevelSettings(
                PersistenceCUtils.reader(tag, RegistryAccess.EMPTY),
                dataVersion,
                settings,
                4
        );
        return settings;
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
