package com.murthinext.ae2pr.block.assembly_line;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import com.murthinext.ae2pr.ModBlockEntities;
import com.murthinext.ae2pr.ModBlocks;

/**
 * 赛特斯石英输入/输出总线方块实体：单格物品存储，仅存一类，上限 32K（32768）件。
 * <p>
 * 输出总线只接收配方输出，禁止玩家/外部存入。
 */
public class ItemBusBlockEntity extends BlockEntity {

    /** 存储上限：32K 件 */
    public static final int CAPACITY = 32 * 1024;
    /** 可存储的类型数（预留多种类扩展） */
    public static final int TYPE_CAPACITY = 1;

    private static final String STORAGE_ID = "storage";

    private final ItemStackHandler storage = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return CAPACITY;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            // 原版实现还会按物品自身堆叠上限（64）截断，这里按槽位上限放开
            return getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // 仅一类：空槽接受任意物品，非空槽只接受同种物品
            ItemStack current = getStackInSlot(slot);
            return current.isEmpty() || ItemStack.isSameItemSameTags(current, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ItemBusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CERTUS_MACHINE_PART.get(), pos, state);
    }

    public ItemStackHandler getStorage() {
        return storage;
    }

    /** 是否允许玩家存入（输出总线仅接受配方输出）。 */
    public boolean acceptsPlayerInsert() {
        return !isOutputBus();
    }

    /** 是否是输出总线。 */
    public boolean isOutputBus() {
        return getBlockState().is(ModBlocks.CERTUS_QUARTZ_OUTPUT_BUS.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(STORAGE_ID, storage.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(STORAGE_ID)) {
            storage.deserializeNBT(tag.getCompound(STORAGE_ID));
        }
    }
}
