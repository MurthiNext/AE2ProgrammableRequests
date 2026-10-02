package com.murthinext.ae2pr.client.assembly_line;

import java.text.NumberFormat;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import com.murthinext.ae2pr.ae2pr;
import com.murthinext.ae2pr.block.assembly_line.FluidHatchBlockEntity;
import com.murthinext.ae2pr.block.assembly_line.FluidHatchMenu;

/**
 * 赛特斯石英水晶输入仓界面：机器区左侧为流体罐（按储量平铺流体贴图），右侧为储量信息与容器槽。
 */
public class FluidHatchScreen extends AbstractContainerScreen<FluidHatchMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ae2pr.MODID,
            "textures/gui/certus_quartz_crystal_input_hatch.png");

    /** 罐内填充区（与 GUI 贴图一致） */
    private static final int TANK_X = 22;
    private static final int TANK_Y = 36;
    private static final int TANK_W = 24;
    private static final int TANK_H = 64;

    private static final int TITLE_X = 7;
    private static final int TITLE_Y = 9;
    private static final int INFO_X = 56;
    private static final int STORED_Y = 40;
    private static final int TYPE_Y = 54;
    private static final int CAPACITY_Y = 68;
    /** 类型行可用的最大宽度（面板内右侧留 2px） */
    private static final int TYPE_MAX_WIDTH = 176 - 2 - INFO_X;

    private static final int COLOR_TITLE = 0x55FFFF;
    private static final int COLOR_VALUE = 0xACE9FF;
    private static final int COLOR_TEXT = 0xAAB8C6;
    private static final int COLOR_GRAY = 0x7A8794;

    private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();

    /** 左侧工具栏位置（相对 GUI 左上角） */
    private static final int TOOLBAR_X = -22;
    private static final int TOOLBAR_Y = 2;

    @Nullable
    private AutoTransferButton autoTransferButton;

    public FluidHatchScreen(FluidHatchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 200;
    }

    /** 客户端同步入口：把服务端罐内流体与自动搬运开关写入本地方块实体（仅由同步包调用）。 */
    public static void applyFluidSync(BlockPos pos, FluidStack fluid, boolean autoTransfer) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getBlockEntity(pos) instanceof FluidHatchBlockEntity hatch) {
            hatch.applyClientFluid(fluid);
            hatch.setAutoTransfer(autoTransfer);
        }
    }

    @Override
    protected void init() {
        super.init();
        autoTransferButton = new AutoTransferButton(leftPos + TOOLBAR_X + 1, topPos + TOOLBAR_Y + 1,
                AutoTransferButton.Type.PULL, this::autoTransferEnabled,
                () -> Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, 0));
        addRenderableWidget(autoTransferButton);
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
        AutoTransferButton.renderToolbar(graphics, leftPos + TOOLBAR_X, topPos + TOOLBAR_Y, 1);
        renderFluid(graphics);
    }

    /** 罐区悬停：显示所存流体与数量。 */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (autoTransferButton != null && autoTransferButton.isHovered()) {
            boolean enabled = autoTransferEnabled();
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable("gui.ae2pr.machine_part.auto.pull"),
                    Component.translatable(enabled ? "gui.ae2pr.machine_part.auto.enabled"
                            : "gui.ae2pr.machine_part.auto.disabled"),
                    Component.translatable("gui.ae2pr.machine_part.auto.desc")),
                    mouseX, mouseY);
            return;
        }
        if (isHoveringTank(mouseX, mouseY)) {
            FluidStack fluid = clientFluid();
            if (!fluid.isEmpty()) {
                graphics.renderComponentTooltip(font, List.of(fluid.getDisplayName(),
                        Component.translatable("gui.ae2pr.machine_part.stored.mb", NUMBER.format(fluid.getAmount()))),
                        mouseX, mouseY);
            }
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean isHoveringTank(int mouseX, int mouseY) {
        int x = leftPos + TANK_X;
        int y = topPos + TANK_Y;
        return mouseX >= x && mouseX < x + TANK_W && mouseY >= y && mouseY < y + TANK_H;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        FluidStack fluid = clientFluid();
        graphics.drawString(font, title, TITLE_X, TITLE_Y, COLOR_TITLE, false);
        graphics.drawString(font, storedText(fluid), INFO_X, STORED_Y,
                fluid.isEmpty() ? COLOR_GRAY : COLOR_VALUE, false);
        drawType(graphics, fluid.isEmpty() ? null : fluid.getDisplayName());
        graphics.drawString(font, Component.translatable("gui.ae2pr.machine_part.capacity.fluid",
                NUMBER.format(FluidHatchBlockEntity.CAPACITY / 1000), NUMBER.format(FluidHatchBlockEntity.TYPE_CAPACITY)),
                INFO_X, CAPACITY_Y, COLOR_GRAY, false);
    }

    /** 类型行：标签 + 截断后的名称。 */
    private void drawType(GuiGraphics graphics, @Nullable Component name) {
        Component label = Component.translatable("gui.ae2pr.machine_part.type_label");
        graphics.drawString(font, label, INFO_X, TYPE_Y, COLOR_TEXT, false);
        if (name == null) {
            return;
        }
        int labelWidth = font.width(label);
        graphics.drawString(font, clip(name.getString(), TYPE_MAX_WIDTH - labelWidth),
                INFO_X + labelWidth, TYPE_Y, COLOR_VALUE, false);
    }

    private String clip(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, maxWidth - font.width("…")) + "…";
    }

    /** 按储量占比从底部向上平铺流体贴图；按罐内区域裁剪，避免溢出到边框。 */
    private void renderFluid(GuiGraphics graphics) {
        FluidStack fluid = clientFluid();
        if (fluid.isEmpty()) {
            return;
        }
        int fill = (int) Math.min(TANK_H,
                Math.max(1L, (long) fluid.getAmount() * TANK_H / FluidHatchBlockEntity.CAPACITY));
        int x0 = leftPos + TANK_X;
        int y0 = topPos + TANK_Y;
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(extensions.getStillTexture(fluid));
        int color = extensions.getTintColor(fluid);
        graphics.setColor(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F, ((color >>> 24) & 0xFF) / 255.0F);
        int fillTop = y0 + TANK_H - fill;
        graphics.enableScissor(x0, fillTop, x0 + TANK_W, y0 + TANK_H);
        for (int ty = y0 + TANK_H - 16; ty + 16 > fillTop; ty -= 16) {
            for (int tx = x0; tx < x0 + TANK_W; tx += 16) {
                graphics.blit(tx, ty, 0, 16, 16, sprite);
            }
        }
        graphics.disableScissor();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean autoTransferEnabled() {
        FluidHatchBlockEntity hatch = clientHatch();
        return hatch == null || hatch.isAutoTransfer();
    }

    private FluidStack clientFluid() {
        FluidHatchBlockEntity hatch = clientHatch();
        return hatch != null ? hatch.getTank().getFluid() : FluidStack.EMPTY;
    }

    @Nullable
    private FluidHatchBlockEntity clientHatch() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getBlockPos()) instanceof FluidHatchBlockEntity hatch) {
            return hatch;
        }
        return null;
    }

    private static Component storedText(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return Component.translatable("gui.ae2pr.machine_part.stored.empty");
        }
        return Component.translatable("gui.ae2pr.machine_part.stored.mb", NUMBER.format(fluid.getAmount()));
    }
}
