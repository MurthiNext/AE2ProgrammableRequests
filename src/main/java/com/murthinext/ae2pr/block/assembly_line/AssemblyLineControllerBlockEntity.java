package com.murthinext.ae2pr.block.assembly_line;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.murthinext.ae2pr.ModBlockEntities;
import com.murthinext.ae2pr.ModBlocks;

/**
 * 水晶装配线控制器方块实体：定期检测结构并驱动控制外壳/部件的成型外观。
 */
public class AssemblyLineControllerBlockEntity extends BlockEntity {

    /** 检测周期（tick） */
    private static final int CHECK_INTERVAL = 20;

    private int tickCounter;

    /** 最近一次检测结果（供状态界面展示） */
    private int lastSlices;
    private int lastMismatches;
    @Nullable
    private BlockPos lastMismatchPos;
    private char lastExpected = ' ';
    @Nullable
    private Block lastFound;

    public AssemblyLineControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_ASSEMBLY_LINE.get(), pos, state);
    }

    /** 服务端 tick：按固定周期重新检测结构。 */
    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (++tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;
        validateStructure();
    }

    /** 立即检测一次结构，并按结果切换控制器与结构内方块的状态。 */
    public void validateStructure() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.is(ModBlocks.CRYSTAL_ASSEMBLY_LINE.get())) {
            return;
        }
        Direction facing = state.getValue(CrystalAssemblyLineBlock.FACING);
        var result = AssemblyLineStructure.match(level, worldPosition, facing);

        lastSlices = result.slices();
        lastMismatches = result.mismatches();
        lastMismatchPos = result.mismatchPos();
        lastExpected = result.expected();
        lastFound = result.found();

        AssemblyLineStructure.updateFormed(level, worldPosition, facing, result.mirrorSide(), result.mirrorFront(),
                result.slices(), result.formed());
        if (state.getValue(CrystalAssemblyLineBlock.FORMED) != result.formed()) {
            level.setBlock(worldPosition, state.setValue(CrystalAssemblyLineBlock.FORMED, result.formed()),
                    Block.UPDATE_ALL);
        }
    }

    /** 控制器被移除/破坏时调用：复位结构中的控制外壳与部件外观。 */
    public void onControllerRemoved() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }
        AssemblyLineStructure.updateFormed(level, worldPosition,
                getBlockState().getValue(CrystalAssemblyLineBlock.FACING), false, false, 0, false);
    }

    /** 是否已成型。 */
    public boolean isFormed() {
        return getBlockState().getValue(CrystalAssemblyLineBlock.FORMED);
    }

    /** 最近一次检测到的片数（未成型为 0）。 */
    public int getLastSlices() {
        return lastSlices;
    }

    /** 最近一次检测的不符方块数量。 */
    public int getLastMismatches() {
        return lastMismatches;
    }

    /** 最近一次检测的首个不符位置（可能为空）。 */
    @Nullable
    public BlockPos getLastMismatchPos() {
        return lastMismatchPos;
    }

    /** 首个不符位置期望的字符（可能为空字符）。 */
    public char getLastExpected() {
        return lastExpected;
    }

    /** 首个不符位置的实际方块（可能为空）。 */
    @Nullable
    public Block getLastFound() {
        return lastFound;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // 结构状态在加载后重新检测（不持久化），交给首次 tick 完成，避免加载期修改世界
            tickCounter = CHECK_INTERVAL - 1;
        }
    }
}
