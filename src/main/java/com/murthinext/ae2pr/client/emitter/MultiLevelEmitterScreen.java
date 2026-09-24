package com.murthinext.ae2pr.client.emitter;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.guidebook.PageAnchor;
import appeng.core.definitions.AEItems;

import com.murthinext.ae2pr.client.ModGuide;
import com.murthinext.ae2pr.block.level_emitter.EmitterConfigSlot;
import com.murthinext.ae2pr.block.level_emitter.MultiLevelEmitterMenu;
import com.murthinext.ae2pr.item.filter_cell.FilterCell;

/**
 * ME 通式标准发信器界面；相比原版发信器增加了过滤槽与 AND/OR 切换按钮。
 */
public class MultiLevelEmitterScreen extends UpgradeableScreen<MultiLevelEmitterMenu> {

    private final SettingToggleButton<YesNo> craftingMode;
    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final NumberEntryWidget level;
    private final CombineModeButton combineMode;

    public MultiLevelEmitterScreen(MultiLevelEmitterMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.craftingMode = new ServerSettingToggleButton<>(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        this.combineMode = new CombineModeButton(menu::setCombineMode);
        this.addToLeftToolbar(this.redstoneMode);
        this.addToLeftToolbar(this.craftingMode);
        this.addToLeftToolbar(this.fuzzyMode);
        this.addToLeftToolbar(this.combineMode);

        this.level = widgets.addNumberEntryWidget("level", NumberEntryType.of(menu.getConfiguredFilter()));
        this.level.setTextFieldStyle(style.getWidget("levelInput"));
        this.level.setLongValue(this.menu.getCurrentValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::onClose);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.level.setType(NumberEntryType.of(menu.getConfiguredFilter()));

        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.supportsFuzzySearch());

        final boolean notCraftingMode = !menu.hasUpgrade(AEItems.CRAFTING_CARD);
        this.level.setActive(notCraftingMode);

        this.redstoneMode.active = notCraftingMode;
        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(notCraftingMode);

        this.craftingMode.set(this.menu.getCraftingMode());
        this.craftingMode.setVisibility(!notCraftingMode);

        this.combineMode.setVisibility(true);
        this.combineMode.setMode(menu.combineMode);
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(menu::setValue);
    }

    /**
     * 配置槽内的过滤元件支持 shift 快速取出到玩家背包。
     * <p>
     * AE2 会把 FakeSlot 的 shift 点击当作普通点击处理，这里提前拦截并改发 QUICK_MOVE。
     */
    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof EmitterConfigSlot && mouseButton == 0
                && slot.getItem().getItem() instanceof FilterCell
                && (clickType == ClickType.QUICK_MOVE || hasShiftDown())) {
            this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, slotIdx, 0,
                    ClickType.QUICK_MOVE, this.minecraft.player);
            return;
        }
        super.slotClicked(slot, slotIdx, mouseButton, clickType);
    }

    /** 复用 AE2 界面自带的帮助按钮，改为打开本模组指南。 */
    @Override
    protected void openHelp() {
        ModGuide.openAt(ModGuide.EMITTER_PAGE);
    }

    /** 返回非空即可让 AE2 的帮助按钮显示（实际点击由 {@link #openHelp()} 处理）。 */
    @Override
    protected PageAnchor getHelpTopic() {
        return new PageAnchor(ModGuide.EMITTER_PAGE, null);
    }
}
