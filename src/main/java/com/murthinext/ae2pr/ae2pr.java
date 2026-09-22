package com.murthinext.ae2pr;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.mojang.logging.LogUtils;
import com.murthinext.ae2pr.repeat.GenericRepeatOrders;
import net.minecraftforge.common.MinecraftForge;
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
        modEventBus.addListener(ModItems::onBuildCreativeTabContents);
        ModItems.ITEMS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModNetwork.register();
        // 通用（VCPU）重复订单：由服务端 tick 驱动，状态包钩子负责轮次推进
        MinecraftForge.EVENT_BUS.addListener(GenericRepeatOrders::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(GenericRepeatOrders::onServerStopped);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("AE2 Programmable Requests initialized");
        // 过滤元件的可用升级：仅模糊卡（1 张），与元件工作台的模糊模式开关联动
        event.enqueueWork(() -> Upgrades.add(AEItems.FUZZY_CARD, ModItems.FILTER_CELL.get(), 1));
    }
}
