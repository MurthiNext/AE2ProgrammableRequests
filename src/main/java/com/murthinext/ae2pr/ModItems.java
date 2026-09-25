package com.murthinext.ae2pr;

import com.murthinext.ae2pr.item.level_emitter.MultiLevelEmitterPartItem;
import com.murthinext.ae2pr.item.level_emitter.MultiThresholdLevelEmitterPartItem;
import com.murthinext.ae2pr.item.filter_cell.AdvancedFilterCellItem;
import com.murthinext.ae2pr.item.filter_cell.FilterCellItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册入口。
 */
public final class ModItems {

    private ModItems() {
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ae2pr.MODID);

    /** 过滤元件 */
    public static final RegistryObject<FilterCellItem> FILTER_CELL = ITEMS.register("filter_cell",
            () -> new FilterCellItem(new Item.Properties().stacksTo(1)));

    /** 高级过滤元件 */
    public static final RegistryObject<AdvancedFilterCellItem> ADVANCED_FILTER_CELL = ITEMS.register(
            "advanced_filter_cell", () -> new AdvancedFilterCellItem(new Item.Properties().stacksTo(1)));

    /** ME 通式标准发信器 */
    public static final RegistryObject<MultiLevelEmitterPartItem> MULTI_LEVEL_EMITTER = ITEMS.register(
            "multi_level_emitter", () -> new MultiLevelEmitterPartItem(new Item.Properties()));

    /** ME 通式阈值发信器 */
    public static final RegistryObject<MultiThresholdLevelEmitterPartItem> MULTI_THRESHOLD_LEVEL_EMITTER = ITEMS.register(
            "multi_threshold_level_emitter", () -> new MultiThresholdLevelEmitterPartItem(new Item.Properties()));

    /** ME 红石请求器 */
    public static final RegistryObject<BlockItem> REDSTONE_REQUESTER = ITEMS.register("redstone_requester",
            () -> new BlockItem(ModBlocks.REDSTONE_REQUESTER.get(), new Item.Properties()));

    // ---------------------------------------------------------------- 水晶装配线

    /** 水晶机壳 */
    public static final RegistryObject<BlockItem> CRYSTAL_MACHINE_CASING = ITEMS.register("crystal_machine_casing",
            () -> new BlockItem(ModBlocks.CRYSTAL_MACHINE_CASING.get(), new Item.Properties()));

    /** 水晶装配线（控制器） */
    public static final RegistryObject<BlockItem> CRYSTAL_ASSEMBLY_LINE = ITEMS.register("crystal_assembly_line",
            () -> new BlockItem(ModBlocks.CRYSTAL_ASSEMBLY_LINE.get(), new Item.Properties()));

    /** 水晶装配线外壳 */
    public static final RegistryObject<BlockItem> CRYSTAL_ASSEMBLY_LINE_CASING = ITEMS.register(
            "crystal_assembly_line_casing",
            () -> new BlockItem(ModBlocks.CRYSTAL_ASSEMBLY_LINE_CASING.get(), new Item.Properties()));

    /** 水晶装配线控制外壳 */
    public static final RegistryObject<BlockItem> CRYSTAL_ASSEMBLY_LINE_UNIT = ITEMS.register(
            "crystal_assembly_line_unit",
            () -> new BlockItem(ModBlocks.CRYSTAL_ASSEMBLY_LINE_UNIT.get(), new Item.Properties()));

    /** 水晶装配线格栅 */
    public static final RegistryObject<BlockItem> CRYSTAL_ASSEMBLY_LINE_GRATING = ITEMS.register(
            "crystal_assembly_line_grating",
            () -> new BlockItem(ModBlocks.CRYSTAL_ASSEMBLY_LINE_GRATING.get(), new Item.Properties()));

    /** 水晶夹层玻璃 */
    public static final RegistryObject<BlockItem> CRYSTAL_LAMINATED_GLASS = ITEMS.register("crystal_laminated_glass",
            () -> new BlockItem(ModBlocks.CRYSTAL_LAMINATED_GLASS.get(), new Item.Properties()));

    /** 赛特斯石英输入总线 */
    public static final RegistryObject<BlockItem> CERTUS_QUARTZ_INPUT_BUS = ITEMS.register("certus_quartz_input_bus",
            () -> new BlockItem(ModBlocks.CERTUS_QUARTZ_INPUT_BUS.get(), new Item.Properties()));

    /** 赛特斯石英输入仓 */
    public static final RegistryObject<BlockItem> CERTUS_QUARTZ_INPUT_HATCH = ITEMS.register(
            "certus_quartz_input_hatch",
            () -> new BlockItem(ModBlocks.CERTUS_QUARTZ_INPUT_HATCH.get(), new Item.Properties()));

    /** 赛特斯石英输出总线 */
    public static final RegistryObject<BlockItem> CERTUS_QUARTZ_OUTPUT_BUS = ITEMS.register("certus_quartz_output_bus",
            () -> new BlockItem(ModBlocks.CERTUS_QUARTZ_OUTPUT_BUS.get(), new Item.Properties()));
}
