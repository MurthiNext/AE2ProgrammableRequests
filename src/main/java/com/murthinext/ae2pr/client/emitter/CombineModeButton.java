package com.murthinext.ae2pr.client.emitter;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;

import com.murthinext.ae2pr.emitter.CombineMode;

/**
 * 左侧工具栏的 AND/OR 组合模式切换按钮（占位图标：工具栏底图上绘制文字）。
 */
public class CombineModeButton extends IconButton {

    private final Consumer<CombineMode> onToggle;
    private CombineMode mode = CombineMode.OR;

    public CombineModeButton(Consumer<CombineMode> onToggle) {
        super(button -> {
        });
        this.onToggle = onToggle;
    }

    public void setMode(CombineMode mode) {
        this.mode = mode;
    }

    @Override
    public void onPress() {
        this.mode = this.mode == CombineMode.AND ? CombineMode.OR : CombineMode.AND;
        this.onToggle.accept(this.mode);
    }

    @Override
    protected Icon getIcon() {
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (!this.visible) {
            return;
        }

        Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter()
                .dest(getX(), getY())
                .opacity(this.active ? 1f : 0.5f)
                .blit(guiGraphics);

        var font = Minecraft.getInstance().font;
        var label = this.mode == CombineMode.AND ? "AND" : "OR";

        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(getX(), getY(), 0);
        pose.scale(0.5f, 0.5f, 1f);
        int textWidth = font.width(label);
        guiGraphics.drawString(font, label, (32 - textWidth) / 2, 12, 0xFFFFFF, true);
        pose.popPose();
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(this.mode == CombineMode.AND
                ? Component.translatable("gui.ae2pr.multi_level_emitter.combine.and")
                : Component.translatable("gui.ae2pr.multi_level_emitter.combine.or"));
    }
}
