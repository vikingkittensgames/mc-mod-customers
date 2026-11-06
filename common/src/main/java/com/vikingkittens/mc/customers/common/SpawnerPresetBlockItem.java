package com.vikingkittens.mc.customers.common;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public final class SpawnerPresetBlockItem extends BlockItem {
    private final ResourceKey<Registry<SpawnerPresetDefinition>> presetType;
    private final SpawnerPresets.Applier applier;

    public SpawnerPresetBlockItem(Block block, Properties properties,
            ResourceKey<Registry<SpawnerPresetDefinition>> presetType, SpawnerPresets.Applier applier) {
        super(block, properties);
        this.presetType = presetType;
        this.applier = applier;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!LevelCUtils.isClientSide(level)) {
            SpawnerPresets.apply(stack, level.registryAccess(), presetType, applier);
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (!LevelCUtils.isClientSide(context.getLevel())) {
            SpawnerPresets.apply(context.getItemInHand(), context.getLevel().registryAccess(), presetType, applier);
        }
        return super.place(context);
    }
}
