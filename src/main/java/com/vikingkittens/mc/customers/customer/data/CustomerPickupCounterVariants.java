package com.vikingkittens.mc.customers.customer.data;

import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class CustomerPickupCounterVariants {
    public static final List<CustomerPickupCounterVariant> ALL = List.of(
            variant("iron", Items.IRON_INGOT, Blocks.IRON_BLOCK, "iron_block"),
            variant(
                    "copper",
                    Items.COPPER_INGOT,
                    Blocks.COPPER_BLOCK,
                    "copper_block"
            ),
            variant("gold", Items.GOLD_INGOT, Blocks.GOLD_BLOCK, "gold_block"),
            variant(
                    "oak",
                    Blocks.STRIPPED_OAK_LOG,
                    Blocks.STRIPPED_OAK_LOG,
                    "stripped_oak_log"
            ),
            variant(
                    "spruce",
                    Blocks.STRIPPED_SPRUCE_LOG,
                    Blocks.STRIPPED_SPRUCE_LOG,
                    "stripped_spruce_log"
            ),
            variant(
                    "birch",
                    Blocks.STRIPPED_BIRCH_LOG,
                    Blocks.STRIPPED_BIRCH_LOG,
                    "stripped_birch_log"
            ),
            variant(
                    "jungle",
                    Blocks.STRIPPED_JUNGLE_LOG,
                    Blocks.STRIPPED_JUNGLE_LOG,
                    "stripped_jungle_log"
            ),
            variant(
                    "acacia",
                    Blocks.STRIPPED_ACACIA_LOG,
                    Blocks.STRIPPED_ACACIA_LOG,
                    "stripped_acacia_log"
            ),
            variant(
                    "dark_oak",
                    Blocks.STRIPPED_DARK_OAK_LOG,
                    Blocks.STRIPPED_DARK_OAK_LOG,
                    "stripped_dark_oak_log"
            ),
            variant(
                    "mangrove",
                    Blocks.STRIPPED_MANGROVE_LOG,
                    Blocks.STRIPPED_MANGROVE_LOG,
                    "stripped_mangrove_log"
            ),
            variant(
                    "cherry",
                    Blocks.STRIPPED_CHERRY_LOG,
                    Blocks.STRIPPED_CHERRY_LOG,
                    "stripped_cherry_log"
            ),
            variant(
                    "crimson",
                    Blocks.STRIPPED_CRIMSON_STEM,
                    Blocks.STRIPPED_CRIMSON_STEM,
                    "stripped_crimson_stem"
            ),
            variant(
                    "warped",
                    Blocks.STRIPPED_WARPED_STEM,
                    Blocks.STRIPPED_WARPED_STEM,
                    "stripped_warped_stem"
            ),
            variant(
                    "bamboo",
                    Blocks.BAMBOO_BLOCK,
                    Blocks.BAMBOO_BLOCK,
                    "bamboo_block"
            ),
            variant(
                    "stripped_bamboo",
                    Blocks.STRIPPED_BAMBOO_BLOCK,
                    Blocks.STRIPPED_BAMBOO_BLOCK,
                    "stripped_bamboo_block"
            )
    );

    private CustomerPickupCounterVariants() {
    }

    private static CustomerPickupCounterVariant variant(
            String name,
            ItemLike ingredient,
            Block textureBlock,
            String textureName
    ) {
        return new CustomerPickupCounterVariant(
                name,
                () -> ingredient,
                () -> textureBlock,
                Identifier.withDefaultNamespace(
                        "block/" + textureName
                )
        );
    }
}
