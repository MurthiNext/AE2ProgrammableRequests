package com.murthinext.ae2pr;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 模组物品标签。
 */
public final class ModTags {

    private ModTags() {
    }

    /**
     * 扳手：用于旋转本模组的机器与部件。
     * <p>
     * 默认引用通用约定标签 {@code forge:tools/wrench}（AE2、沉浸工程等多数扳手均已加入），
     * 因此无需额外配置即可兼容常见扳手；也可直接向本标签追加物品。
     */
    public static final TagKey<Item> WRENCHES = ItemTags.create(new ResourceLocation(ae2pr.MODID, "wrenches"));
}
