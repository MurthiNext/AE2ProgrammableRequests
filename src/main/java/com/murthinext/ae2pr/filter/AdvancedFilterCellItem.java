package com.murthinext.ae2pr.filter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.items.AEBaseItem;

/**
 * 高级过滤元件：在元件工作台中通过"标签表达式"配置过滤规则，而不是逐项标记。
 * <p>
 * 配置分为白名单与黑名单两段文本，均支持以 {@code &}（与）、{@code |}（或）、{@code ()}（分组）
 * 组合物品标签 id（如 {@code minecraft:logs}）。判定规则：白名单命中且黑名单未命中才通过；
 * 白名单为空视为全通过，黑名单为空视为不排除。
 * <p>
 * 通过 {@link #createPredicate(ItemStack)} 编译为针对网络存储键的谓词，供通式标准发信器与
 * 通式阈值发信器读取。
 */
public class AdvancedFilterCellItem extends AEBaseItem implements ICellWorkbenchItem, FilterCell {

    /** 元件工作台同步白名单表达式的 client action 名称。 */
    public static final String ACTION_SET_WHITELIST = "ae2pr:set_adv_whitelist";
    /** 元件工作台同步黑名单表达式的 client action 名称。 */
    public static final String ACTION_SET_BLACKLIST = "ae2pr:set_adv_blacklist";

    private static final String WHITELIST_TAG = "Whitelist";
    private static final String BLACKLIST_TAG = "Blacklist";

    /** 解析结果缓存，避免发信器每 tick 重复解析同一表达式。 */
    private static final Map<String, Optional<TagExpression>> PARSE_CACHE = new ConcurrentHashMap<>();

    public AdvancedFilterCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
    }

    public static String getWhitelist(ItemStack cell) {
        return getString(cell, WHITELIST_TAG);
    }

    public static String getBlacklist(ItemStack cell) {
        return getString(cell, BLACKLIST_TAG);
    }

    public static void setWhitelist(ItemStack cell, String value) {
        setString(cell, WHITELIST_TAG, value);
    }

    public static void setBlacklist(ItemStack cell, String value) {
        setString(cell, BLACKLIST_TAG, value);
    }

    /** 校验单条表达式是否合法；空白视为合法。 */
    public static boolean isValid(@Nullable String text) {
        return text == null || text.isBlank() || parse(text) != null;
    }

    /**
     * 编译为过滤谓词；未配置任何表达式时返回 null（此时发信器回退到单配置项模式）。
     * <p>
     * 白名单非法时视为"不匹配任何物品"（fail-closed）；黑名单非法时忽略该黑名单。
     */
    @Nullable
    public static Predicate<AEKey> createPredicate(ItemStack cell) {
        if (!(cell.getItem() instanceof AdvancedFilterCellItem)) {
            return null;
        }

        var whiteText = getWhitelist(cell);
        var blackText = getBlacklist(cell);
        boolean hasWhite = !whiteText.isBlank();
        boolean hasBlack = !blackText.isBlank();
        if (!hasWhite && !hasBlack) {
            return null;
        }

        var white = hasWhite ? parse(whiteText) : null;
        var black = hasBlack ? parse(blackText) : null;
        return key -> {
            if (hasWhite && (white == null || !white.test(key))) {
                return false;
            }
            return !hasBlack || black == null || !black.test(key);
        };
    }

    @Nullable
    private static TagExpression parse(String text) {
        if (PARSE_CACHE.size() > 512) {
            PARSE_CACHE.clear();
        }
        return PARSE_CACHE.computeIfAbsent(text, t -> Optional.ofNullable(TagExpression.parse(t))).orElse(null);
    }

    private static String getString(ItemStack cell, String key) {
        var tag = cell.getTag();
        return tag != null ? tag.getString(key) : "";
    }

    private static void setString(ItemStack cell, String key, @Nullable String value) {
        if (value == null || value.isBlank()) {
            var tag = cell.getTag();
            if (tag != null) {
                tag.remove(key);
            }
        } else {
            cell.getOrCreateTag().putString(key, value);
        }
    }
}
