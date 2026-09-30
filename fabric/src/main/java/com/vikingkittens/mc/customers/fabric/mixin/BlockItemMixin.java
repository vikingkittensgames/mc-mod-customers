package com.vikingkittens.mc.customers.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.customer.CustomerLeaderboardCache;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerCache;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {
    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void onPlaceBlock(
            BlockPlaceContext context,
            BlockState state,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (callback.getReturnValue()
                && context.getLevel() instanceof ServerLevel level) {
            BlockPos position = context.getClickedPos();
            Player player = context.getPlayer();
            CustomerSpawnerCache.onBlockPlaced(level, position, state, player);
            CustomerLeaderboardCache.onBlockPlaced(
                    level,
                    position,
                    state
            );
        }
    }
}
