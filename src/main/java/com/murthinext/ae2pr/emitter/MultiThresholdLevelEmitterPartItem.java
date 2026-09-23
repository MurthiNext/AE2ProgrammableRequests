package com.murthinext.ae2pr.emitter;

import appeng.items.parts.PartItem;

/**
 * ME 通式阈值发信器的部件物品。
 */
public class MultiThresholdLevelEmitterPartItem extends PartItem<MultiThresholdLevelEmitterPart> {

    public MultiThresholdLevelEmitterPartItem(Properties properties) {
        super(properties, MultiThresholdLevelEmitterPart.class, MultiThresholdLevelEmitterPart::new);
    }
}
