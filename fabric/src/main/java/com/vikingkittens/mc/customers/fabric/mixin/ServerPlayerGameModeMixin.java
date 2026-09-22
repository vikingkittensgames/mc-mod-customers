package com.vikingkittens.mc.customers.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.vikingkittens.mc.customers.common.BlockBreakConfirmation;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardBlock;
import com.vikingkittens.mc.customers.customer.CustomerLeaderboardBlockEntity;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlock;
import com.vikingkittens.mc.customers.customer.CustomerSpawnerBlockEntity;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerBlock;
import com.vikingkittens.mc.customers.supplier.SupplierSpawnerBlockEntity;

@Mixin(value = ServerPlayerGameMode.class, priority = 900)
abstract class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    protected ServerPlayer player;

    @Inject(
            method = "destroyBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy("
                            + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
                            + "Lnet/minecraft/world/level/block/state/BlockState;"
                            + "Lnet/minecraft/world/entity/player/Player;)"
                            + "Lnet/minecraft/world/level/block/state/BlockState;"
            ),
            cancellable = true
    )
    private void confirmBlockBreak(BlockPos position, CallbackInfoReturnable<Boolean> callback) {
        BlockState state = level.getBlockState(position);
        Block block = state.getBlock();
        if (block instanceof CustomerSpawnerBlock
                && level.getBlockEntity(position) instanceof CustomerSpawnerBlockEntity spawner
                && spawner.shouldConfirmBreak()
                && BlockBreakConfirmation.shouldCancelBreak(
                        player,
                        position,
                        spawner,
                        "screen.customers.break_confirmation.customer_spawner_title",
                        "screen.customers.break_confirmation.message"
                )) {
            callback.setReturnValue(false);
        } else if (block instanceof CustomerLeaderboardBlock
                && level.getBlockEntity(position) instanceof CustomerLeaderboardBlockEntity leaderboard
                && leaderboard.shouldConfirmBreak()
                && BlockBreakConfirmation.shouldCancelBreak(
                        player,
                        position,
                        leaderboard,
                        "screen.customers.break_confirmation.customer_leaderboard_title",
                        "screen.customers.break_confirmation.customer_leaderboard_message"
                )) {
            callback.setReturnValue(false);
        } else if (block instanceof SupplierSpawnerBlock
                && level.getBlockEntity(position) instanceof SupplierSpawnerBlockEntity spawner
                && spawner.shouldConfirmBreak()
                && BlockBreakConfirmation.shouldCancelBreak(
                        player,
                        position,
                        spawner,
                        "screen.customers.break_confirmation.supplier_spawner_title",
                        "screen.customers.break_confirmation.message"
                )) {
            callback.setReturnValue(false);
        }
    }
}
