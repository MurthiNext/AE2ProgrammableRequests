package com.murthinext.ae2pr.client.emitter;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.guidebook.PageAnchor;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.definitions.AEItems;

import com.murthinext.ae2pr.client.ModGuide;
import com.murthinext.ae2pr.emitter.MultiThresholdLevelEmitterMenu;

/**
 * ME 通式阈值发信器界面：左侧工具栏 + 上限/下限两个阈值输入框（占位布局，复用原版发信器背景）。
 */
public class MultiThresholdLevelEmitterScreen extends UpgradeableScreen<MultiThresholdLevelEmitterMenu> {

    private final SettingToggleButton<YesNo> craftingMode;
    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final CombineModeButton combineMode;

    private final AETextField upperField;
    private final AETextField lowerField;
    private boolean updatingFields;

    public MultiThresholdLevelEmitterScreen(MultiThresholdLevelEmitterMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.combineMode = new CombineModeButton(menu::setCombineMode);
        this.addToLeftToolbar(this.redstoneMode);
        this.addToLeftToolbar(this.craftingMode);
        this.addToLeftToolbar(this.fuzzyMode);
        this.addToLeftToolbar(this.combineMode);

        this.upperField = createValueField(menu.getCurrentUpper(), this::onUpperChanged);
        this.lowerField = createValueField(menu.getCurrentLower(), this::onLowerChanged);
    }

    private AETextField createValueField(long value, Consumer<String> responder) {
        var field = new AETextField(this.style, this.font, 0, 0, 92, 12);
        field.setBordered(false);
        field.setMaxLength(18);
        field.setValue(Long.toString(value));
        field.setResponder(responder);
        return field;
    }

    private void onUpperChanged(String text) {
        if (updatingFields) {
            return;
        }
        try {
            long value = Long.parseLong(text.trim());
            if (value >= 0) {
                menu.setUpperValue(value);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void onLowerChanged(String text) {
        if (updatingFields) {
            return;
        }
        try {
            long value = Long.parseLong(text.trim());
            if (value >= 0) {
                menu.setLowerValue(value);
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        // 同步并摆放阈值输入框
        this.updatingFields = true;
        if (!upperField.isFocused()) {
            var value = Long.toString(menu.getCurrentUpper());
            if (!upperField.getValue().equals(value)) {
                upperField.setValue(value);
            }
        }
        if (!lowerField.isFocused()) {
            var value = Long.toString(menu.getCurrentLower());
            if (!lowerField.getValue().equals(value)) {
                lowerField.setValue(value);
            }
        }
        this.updatingFields = false;

        if (!this.children().contains(upperField)) {
            this.addRenderableWidget(upperField);
        }
        if (!this.children().contains(lowerField)) {
            this.addRenderableWidget(lowerField);
        }
        // AETextField 构造时会内部加 2px 内边距，setY 直接覆盖 getY，故此处 +2 使文本与左侧标签对齐
        // X 右移，避免输入框与左侧"上限/下限"标签重叠
        upperField.setX(leftPos + 26);
        upperField.setY(topPos + 42);
        lowerField.setX(leftPos + 26);
        lowerField.setY(topPos + 60);

        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.supportsFuzzySearch());

        final boolean notCraftingMode = !menu.hasUpgrade(AEItems.CRAFTING_CARD);
        this.redstoneMode.active = notCraftingMode;
        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(notCraftingMode);
        this.craftingMode.set(this.menu.getCraftingMode());
        this.craftingMode.setVisibility(!notCraftingMode);

        this.combineMode.setVisibility(true);
        this.combineMode.setMode(menu.combineMode);
    }

    /** 复用 AE2 界面自带的帮助按钮，改为打开本模组指南。 */
    @Override
    protected void openHelp() {
        ModGuide.openAt(ModGuide.INDEX_PAGE);
    }

    /** 返回非空即可让 AE2 的帮助按钮显示（实际点击由 {@link #openHelp()} 处理）。 */
    @Override
    protected PageAnchor getHelpTopic() {
        return new PageAnchor(ModGuide.INDEX_PAGE, null);
    }
}
