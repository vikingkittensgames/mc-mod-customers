package com.vikingkittens.mc.customers.customer.data;

import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounter;
import com.vikingkittens.mc.customers.customer.CustomerPickupCounterBlock;

public class CustomerPickupCounterBlockStateProvider
        extends BlockStateProvider {
    private static final ResourceLocation TOP_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "block/customer_pickup_counter_top_overlay"
            );
    private static final ResourceLocation BOTTOM_SIDE_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(
                    Customers.MODID,
                    "block/customer_pickup_counter_bottom_side_overlay"
            );

    public CustomerPickupCounterBlockStateProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, Customers.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Map.Entry<
                CustomerPickupCounterVariant,
                ? extends java.util.function.Supplier<
                        CustomerPickupCounterBlock
                >
        > entry : CustomerPickupCounter.BLOCKS.entrySet()) {
            CustomerPickupCounterVariant variant = entry.getKey();
            CustomerPickupCounterBlock block = entry.getValue().get();
            String name = CustomerPickupCounter.getBlockName(variant);

            BlockModelBuilder model = models()
                    .getBuilder(name)
                    .texture("particle", variant.sideTexture())
                    .renderType("minecraft:translucent");
            model.customLoader(CompositeModelBuilder::begin)
                    .child("base", createBaseModel(variant))
                    .child("overlay", createOverlayModel())
                    .end();

            simpleBlock(block, model);
            itemModels()
                    .getBuilder(name)
                    .parent(model)
                    .transforms()
                    .transform(ItemDisplayContext.GUI)
                    .rotation(45.0F, 225.0F, 0.0F)
                    .translation(0.0F, 3.0F, 0.0F)
                    .scale(0.8F)
                    .end()
                    .end();
        }
    }

    private BlockModelBuilder createBaseModel(
            CustomerPickupCounterVariant variant
    ) {
        return models().nested()
                .renderType("minecraft:solid")
                .texture("particle", variant.sideTexture())
                .texture("base", variant.sideTexture())
                .element()
                .from(0.0F, 0.0F, 0.0F)
                .to(16.0F, 1.0F, 16.0F)
                .textureAll("#base")
                .end();
    }

    private BlockModelBuilder createOverlayModel() {
        return models().nested()
                .renderType("minecraft:translucent")
                .texture("top_overlay", TOP_OVERLAY)
                .texture("bottom_side_overlay", BOTTOM_SIDE_OVERLAY)
                .element()
                .from(-0.001F, -0.001F, -0.001F)
                .to(16.001F, 1.001F, 16.001F)
                .face(Direction.UP)
                .texture("#top_overlay")
                .end()
                .face(Direction.DOWN)
                .texture("#bottom_side_overlay")
                .end()
                .face(Direction.NORTH)
                .texture("#bottom_side_overlay")
                .end()
                .face(Direction.SOUTH)
                .texture("#bottom_side_overlay")
                .end()
                .face(Direction.WEST)
                .texture("#bottom_side_overlay")
                .end()
                .face(Direction.EAST)
                .texture("#bottom_side_overlay")
                .end()
                .end();
    }
}
