package com.vikingkittens.mc.customers.customer.data;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.vikingkittens.mc.customers.MinecraftTestBootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerPickupCounterVariantsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftTestBootstrap.bootstrap();
    }

    @Test
    void definesEveryPickupCounterVariant() {
        assertEquals(
                List.of(
                        "iron",
                        "copper",
                        "gold",
                        "oak",
                        "spruce",
                        "birch",
                        "jungle",
                        "acacia",
                        "dark_oak",
                        "mangrove",
                        "cherry",
                        "crimson",
                        "warped",
                        "bamboo",
                        "stripped_bamboo"
                ),
                CustomerPickupCounterVariants.ALL.stream()
                        .map(CustomerPickupCounterVariant::name)
                        .toList()
        );
    }

    @Test
    void definesMetalIngredientsAndTextures() {
        assertVariant(
                "iron",
                Items.IRON_INGOT,
                Blocks.IRON_BLOCK,
                "block/iron_block"
        );
        assertVariant(
                "copper",
                Items.COPPER_INGOT,
                Blocks.COPPER_BLOCK,
                "block/copper_block"
        );
        assertVariant(
                "gold",
                Items.GOLD_INGOT,
                Blocks.GOLD_BLOCK,
                "block/gold_block"
        );
    }

    @Test
    void definesWoodAndBambooIngredientsAndTextures() {
        assertVariant(
                "oak",
                Blocks.STRIPPED_OAK_LOG.asItem(),
                Blocks.STRIPPED_OAK_LOG,
                "block/stripped_oak_log"
        );
        assertVariant(
                "spruce",
                Blocks.STRIPPED_SPRUCE_LOG.asItem(),
                Blocks.STRIPPED_SPRUCE_LOG,
                "block/stripped_spruce_log"
        );
        assertVariant(
                "birch",
                Blocks.STRIPPED_BIRCH_LOG.asItem(),
                Blocks.STRIPPED_BIRCH_LOG,
                "block/stripped_birch_log"
        );
        assertVariant(
                "jungle",
                Blocks.STRIPPED_JUNGLE_LOG.asItem(),
                Blocks.STRIPPED_JUNGLE_LOG,
                "block/stripped_jungle_log"
        );
        assertVariant(
                "acacia",
                Blocks.STRIPPED_ACACIA_LOG.asItem(),
                Blocks.STRIPPED_ACACIA_LOG,
                "block/stripped_acacia_log"
        );
        assertVariant(
                "dark_oak",
                Blocks.STRIPPED_DARK_OAK_LOG.asItem(),
                Blocks.STRIPPED_DARK_OAK_LOG,
                "block/stripped_dark_oak_log"
        );
        assertVariant(
                "mangrove",
                Blocks.STRIPPED_MANGROVE_LOG.asItem(),
                Blocks.STRIPPED_MANGROVE_LOG,
                "block/stripped_mangrove_log"
        );
        assertVariant(
                "cherry",
                Blocks.STRIPPED_CHERRY_LOG.asItem(),
                Blocks.STRIPPED_CHERRY_LOG,
                "block/stripped_cherry_log"
        );
        assertVariant(
                "crimson",
                Blocks.STRIPPED_CRIMSON_STEM.asItem(),
                Blocks.STRIPPED_CRIMSON_STEM,
                "block/stripped_crimson_stem"
        );
        assertVariant(
                "warped",
                Blocks.STRIPPED_WARPED_STEM.asItem(),
                Blocks.STRIPPED_WARPED_STEM,
                "block/stripped_warped_stem"
        );
        assertVariant(
                "bamboo",
                Blocks.BAMBOO_BLOCK.asItem(),
                Blocks.BAMBOO_BLOCK,
                "block/bamboo_block"
        );
        assertVariant(
                "stripped_bamboo",
                Blocks.STRIPPED_BAMBOO_BLOCK.asItem(),
                Blocks.STRIPPED_BAMBOO_BLOCK,
                "block/stripped_bamboo_block"
        );
    }

    private static void assertVariant(
            String name,
            Item ingredient,
            Block textureBlock,
            String texturePath
    ) {
        CustomerPickupCounterVariant variant =
                CustomerPickupCounterVariants.ALL.stream()
                        .filter(candidate -> candidate.name().equals(name))
                        .findFirst()
                        .orElseThrow();

        assertEquals(ingredient, variant.ingredient().get().asItem());
        assertEquals(textureBlock, variant.textureBlock().get());
        assertEquals(
                Identifier.withDefaultNamespace(texturePath),
                variant.sideTexture()
        );
    }
}
