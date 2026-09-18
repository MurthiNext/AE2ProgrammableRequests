package com.murthinext.ae2pr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.network.RepeatOrderConfirmRoundsPacket;
import com.murthinext.ae2pr.repeat.RepeatOrderBinding;
import com.murthinext.ae2pr.repeat.ServerRepeatRegistry;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.me.crafting.CraftConfirmMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * 确认菜单：构造时同步轮数给客户端，开始合成时消费登记表中的轮数并绑定到线程本地。
 */
@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuMixin {

    /**
     * 打开确认界面时把登记表中的轮数同步给客户端（标题显示）。
     * 由 {@code MenuOpenerMixin} 在打开前完成登记，因此这里直接 peek。
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2pr$syncConfirmRounds(int id, Inventory playerInventory, ISubMenuHost host, CallbackInfo ci) {
        var menu = (CraftConfirmMenu) (Object) this;
        if (menu.getPlayer() instanceof ServerPlayer serverPlayer) {
            var rounds = ServerRepeatRegistry.peek(serverPlayer.getUUID());
            if (rounds != null && rounds > 1) {
                ModNetwork.sendToPlayer(serverPlayer, new RepeatOrderConfirmRoundsPacket(menu.containerId, rounds));
            }
        }
    }

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
    /**
     * 离开确认界面（取消/返回/开始合成后）时把残留轮数转入恢复队列。
     * 返回数量界面时由 {@code CraftAmountMenu} 构造阶段回填显示；
     * GTLCore 会取消 {@code goBack} 主体，因此不能依赖其 RETURN。
     */
    @Inject(method = "removed", at = @At("HEAD"))
    private void ae2pr$pushRestoreRepeatRounds(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerRepeatRegistry.pushRestore(serverPlayer.getUUID());
        }
    }
}
