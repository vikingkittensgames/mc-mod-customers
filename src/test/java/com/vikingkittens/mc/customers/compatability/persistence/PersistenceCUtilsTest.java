package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenceCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void readsAndWritesValuesAndChildren() {
        CompoundTag tag = new CompoundTag();
        BlockPos position = new BlockPos(1, 2, 3);
        BlockState state = Blocks.STONE.defaultBlockState();
        UUID uuid = UUID.randomUUID();

        DataWriter writer = PersistenceCUtils.writer(tag);
        writer.putString("string", "value");
        writer.putBoolean("boolean", true);
        writer.putBlockPos("position", position);
        writer.putBlockState("state", state);
        writer.putUuid("uuid", uuid);
        writer.putUuids("uuids", List.of(uuid));
        writer.child("child").putString("nested", "child value");
        writer.addChild("children").putString("nested", "list child value");
        writer.addChild("children").putString("nested", "second list child value");

        DataReader reader = PersistenceCUtils.reader(tag);

        assertEquals(Optional.of("value"), reader.getString("string"));
        assertEquals(true, reader.getBoolean("boolean"));
        assertEquals(Optional.of(position), reader.getBlockPos("position"));
        assertEquals(Optional.of(state), reader.getBlockState("state"));
        assertEquals(Optional.of(uuid), reader.getUuid("uuid"));
        assertEquals(List.of(uuid), reader.getUuids("uuids"));
        assertEquals(Optional.of("child value"), reader.childOrEmpty("child").getString("nested"));
        assertEquals(
                List.of(Optional.of("list child value"), Optional.of("second list child value")),
                reader.getChildren("children").stream()
                        .map(child -> child.getString("nested"))
                        .toList()
        );
    }

    @Test
    void returnsDefaultsForMissingValues() {
        DataReader reader = PersistenceCUtils.reader(new CompoundTag());

        assertEquals(Optional.empty(), reader.getString("string"));
        assertFalse(reader.getBoolean("boolean"));
        assertEquals(Optional.empty(), reader.getBlockPos("position"));
        assertEquals(Optional.empty(), reader.getBlockState("state"));
        assertEquals(Optional.empty(), reader.getUuid("uuid"));
        assertEquals(List.of(), reader.getUuids("uuids"));
        assertEquals(Optional.empty(), reader.childOrEmpty("child").getString("nested"));
        assertEquals(List.of(), reader.getChildren("children"));
    }
    /** Preserves item identity, components, and counts. */
    @Test
    void readsAndWritesItemStacks() {
        CompoundTag tag = new CompoundTag();
        ItemStack bread = new ItemStack(Items.BREAD, 25);

        PersistenceCUtils.writer(tag, RegistryAccess.EMPTY)
                .putItemStacks("stacks", List.of(bread));
        List<ItemStack> restored =
                PersistenceCUtils.reader(tag, RegistryAccess.EMPTY)
                        .getItemStacks("stacks");

        assertEquals(1, restored.size());
        assertTrue(ItemStack.isSameItemSameComponents(
                bread,
                restored.getFirst()
        ));
        assertEquals(25, restored.getFirst().getCount());
    }
}
