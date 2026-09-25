package com.murthinext.ae2pr.client.assembly_line;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.network.AssemblyLineStatusPacket;
import com.murthinext.ae2pr.network.AssemblyLineStatusRequestPacket;

/**
 * 水晶装配线状态界面：显示是否成型、片数；未成型时给出首个不符方块以方便排查。
 * 打开期间每秒向服务端刷新一次。
 */
public class AssemblyLineStatusScreen extends Screen {

    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_HEIGHT = 134;
    private static final int REFRESH_INTERVAL = 20;

    private static final int COLOR_PANEL = 0xE8101418;
    private static final int COLOR_BORDER = 0xFF5A7A9A;
    private static final int COLOR_TITLE = 0x55FFFF;
    private static final int COLOR_OK = 0x55FF55;
    private static final int COLOR_FAIL = 0xFF5555;
    private static final int COLOR_TEXT = 0xDDDDDD;
    private static final int COLOR_HINT = 0x808080;

    private AssemblyLineStatusPacket status;
    private int refreshTimer;

    public AssemblyLineStatusScreen(AssemblyLineStatusPacket status) {
        super(Component.translatable("gui.ae2pr.crystal_assembly_line.title"));
        this.status = status;
    }

    public boolean isFor(BlockPos other) {
        return status.pos().equals(other);
    }

    public void update(AssemblyLineStatusPacket status) {
        this.status = status;
    }

    @Override
    public void tick() {
        if (++refreshTimer >= REFRESH_INTERVAL) {
            refreshTimer = 0;
            ModNetwork.CHANNEL.sendToServer(new AssemblyLineStatusRequestPacket(status.pos()));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;

        graphics.fill(left, top, right, bottom, COLOR_PANEL);
        graphics.fill(left, top, right, top + 1, COLOR_BORDER);
        graphics.fill(left, bottom - 1, right, bottom, COLOR_BORDER);
        graphics.fill(left, top, left + 1, bottom, COLOR_BORDER);
        graphics.fill(right - 1, top, right, bottom, COLOR_BORDER);

        boolean formed = status.formed();
        graphics.drawCenteredString(font, title, width / 2, top + 10, COLOR_TITLE);
        graphics.drawCenteredString(font,
                Component.translatable(formed
                        ? "gui.ae2pr.crystal_assembly_line.formed"
                        : "gui.ae2pr.crystal_assembly_line.unformed"),
                width / 2, top + 30, formed ? COLOR_OK : COLOR_FAIL);

        Component sizeLine = formed
                ? Component.translatable("gui.ae2pr.crystal_assembly_line.slices", status.slices())
                : Component.translatable("gui.ae2pr.crystal_assembly_line.size", status.minSlices(),
                        status.maxSlices());
        graphics.drawCenteredString(font, sizeLine, width / 2, top + 48, COLOR_TEXT);

        if (formed) {
            graphics.drawCenteredString(font,
                    Component.translatable("gui.ae2pr.crystal_assembly_line.hint"),
                    width / 2, top + 70, COLOR_HINT);
        } else {
            graphics.drawCenteredString(font,
                    Component.translatable("gui.ae2pr.crystal_assembly_line.mismatch", status.mismatches()),
                    width / 2, top + 68, COLOR_TEXT);
            BlockPos mismatch = status.mismatchPos();
            if (mismatch != null) {
                graphics.drawCenteredString(font,
                        Component.translatable("gui.ae2pr.crystal_assembly_line.mismatch.at",
                                mismatch.getX(), mismatch.getY(), mismatch.getZ()),
                        width / 2, top + 86, COLOR_TEXT);
                graphics.drawCenteredString(font,
                        Component.translatable("gui.ae2pr.crystal_assembly_line.mismatch.need",
                                expectedName(status.expected())),
                        width / 2, top + 102, COLOR_TEXT);
                graphics.drawCenteredString(font,
                        Component.translatable("gui.ae2pr.crystal_assembly_line.mismatch.found",
                                foundName(status.foundId())),
                        width / 2, top + 118, COLOR_TEXT);
            }
        }

        graphics.drawCenteredString(font,
                Component.translatable("gui.ae2pr.crystal_assembly_line.close"),
                width / 2, bottom - 12, COLOR_HINT);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** 期望字符 → 方块名称（见语言文件 {@code ...expect.<字符>}）。 */
    private static Component expectedName(char ch) {
        return Component.translatable("gui.ae2pr.crystal_assembly_line.expect." + ch);
    }

    /** 实际方块 id → 方块名称。 */
    private static Component foundName(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return Component.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(blockId);
        Block block = id != null ? ForgeRegistries.BLOCKS.getValue(id) : null;
        return block != null && block != Blocks.AIR ? block.getName() : Component.literal(blockId);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        ClientAssemblyLineStatus.clear(this);
    }
}
