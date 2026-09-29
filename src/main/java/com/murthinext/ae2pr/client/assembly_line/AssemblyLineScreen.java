package com.murthinext.ae2pr.client.assembly_line;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

    private static final int COLOR_TITLE = 0x55FFFF;
    private static final int COLOR_OK = 0x55FF55;
    private static final int COLOR_FAIL = 0xFF5555;
    private static final int COLOR_RUNNING = 0xFFD060;

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
        graphics.drawString(font, title, TEXT_X, TITLE_Y, COLOR_TITLE, false);
        graphics.drawString(font, statusText(), TEXT_X, STATUS_Y, statusColor(), false);
    }

    private Component statusText() {
        String key;
        if (!menu.isFormed()) {
            key = "gui.ae2pr.crystal_assembly_line.status.unformed";
        } else if (menu.isRunning()) {
            key = "gui.ae2pr.crystal_assembly_line.status.running";
        } else {
            key = "gui.ae2pr.crystal_assembly_line.status.formed";
        }
        return Component.translatable(key);
    }

    private int statusColor() {
        if (!menu.isFormed()) {
            return COLOR_FAIL;
        }
        return menu.isRunning() ? COLOR_RUNNING : COLOR_OK;
    }
}
