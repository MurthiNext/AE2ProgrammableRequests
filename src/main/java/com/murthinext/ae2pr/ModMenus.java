package com.murthinext.ae2pr;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.murthinext.ae2pr.emitter.MultiLevelEmitterMenu;
import com.murthinext.ae2pr.emitter.MultiThresholdLevelEmitterMenu;
import com.murthinext.ae2pr.requester.RedstoneRequesterMenu;

/**
 * 菜单类型注册入口。
 */
public final class ModMenus {

    private ModMenus() {
    }

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
            ae2pr.MODID);

    public static final RegistryObject<MenuType<MultiLevelEmitterMenu>> MULTI_LEVEL_EMITTER = MENUS
            .register("multi_level_emitter", () -> MultiLevelEmitterMenu.TYPE);

    public static final RegistryObject<MenuType<MultiThresholdLevelEmitterMenu>> MULTI_THRESHOLD_LEVEL_EMITTER = MENUS
            .register("multi_threshold_level_emitter", () -> MultiThresholdLevelEmitterMenu.TYPE);

    public static final RegistryObject<MenuType<RedstoneRequesterMenu>> REDSTONE_REQUESTER = MENUS
            .register("redstone_requester", () -> RedstoneRequesterMenu.TYPE);
}
