package com.murthinext.ae2pr;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.mojang.logging.LogUtils;
import com.murthinext.ae2pr.logic.repeat.GenericRepeatOrders;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * AE2 Programmable Requests
 */
@Mod(ae2pr.MODID)
public class ae2pr {

    public static final String MODID = "ae2pr";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ae2pr() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModNetwork.register();
        com.murthinext.ae2pr.block.redstone_requester.network.RequesterNetwork.init();
        // 通用（VCPU）重复订单：由服务端 tick 驱动，状态包钩子负责轮次推进
        MinecraftForge.EVENT_BUS.addListener(GenericRepeatOrders::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(GenericRepeatOrders::onServerStopped);

        // 客户端：构建并注册 GuideME 指南
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.murthinext.ae2pr.client.ModGuide.init();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("AE2 Programmable Requests initialized");
        event.enqueueWork(() -> {
            // 过滤元件的可用升级：仅模糊卡（1 张），与元件工作台的模糊模式开关联动
            Upgrades.add(AEItems.FUZZY_CARD, ModItems.FILTER_CELL.get(), 1);
            // 通式标准发信器的可用升级：模糊卡 + 合成卡（过滤元件走专用槽，不作为升级卡）
            var emitterItem = ModItems.MULTI_LEVEL_EMITTER.get();
            Upgrades.add(AEItems.FUZZY_CARD, emitterItem, 1);
            Upgrades.add(AEItems.CRAFTING_CARD, emitterItem, 1);
            // 通式阈值发信器同上
            var thresholdEmitterItem = ModItems.MULTI_THRESHOLD_LEVEL_EMITTER.get();
            Upgrades.add(AEItems.FUZZY_CARD, thresholdEmitterItem, 1);
            Upgrades.add(AEItems.CRAFTING_CARD, thresholdEmitterItem, 1);

            // ME 红石请求器：绑定方块实体类型并登记代表物品
            var requesterType = ModBlockEntities.REDSTONE_REQUESTER.get();
            ModBlocks.REDSTONE_REQUESTER.get().setBlockEntity(
                    com.murthinext.ae2pr.block.redstone_requester.RedstoneRequesterBlockEntity.class, requesterType, null, null);
            appeng.blockentity.AEBaseBlockEntity.registerBlockEntityItem(requesterType,
                    ModItems.REDSTONE_REQUESTER.get());
        });
    }
}
