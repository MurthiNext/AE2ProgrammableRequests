package com.murthinext.ae2pr;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.murthinext.ae2pr.block.assembly_line.AssemblyLineUnitBlock;
import com.murthinext.ae2pr.block.assembly_line.CertusMachinePartBlock;
import com.murthinext.ae2pr.block.assembly_line.CrystalAssemblyLineBlock;
import com.murthinext.ae2pr.block.redstone_requester.RedstoneRequesterBlock;

/**
 * 方块注册入口。
 */
public final class ModBlocks {

    private ModBlocks() {
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ae2pr.MODID);

    /** ME 红石请求器 */
    public static final RegistryObject<RedstoneRequesterBlock> REDSTONE_REQUESTER = BLOCKS.register(
            "redstone_requester", RedstoneRequesterBlock::new);

    // ---------------------------------------------------------------- 水晶装配线

    /** 水晶机壳 */
    public static final RegistryObject<Block> CRYSTAL_MACHINE_CASING = BLOCKS.register("crystal_machine_casing",
            () -> new Block(casingProperties()));

    /** 水晶装配线 */
    public static final RegistryObject<CrystalAssemblyLineBlock> CRYSTAL_ASSEMBLY_LINE = BLOCKS.register(
            "crystal_assembly_line", CrystalAssemblyLineBlock::new);

    /** 水晶装配线外壳 */
    public static final RegistryObject<Block> CRYSTAL_ASSEMBLY_LINE_CASING = BLOCKS.register(
            "crystal_assembly_line_casing", () -> new Block(casingProperties()));

    /** 水晶装配线控制外壳 */
    public static final RegistryObject<AssemblyLineUnitBlock> CRYSTAL_ASSEMBLY_LINE_UNIT = BLOCKS.register(
            "crystal_assembly_line_unit", AssemblyLineUnitBlock::new);

    /** 水晶装配线格栅 */
    public static final RegistryObject<Block> CRYSTAL_ASSEMBLY_LINE_GRATING = BLOCKS.register(
            "crystal_assembly_line_grating", () -> new Block(casingProperties()));

    /** 水晶夹层玻璃 */
    public static final RegistryObject<GlassBlock> CRYSTAL_LAMINATED_GLASS = BLOCKS.register(
            "crystal_laminated_glass",
            () -> new GlassBlock(Block.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(0.8F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    /** 赛特斯石英输入总线 */
    public static final RegistryObject<CertusMachinePartBlock> CERTUS_QUARTZ_INPUT_BUS = BLOCKS.register(
            "certus_quartz_input_bus", CertusMachinePartBlock::new);

    /** 赛特斯石英输入仓 */
    public static final RegistryObject<CertusMachinePartBlock> CERTUS_QUARTZ_INPUT_HATCH = BLOCKS.register(
            "certus_quartz_input_hatch", CertusMachinePartBlock::new);

    /** 赛特斯石英输出总线 */
    public static final RegistryObject<CertusMachinePartBlock> CERTUS_QUARTZ_OUTPUT_BUS = BLOCKS.register(
            "certus_quartz_output_bus", CertusMachinePartBlock::new);

    private static Block.Properties casingProperties() {
        return Block.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }
}
