package com.murthinext.ae2pr;

import com.murthinext.ae2pr.emitter.MultiLevelEmitterPartItem;
import com.murthinext.ae2pr.emitter.MultiThresholdLevelEmitterPartItem;
import com.murthinext.ae2pr.filter.AdvancedFilterCellItem;
import com.murthinext.ae2pr.filter.FilterCellItem;
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
}
