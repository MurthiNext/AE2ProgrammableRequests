package com.murthinext.ae2pr.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.repeat.IRepeatRoundsHolder;
import com.murthinext.ae2pr.repeat.RepeatOrderBinding;
import com.murthinext.ae2pr.repeat.ServerRepeatRegistry;

import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 确认菜单：开始合成时消费登记表中的轮数，并在本次提交期间绑定到线程本地。
 */
@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuMixin {

    /**
     * 从确认界面返回数量界面时用于恢复的轮数（HEAD 捕获，RETURN 应用）。
     */
    @Unique
    @Nullable
    private Integer ae2pr$pendingRestoreRounds;

    // startJob 为 AE2 自定义方法，不做映射
    @Inject(method = "startJob", at = @At("HEAD"), remap = false)
    private void ae2pr$bindRepeatRounds(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (menu.getPlayer() instanceof ServerPlayer serverPlayer) {
            var rounds = ServerRepeatRegistry.take(serverPlayer.getUUID());
            if (rounds != null && rounds > 1) {
                RepeatOrderBinding.set(rounds);
            }
        }
    }

    @Inject(method = "startJob", at = @At("RETURN"), remap = false)
    private void ae2pr$clearRepeatRoundsBinding(CallbackInfo ci) {
        RepeatOrderBinding.clear();
    }

    // removed(Player) 是原版菜单方法重写，必须保留重映射
    @Inject(method = "removed", at = @At("HEAD"))
    private void ae2pr$clearPendingRepeatRounds(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerRepeatRegistry.clear(serverPlayer.getUUID());
        }
    }

    /**
     * 返回数量界面时，把已登记的轮数恢复到新菜单（否则会被重置为 1）。
     */
    @Inject(method = "goBack", at = @At("HEAD"), remap = false)
    private void ae2pr$captureRoundsForRestore(CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (menu.getPlayer() instanceof ServerPlayer serverPlayer) {
            this.ae2pr$pendingRestoreRounds = ServerRepeatRegistry.peek(serverPlayer.getUUID());
        }
    }

    @Inject(method = "goBack", at = @At("RETURN"), remap = false)
    private void ae2pr$restoreRoundsOnAmountScreen(CallbackInfo ci) {
        var pending = this.ae2pr$pendingRestoreRounds;
        this.ae2pr$pendingRestoreRounds = null;
        if (pending == null || pending <= 1) {
            return;
        }
        var menu = (CraftConfirmMenu) (Object) this;
        if (menu.getPlayer() instanceof ServerPlayer serverPlayer
                && serverPlayer.containerMenu instanceof CraftAmountMenu amountMenu) {
            ((IRepeatRoundsHolder) amountMenu).ae2pr$setRepeatRounds(pending);
            amountMenu.broadcastChanges();
        }
    }
}
