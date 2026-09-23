package com.murthinext.ae2pr;

import com.murthinext.ae2pr.emitter.MultiLevelEmitterPartItem;
import com.murthinext.ae2pr.emitter.MultiThresholdLevelEmitterPartItem;
import com.murthinext.ae2pr.filter.FilterCellItem;
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

    /** 过滤元件：在元件工作台中配置过滤项，可用升级仅模糊卡。 */
    public static final RegistryObject<FilterCellItem> FILTER_CELL = ITEMS.register("filter_cell",
            () -> new FilterCellItem(new Item.Properties().stacksTo(1)));

    /** ME 通式标准发信器：可插入过滤元件实现多触发项。 */
    public static final RegistryObject<MultiLevelEmitterPartItem> MULTI_LEVEL_EMITTER = ITEMS.register(
            "multi_level_emitter", () -> new MultiLevelEmitterPartItem(new Item.Properties()));

    /** ME 通式阈值发信器：双阈值锁存 + 可插入过滤元件。 */
    public static final RegistryObject<MultiThresholdLevelEmitterPartItem> MULTI_THRESHOLD_LEVEL_EMITTER = ITEMS.register(
            "multi_threshold_level_emitter", () -> new MultiThresholdLevelEmitterPartItem(new Item.Properties()));
}
