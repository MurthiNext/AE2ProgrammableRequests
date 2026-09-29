package com.murthinext.ae2pr.block.assembly_line;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

import com.murthinext.ae2pr.ModBlockEntities;

/**
 * 赛特斯石英输入仓方块实体：单类流体存储，上限 16K 桶（16,384,000 mB）。
 * <p>
 * 容器槽为「输入 → 输出」两格：输入格放入流体容器，每 {@link #TRANSFER_INTERVAL} tick 处理一次，
 * 空容器从罐中取液、满容器向罐中注液，处理后的容器移到输出格（输出格被占用时等待）。
 */
public class FluidHatchBlockEntity extends BlockEntity {

    /** 存储上限：16K 桶 */
    public static final int CAPACITY = 16 * 1024 * 1000;
    /** 可存储的类型数（预留多种类扩展） */
    public static final int TYPE_CAPACITY = 1;
    /** 流体容器处理周期（tick） */
    private static final int TRANSFER_INTERVAL = 10;

    private static final String TANK_ID = "tank";
    private static final String INPUT_ID = "input";
    private static final String OUTPUT_ID = "output";

    private final FluidTank tank = new FluidTank(CAPACITY);
    /** 容器输入格 */
    private final ItemStackHandler inputSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    /** 容器输出格 */
    private final ItemStackHandler outputSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int transferCounter;

    public FluidHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CERTUS_MACHINE_PART.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getInputSlot() {
        return inputSlot;
    }

    public ItemStackHandler getOutputSlot() {
        return outputSlot;
    }

    /** 服务端 tick：定期处理输入格的流体容器。 */
    public void serverTick() {
        if (++transferCounter < TRANSFER_INTERVAL) {
            return;
        }
        transferCounter = 0;
        processContainer();
    }

    /**
     * 处理输入格容器：每次取一个容器，先在副本容器与临时罐上试算转移，
     * 结果容器能放入输出格（可与同类容器堆叠）时才真正执行。
     * 无法转移（流体不同、罐满或空）或输出格放不下时留在输入格等待。
     */
    private void processContainer() {
        ItemStack input = inputSlot.getStackInSlot(0);
        if (input.isEmpty()) {
            return;
        }
        ItemStack preview = input.copyWithCount(1);
        IFluidHandlerItem previewHandler = preview.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (previewHandler == null) {
            return;
        }
        FluidTank previewTank = new FluidTank(CAPACITY);
        previewTank.setFluid(tank.getFluid().copy());
        if (!transferFluid(previewHandler, previewTank)) {
            return;
        }
        if (!canAcceptResult(previewHandler.getContainer())) {
            return;
        }

        ItemStack single = input.copyWithCount(1);
        IFluidHandlerItem handler = single.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null || !transferFluid(handler, tank)) {
            return;
        }
        inputSlot.extractItem(0, 1, false);
        storeResult(handler.getContainer());
        setChanged();
    }

    /** 在容器与给定罐之间执行一次转移（容器→罐优先），返回是否发生转移。 */
    private static boolean transferFluid(IFluidHandlerItem handler, FluidTank destination) {
        FluidStack drained = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty()) {
            int accepted = destination.fill(drained, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) {
                return false;
            }
            FluidStack moved = handler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            if (moved.isEmpty()) {
                return false;
            }
            destination.fill(moved, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }

        FluidStack stored = destination.getFluid();
        if (stored.isEmpty()) {
            return false;
        }
        int filled = handler.fill(stored.copy(), IFluidHandler.FluidAction.SIMULATE);
        if (filled <= 0) {
            return false;
        }
        handler.fill(stored.copy(), IFluidHandler.FluidAction.EXECUTE);
        destination.drain(filled, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }

    /** 输出格能否接受该结果容器（空槽或与同类容器堆叠且不超上限）。 */
    private boolean canAcceptResult(ItemStack result) {
        ItemStack existing = outputSlot.getStackInSlot(0);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(existing, result)
                && existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    /** 把结果容器放入输出格（合并到已有同类容器）。 */
    private void storeResult(ItemStack result) {
        ItemStack existing = outputSlot.getStackInSlot(0);
        if (existing.isEmpty()) {
            outputSlot.setStackInSlot(0, result);
        } else {
            existing.grow(result.getCount());
            outputSlot.setStackInSlot(0, existing);
        }
    }

    /** 客户端展示同步：直接覆盖本地罐内容（仅由同步包调用）。 */
    public void applyClientFluid(FluidStack fluid) {
        tank.setFluid(fluid.copy());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TANK_ID, tank.writeToNBT(new CompoundTag()));
        tag.put(INPUT_ID, inputSlot.serializeNBT());
        tag.put(OUTPUT_ID, outputSlot.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TANK_ID)) {
            tank.readFromNBT(tag.getCompound(TANK_ID));
        }
        if (tag.contains(INPUT_ID)) {
            inputSlot.deserializeNBT(tag.getCompound(INPUT_ID));
        }
        if (tag.contains(OUTPUT_ID)) {
            outputSlot.deserializeNBT(tag.getCompound(OUTPUT_ID));
        }
    }
}
