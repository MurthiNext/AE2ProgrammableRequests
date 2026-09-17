package com.murthinext.ae2pr;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * AE2 Programmable Requests：为 AE2 手动合成请求增加"重复下单"能力。
 */
@Mod(ae2pr.MODID)
public class ae2pr {

    public static final String MODID = "ae2pr";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ae2pr() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModNetwork.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("AE2 Programmable Requests initialized");
    }
}
