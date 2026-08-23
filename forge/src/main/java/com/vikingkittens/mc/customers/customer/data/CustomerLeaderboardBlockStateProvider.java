package com.vikingkittens.mc.customers.customer.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import com.vikingkittens.mc.customers.Customers;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboard;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardBlock;

public class CustomerLeaderboardBlockStateProvider extends BlockStateProvider {
    public CustomerLeaderboardBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Customers.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Customer Leaderboard Block States: " + Customers.MODID;
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(Customers.MODID, "block/leaderboard_block");
        horizontalBlock(CustomerLeaderboard.BLOCK.get(), models().getExistingFile(modelId));
        itemModels().withExistingParent(CustomerLeaderboardBlock.NAME, modelId)
                .transforms()
                .transform(ItemDisplayContext.GUI).rotation(0.0F, 180.0F, 0.0F).end()
                .transform(ItemDisplayContext.GROUND).rotation(0.0F, 180.0F, 0.0F).end()
                .transform(ItemDisplayContext.FIXED).rotation(0.0F, 180.0F, 0.0F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0.0F, 180.0F, 0.0F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0.0F, 180.0F, 0.0F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0.0F, 180.0F, 0.0F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0.0F, 180.0F, 0.0F).end()
                .end();
    }
}
