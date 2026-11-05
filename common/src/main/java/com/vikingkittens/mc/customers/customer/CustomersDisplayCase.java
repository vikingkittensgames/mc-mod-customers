package com.vikingkittens.mc.customers.customer;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariant;
import com.vikingkittens.mc.customers.customer.data.CustomerOverlayBlockVariants;

public final class CustomersDisplayCase {
    public static final String NAME = "customers_display_case";

    private static final DeferredRegister<Block> BLOCK_REGISTRY =
            DeferredRegister.create(Customers.MODID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEM_REGISTRY =
            DeferredRegister.create(Customers.MODID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>>
            BLOCK_ENTITY_REGISTRY = DeferredRegister.create(
                    Customers.MODID,
                    Registries.BLOCK_ENTITY_TYPE
            );
    private static final DeferredRegister<MenuType<?>> MENU_REGISTRY =
            DeferredRegister.create(Customers.MODID, Registries.MENU);

    public static final Map<
            CustomerOverlayBlockVariant,
            RegistrySupplier<CustomersDisplayCaseBlock>
    > BLOCKS = registerBlocks();
    public static final Map<
            CustomerOverlayBlockVariant,
            RegistrySupplier<BlockItem>
    > ITEMS = registerItems();
    public static final RegistrySupplier<
            BlockEntityType<CustomersDisplayCaseBlockEntity>
    > BLOCK_ENTITY = BLOCK_ENTITY_REGISTRY.register(
            NAME,
            () -> BlockEntityType.Builder.of(
                    CustomersDisplayCase::createBlockEntity,
                    BLOCKS.values().stream()
                            .map(RegistrySupplier::get)
                            .toArray(CustomersDisplayCaseBlock[]::new)
            ).build(null)
    );
    public static final RegistrySupplier<
            MenuType<CustomersDisplayCaseBlockMenu>
    > MENU = MENU_REGISTRY.register(
            NAME,
            () -> new MenuType<>(
                    CustomersDisplayCaseBlockMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    private CustomersDisplayCase() {}

    public static void initialize() {
        BLOCK_REGISTRY.register();
        ITEM_REGISTRY.register();
        BLOCK_ENTITY_REGISTRY.register();
        MENU_REGISTRY.register();
    }

    public static String getBlockName(
            CustomerOverlayBlockVariant variant
    ) {
        return variant.name() + "_" + NAME;
    }

    private static Map<
            CustomerOverlayBlockVariant,
            RegistrySupplier<CustomersDisplayCaseBlock>
    > registerBlocks() {
        Map<
                CustomerOverlayBlockVariant,
                RegistrySupplier<CustomersDisplayCaseBlock>
        > blocks = new LinkedHashMap<>();
        for (CustomerOverlayBlockVariant variant
                : CustomerOverlayBlockVariants.ALL) {
            blocks.put(
                    variant,
                    BLOCK_REGISTRY.register(
                            getBlockName(variant),
                            () -> new CustomersDisplayCaseBlock(
                                    createProperties(variant)
                            )
                    )
            );
        }
        return Map.copyOf(blocks);
    }

    private static BlockBehaviour.Properties createProperties(
            CustomerOverlayBlockVariant variant
    ) {
        Block source = variant.textureBlock().get();
        if (source instanceof RotatedPillarBlock) {
            source = Blocks.OAK_PLANKS;
        }
        return BlockBehaviour.Properties.ofFullCopy(source)
                .strength(2.0F)
                .noOcclusion();
    }

    private static Map<
            CustomerOverlayBlockVariant,
            RegistrySupplier<BlockItem>
    > registerItems() {
        Map<
                CustomerOverlayBlockVariant,
                RegistrySupplier<BlockItem>
        > items = new LinkedHashMap<>();
        for (Map.Entry<
                CustomerOverlayBlockVariant,
                RegistrySupplier<CustomersDisplayCaseBlock>
        > entry : BLOCKS.entrySet()) {
            items.put(
                    entry.getKey(),
                    ITEM_REGISTRY.register(
                            getBlockName(entry.getKey()),
                            () -> new BlockItem(
                                    entry.getValue().get(),
                                    new Item.Properties()
                            )
                    )
            );
        }
        return Map.copyOf(items);
    }

    private static CustomersDisplayCaseBlockEntity createBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new CustomersDisplayCaseBlockEntity(
                BLOCK_ENTITY.get(),
                pos,
                state
        );
    }
}
