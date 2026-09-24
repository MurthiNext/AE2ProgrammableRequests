package com.murthinext.ae2pr;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.murthinext.ae2pr.block.redstone_requester.RedstoneRequesterBlockEntity;

/**
 * 方块实体类型注册入口。
 */
public final class ModBlockEntities {

    private ModBlockEntities() {
    }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES, ae2pr.MODID);

    /** ME 红石请求器 */
    public static final RegistryObject<BlockEntityType<RedstoneRequesterBlockEntity>> REDSTONE_REQUESTER = BLOCK_ENTITIES
            .register("redstone_requester", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new RedstoneRequesterBlockEntity(
                            ModBlockEntities.REDSTONE_REQUESTER.get(), pos, state),
                    ModBlocks.REDSTONE_REQUESTER.get()).build(null));
}
