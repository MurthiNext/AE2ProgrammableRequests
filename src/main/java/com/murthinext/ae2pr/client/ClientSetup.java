package com.murthinext.ae2pr.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import appeng.api.parts.PartModels;
import appeng.init.client.InitScreens;
import appeng.items.parts.PartModelsHelper;

import com.murthinext.ae2pr.ModMenus;
import com.murthinext.ae2pr.ae2pr;
import com.murthinext.ae2pr.client.emitter.MultiLevelEmitterScreen;
import com.murthinext.ae2pr.emitter.MultiLevelEmitterPart;

/**
 * 客户端初始化：注册部件模型与界面。
 */
@Mod.EventBusSubscriber(modid = ae2pr.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void registerPartModels(ModelEvent.RegisterGeometryLoaders event) {
        PartModels.registerModels(PartModelsHelper.createModels(MultiLevelEmitterPart.class));
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        InitScreens.register(ModMenus.MULTI_LEVEL_EMITTER.get(),
                MultiLevelEmitterScreen::new,
                "/screens/multi_level_emitter.json");
    }
}
