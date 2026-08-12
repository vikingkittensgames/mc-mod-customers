package com.vikingkittens.mc.customers.customer.data;

import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

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
    public CustomerPickupCounterBlockStateProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, Customers.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Map.Entry<
                CustomerOverlayBlockVariant,
                ? extends java.util.function.Supplier<
                        CustomerPickupCounterBlock
                >
        > entry : CustomerPickupCounter.BLOCKS.entrySet()) {
            CustomerOverlayBlockVariant variant = entry.getKey();
            CustomerPickupCounterBlock block = entry.getValue().get();
            String name = CustomerPickupCounter.getBlockName(variant);

            BlockModelBuilder baseModel = models()
                    .getBuilder(name + "_base")
                    .texture("particle", variant.baseTexture())
                    .texture("base", variant.baseTexture());
            baseModel.element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 1.0F, 16.0F)
                    .textureAll("#base")
                    .end();

            BlockModelBuilder overlayModel = models()
                    .getBuilder(name + "_overlay")
                    .texture("particle", variant.baseTexture())
                    .texture("top_overlay", TOP_OVERLAY)
                    .renderType("minecraft:translucent");
            overlayModel.element()
                    .from(-0.01F, -0.01F, -0.01F)
                    .to(16.01F, 1.01F, 16.01F)
                    .face(Direction.UP)
                    .texture("#top_overlay")
                    .end()
                    .end();

            BlockModelBuilder model = models()
                    .getBuilder(name)
                    .texture("particle", variant.baseTexture());
            model.customLoader(CompositeModelBuilder::begin)
                    .child("base", baseModel)
                    .child("overlay", overlayModel)
                    .end();

            simpleBlock(block, model);
            ItemModelBuilder itemBaseModel = itemModels()
                    .getBuilder(name + "_base")
                    .texture("particle", variant.baseTexture())
                    .texture("base", variant.baseTexture())
                    .renderType("minecraft:translucent");
            itemBaseModel.element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 1.0F, 16.0F)
                    .textureAll("#base")
                    .end();

            ItemModelBuilder itemOverlayModel = itemModels()
                    .getBuilder(name + "_overlay")
                    .texture("particle", variant.baseTexture())
                    .texture("top_overlay", TOP_OVERLAY)
                    .renderType("minecraft:translucent");
            itemOverlayModel.element()
                    .from(-0.01F, -0.01F, -0.01F)
                    .to(16.01F, 1.01F, 16.01F)
                    .face(Direction.UP)
                    .texture("#top_overlay")
                    .end()
                    .end();

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
                    .translation(0.0F, 3.0F, 0.0F)
                    .scale(0.8F)
                    .end()
                    .end();
        }
    }

}
