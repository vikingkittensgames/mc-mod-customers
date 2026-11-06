package com.vikingkittens.mc.customers.common;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.vikingkittens.mc.customers.compatability.LevelCUtils;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerItem;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerItem;

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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        if (presetType.equals(SpawnerPresets.CUSTOMER)) {
            lines.addAll(CustomerSpawnerItem.getTooltipLines(stack, context.registries()));
        } else if (presetType.equals(SpawnerPresets.SUPPLIER)) {
            lines.addAll(SupplierSpawnerItem.getTooltipLines(stack, context.registries()));
        }
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
