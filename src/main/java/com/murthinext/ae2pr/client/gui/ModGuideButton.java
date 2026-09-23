package com.murthinext.ae2pr.client.gui;

import java.util.List;

import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;

/**
 * 左侧工具栏的"打开本模组指南"按钮。
 */
public class ModGuideButton extends IconButton {

    public ModGuideButton(OnPress onPress) {
        super(onPress);
    }

    @Override
    protected Icon getIcon() {
        return Icon.HELP;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(Component.translatable("gui.ae2pr.open_guide"));
    }
}
