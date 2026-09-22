package com.murthinext.ae2pr;

import appeng.api.ids.AECreativeTabIds;
import com.murthinext.ae2pr.filter.FilterCellItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
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

    /** 将本模组物品加入 AE2 主创造标签页。 */
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (AECreativeTabIds.MAIN.equals(event.getTabKey())) {
            event.accept(FILTER_CELL.get());
        }
    }
}
