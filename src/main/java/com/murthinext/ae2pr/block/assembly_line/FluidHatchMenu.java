package com.murthinext.ae2pr.block.assembly_line;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import com.murthinext.ae2pr.ModNetwork;
import com.murthinext.ae2pr.network.MachinePartFluidPacket;

/**
 * 赛特斯石英输入仓的容器菜单：容器输入格 + 容器输出格 + 玩家背包。
 * <p>
 * 罐内流体不占槽位，打开界面期间由本菜单在内容变化时发送同步包（仅发给本人）。
 */
public class FluidHatchMenu extends AbstractContainerMenu {

    public static final MenuType<FluidHatchMenu> TYPE = IForgeMenuType.create(FluidHatchMenu::create);

    private static final int INV_COLS = 9;
    private static final int SLOT_SIZE = 18;
    private static final int INV_X = 7;
    private static final int INV_Y = 122;
    private static final int HOTBAR_Y = 180;
    /** 容器输入格物品位（与 GUI 贴图一致） */
    private static final int INPUT_SLOT_X = 60;
    private static final int INPUT_SLOT_Y = 86;
    /** 容器输出格物品位（与 GUI 贴图一致） */
    private static final int OUTPUT_SLOT_X = 100;
    private static final int OUTPUT_SLOT_Y = 86;

    private final BlockPos pos;
    private final Player owner;
    private final FluidHatchBlockEntity blockEntity;
    private FluidStack lastSentFluid = FluidStack.EMPTY;
    private boolean fluidSynced;

    private FluidHatchMenu(int id, Inventory playerInventory, BlockPos pos, @Nullable FluidHatchBlockEntity blockEntity) {
        super(TYPE, id);
        this.pos = pos;
        this.owner = playerInventory.player;
        this.blockEntity = blockEntity;
        addSlot(new SlotItemHandler(input(), 0, INPUT_SLOT_X, INPUT_SLOT_Y));
        addSlot(new SlotItemHandler(output(), 0, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < INV_COLS; col++) {
                addSlot(new Slot(playerInventory, col + row * INV_COLS + INV_COLS,
                        INV_X + col * SLOT_SIZE, INV_Y + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < INV_COLS; col++) {
            addSlot(new Slot(playerInventory, col, INV_X + col * SLOT_SIZE, HOTBAR_Y));
        }
    }

    public FluidHatchMenu(int id, Inventory playerInventory, FluidHatchBlockEntity blockEntity) {
        this(id, playerInventory, blockEntity.getBlockPos(), blockEntity);
    }

    public static FluidHatchMenu create(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        return new FluidHatchMenu(id, playerInventory, pos,
                blockEntity instanceof FluidHatchBlockEntity hatch ? hatch : null);
    }

    private IItemHandler input() {
        return blockEntity != null ? blockEntity.getInputSlot() : new ItemStackHandler(1);
    }

    private IItemHandler output() {
        return blockEntity != null ? blockEntity.getOutputSlot() : new ItemStackHandler(1);
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    /** 服务端每 tick：罐内流体变化时向打开界面的玩家发送同步包；首次广播强制同步一次。 */
    @Override
    public void broadcastChanges() {
        if (blockEntity != null && owner instanceof ServerPlayer serverPlayer) {
            FluidStack current = blockEntity.getTank().getFluid();
            if (!fluidSynced || !sameFluid(current, lastSentFluid)) {
                fluidSynced = true;
                lastSentFluid = current.copy();
                ModNetwork.sendToPlayer(serverPlayer, new MachinePartFluidPacket(pos, lastSentFluid));
            }
        }
        super.broadcastChanges();
    }

    private static boolean sameFluid(FluidStack a, FluidStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return a.isEmpty() && b.isEmpty();
        }
        return a.getAmount() == b.getAmount() && a.isFluidEqual(b);
    }

    /** Shift 点击：容器格 → 背包；背包 → 容器输入格（仅接受流体容器）。 */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        if (index <= 1) {
            // 容器格 → 背包：整堆取出后按背包容量分配，未放入的部分退回原格
            IItemHandler machineSlot = index == 0 ? input() : output();
            ItemStack moved = machineSlot.extractItem(0, Integer.MAX_VALUE, false);
            if (moved.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(moved, 2, slots.size(), true)) {
                machineSlot.insertItem(0, moved, false);
                return ItemStack.EMPTY;
            }
            if (!moved.isEmpty()) {
                machineSlot.insertItem(0, moved, false);
            }
        } else {
            // 背包 → 容器输入格
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(input(), slot.getItem().copy(), false);
            if (remainder.getCount() == slot.getItem().getCount()) {
                return ItemStack.EMPTY;
            }
            slot.set(remainder);
        }
        slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.distanceToSqr(Vec3.atCenterOf(pos)) <= 64.0;
    }
}
