package com.vikingkittens.mc.customers.customer.data;

import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBox;
import com.vikingkittens.mc.customers.customer.CustomerPaymentBoxBlock;

public class CustomerPaymentBoxBlockStateProvider
        extends BlockStateProvider {
    private static final ResourceLocation TOP_OVERLAY =
            overlay("top");
    private static final ResourceLocation BOTTOM_OVERLAY =
            overlay("bottom");
    private static final ResourceLocation SIDE_OVERLAY =
            overlay("side");
    private static final ResourceLocation FRONT_OVERLAY =
            overlay("front");

    public CustomerPaymentBoxBlockStateProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, Customers.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Customer Payment Box Block States: " + Customers.MODID;
    }

    @Override
    protected void registerStatesAndModels() {
        for (Map.Entry<
                CustomerOverlayBlockVariant,
                ? extends java.util.function.Supplier<
                        CustomerPaymentBoxBlock
                >
        > entry : CustomerPaymentBox.BLOCKS.entrySet()) {
            CustomerOverlayBlockVariant variant = entry.getKey();
            CustomerPaymentBoxBlock block = entry.getValue().get();
            String name = CustomerPaymentBox.getBlockName(variant);

            BlockModelBuilder baseModel = models()
                    .getBuilder(name + "_base")
                    .texture("particle", variant.baseTexture())
                    .texture("base", variant.baseTexture());
            baseModel.element()
                    .from(1.0F, 0.0F, 1.0F)
                    .to(15.0F, 14.0F, 15.0F)
                    .textureAll("#base")
                    .end();

            BlockModelBuilder overlayModel = models()
                    .getBuilder(name + "_overlay")
                    .texture("particle", variant.baseTexture())
                    .texture("top", TOP_OVERLAY)
                    .texture("bottom", BOTTOM_OVERLAY)
                    .texture("side", SIDE_OVERLAY)
                    .texture("front", FRONT_OVERLAY)
                    .renderType("minecraft:translucent");
            BlockModelBuilder.ElementBuilder overlay =
                    overlayModel.element()
                            .from(0.99F, -0.01F, 0.99F)
                            .to(15.01F, 14.01F, 15.01F);
            overlay.face(Direction.UP).texture("#top").end();
            overlay.face(Direction.DOWN).texture("#bottom").end();
            overlay.face(Direction.NORTH).texture("#front").end();
            overlay.face(Direction.SOUTH).texture("#side").end();
            overlay.face(Direction.EAST).texture("#side").end();
            overlay.face(Direction.WEST).texture("#side").end();
            overlay.end();

            BlockModelBuilder model = models()
                    .getBuilder(name)
                    .texture("particle", variant.baseTexture());
            model.customLoader(CompositeModelBuilder::begin)
                    .child("base", baseModel)
                    .child("overlay", overlayModel)
                    .end();

            horizontalBlock(block, model);
            ItemModelBuilder itemBaseModel = itemModels()
                    .getBuilder(name + "_base")
                    .texture("particle", variant.baseTexture())
                    .texture("base", variant.baseTexture())
                    .renderType("minecraft:translucent");
            itemBaseModel.element()
                    .from(1.0F, 0.0F, 1.0F)
                    .to(15.0F, 14.0F, 15.0F)
                    .textureAll("#base")
                    .end();

            ItemModelBuilder itemOverlayModel = itemModels()
                    .getBuilder(name + "_overlay")
                    .texture("particle", variant.baseTexture())
                    .texture("top", TOP_OVERLAY)
                    .texture("bottom", BOTTOM_OVERLAY)
                    .texture("side", SIDE_OVERLAY)
                    .texture("front", FRONT_OVERLAY)
                    .renderType("minecraft:translucent");
            var itemOverlay = itemOverlayModel.element()
                    .from(0.99F, -0.01F, 0.99F)
                    .to(15.01F, 14.01F, 15.01F);
            itemOverlay.face(Direction.UP).texture("#top").end();
            itemOverlay.face(Direction.DOWN).texture("#bottom").end();
            itemOverlay.face(Direction.NORTH).texture("#front").end();
            itemOverlay.face(Direction.SOUTH).texture("#side").end();
            itemOverlay.face(Direction.EAST).texture("#side").end();
            itemOverlay.face(Direction.WEST).texture("#side").end();
            itemOverlay.end();

            ItemModelBuilder itemModel = itemModels()
                    .getBuilder(name)
                    .texture("particle", variant.baseTexture());
            itemModel.customLoader(CompositeModelBuilder::begin)
                    .child("base", itemBaseModel)
                    .child("overlay", itemOverlayModel)
                    .end();
            itemModel.transforms()
                    .transform(ItemDisplayContext.GUI)
                    .rotation(45.0F, 225.0F, 0.0F)
                    .translation(0.0F, 1.0F, 0.0F)
                    .scale(0.6F)
                    .end()
                    .end();
        }
    }

    private static ResourceLocation overlay(String side) {
        return ResourceLocation.fromNamespaceAndPath(
                Customers.MODID,
                "block/customer_payment_box_block_overlay_" + side
        );
    }
}
