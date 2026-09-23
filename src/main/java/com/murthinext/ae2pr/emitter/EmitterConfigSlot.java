package com.murthinext.ae2pr.emitter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.FakeSlot;

import com.murthinext.ae2pr.filter.FilterCellItem;

/**
 * 发信器的配置槽：与过滤元件共用同一槽位。
 * <ul>
 * <li>空槽 / 普通物品：按原版发信器的方式"标记"配置项（幽灵槽）；</li>
 * <li>手持过滤元件左键：作为真实物品放入（消耗光标中的元件），并清除已有标记；</li>
 * <li>过滤元件不可被标记（右键幽灵放置与 JEI/REI 拖拽均被忽略）。</li>
 * </ul>
 */
public class EmitterConfigSlot extends FakeSlot {

    private final InternalInventory filterInventory;

    public EmitterConfigSlot(InternalInventory configInventory, InternalInventory filterInventory) {
        super(configInventory, 0);
        this.filterInventory = filterInventory;
    }

    private ItemStack getFilterCell() {
        return filterInventory.getStackInSlot(0);
    }

    private boolean isClientSide() {
        var menu = getMenu();
        return menu != null && menu.isClientSide();
    }

    @Override
    public ItemStack getItem() {
        var cell = getFilterCell();
        return cell.isEmpty() ? super.getItem() : cell;
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.getItem() instanceof FilterCellItem) {
            // 仅客户端用于同步显示；服务端的真实插入由 increase 处理
            if (isClientSide()) {
                filterInventory.setItemDirect(0, stack.copy());
            }
            return;
        }

        if (!getFilterCell().isEmpty()) {
            if (!isClientSide()) {
                // 已插入过滤元件时不再接受标记
                return;
            }
            filterInventory.setItemDirect(0, ItemStack.EMPTY);
        }

        super.set(stack);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !getFilterCell().isEmpty() || super.mayPickup(player);
    }

    @Override
    public ItemStack remove(int amount) {
        var cell = getFilterCell();
        if (!cell.isEmpty()) {
            filterInventory.setItemDirect(0, ItemStack.EMPTY);
            return cell.copy();
        }
        return super.remove(amount);
    }

    @Override
    public void increase(ItemStack hand) {
        if (!getFilterCell().isEmpty()) {
            if (hand.isEmpty()) {
                pickUpFilterCell();
            }
            return;
        }
        if (hand.getItem() instanceof FilterCellItem) {
            insertFilterCell(hand);
            return;
        }
        super.increase(hand);
    }

    @Override
    public void decrease(ItemStack hand) {
        if (!getFilterCell().isEmpty()) {
            if (hand.isEmpty()) {
                pickUpFilterCell();
            }
            return;
        }
        super.decrease(hand);
    }

    @Override
    public boolean canSetFilterTo(ItemStack stack) {
        return !(stack.getItem() instanceof FilterCellItem) && super.canSetFilterTo(stack);
    }

    /**
     * Shift 直移兼容：从源槽（玩家背包）取出 1 个过滤元件放入本槽。
     *
     * @return 是否成功放入
     */
    public boolean insertFromShiftClick(Slot source) {
        if (!getFilterCell().isEmpty()) {
            return false;
        }
        var stack = source.getItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof FilterCellItem)) {
            return false;
        }
        // 同一槽位二选一：插入过滤元件时清除原有标记
        super.set(ItemStack.EMPTY);
        filterInventory.setItemDirect(0, stack.copyWithCount(1));
        stack.shrink(1);
        if (stack.isEmpty()) {
            source.set(ItemStack.EMPTY);
        }
        source.setChanged();
        setChanged();
        return true;
    }

    private void insertFilterCell(ItemStack hand) {
        var menu = getMenu();
        if (menu == null) {
            return;
        }
        // 使用菜单真实光标（拖拽路径传入的是数量 1 的副本），保证从光标消耗 1 个
        var carried = menu.getCarried();
        if (carried.isEmpty()) {
            carried = hand;
        }
        // 同一槽位二选一：插入过滤元件时清除原有标记
        super.set(ItemStack.EMPTY);
        filterInventory.setItemDirect(0, carried.copyWithCount(1));
        carried.shrink(1);
        menu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
        setChanged();
    }

    private void pickUpFilterCell() {
        var menu = getMenu();
        var cell = getFilterCell();
        if (menu == null || cell.isEmpty()) {
            return;
        }
        filterInventory.setItemDirect(0, ItemStack.EMPTY);
        menu.setCarried(cell.copy());
        setChanged();
    }
}
