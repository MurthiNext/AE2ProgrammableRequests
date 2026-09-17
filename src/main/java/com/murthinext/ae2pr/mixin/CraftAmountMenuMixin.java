package com.murthinext.ae2pr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.network.RepeatOrderConfirmRoundsPacket;
import com.murthinext.ae2pr.repeat.IRepeatRoundsHolder;
import com.murthinext.ae2pr.repeat.RepeatOrderConstants;
import com.murthinext.ae2pr.repeat.ServerRepeatRegistry;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * 数量选择菜单：注册轮数同步 client action，并在确认时把轮数写入服务端登记表。
 */
@Mixin(value = CraftAmountMenu.class, remap = false)
public abstract class CraftAmountMenuMixin extends AEBaseMenu implements IRepeatRoundsHolder {

    @Unique
    @GuiSync(30)
    public int ae2pr$repeatRounds = 1;

    public CraftAmountMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lappeng/api/storage/ISubMenuHost;)V",
            at = @At("TAIL"))
    private void ae2pr$registerRepeatRoundsAction(int id, Inventory playerInventory, ISubMenuHost host,
            CallbackInfo ci) {
        this.registerClientAction(RepeatOrderConstants.ACTION_SET_REPEAT_ROUNDS, Integer.class,
                this::ae2pr$setRepeatRounds);
    }

    @Inject(method = "confirm", at = @At("TAIL"), remap = false)
    private void ae2pr$storeRepeatRounds(int amount, boolean craftMissingAmount, boolean autoStart,
            CallbackInfo ci) {
        if (this.isClientSide() || !(getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // 只有真正打开了确认界面才登记（数量为 0 等提前返回的情况不登记，避免后续串单）
        if (!(serverPlayer.containerMenu instanceof CraftConfirmMenu confirmMenu)) {
            return;
        }
        var rounds = Math.max(1, this.ae2pr$repeatRounds);
        ModNetwork.sendToPlayer(serverPlayer,
                new RepeatOrderConfirmRoundsPacket(confirmMenu.containerId, rounds));
        if (rounds <= 1) {
            ServerRepeatRegistry.clear(serverPlayer.getUUID());
        } else {
            ServerRepeatRegistry.put(serverPlayer.getUUID(), rounds);
        }
    }

    @Override
    public int ae2pr$getRepeatRounds() {
        return this.ae2pr$repeatRounds;
    }

    @Override
    public void ae2pr$setRepeatRounds(int rounds) {
        this.ae2pr$repeatRounds = Math.max(1, rounds);
    }
}
