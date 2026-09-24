package com.murthinext.ae2pr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.murthinext.ae2pr.logic.repeat.IRepeatRoundsHolder;
import com.murthinext.ae2pr.logic.repeat.ServerRepeatRegistry;

import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

/**
 * 下单入口兼容层：打开确认菜单前统一登记重复轮数。
 *
 * <p>AE2 原生路径与 GTLCore 的长数量路径都会经过 {@code MenuOpener.open} 打开确认菜单，
 * 因此不再依赖 {@code CraftAmountMenu.confirm}（该方法会被 GTLCore 的
 * {@code CraftAmountScreenMixin} 截断并改走 {@code gtlcore$confirmLongAmount}）。
 */
@Mixin(value = MenuOpener.class, remap = false)
public abstract class MenuOpenerMixin {

    @Inject(method = "open(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/entity/player/Player;Lappeng/menu/locator/MenuLocator;Z)Z",
            at = @At("HEAD"), remap = false)
    private static void ae2pr$captureRepeatRounds(MenuType<?> type, Player player, MenuLocator locator,
            boolean fromSubMenu, CallbackInfoReturnable<Boolean> cir) {
        if (type != CraftConfirmMenu.TYPE || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        var playerId = serverPlayer.getUUID();
        ServerRepeatRegistry.clearRestore(playerId);
        // 仅当从数量选择界面进入确认界面时才登记，避免直接打开确认菜单时误用残留轮数
        if (player.containerMenu instanceof CraftAmountMenu
                && player.containerMenu instanceof IRepeatRoundsHolder holder) {
            var rounds = Math.max(1, holder.ae2pr$getRepeatRounds());
            if (rounds > 1) {
                ServerRepeatRegistry.put(playerId, rounds);
            } else {
                ServerRepeatRegistry.clear(playerId);
            }
        } else {
            ServerRepeatRegistry.clear(playerId);
        }
    }
}
