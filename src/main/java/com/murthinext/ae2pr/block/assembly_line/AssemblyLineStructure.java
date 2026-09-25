package com.murthinext.ae2pr.block.assembly_line;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.murthinext.ae2pr.ModBlocks;

/**
 * 水晶装配线结构定义与检测。
 * <p>
 * <ul>
 * <li><b>片（slice）</b>：默认沿控制器<b>右侧</b>延伸，共 5~17 片（首尾固定，中间 3~15 可重复）</li>
 * <li><b>行（string）</b>：沿<b>上</b>递增，即字符串自下而上书写（第 0 行是最底行）</li>
 * <li><b>列（char）</b>：默认沿控制器<b>背面</b>递增，即 3 个字符是前→后的深度</li>
 * </ul>
 * 结构允许<b>左右镜像</b>（片沿另一侧延伸）与<b>前后镜像</b>（深度方向取反），
 * 检测时逐一尝试正常与三种镜像布局，任一匹配即成型，因此镜像搭建同样有效。
 * 每片横截面（3 深 × 4 高，自下而上、前→后）：
 *
 * <pre>
 * 顶  # Y #
 *     S A G      S=控制器(最前) A=装配线外壳 G=装配线格栅(最后)
 *     R T R      R=夹层玻璃 T=装配线控制外壳
 * 底  F I F      F=水晶机壳 I=输入总线
 * </pre>
 */
public final class AssemblyLineStructure {

    /** 第 0 行（最底行）→ 第 3 行（最顶行），字符自前向后 */
    private static final String[] SLICE_FIRST = { "FIF", "RTR", "SAG", "#Y#" };
    private static final String[] SLICE_MIDDLE = { "FIF", "RTR", "DAG", "#Y#" };
    private static final String[] SLICE_LAST = { "FOF", "RTR", "DAG", "#Y#" };

    /** 中间片最少/最多可重复次数 */
    public static final int MIN_MIDDLE = 3;
    public static final int MAX_MIDDLE = 15;
    /** 整体片数范围 */
    public static final int MIN_SLICES = MIN_MIDDLE + 2;
    public static final int MAX_SLICES = MAX_MIDDLE + 2;

    private static final int ROWS = 4;
    private static final int COLS = 3;
    /** 控制器所在单元格：第 2 行（自下而上）、第 0 列（最前） */
    private static final int CONTROLLER_ROW = 2;
    private static final int CONTROLLER_COL = 0;

    private AssemblyLineStructure() {
    }

    /**
     * 结构检测结果。
     *
     * @param formed       是否成型
     * @param slices       成型时的片数（未成型为 0）
     * @param mirrorSide   匹配到的左右镜像（片延伸方向取反）
     * @param mirrorFront  匹配到的前后镜像（深度方向取反）
     * @param mismatches   不符方块数量（用于诊断）
     * @param mismatchPos  第一个不符方块的位置
     * @param expected     该位置期望的字符
     * @param found        该位置实际方块
     */
    public record Result(boolean formed, int slices, boolean mirrorSide, boolean mirrorFront, int mismatches,
            @Nullable BlockPos mismatchPos, char expected, @Nullable Block found) {

        public static final Result EMPTY = new Result(false, 0, false, false, 0, null, ' ', null);
    }

    /** 四种镜像组合：正常、左右镜像、前后镜像、双镜像。 */
    private static final boolean[][] MIRRORS = { { false, false }, { true, false }, { false, true },
            { true, true } };

    /** 片延伸方向：默认控制器右侧，左右镜像时取左侧。 */
    private static Direction sliceDir(Direction facing, boolean mirrorSide) {
        return mirrorSide ? facing.getCounterClockWise() : facing.getClockWise();
    }

    /** 深度方向（列递增方向）：默认控制器背向，前后镜像时取面向。 */
    private static Direction depthDir(Direction facing, boolean mirrorFront) {
        return mirrorFront ? facing : facing.getOpposite();
    }

    /**
     * 以控制器为原点检测结构（只读，不修改世界），依次尝试正常与三种镜像布局，任一匹配即成型；
     * 未成型时返回最接近的候选片数与首个不符位置。
     */
    public static Result match(Level level, BlockPos controllerPos, Direction facing) {
        Result best = null;
        for (boolean[] mirror : MIRRORS) {
            Direction sliceDir = sliceDir(facing, mirror[0]);
            Direction depthDir = depthDir(facing, mirror[1]);
            for (int slices = MAX_SLICES; slices >= MIN_SLICES; slices--) {
                Result result = check(level, controllerPos, sliceDir, depthDir, slices, mirror[0], mirror[1]);
                if (result.formed()) {
                    return result;
                }
                if (best == null || result.mismatches() < best.mismatches()) {
                    best = result;
                }
            }
        }
        return best != null ? best : Result.EMPTY;
    }

    /**
     * 同步结构内“状态型”方块的外观：
     * <ul>
     * <li>成型：按匹配到的镜像方向，点亮控制外壳、部件（总线/仓）切换为与机身一致的成型贴图</li>
     * <li>失活：逐一复位全部镜像布局的可能位置（含结构损坏或控制器被拆后的残留状态）</li>
     * </ul>
     */
    public static void updateFormed(Level level, BlockPos controllerPos, Direction facing, boolean mirrorSide,
            boolean mirrorFront, int slices, boolean formed) {
        if (formed) {
            applyFormed(level, controllerPos, sliceDir(facing, mirrorSide), depthDir(facing, mirrorFront), slices,
                    true);
            return;
        }
        // 未成型：不确定此前是哪种镜像布局，逐一复位全部组合
        for (boolean[] mirror : MIRRORS) {
            applyFormed(level, controllerPos, sliceDir(facing, mirror[0]), depthDir(facing, mirror[1]), MAX_SLICES,
                    false);
        }
    }

    private static void applyFormed(Level level, BlockPos controllerPos, Direction sliceDir, Direction depthDir,
            int count, boolean formed) {
        for (int s = 0; s < count; s++) {
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    BlockPos pos = cell(controllerPos, sliceDir, depthDir, s, r, c);
                    BlockState state = level.getBlockState(pos);
                    BlockState updated = null;
                    if (state.is(ModBlocks.CRYSTAL_ASSEMBLY_LINE_UNIT.get())) {
                        if (state.getValue(AssemblyLineUnitBlock.ACTIVE) != formed) {
                            updated = state.setValue(AssemblyLineUnitBlock.ACTIVE, formed);
                        }
                    } else if (isPart(state)) {
                        if (state.getValue(CertusMachinePartBlock.FORMED) != formed) {
                            updated = state.setValue(CertusMachinePartBlock.FORMED, formed);
                        }
                    }
                    if (updated != null) {
                        level.setBlock(pos, updated, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    /** 计算单元格世界坐标：片沿右侧、行沿上、列沿后。 */
    private static BlockPos cell(BlockPos controllerPos, Direction sliceDir, Direction depthDir, int s, int r, int c) {
        return controllerPos.relative(sliceDir, s)
                .relative(Direction.UP, r - CONTROLLER_ROW)
                .relative(depthDir, c - CONTROLLER_COL);
    }

    private static boolean isPart(BlockState state) {
        return state.is(ModBlocks.CERTUS_QUARTZ_INPUT_BUS.get())
                || state.is(ModBlocks.CERTUS_QUARTZ_INPUT_HATCH.get())
                || state.is(ModBlocks.CERTUS_QUARTZ_OUTPUT_BUS.get());
    }

    private static Result check(Level level, BlockPos controllerPos, Direction sliceDir, Direction depthDir,
            int slices, boolean mirrorSide, boolean mirrorFront) {
        int mismatches = 0;
        BlockPos firstPos = null;
        char firstExpected = ' ';
        Block firstFound = null;
        for (int s = 0; s < slices; s++) {
            String[] slice = s == 0 ? SLICE_FIRST : s == slices - 1 ? SLICE_LAST : SLICE_MIDDLE;
            for (int r = 0; r < ROWS; r++) {
                String row = slice[r];
                for (int c = 0; c < COLS; c++) {
                    char ch = row.charAt(c);
                    if (ch == '#') {
                        continue;
                    }
                    BlockState state = level.getBlockState(cell(controllerPos, sliceDir, depthDir, s, r, c));
                    if (!matches(ch, state)) {
                        mismatches++;
                        if (firstPos == null) {
                            firstPos = cell(controllerPos, sliceDir, depthDir, s, r, c);
                            firstExpected = ch;
                            firstFound = state.getBlock();
                        }
                    }
                }
            }
        }
        return new Result(mismatches == 0, mismatches == 0 ? slices : 0, mirrorSide, mirrorFront, mismatches,
                firstPos, firstExpected, firstFound);
    }

    /** 单元格字符 → 可接受的方块。 */
    private static boolean matches(char ch, BlockState state) {
        return switch (ch) {
            case 'S' -> state.is(ModBlocks.CRYSTAL_ASSEMBLY_LINE.get());
            // 机壳位允许用输入仓替代（对应 GT 的“带流体仓外壳”）
            case 'F' -> state.is(ModBlocks.CRYSTAL_MACHINE_CASING.get())
                    || state.is(ModBlocks.CERTUS_QUARTZ_INPUT_HATCH.get());
            case 'Y' -> state.is(ModBlocks.CRYSTAL_MACHINE_CASING.get());
            case 'I' -> state.is(ModBlocks.CERTUS_QUARTZ_INPUT_BUS.get());
            case 'O' -> state.is(ModBlocks.CERTUS_QUARTZ_OUTPUT_BUS.get());
            case 'A' -> state.is(ModBlocks.CRYSTAL_ASSEMBLY_LINE_CASING.get());
            case 'G', 'D' -> state.is(ModBlocks.CRYSTAL_ASSEMBLY_LINE_GRATING.get());
            case 'R' -> state.is(ModBlocks.CRYSTAL_LAMINATED_GLASS.get());
            case 'T' -> state.is(ModBlocks.CRYSTAL_ASSEMBLY_LINE_UNIT.get());
            default -> false;
        };
    }
}
