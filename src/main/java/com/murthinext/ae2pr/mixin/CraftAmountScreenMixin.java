package com.murthinext.ae2pr.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.murthinext.ae2pr.Config;
import com.murthinext.ae2pr.client.gui.RepeatOrderButton;
import com.murthinext.ae2pr.repeat.IRepeatRoundsHolder;
import com.murthinext.ae2pr.repeat.RepeatOrderConstants;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.crafting.CraftAmountScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.menu.me.crafting.CraftAmountMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 数量选择界面：左侧工具栏添加"重复下单"按钮与轮数输入框。
 * 输入框不是 AE2 WidgetContainer 的成员，因此在每帧 {@code updateBeforeRender} 时补挂到屏幕控件列表，
 * 以兼容窗口缩放导致的控件重建。
 */
@Mixin(value = CraftAmountScreen.class, remap = false)
public abstract class CraftAmountScreenMixin extends AEBaseScreen<CraftAmountMenu> {

    @Unique
    private RepeatOrderButton ae2pr$repeatButton;
    @Unique
    private AETextField ae2pr$repeatInput;
    @Unique
    private boolean ae2pr$fieldVisible;
    @Unique
    private int ae2pr$rounds = 1;
    @Unique
    private int ae2pr$syncedRounds = 1;

    public CraftAmountScreenMixin(CraftAmountMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ae2pr$initRepeatOrder(CraftAmountMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        this.ae2pr$repeatButton = this.addToLeftToolbar(
                new RepeatOrderButton(button -> this.ae2pr$toggleRepeatField()));

        var input = new AETextField(this.style, this.font, 0, 0, 44, 14);
        input.setMaxLength(9);
        input.setBordered(false);
        input.setValue("1");
        input.setResponder(this::ae2pr$onRepeatInputChanged);
        input.setVisible(false);
        this.ae2pr$repeatInput = input;

        this.ae2pr$updateButtonMessage();
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"), remap = false)
    private void ae2pr$updateRepeatUi(CallbackInfo ci) {
        var input = this.ae2pr$repeatInput;
        if (input == null) {
            return;
        }
        if (!this.children().contains(input)) {
            this.addRenderableWidget(input);
        }

        this.ae2pr$adoptRemoteRounds();

        if (!this.ae2pr$fieldVisible) {
            input.setVisible(false);
            return;
        }
        var button = this.ae2pr$repeatButton;
        if (button == null) {
            return;
        }
        input.setVisible(true);
        input.setX(button.getX() - input.getWidth() - 12);
        input.setY(button.getY() + (button.getHeight() - input.getHeight()) / 2);
    }

    /**
     * 接受服务端同步的轮数（例如从确认界面返回时恢复），输入框聚焦时不覆盖玩家输入。
     */
    @Unique
    private void ae2pr$adoptRemoteRounds() {
        var input = this.ae2pr$repeatInput;
        if (input == null) {
            return;
        }
        var remote = ((IRepeatRoundsHolder) this.menu).ae2pr$getRepeatRounds();
        if (remote < 1) {
            remote = 1;
        }
        if (remote == this.ae2pr$rounds || input.isFocused()) {
            return;
        }
        this.ae2pr$rounds = remote;
        this.ae2pr$syncedRounds = remote;
        if (!input.getValue().equals(Integer.toString(remote))) {
            input.setValue(Integer.toString(remote));
        }
        if (remote > 1) {
            this.ae2pr$fieldVisible = true;
        }
        this.ae2pr$updateButtonMessage();
    }

    @Unique
    private void ae2pr$toggleRepeatField() {
        this.ae2pr$fieldVisible = !this.ae2pr$fieldVisible;
        var input = this.ae2pr$repeatInput;
        if (input == null) {
            return;
        }

        if (this.ae2pr$fieldVisible) {
            input.setValue(Integer.toString(this.ae2pr$rounds));
            input.setVisible(true);
            this.setFocused(input);
        } else {
            this.setFocused(null);
            input.setVisible(false);
            input.setValue("1");
        }
        this.ae2pr$updateButtonMessage();
    }

    @Unique
    private void ae2pr$onRepeatInputChanged(String text) {
        int rounds;
        try {
            rounds = Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
            return;
        }
        if (rounds < 1) {
            return;
        }
        rounds = Math.min(rounds, Config.maxRounds());
        this.ae2pr$rounds = rounds;
        this.ae2pr$syncRounds();
        this.ae2pr$updateButtonMessage();
    }

    @Unique
    private void ae2pr$syncRounds() {
        if (this.ae2pr$rounds == this.ae2pr$syncedRounds) {
            return;
        }
        this.ae2pr$syncedRounds = this.ae2pr$rounds;
        if (this.menu instanceof AEBaseMenuInvoker invoker) {
            invoker.ae2pr$sendClientAction(RepeatOrderConstants.ACTION_SET_REPEAT_ROUNDS, this.ae2pr$rounds);
        }
    }

    @Unique
    private void ae2pr$updateButtonMessage() {
        if (this.ae2pr$repeatButton == null) {
            return;
        }
        if (this.ae2pr$rounds > 1) {
            this.ae2pr$repeatButton.setMessage(
                    Component.translatable("gui.ae2pr.repeat_order.button.enabled", this.ae2pr$rounds));
        } else {
            this.ae2pr$repeatButton.setMessage(Component.translatable("gui.ae2pr.repeat_order.button"));
        }
    }
}
