package com.murthinext.ae2pr.client.ctm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * 连接纹理（CTM）使用的模型数据属性。
 * <p>
 * Forge 会在烘焙模型渲染前调用 {@code BakedModel#getModelData(level, pos, state, data)}，
 * 借此把方块位置与其邻居环境传入模型，供运行时按世界邻居重写 UV。
 */
public final class CtmModelProperties {

    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();
    /** 内层模型原有的 ModelData（避免丢失方块实体提供的数据） */
    public static final ModelProperty<ModelData> PARENT = new ModelProperty<>();

    private CtmModelProperties() {
    }
}
