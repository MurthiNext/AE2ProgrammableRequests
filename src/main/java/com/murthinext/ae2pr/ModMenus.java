package com.murthinext.ae2pr;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.murthinext.ae2pr.emitter.MultiLevelEmitterMenu;

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
}
