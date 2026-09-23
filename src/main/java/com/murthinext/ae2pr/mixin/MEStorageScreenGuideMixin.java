package com.murthinext.ae2pr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.common.MEStorageMenu;

import guideme.PageAnchor;

import com.murthinext.ae2pr.client.ModGuide;
import com.murthinext.ae2pr.client.gui.ModGuideButton;

/**
 * AE2 终端界面：左侧工具栏添加"打开本模组指南"按钮。
 */
@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenGuideMixin extends AEBaseScreen<MEStorageMenu> {

    public MEStorageScreenGuideMixin(MEStorageMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2pr$addGuideButton(MEStorageMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        this.addToLeftToolbar(new ModGuideButton(button -> ModGuide.open(PageAnchor.page(ModGuide.INDEX_PAGE))));
    }
}
