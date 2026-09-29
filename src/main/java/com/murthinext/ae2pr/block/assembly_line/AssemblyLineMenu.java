package com.murthinext.ae2pr.block.assembly_line;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;

/**
 * 水晶装配线主机的容器菜单。
 */
public class AssemblyLineMenu extends AbstractContainerMenu {

    public static final MenuType<AssemblyLineMenu> TYPE = IForgeMenuType.create(AssemblyLineMenu::new);

    private static final int INV_COLS = 9;
    private static final int INV_ROWS = 3;
    private static final int SLOT = 18;

    private static final int INV_X = 7;
    private static final int INV_Y = 122;
    private static final int HOTBAR_Y = 180;

    private final AssemblyLineControllerBlockEntity controller;
    private final SimpleContainerData data = new SimpleContainerData(1);
    private final boolean clientSide;

    public AssemblyLineMenu(int id, Inventory playerInventory, AssemblyLineControllerBlockEntity controller) {
        super(TYPE, id);
        this.controller = controller;
        this.clientSide = playerInventory.player.level().isClientSide;

        for (int row = 0; row < INV_ROWS; row++) {
            for (int col = 0; col < INV_COLS; col++) {
                addSlot(new Slot(playerInventory, col + row * INV_COLS + INV_COLS,
                        INV_X + col * SLOT, INV_Y + row * SLOT));
            }
        }
        for (int col = 0; col < INV_COLS; col++) {
            addSlot(new Slot(playerInventory, col, INV_X + col * SLOT, HOTBAR_Y));
        }
        addDataSlots(data);
    }

    public AssemblyLineMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, (AssemblyLineControllerBlockEntity) playerInventory.player.level()
                .getBlockEntity(buffer.readBlockPos()));
    }

    /** 每 tick 把成型/运行状态写入同步数据：bit0=已成型，bit1=正在运行。 */
    @Override
    public void broadcastChanges() {
        if (!clientSide && controller != null) {
            int flags = (controller.isFormed() ? 1 : 0) | (controller.isRunning() ? 2 : 0);
            data.set(0, flags);
        }
        super.broadcastChanges();
    }

    public boolean isFormed() {
        return (data.get(0) & 1) != 0;
    }

    public boolean isRunning() {
        return (data.get(0) & 2) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 仅玩家背包，无需跨容器搬运
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return controller != null && player.distanceToSqr(controller.getBlockPos().getCenter()) <= 64.0;
    }
}
