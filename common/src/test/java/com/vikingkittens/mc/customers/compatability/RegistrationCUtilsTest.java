package com.vikingkittens.mc.customers.compatability;

import dev.architectury.registry.registries.DeferredRegister;
import org.junit.jupiter.api.Test;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrationCUtilsTest {
    @Test
    void registersBlockWithTheKeyGivenToItsProperties() {
        DeferredRegister<Block> registry = DeferredRegister.create("customers", Registries.BLOCK);
        var block = RegistrationCUtils.registerBlock(
                registry,
                "test_block",
                key -> new Block(BlockBehaviour.Properties.of().setId(key))
        );

        assertEquals(
                ResourceKey.create(
                        Registries.BLOCK,
                        Identifier.fromNamespaceAndPath("customers", "test_block")
                ),
                block.getKey()
        );
    }

    @Test
    void registersItemWithTheKeyGivenToItsProperties() {
        DeferredRegister<Item> registry = DeferredRegister.create("customers", Registries.ITEM);
        var item = RegistrationCUtils.registerItem(
                registry,
                "test_item",
                key -> new Item(new Item.Properties().setId(key))
        );

        assertEquals(
                ResourceKey.create(
                        Registries.ITEM,
                        Identifier.fromNamespaceAndPath("customers", "test_item")
                ),
                item.getKey()
        );
    }
}
