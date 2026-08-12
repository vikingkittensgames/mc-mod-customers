package com.vikingkittens.mc.customers.customer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.vikingkittens.mc.customers.compatability.CustomersServices;
import com.vikingkittens.mc.customers.compatability.LevelCUtils;

public final class CustomerInteractions {
    private CustomerInteractions() {}

    public static boolean shouldUsePickupCounter(Player player, Level level, BlockPos position) {
        return player.isSecondaryUseActive() &&
                level.getBlockState(position).getBlock() instanceof CustomerPickupCounterBlock;
    }

    public static boolean tryQuickSell(Player player, InteractionHand hand, Entity target) {
        return CustomersServices.config().quickSellEnabled() &&
                hand == InteractionHand.MAIN_HAND &&
                !LevelCUtils.isClientSide(player.level()) &&
                target instanceof CustomerVillagerEntity customer &&
                customer.tryQuickSell(player);
    }
}
