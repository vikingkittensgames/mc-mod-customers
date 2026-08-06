package com.vikingkittens.mc.customers.compatability.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceCUtilsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }
    @Test
    @SuppressWarnings("unchecked")
    void readsValuesAndChildren() {
        ValueInput input = mock(ValueInput.class);
        ValueInput childInput = mock(ValueInput.class);
        ValueInput listChildInput = mock(ValueInput.class);
        ValueInput.TypedInputList<UUID> uuidInputs = mock(ValueInput.TypedInputList.class);
        ValueInput.TypedInputList<ItemStack> itemInputs = mock(ValueInput.TypedInputList.class);
        ValueInput.ValueInputList childInputs = mock(ValueInput.ValueInputList.class);
        BlockPos position = new BlockPos(1, 2, 3);
        BlockState state = Blocks.STONE.defaultBlockState();
        UUID uuid = UUID.randomUUID();
        ItemStack bread = new ItemStack(Items.BREAD, 25);

        when(input.getString("string")).thenReturn(Optional.of("value"));
        when(input.getBooleanOr("boolean", false)).thenReturn(true);
        when(input.read("position", BlockPos.CODEC)).thenReturn(Optional.of(position));
        when(input.read("state", BlockState.CODEC)).thenReturn(Optional.of(state));
        when(input.read("uuid", UUIDUtil.CODEC)).thenReturn(Optional.of(uuid));
        when(input.listOrEmpty("uuids", UUIDUtil.CODEC)).thenReturn(uuidInputs);
        when(uuidInputs.stream()).thenReturn(List.of(uuid).stream());
        when(input.listOrEmpty("stacks", ItemStack.CODEC)).thenReturn(itemInputs);
        when(itemInputs.stream()).thenReturn(List.of(bread).stream());
        when(input.childOrEmpty("child")).thenReturn(childInput);
        when(childInput.getString("nested")).thenReturn(Optional.of("child value"));
        when(input.childrenListOrEmpty("children")).thenReturn(childInputs);
        when(childInputs.stream()).thenReturn(List.of(listChildInput).stream());
        when(listChildInput.getString("nested")).thenReturn(Optional.of("list child value"));

        DataReader reader = PersistenceCUtils.reader(input);

        assertEquals(Optional.of("value"), reader.getString("string"));
        assertEquals(true, reader.getBoolean("boolean"));
        assertEquals(Optional.of(position), reader.getBlockPos("position"));
        assertEquals(Optional.of(state), reader.getBlockState("state"));
        assertEquals(Optional.of(uuid), reader.getUuid("uuid"));
        assertEquals(List.of(uuid), reader.getUuids("uuids"));
        assertEquals(List.of(bread), reader.getItemStacks("stacks"));
        assertEquals(Optional.of("child value"), reader.childOrEmpty("child").getString("nested"));
        assertEquals(Optional.of("list child value"), reader.getChildren("children").getFirst().getString("nested"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsDefaultsForMissingValues() {
        ValueInput input = mock(ValueInput.class);
        ValueInput.TypedInputList<UUID> uuidInputs = mock(ValueInput.TypedInputList.class);
        ValueInput.TypedInputList<ItemStack> itemInputs = mock(ValueInput.TypedInputList.class);
        ValueInput.ValueInputList childInputs = mock(ValueInput.ValueInputList.class);

        when(input.getString("string")).thenReturn(Optional.empty());
        when(input.getBooleanOr("boolean", false)).thenReturn(false);
        when(input.listOrEmpty("uuids", UUIDUtil.CODEC)).thenReturn(uuidInputs);
        when(uuidInputs.stream()).thenReturn(List.<UUID>of().stream());
        when(input.childrenListOrEmpty("children")).thenReturn(childInputs);
        when(childInputs.stream()).thenReturn(List.<ValueInput>of().stream());

        DataReader reader = PersistenceCUtils.reader(input);

        assertEquals(Optional.empty(), reader.getString("string"));
        assertFalse(reader.getBoolean("boolean"));
        assertEquals(List.of(), reader.getUuids("uuids"));
        assertEquals(List.of(), reader.getChildren("children"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void writesValuesAndChildren() {
        ValueOutput output = mock(ValueOutput.class);
        ValueOutput childOutput = mock(ValueOutput.class);
        ValueOutput listChildOutput = mock(ValueOutput.class);
        ValueOutput secondListChildOutput = mock(ValueOutput.class);
        ValueOutput.TypedOutputList<UUID> uuidOutputs = mock(ValueOutput.TypedOutputList.class);
        ValueOutput.TypedOutputList<ItemStack> itemOutputs = mock(ValueOutput.TypedOutputList.class);
        ValueOutput.ValueOutputList childOutputs = mock(ValueOutput.ValueOutputList.class);
        BlockPos position = new BlockPos(1, 2, 3);
        BlockState state = Blocks.STONE.defaultBlockState();
        UUID uuid = UUID.randomUUID();
        ItemStack bread = new ItemStack(Items.BREAD, 25);

        when(output.list("uuids", UUIDUtil.CODEC)).thenReturn(uuidOutputs);
        when(output.list("stacks", ItemStack.CODEC)).thenReturn(itemOutputs);
        when(output.child("child")).thenReturn(childOutput);
        when(output.childrenList("children")).thenReturn(childOutputs);
        when(childOutputs.addChild()).thenReturn(listChildOutput, secondListChildOutput);

        DataWriter writer = PersistenceCUtils.writer(output);
        writer.putString("string", "value");
        writer.putBoolean("boolean", true);
        writer.putBlockPos("position", position);
        writer.putBlockState("state", state);
        writer.putUuid("uuid", uuid);
        writer.putUuids("uuids", List.of(uuid));
        writer.putItemStacks("stacks", List.of(bread));
        writer.child("child").putString("nested", "child value");
        writer.addChild("children").putString("nested", "list child value");
        writer.addChild("children").putString("nested", "second list child value");

        verify(output).putString("string", "value");
        verify(output).putBoolean("boolean", true);
        verify(output).store("position", BlockPos.CODEC, position);
        verify(output).store("state", BlockState.CODEC, state);
        verify(output).store("uuid", UUIDUtil.CODEC, uuid);
        verify(uuidOutputs).add(uuid);
        verify(itemOutputs).add(bread);
        verify(childOutput).putString("nested", "child value");
        verify(listChildOutput).putString("nested", "list child value");
        verify(secondListChildOutput).putString("nested", "second list child value");
        verify(output, times(1)).childrenList("children");
    }
}
