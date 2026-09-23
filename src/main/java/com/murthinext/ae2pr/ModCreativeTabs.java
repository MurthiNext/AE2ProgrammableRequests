package com.murthinext.ae2pr;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 创造模式标签页注册入口：存放本模组的全部物品。
 */
public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, ae2pr.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ae2pr"))
                    .icon(() -> new ItemStack(ModItems.MULTI_LEVEL_EMITTER.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.FILTER_CELL.get());
                        output.accept(ModItems.MULTI_LEVEL_EMITTER.get());
                        output.accept(ModItems.MULTI_THRESHOLD_LEVEL_EMITTER.get());
                    })
                    .build());
}
