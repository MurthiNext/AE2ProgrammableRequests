package com.murthinext.ae2pr;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.murthinext.ae2pr.block.redstone_requester.RedstoneRequesterBlock;

/**
 * 方块注册入口。
 */
public final class ModBlocks {

    private ModBlocks() {
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ae2pr.MODID);

    /** ME 红石请求器 */
    public static final RegistryObject<RedstoneRequesterBlock> REDSTONE_REQUESTER = BLOCKS.register(
            "redstone_requester", RedstoneRequesterBlock::new);
}
