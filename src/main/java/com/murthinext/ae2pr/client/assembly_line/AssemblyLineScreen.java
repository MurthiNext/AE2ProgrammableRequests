package com.murthinext.ae2pr.client.assembly_line;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import appeng.util.ReadableNumberConverter;

import com.murthinext.ae2pr.ae2pr;
import com.murthinext.ae2pr.block.assembly_line.AssemblyLineMenu;

/**
 * 水晶装配线主机界面。
 */
public class AssemblyLineScreen extends AbstractContainerScreen<AssemblyLineMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ae2pr.MODID,
            "textures/gui/crystal_assembly_line.png");

    private static final int TEXT_X = 7;
    private static final int TITLE_Y = 9;
    private static final int STATUS_Y = 30;
    private static final int POWER_Y = 44;
    private static final int ERROR_Y = 58;

    private static final int COLOR_TITLE = 0x55FFFF;
    private static final int COLOR_OK = 0x55FF55;
    private static final int COLOR_FAIL = 0xFF5555;
    private static final int COLOR_RUNNING = 0xFFD060;
    private static final int COLOR_PAUSED = 0xFFDE00;
    private static final int COLOR_POWER = 0xACE9FF;
    private static final int COLOR_GRAY = 0x7A8794;

    public AssemblyLineScreen(AssemblyLineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 200;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 第一行：结构名（左对齐）
        graphics.drawString(font, title, TEXT_X, TITLE_Y, COLOR_TITLE, false);
        // 第二行：状态（左对齐）
        graphics.drawString(font, statusText(), TEXT_X, STATUS_Y, statusColor(), false);
        // 第三行：电力连接情况（左对齐）
        graphics.drawString(font, powerText(), TEXT_X, POWER_Y,
                menu.isEnergyConnected() ? COLOR_POWER : COLOR_GRAY, false);
        // 第四行：暂停原因（左对齐，仅暂停时显示）
        Component error = errorText();
        if (error != null) {
            graphics.drawString(font, error, TEXT_X, ERROR_Y, COLOR_FAIL, false);
        }
    }

    /** 暂停原因文本（无暂停时为 null）。 */
    private Component errorText() {
        return switch (menu.getErrorCode()) {
            case 1 -> Component.translatable("gui.ae2pr.crystal_assembly_line.error.power");
            case 2 -> Component.translatable("gui.ae2pr.crystal_assembly_line.error.output");
            default -> null;
        };
    }

    /** 电力连接情况：未连接 ME 网络，或显示所接网络的可用能量。 */
    private Component powerText() {
        if (!menu.isEnergyConnected()) {
            return Component.translatable("gui.ae2pr.crystal_assembly_line.power.disconnected");
        }
        return Component.translatable("gui.ae2pr.crystal_assembly_line.power.stored",
                ReadableNumberConverter.format(menu.getNetworkStoredPower(), 5) + " AE");
    }

    private Component statusText() {
        String key;
        if (!menu.isFormed()) {
            key = "gui.ae2pr.crystal_assembly_line.status.unformed";
        } else if (menu.isRunning()) {
            key = "gui.ae2pr.crystal_assembly_line.status.running";
        } else if (menu.isPaused()) {
            key = "gui.ae2pr.crystal_assembly_line.status.paused";
        } else {
            key = "gui.ae2pr.crystal_assembly_line.status.formed";
        }
        return Component.translatable(key);
    }

    private int statusColor() {
        if (!menu.isFormed()) {
            return COLOR_FAIL;
        }
        if (menu.isRunning()) {
            return COLOR_RUNNING;
        }
        return menu.isPaused() ? COLOR_PAUSED : COLOR_OK;
    }
}
